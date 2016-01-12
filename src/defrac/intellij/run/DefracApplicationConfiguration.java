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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import defrac.intellij.config.DefracConfigBase;
import defrac.intellij.project.DefracProjectUtil;
import defrac.intellij.run.ui.DefracRunConfigurationEditor;
import defrac.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 */
public abstract class DefracApplicationConfiguration extends ApplicationConfiguration implements DefracRunConfigurationBase {
  public boolean DEBUG;

  public DefracApplicationConfiguration(@NotNull final String name,
                                        @NotNull final Project project,
                                        @NotNull final ConfigurationFactory factory) {
    super(name, project, factory);
  }

  @Override
  public Collection<Module> getValidModules() {
    return Arrays.asList(DefracProjectUtil.findModulesForPlatform(getProject(), getPlatform(), null));
  }

  @NotNull
  @Override
  public DefracRunConfigurationEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DefracRunConfigurationEditor<DefracApplicationConfiguration>(getProject(), getPlatform());
  }

  @Override
  public abstract RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException;

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    DefracRunUtil.checkConfiguration(this);
  }

  @Override
  public String suggestedName() {
    return DefracRunUtil.suggestedName(this);
  }

  @Nullable
  public String getMain() {
    return MAIN_CLASS_NAME;
  }

  public void setMain(final String qualifiedName) {
    MAIN_CLASS_NAME = qualifiedName;
  }

  @Nullable
  @Override
  public Module getModule() {
    return getConfigurationModule().getModule();
  }

  @Override
  public boolean isDebug() {
    return DEBUG;
  }

  @Override
  public void setDebug(final boolean value) {
    DEBUG = value;
  }

  @NotNull
  @Override
  public DefracConfigBase getAdditionalSettings() {
    final DefracConfigBase settings = new DefracConfigBase();
    settings.setMain(MAIN_CLASS_NAME);
    return settings;
  }
}
