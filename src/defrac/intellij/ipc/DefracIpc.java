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
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import defrac.concurrent.Future;
import defrac.concurrent.Promise;
import defrac.concurrent.Promises;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfigBase;
import defrac.intellij.project.DefracProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.PooledThreadExecutor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public final class DefracIpc extends ProcessAdapter {
  @NotNull
  private static final Pattern RESULT_PATTERN = Pattern.compile("Command finished with exit code (\\d)");

  public interface ExecutorListener {
    void onMessage(@NotNull final DefracCommandLineParser.Message message);

    void onError(@NotNull final Exception exception);

    void onComplete(final int exitCode);

    void onCancel();
  }

  public static class ExecutorAdapter implements ExecutorListener {
    @Override
    public void onMessage(@NotNull final DefracCommandLineParser.Message message) {
    }

    @Override
    public void onError(@NotNull final Exception exception) {
    }

    @Override
    public void onComplete(final int exitCode) {
    }

    @Override
    public void onCancel() {
    }
  }

  public final class Executor implements Runnable {
    @NotNull
    public final DefracPlatform platform;
    @NotNull
    public final String command;
    @NotNull
    public final String[] arguments;
    @NotNull
    final Promise<Boolean> promise = Promises.create();
    @NotNull
    final List<ExecutorListener> listeners = new CopyOnWriteArrayList<ExecutorListener>();

    public Executor(@NotNull final DefracPlatform platform,
                    @NotNull final String command,
                    @NotNull final String... arguments) {
      this.platform = platform;
      this.command = command;
      this.arguments = arguments;
    }

    public void addListener(@NotNull final ExecutorListener listener) {
      listeners.add(listener);
    }

    public void removeListener(@NotNull final ExecutorListener listener) {
      listeners.remove(listener);
    }

    @Override
    public final void run() {
      executeCommand(platform, command, arguments);
    }

    public void cancel() {
      validate();

      if(!promise.future().isCompleted()) {
        promise.failure(new CommandExecutionException(CommandExecutionException.Reason.CANCELLED, command + " command cancelled"));
      }

      for(final ExecutorListener listener : listeners) {
        listener.onCancel();
      }
    }

    void onError(@NotNull final Exception exception) {
      if(!promise.future().isCompleted()) {
        promise.failure(exception);
      }

      for(final ExecutorListener listener : listeners) {
        listener.onError(exception);
      }
    }

    void onComplete(final int exitCode) {
      if(!promise.future().isCompleted()) {
        promise.success(exitCode == 0);
      }

      for(final ExecutorListener listener : listeners) {
        listener.onComplete(exitCode);
      }
    }

    void onMessage(@NotNull final DefracCommandLineParser.Message message) {
      for(final ExecutorListener listener : listeners) {
        listener.onMessage(message);
      }
    }

    public boolean listening() {
      return currentExecutor == this;
    }

    public void dispose() {
      if(listening()) {
        currentExecutor = null;
      }

      listeners.clear();
    }

    final void validate() {
      if(!listening()) {
        throw new IllegalStateException("Executor not running");
      }
    }
  }

  @NotNull
  public static DefracIpc create(@NotNull final ProcessHandler process) {
    return new DefracIpc(process);
  }

  @Nullable
  public static DefracIpc getInstance(@NotNull final Project project) {
    return DefracProcess.getInstance(project).getIpc();
  }

  @NotNull
  private final ProcessHandler process;
  @NotNull
  private final DefracCommandLineParser parser = new DefracCommandLineParser();
  @Nullable
  private Executor currentExecutor;

  public DefracIpc(@NotNull final ProcessHandler process) {
    this.process = process;

    process.addProcessListener(this);
  }

  @NotNull
  public Executor load(@NotNull final DefracPlatform platform,
                       @NotNull final DefracConfigBase settings) {
    return new Executor(platform, DefracCommands.LOAD, settings.toString());
  }

  @NotNull
  public Executor compile(@NotNull final DefracPlatform platform, final boolean debug) {
    final String[] args = debug ? new String[]{"debug"} : new String[0];

    return new Executor(platform, DefracCommands.COMPILE, args);
  }

  @NotNull
  public Executor pack(@NotNull final DefracPlatform platform) {
    return new Executor(platform, DefracCommands.PACKAGE);
  }

  @NotNull
  public Executor run(@NotNull final DefracPlatform platform) {
    return new Executor(platform, DefracCommands.RUN);
  }

  @NotNull
  public Executor debug(@NotNull final DefracPlatform platform, final int port) {
    return new Executor(platform, DefracCommands.DEBUG, String.valueOf(port));
  }

  @NotNull
  public Executor test(@NotNull final DefracPlatform platform, @NotNull final String pattern) {
    return new Executor(platform, DefracCommands.TEST, pattern);
  }

  @NotNull
  public Executor close(@NotNull final DefracPlatform platform) {
    return new Executor(platform, DefracCommands.CLOSE);
  }

  @Override
  public void processTerminated(final ProcessEvent event) {
    onError(new CommandExecutionException(CommandExecutionException.Reason.TERMINATED, "defrac process terminated"));
  }

  @Override
  public void onTextAvailable(final ProcessEvent event, final Key outputType) {
    final String text = event.getText().trim();

    if(outputType == ProcessOutputTypes.STDERR) {
      onError(new CommandExecutionException(CommandExecutionException.Reason.ERROR, text));
    } else if(outputType == ProcessOutputTypes.STDOUT) {
      onMessage(parser.parse(text));
    }
  }

  @NotNull
  public synchronized Future<Boolean> submit(@NotNull final Executor executor) {
    if(currentExecutor != null) {
      currentExecutor.cancel();
    }

    currentExecutor = executor;

    PooledThreadExecutor.INSTANCE.execute(executor);

    return currentExecutor.promise.future();
  }

  private void executeCommand(@NotNull final DefracPlatform platform,
                              @NotNull final String command,
                              @NotNull final String... args) {
    final OutputStream out = process.getProcessInput();

    if(out == null) {
      onError(new IOException("Process has no input"));
      return;
    }

    try {
      out.write(platform.prefixCommand(command).getBytes(Charsets.UTF_8));

      for(final String arg : args) {
        out.write(' ');
        out.write(arg.getBytes(Charsets.UTF_8));
      }

      out.write('\n');
      out.flush();
    } catch(final IOException e) {
      onError(e);
    }
  }

  private void onError(@NotNull final Exception exception) {
    if(currentExecutor != null) {
      currentExecutor.onError(exception);
    }
  }

  private void onMessage(@NotNull final DefracCommandLineParser.Message message) {
    if(currentExecutor != null) {
      final Matcher matcher = RESULT_PATTERN.matcher(message.text);

      if(matcher.find()) {
        currentExecutor.onComplete(Integer.parseInt(matcher.group(1)));
      } else {
        currentExecutor.onMessage(message);
      }
    }
  }

}
