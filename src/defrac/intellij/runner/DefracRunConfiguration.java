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

package defrac.intellij.runner;

import defrac.intellij.runner.ui.DefracRunConfigurationEditor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class DefracRunConfiguration extends RunConfigurationBase {
  public DefracRunConfiguration(@NotNull final Project project,
                                @NotNull final ConfigurationFactory factory,
                                @NotNull final String name) {
    super(project, factory, name);
  }

  @NotNull
  @Override
  public SettingsEditor<DefracRunConfiguration> getConfigurationEditor() {
    return new DefracRunConfigurationEditor(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    //TODO(joa): implement me
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor,
                                  @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
    return null; //TODO(joa): implement me
  }
}
