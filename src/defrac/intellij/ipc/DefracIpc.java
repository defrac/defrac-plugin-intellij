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

import com.google.common.base.Charsets;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.ui.UIUtil;
import defrac.intellij.DefracPlatform;
import defrac.intellij.project.DefracProcess;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracIpc implements ProcessListener {
  @NotNull
  private static final Logger LOG = Logger.getInstance(DefracIpc.class.getName());

  @NotNull
  @NonNls
  public static final String LEVEL_DEBUG = "debug";
  @NotNull
  @NonNls
  public static final String LEVEL_INFO = "info";
  @NotNull
  @NonNls
  public static final String LEVEL_WARN = "warn";
  @NotNull
  @NonNls
  public static final String LEVEL_ERROR = "error";

  private static final Pattern COMPILER_MESSAGE =
      Pattern.compile("\\[(" + LEVEL_DEBUG + '|' + LEVEL_INFO + '|' + LEVEL_WARN + '|' + LEVEL_ERROR + ")\\]\\s(.*)", Pattern.DOTALL);

  @NotNull
  public static DefracIpc getInstance(@NotNull final ProcessHandler process) {
    return new DefracIpc(process);
  }

  @Nullable
  public static DefracIpc getInstance(@NotNull final Project project) {
    return DefracProcess.getInstance(project).getIpc();
  }

  @NotNull
  private final ProcessHandler process;

  @Nullable
  private Parser parser;

  @Nullable
  private Context context;

  @Nullable
  private CompilerMessageCategory currentCategory;

  @NotNull
  private final StringBuilder currentMessage = new StringBuilder();

  private boolean dontKillParserImmediately;

  @NotNull
  private final Lock lock = new ReentrantLock(/*fair=*/true);

  @Nullable
  private Thread currentThread;

  private volatile boolean someOutputReceived = false;

  private DefracIpc(@NotNull final ProcessHandler process) {
    this.process = process;

    process.addProcessListener(this);
  }

  public boolean compile(@Nullable final Context context,
                         @NotNull final DefracPlatform platform,
                         @NotNull final String mainClass,
                         final boolean debugMode) throws IOException, InterruptedException {
    return execute(context, platform, DefracCommands.COMPILE + " " + debugMode + " " + mainClass);
  }

  public boolean open(@Nullable final Context context,
                      @NotNull final DefracPlatform platform) throws IOException, InterruptedException {
    return execute(context, platform, DefracCommands.OPEN);
  }

  public boolean close(@Nullable final Context context,
                       @NotNull final DefracPlatform platform) throws IOException, InterruptedException {
    return execute(context, platform, DefracCommands.CLOSE);
  }

  public boolean genMacros(@Nullable final Context context,
                           @NotNull final DefracPlatform platform) throws IOException, InterruptedException {
    return execute(context, platform, DefracCommands.GEN_MACROS);
  }

  public boolean pack(@Nullable final Context context,
                      @NotNull final DefracPlatform platform) throws IOException, InterruptedException {
    return execute(context, platform, DefracCommands.PACKAGE);
  }

  private boolean execute(@Nullable final Context context,
                          @NotNull final DefracPlatform platform,
                          @NotNull final String command) throws IOException, InterruptedException {
    LOG.info("defrac " + platform.prefixCommand(command) + " requested");

    return execAndGet(context, new Task<Boolean>() {
      @NotNull
      @Override
      public Boolean exec() throws IOException, InterruptedException, BrokenBarrierException {
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
              final boolean isSuccess = errors == 0;

              LOG.info(command + " completed. Result: " + isSuccess);
              result.set(isSuccess);

              LOG.info("Awaiting barrier ...");
              barrier.await();
            } catch(final NumberFormatException exception) {
              LOG.error(exception);
              onFailure();
            }
          }

          @Override
          public void onFailure() throws InterruptedException, BrokenBarrierException {
            LOG.info("Received onFailure callback");
            result.set(false);

            LOG.info("Awaiting barrier ...");
            barrier.await();
          }
        });

        LOG.info("Invoking " + platform.prefixCommand(command));
        execCommand(context, platform, command);

        LOG.info("Waiting for command to complete ...");
        barrier.await();

        return result.get();
      }
    });
  }

  @NotNull
  private <V> V execAndGet(@Nullable Context context,
                           @NotNull final Task<V> task) throws InterruptedException, IOException {
    try {
      LOG.info("Acquiring lock ...");
      lock.lockInterruptibly();
      currentThread = Thread.currentThread();

      installContext(context);

      LOG.info("Executing task ...");
      return task.exec();
    } catch(final BrokenBarrierException brokenBarrier) {
      throw new IllegalStateException(brokenBarrier);
    } finally {
      flushMessage();
      resetContext();
      resetParser();

      LOG.info("Releasing lock...");
      currentThread = null;
      lock.unlock();
    }
  }

  private void execCommand(@Nullable final Context context,
                           @NotNull final DefracPlatform platform,
                           @NotNull final String command) throws IOException, InterruptedException {
    final OutputStream out = process.getProcessInput();

    if(out == null) {
      throw new IOException("Process has no input");
    }

    someOutputReceived = false;

    if(!command.isEmpty()) {
      out.write(platform.prefixCommand(command).getBytes(Charsets.UTF_8));
    }
    out.write('\n');
    out.flush();

    checkForLiveness(context);
  }

  private void checkForLiveness(@Nullable final Context context) throws InterruptedException, IOException {
    final long t0 = System.nanoTime();

    while(!someOutputReceived && TimeUnit.SECONDS.convert(System.nanoTime() - t0, TimeUnit.NANOSECONDS) < 5L) {
      Thread.sleep(0x80L);
    }

    if(!someOutputReceived) {
      addMessage(CompilerMessageCategory.ERROR, "Process doesn't answer");

      if(context != null) {
        final ProcessHandler processHandler =
            DefracProcess.getInstance(context.getProject()).
                getProcessHandler();

        if(processHandler != null) {
          processHandler.destroyProcess();
        }
      }

      throw new IOException("Process is hanging");
    }
  }

  private void installContext(@Nullable final Context newContext) {
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

  @Override
  public void startNotified(final ProcessEvent event) {
  }

  @Override
  public void processTerminated(final ProcessEvent event) {
    addMessage(CompilerMessageCategory.ERROR, "defrac process terminated");

    final Thread currentThread = this.currentThread;

    if(currentThread != null) {
      LOG.info("Process died will worker is waiting. Sending interrupt to " + currentThread.toString());
      currentThread.interrupt();
    }
  }

  private void addMessage(@NotNull final CompilerMessageCategory category,
                          @Nullable final String message) {
    if(isNullOrEmpty(message)) {
      return;
    }

    final Context context = this.context;

    if(context != null) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          context.addMessage(category, message);
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
    someOutputReceived = true;

    final Parser parser = this.parser;

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
          if(parser != null) {
            parser.onFailure();
          }
          return;
        }
      } else {
        currentMessage.append('\n').append(text.trim());
      }

      if(parser != null) {
        parser.onTextAvailable(event, outputType);
      }
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

  public interface Context {
    void addMessage(@NotNull final CompilerMessageCategory category, @NotNull final String message);

    @NotNull
    Project getProject();
  }

  private interface Parser {
    void onFailure() throws InterruptedException, BrokenBarrierException;

    void onTextAvailable(final ProcessEvent event, final Key outputType)
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

  private interface Task<V> {
    @NotNull
    V exec() throws IOException, InterruptedException, BrokenBarrierException;
  }
}
