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
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfigBase;
import defrac.intellij.run.ui.DefracRunConfigurationEditor;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.run.AndroidRunConfiguration;
import org.jetbrains.android.run.AndroidRunningState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 */
public final class DefracAndroidRunConfiguration extends AndroidRunConfiguration implements DefracRunConfigurationBase {
  public boolean DEBUG;

  public DefracAndroidRunConfiguration(final Project project, final ConfigurationFactory factory) {
    super(project, factory);
  }

  @Override
  protected void checkConfiguration(@NotNull final AndroidFacet facet) throws RuntimeConfigurationException {
    DefracRunUtil.checkConfiguration(this);
    //super.checkConfiguration(facet);
  }

  @Override
  public AndroidRunningState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    DefracRunUtil.checkState(this, env);
    MODE = LAUNCH_DEFAULT_ACTIVITY;
    DEPLOY = true;

    final AndroidFacet facet = FacetManager.getInstance(getModule()).getFacetByType(AndroidFacet.ID);
facet.getProperties().ENABLE_PRE_DEXING = false;

    final File file = new File("/Applications/Development/testapp2/target/android/AndroidManifest.xml");

    if(!file.exists()) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          try {
            file.createNewFile();
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
      });
    }

    return super.getState(executor, env);
  }

  @NotNull
  @Override
  public DefracRunConfigurationEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DefracRunConfigurationEditor<DefracAndroidRunConfiguration>(getProject(), getPlatform());
  }

  @Override
  public String suggestedName() {
    return DefracRunUtil.suggestedName(this);
  }

  @Nullable
  @Override
  public String getMain() {
    return ACTIVITY_CLASS;
  }

  @Override
  public void setMain(final String qualifiedName) {
    ACTIVITY_CLASS = qualifiedName;
  }

  @Nullable
  @Override
  public Module getModule() {
    return getConfigurationModule().getModule();
  }

  @NotNull
  @Override
  public DefracPlatform getPlatform() {
    return DefracPlatform.ANDROID;
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
    settings.setMain(ACTIVITY_CLASS);
    return settings;
  }
}
