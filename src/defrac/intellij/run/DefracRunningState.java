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
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import defrac.intellij.DefracBundle;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.ipc.DefracIpc;
import org.jetbrains.annotations.NotNull;

/**
 */
public class DefracRunningState extends CommandLineState implements DefracRemoteState {
  @NotNull
  protected final DefracFacet facet;
  @NotNull
  protected final DefracRunConfiguration configuration;

  public DefracRunningState(@NotNull final ExecutionEnvironment environment,
                            @NotNull final DefracRunConfiguration configuration,
                            @NotNull final DefracFacet facet) {
    super(environment);
    this.configuration = configuration;
    this.facet = facet;
  }

  @NotNull
  @Override
  public RemoteConnection getRemoteConnection() throws ExecutionException {
    return new RemoteConnection(true, "127.0.0.1", configuration.getDebugPort(), true);
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
      executor = ipc.debug(facet.getPlatform(), configuration.getDebugPort());
      process = new RemoteDebugProcessHandler(project);
    } else {
      executor = ipc.run(facet.getPlatform());
      process = new DefaultDebugProcessHandler();
    }

    // register listeners
    registerListeners(ipc, executor, process);

    // submit the executor to ipc
    ipc.submit(executor);

    return process;
  }

  protected void registerListeners(@NotNull final DefracIpc ipc,
                                   @NotNull final DefracIpc.Executor executor,
                                   @NotNull final ProcessHandler process) {
    executor.addListener(new DefracRunExecutorListener(process, executor));

    process.addProcessListener(new DefracRunProcessListener(ipc, executor, facet.getPlatform()));
  }
}
