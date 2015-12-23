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
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.ipc.DefracIpc;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class WebRunningState extends CommandLineState {
  @NotNull
  private final ExecutionEnvironment environment;

  @NotNull
  private final DefracRunConfiguration configuration;


  public WebRunningState(@NotNull final ExecutionEnvironment environment,
                         @NotNull final DefracRunConfiguration configuration) {
    super(environment);
    this.environment = environment;
    this.configuration = configuration;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    final Module module = configuration.getConfigurationModule().getModule();
    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.facetMissing", module));
    }

    final DefracIpc ipc = DefracIpc.getInstance(module.getProject());

    if(ipc == null) {
      throw new ExecutionException(DefracBundle.message("ipc.error.ipcMissing"));
    }

    final DefracRunContext context = new DefracRunContext(module.getProject());

    try {
      final boolean running = ipc.open(context, DefracPlatform.WEB);

      if(!running) {
        throw new ExecutionException("Could not start web app");
      }
    } catch(Throwable e) {
      throw new ExecutionException(e);
    }

    if(configuration.DEBUG) {
      return new RemoteDebugProcessHandler(environment.getProject());
    }

    return new DefaultDebugProcessHandler() {
      @Override
      protected void destroyProcessImpl() {
        try {
          ipc.close(context, DefracPlatform.WEB);
        } catch(Throwable e) {
          notifyProcessTerminated(-1);
          return;
        }

        notifyProcessTerminated(0);
      }

      @Override
      protected void detachProcessImpl() {
        notifyProcessDetached();
      }
    };
  }
}
