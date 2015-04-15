/*
 * Copyright 2014 defrac inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package defrac.intellij.ipc;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.ui.UIUtil;
import defrac.intellij.DefracPlatform;
import defrac.intellij.project.DefracProcess;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracIpc implements ProcessListener {
  @NotNull @NonNls public static final String LEVEL_DEBUG = "debug";
  @NotNull @NonNls public static final String LEVEL_INFO = "info";
  @NotNull @NonNls public static final String LEVEL_WARN = "warn";
  @NotNull @NonNls public static final String LEVEL_ERROR = "error";

  private static final Pattern COMPILER_MESSAGE =
      Pattern.compile("\\[("+LEVEL_DEBUG+'|'+LEVEL_INFO+'|'+LEVEL_WARN+'|'+LEVEL_ERROR+")\\]\\s(.*)", Pattern.DOTALL);

  @NotNull
  public static DefracIpc getInstance(@NotNull final ProcessHandler process) {
    return new DefracIpc(process);
  }

  @Nullable
  public static DefracIpc getInstance(@NotNull final CompileContext context) {
    return getInstance(context.getProject());
  }

  @Nullable
  public static DefracIpc getInstance(@NotNull final Project project) {
    return DefracProcess.getInstance(project).getIpc();
  }

  @NotNull
  private final ProcessHandler process;

  @NotNull
  private final PrintStream out;

  @Nullable
  private Parser parser;

  @Nullable
  private CompileContext context;

  @Nullable
  private CompilerMessageCategory currentCategory;

  @NotNull
  private final StringBuilder currentMessage = new StringBuilder();

  private boolean dontKillParserImmediately;

  @NotNull
  private final Lock lock = new ReentrantLock(/*fair=*/true);

  private DefracIpc(@NotNull final ProcessHandler process) {
    this.process = process;
    this.out = new PrintStream(checkNotNull(process.getProcessInput()), /*autoFlush=*/true);
    process.addProcessListener(this);
    out.println("");
  }

  public boolean compile(@Nullable final CompileContext context,
                         @NotNull final DefracPlatform platform) throws InterruptedException {
    return execAndGet(context, new Task<Boolean>() {
      @NotNull
      @Override
      public Boolean exec() throws InterruptedException, BrokenBarrierException {
        if(process.isProcessTerminated() || process.isProcessTerminating()) {
          addMessage(CompilerMessageCategory.ERROR, "defrac process lost");
          return false;
        }

        final CyclicBarrier barrier = new CyclicBarrier(2);
        final AtomicBoolean result = new AtomicBoolean();

        installParser(new RegExpBasedParser(DefracCommands.COMPILE_RESULT) {
          @Override
          protected void onMatch(@NotNull final Matcher matcher) throws InterruptedException, BrokenBarrierException {
            try {
              final int errors = Integer.parseInt(matcher.group(4));
              result.set(errors == 0);
              barrier.await();
            } catch(final NumberFormatException exception) {
              onFailure();
            }
          }

          @Override
          public void onFailure() throws InterruptedException, BrokenBarrierException {
            result.set(false);
            barrier.await();
          }
        });

        execCommand(platform, DefracCommands.COMPILE);
        barrier.await();

        return result.get();
      }
    });
  }

  public boolean pack(@Nullable final CompileContext context,
                         @NotNull final DefracPlatform platform) throws InterruptedException {
    return execAndGet(context, new Task<Boolean>() {
      @NotNull
      @Override
      public Boolean exec() throws InterruptedException, BrokenBarrierException {
        //TODO(joa): dup cleanup

        if(process.isProcessTerminated() || process.isProcessTerminating()) {
          addMessage(CompilerMessageCategory.ERROR, "defrac process lost");
          return false;
        }

        final CyclicBarrier barrier = new CyclicBarrier(2);
        final AtomicBoolean result = new AtomicBoolean();

        installParser(new RegExpBasedParser(DefracCommands.COMPILE_RESULT) {
          @Override
          protected void onMatch(@NotNull final Matcher matcher) throws InterruptedException, BrokenBarrierException {
            try {
              final int errors = Integer.parseInt(matcher.group(4));
              result.set(errors == 0);
              barrier.await();
            } catch(final NumberFormatException exception) {
              onFailure();
            }
          }

          @Override
          public void onFailure() throws InterruptedException, BrokenBarrierException {
            result.set(false);
            barrier.await();
          }
        });

        execCommand(platform, DefracCommands.PACKAGE);
        barrier.await();

        return result.get();
      }
    });
  }

  @NotNull
  private <V> V execAndGet(@Nullable CompileContext context,
                           @NotNull final Task<V> task) throws InterruptedException {
    try {
      lock.lockInterruptibly();
      installContext(context);

      return task.exec();
    } catch(final BrokenBarrierException brokenBarrier) {
      throw new IllegalStateException(brokenBarrier);
    } finally {
      flushMessage();
      resetContext();
      resetParser();
      lock.unlock();
    }
  }

  private void execCommand(@NotNull final DefracPlatform platform,
                           @NotNull final String command) {
    out.println(createScopedCommand(platform, command));
  }

  private void installContext(@Nullable final CompileContext newContext) {
    context = newContext;
  }

  private void installParser(@NotNull final Parser newParser) {
    parser = newParser;
    dontKillParserImmediately = true;
  }

  private void resetContext() {
    context = null;
  }

  private void resetParser() {
    parser = null;
  }

  @NotNull
  private static String createScopedCommand(@NotNull final DefracPlatform platform,
                                            @NotNull final String command) {
    return platform.isGeneric()
        ? command
        : platform.name+':'+command;
  }

  @Override
  public void startNotified(final ProcessEvent event) {
  }

  @Override
  public void processTerminated(final ProcessEvent event) {
    addMessage(CompilerMessageCategory.ERROR, "defrac process terminated");
  }

  private void addMessage(@NotNull final CompilerMessageCategory category,
                          @Nullable final String message) {
    if(isNullOrEmpty(message)) {
      return;
    }

    final CompileContext context = this.context;

    if(context != null) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          context.addMessage(category, message, null, -1, -1);
        }
      });
    }
  }

  @Override
  public void processWillTerminate(final ProcessEvent event,
                                   final boolean willBeDestroyed) {
  }

  @Override
  public void onTextAvailable(final ProcessEvent event,
                              final Key outputType) {
    final Parser parser = this.parser;

    if(parser == null) {
      return;
    }

    try {
      final String text = event.getText();
      final Matcher matcher = COMPILER_MESSAGE.matcher(text);

      if(matcher.find()) {
        flushMessage();
        updateCompilerMessageCategory(matcher.group(1));
        currentMessage.append(matcher.group(2));
      } else if(isPrompt(text)) {
        flushMessage();

        if(dontKillParserImmediately) {
          dontKillParserImmediately = false;
        } else {
          parser.onFailure();
          return;
        }
      } else {
        currentMessage.append('\n').append(text.trim());
      }

      parser.onTextAvailable(event, outputType);
    } catch(final InterruptedException interrupt) {
      Thread.currentThread().interrupt();
    } catch(final BrokenBarrierException brokenBarrier) {
      throw new IllegalStateException(brokenBarrier);
    }
  }

  private boolean isPrompt(@Nullable final String text) {
    return text != null
        && ("> ".equals(text) || text.charAt(0) == '\b' && text.endsWith("> "));

  }

  private void updateCompilerMessageCategory(final String level) {
    if(LEVEL_DEBUG.equals(level) || LEVEL_INFO.equals(level)) {
      currentCategory = CompilerMessageCategory.INFORMATION;
    } else if(LEVEL_WARN.equals(level)) {
      currentCategory = CompilerMessageCategory.WARNING;
    } else if(LEVEL_ERROR.equals(level)) {
      currentCategory = CompilerMessageCategory.ERROR;
    }
  }

  private void flushMessage() {
    final CompilerMessageCategory category = currentCategory;

    if(category != null && currentMessage.length() > 0) {
      addMessage(category, currentMessage.toString());
      currentMessage.setLength(0);
    }
  }

  private static interface Parser {
    public void onFailure() throws InterruptedException, BrokenBarrierException;
    public void onTextAvailable(final ProcessEvent event, final Key outputType)
        throws InterruptedException, BrokenBarrierException;
  }

  private static abstract class RegExpBasedParser implements Parser {
    @NotNull
    private final Pattern pattern;

    public RegExpBasedParser(@NotNull final String regex) {
      pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    @Override
    public final void onTextAvailable(final ProcessEvent event, final Key outputType) throws InterruptedException, BrokenBarrierException {
      if(outputType == ProcessOutputTypes.STDERR) {
        onFailure();
      } else if(outputType == ProcessOutputTypes.STDOUT) {
        final Matcher matcher = pattern.matcher(event.getText());
        while(matcher.find()) {
          onMatch(matcher);
        }
      }
    }

    protected abstract void onMatch(@NotNull final Matcher matcher) throws InterruptedException, BrokenBarrierException;
  }

  private static interface Task<V> {
    @NotNull
    public V exec() throws InterruptedException, BrokenBarrierException;
  }
}
