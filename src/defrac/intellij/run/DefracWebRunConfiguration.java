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
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfigBase;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.run.ui.DefracCustomWebRunConfigEditor;
import defrac.intellij.run.ui.DefracRunConfigurationEditor;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public final class DefracWebRunConfiguration extends DefracApplicationConfiguration {
  public boolean STRICT;
  public boolean MINIFY;

  public DefracWebRunConfiguration(@NotNull final Project project,
                                   @NotNull final ConfigurationFactory factory) {
    super("defrac.web.runConfig", project, factory);
  }

  @NotNull
  @Override
  public DefracRunConfigurationEditor<? extends RunConfiguration> getConfigurationEditor() {
    final DefracRunConfigurationEditor<DefracWebRunConfiguration> editor =
        new DefracRunConfigurationEditor<DefracWebRunConfiguration>(getProject(), getPlatform());
    editor.addCustomSettingsEditor(new DefracCustomWebRunConfigEditor());
    return editor;
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor,
                                  @NotNull final ExecutionEnvironment env) throws ExecutionException {
    DefracRunUtil.checkState(this, env);
    return new DefracWebRunningState(env, this, checkNotNull(DefracFacet.getInstance(getModule())));
  }

  @NotNull
  @Override
  public DefracPlatform getPlatform() {
    return DefracPlatform.WEB;
  }

  public void isStrict(final boolean value) {
    STRICT = value;
  }

  public boolean isStrict() {
    return STRICT;
  }

  public void minify(final boolean value) {
    MINIFY = value;
  }

  public boolean minify() {
    return MINIFY;
  }


  @NotNull
  @Override
  public DefracConfigBase getAdditionalSettings() {
    final DefracConfigBase settings = super.getAdditionalSettings();
    settings.setStrict(STRICT);
    settings.setMinify(MINIFY);
    return settings;
  }
}
