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

package defrac.intellij.run;

import com.intellij.debugger.engine.RemoteDebugProcessHandler;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.ipc.DefracCommandLineParser;
import defrac.intellij.ipc.DefracIpc;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracWebRunningState extends CommandLineState {
  @NotNull
  private final DefracFacet facet;
  @NotNull
  private final DefracRunConfiguration configuration;

  public DefracWebRunningState(@NotNull final ExecutionEnvironment environment,
                               @NotNull final DefracRunConfiguration configuration,
                               @NotNull final DefracFacet facet) {
    super(environment);
    this.configuration = configuration;
    this.facet = facet;
  }

  @NotNull
  public DefracRunConfiguration getConfiguration() {
    return configuration;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    final Project project = facet.getModule().getProject();

    final DefracIpc ipc = DefracIpc.getInstance(project);

    if(ipc == null) {
      throw new ExecutionException(DefracBundle.message("ipc.error.ipcMissing"));
    }

    final ProcessHandler process;

    final DefracIpc.Executor executor;

    if(configuration.isDebug()) {
      executor = ipc.debug(DefracPlatform.WEB, 5005);
      process = new RemoteDebugProcessHandler(project);
    } else {
      executor = ipc.run(DefracPlatform.WEB);
      process = new DefaultDebugProcessHandler();
    }

    executor.addListener(new DefracIpc.ExecutorListener() {
      @Override
      public void onMessage(@NotNull final DefracCommandLineParser.Message message) {
        if(message.isError()) {
          if(message.text.startsWith("Uncaught")) {
            // end of error message
            onError(new RuntimeException(message.text));
          } else {
            process.notifyTextAvailable(message.text + "\n", ProcessOutputTypes.STDERR);
          }
          return;
        }

        process.notifyTextAvailable(message.text + "\n", ProcessOutputTypes.STDOUT);

        if(message.text.equals("Connection to Chrome lost")) {
          // cancel executor since we cannot listen to chrome anymore
          executor.cancel();
        }
      }

      @Override
      public void onError(@NotNull final Exception exception) {
        process.notifyTextAvailable(exception.getMessage() + "\n", ProcessOutputTypes.STDERR);

        process.notifyTextAvailable("\nProcess finished with exit code 1\n", ProcessOutputTypes.SYSTEM);

        executor.dispose();

        process.destroyProcess();
      }

      @Override
      public void onComplete(final int exitCode) {
        // we do not dispose the executor since we want to fetch incoming messages
      }

      @Override
      public void onCancel() {
        process.notifyTextAvailable("\nProcess finished with exit code 0\n", ProcessOutputTypes.SYSTEM);

        executor.dispose();

        process.destroyProcess();
      }
    });

    process.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(final ProcessEvent event) {
        if(executor.listening()) {
          // case when the process is destroyed from within the IDE
          // we will close the current tab and stop listening to the defrac process
          executor.cancel();

          final DefracIpc.Executor abort = ipc.close(DefracPlatform.WEB);

          abort.addListener(new DefracIpc.ExecutorAdapter() {
            @Override
            public void onCancel() {
              abort.dispose();
            }

            @Override
            public void onComplete(final int exitCode) {
              abort.dispose();
            }

            @Override
            public void onError(@NotNull final Exception exception) {
              abort.dispose();
            }
          });

          ipc.submit(abort);
        }

        super.processTerminated(event);
      }
    });

    // submit the executor to ipc
    ipc.submit(executor);

    return process;
  }
}
