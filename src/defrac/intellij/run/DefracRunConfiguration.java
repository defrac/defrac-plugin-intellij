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

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.run.ui.DefracRunConfigurationEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static defrac.intellij.run.DefracRunUtil.isValidMainClass;

/**
 */
public final class DefracRunConfiguration extends ApplicationConfiguration {
  public static final int LAUNCH_ON_DEVICE = 1;
  public static final int LAUNCH_IN_EMULATOR = 2;

  public boolean DEBUG;
  public String DEBUG_PORT;
  public boolean STRICT;
  public boolean MINIFY;
  public int MODE;

  public DefracRunConfiguration(@NotNull final Project project,
                                @NotNull final DefracRunConfigurationType configurationType) {
    super("defrac.runConfig", project, configurationType);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    // NOTE: no super call here since android has a different main class policy compared to java
    final Module module = getModule();

    if(module == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("config.run.error.noModule"));
    }

    if(isNullOrEmpty(getRunClass())) {
      throw new RuntimeConfigurationError(DefracBundle.message("config.run.error.noMain"));
    }

    if(!isValidMainClass(module, getRunClass())) {
      throw new RuntimeConfigurationError(DefracBundle.message("config.run.error.illegalMain"));
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.facetMissing", module.getName()));
    }

    if(facet.isMacroLibrary()) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.isMacroLibrary"));
    }

    if(facet.getDefracVersion() == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.noVersion"));
    }

    if(!facet.getSettingsFile().canRead()) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.fileDoesNotExist", facet.getSettingsFile()));
    }

    if(facet.getPlatform().isGeneric()) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.genericPlatform", module.getName()));
    }

    if(facet.getConfigOracle() == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.noSettings"));
    }
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    final Module module = getModule();

    if(module == null) {
      throw new ExecutionException(DefracBundle.message("config.run.error.noModule"));
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.facetMissing", module.getName()));
    }

    final DefracPlatform platform = facet.getPlatform();

    setDebug(DefracRunUtil.isDebug(env));

    if(isDebug()) {
      setDebugPort(DebuggerUtils.getInstance().findAvailableDebugAddress(true));
    } else {
      setDebugPort("");
    }

    switch(platform) {
      case JVM:
        return new DefracJvmRunningState(env, this, facet);
      case WEB:
        return new DefracWebRunningState(env, this, facet);
      case ANDROID:
      case IOS:
        return new DefracRunningState(env, this, facet);
      default:
        throw new ExecutionException("WTF happened here?!");
    }
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DefracRunConfigurationEditor(getProject());
  }

  @Nullable
  @Override
  public String suggestedName() {
    final String result = super.suggestedName();

    if(result == null) {
      return null;
    }

    final DefracFacet facet = getFacet();

    if(facet == null) {
      return result;
    }

    return result + '(' + facet.getPlatform().displayName + ')';
  }

  @Nullable
  public Module getModule() {
    return getConfigurationModule().getModule();
  }

  @Nullable
  public DefracFacet getFacet() {
    final Module module = getModule();

    if(module == null) {
      return null;
    }

    return DefracFacet.getInstance(module);
  }

  public boolean isDebug() {
    return DEBUG;
  }

  public void setDebug(final boolean value) {
    DEBUG = value;
  }

  public String getDebugPort() {
    return DEBUG_PORT;
  }

  public void setDebugPort(@NotNull final String value) {
    DEBUG_PORT = value;
  }

  public boolean isStrict() {
    return STRICT;
  }

  public void setStrict(final boolean value) {
    STRICT = value;
  }

  public boolean isMinify() {
    return MINIFY;
  }

  public void setMinify(final boolean value) {
    MINIFY = value;
  }

  public boolean launchOnDevice() {
    return MODE == LAUNCH_ON_DEVICE;
  }

  public boolean launchInEmulator() {
    return MODE == LAUNCH_IN_EMULATOR;
  }

  public void setLaunchMode(final int mode) {
    MODE = mode;
  }

  public void setLaunchInEmulator() {
    MODE = LAUNCH_IN_EMULATOR;
  }

  public void setLaunchOnDevice() {
    MODE = LAUNCH_ON_DEVICE;
  }
}
