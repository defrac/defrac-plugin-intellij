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

import com.intellij.debugger.DebugEnvironment;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.DefaultDebugUIEnvironment;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.ui.tree.render.BatchEvaluator;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public final class DefracRunner extends DefaultProgramRunner {
  @NotNull
  @NonNls
  public static final String RUNNER_ID = "DefracRunner";

  @NotNull
  @Override
  public String getRunnerId() {
    return RUNNER_ID;
  }

  @Override
  public void execute(@NotNull final ExecutionEnvironment environment) throws ExecutionException {
    super.execute(environment);
  }

  @Override
  public void execute(@NotNull final ExecutionEnvironment environment, @Nullable final Callback callback) throws ExecutionException {
    // we need to pass the run profile info to the compiler so
    // we can decide if this is a debug or release build
    final DefracRunConfiguration configuration = (DefracRunConfiguration) checkNotNull(environment.getRunnerAndConfigurationSettings()).getConfiguration();
    configuration.DEBUG = DefaultDebugExecutor.EXECUTOR_ID.equals(environment.getExecutor().getId());
    super.execute(environment, callback);
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
    if(DefaultDebugExecutor.EXECUTOR_ID.equals(environment.getExecutor().getId())) {
      final RemoteConnection connection = new RemoteConnection(true, "127.0.0.1", "5005", true);
      return attachVirtualMachine(state, environment, connection, false);
    }

    final ExecutionResult executionResult = state.execute(environment.getExecutor(), this);

    if(executionResult == null) {
      return null;
    }
    return new RunContentBuilder(executionResult, environment).showRunContent(environment.getContentToReuse());
  }

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    if(profile instanceof DefracRunConfiguration) {
      final DefracRunConfiguration runConfiguration = (DefracRunConfiguration) profile;
      final Module module = runConfiguration.getConfigurationModule().getModule();

      if(module == null) {
        return false;
      }

      final DefracFacet facet = DefracFacet.getInstance(module);

      assert facet != null : DefracBundle.message("facet.error.facetMissing", module.getName());

      final DefracPlatform platform = facet.getPlatform();

      // TODO: add platform when debug is available
      return facet.getPlatform().isAvailableOnHostOS()
          && (!DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) || platform == DefracPlatform.JVM || platform == DefracPlatform.WEB);

    }

    return false;
  }

  @Nullable
  protected RunContentDescriptor attachVirtualMachine(RunProfileState state,
                                                      @NotNull ExecutionEnvironment env,
                                                      RemoteConnection connection,
                                                      boolean pollConnection) throws ExecutionException {
    final DebugEnvironment environment = new DefaultDebugUIEnvironment(env, state, connection, pollConnection).getEnvironment();
    final DebuggerSession debuggerSession = DebuggerManagerEx.getInstanceEx(env.getProject()).attachVirtualMachine(environment);

    if(debuggerSession == null) {
      return null;
    }

    final DebugProcessImpl debugProcess = debuggerSession.getProcess();
    if(debugProcess.isDetached() || debugProcess.isDetaching()) {
      debuggerSession.dispose();
      return null;
    }
    // optimization: that way BatchEvaluator will not try to lookup the class file in remote VM
    // which is an expensive operation when executed first time
    debugProcess.putUserData(BatchEvaluator.REMOTE_SESSION_KEY, Boolean.TRUE);

    return XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {
      @Override
      @NotNull
      public XDebugProcess start(@NotNull XDebugSession session) {
        XDebugSessionImpl sessionImpl = (XDebugSessionImpl) session;
        ExecutionResult executionResult = debugProcess.getExecutionResult();
        sessionImpl.addExtraActions(executionResult.getActions());
        if(executionResult instanceof DefaultExecutionResult) {
          sessionImpl.addRestartActions(((DefaultExecutionResult) executionResult).getRestartActions());
          sessionImpl.addExtraStopActions(((DefaultExecutionResult) executionResult).getAdditionalStopActions());
        }
        return JavaDebugProcess.create(session, debuggerSession);
      }
    }).getRunContentDescriptor();
  }
}
