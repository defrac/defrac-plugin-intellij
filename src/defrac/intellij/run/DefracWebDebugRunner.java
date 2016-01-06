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

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import defrac.intellij.DefracPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 */
public final class DefracWebDebugRunner extends GenericDebuggerRunner {
  @NotNull
  @Override
  public String getRunnerId() {
    return "defrac.web.debug";
  }

  @Nullable
  @Override
  protected RunContentDescriptor createContentDescriptor(@NotNull final RunProfileState state,
                                                         @NotNull final ExecutionEnvironment environment) throws ExecutionException {
    final RemoteConnection connection = new RemoteConnection(true, "127.0.0.1", "5005", true);
    return attachVirtualMachine(state, environment, connection, false);
  }

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefracRunUtil.canRun(executorId, profile, DefracPlatform.WEB, true);
  }
}
