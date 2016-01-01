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

import com.google.common.collect.Maps;
import com.intellij.execution.CommonJavaRunConfigurationParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.ExternalizablePath;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import defrac.intellij.DefracBundle;
import defrac.intellij.config.DefracConfigOracle;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.run.ui.DefracRunConfigurationEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracRunConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule>
    implements CommonJavaRunConfigurationParameters, RunConfigurationWithSuppressedDefaultDebugAction, RunConfigurationWithSuppressedDefaultRunAction {
  public String MAIN_CLASS_NAME;
  public boolean DEBUG;
  public String VM_PARAMETERS;
  public String PROGRAM_PARAMETERS;
  public String WORKING_DIRECTORY;
  public boolean ALTERNATIVE_JRE_PATH_ENABLED;
  public String ALTERNATIVE_JRE_PATH;
  public boolean ENABLE_SWING_INSPECTOR;
  public String ENV_VARIABLES;
  @NotNull
  private final Map<String, String> envs = Maps.newLinkedHashMap();
  public boolean PASS_PARENT_ENVS = true;

  public DefracRunConfiguration(@NotNull final Project project,
                                @NotNull final ConfigurationFactory factory) {
    super(new JavaRunConfigurationModule(project, false), factory);
  }

  @Override
  public Collection<Module> getValidModules() {
    final List<Module> result = new ArrayList<Module>();
    final Module[] modules = ModuleManager.getInstance(getProject()).getModules();

    for(final Module module : modules) {
      final DefracFacet facet = DefracFacet.getInstance(module);

      if(facet != null && !facet.getPlatform().isGeneric()) {
        result.add(module);
      }
    }

    return result;
  }

  @NotNull
  @Override
  public DefracRunConfigurationEditor getConfigurationEditor() {
    return new DefracRunConfigurationEditor(getProject());
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull final Executor executor,
                                  @NotNull final ExecutionEnvironment environment) throws ExecutionException {
    final Module module = getConfigurationModule().getModule();

    if(module == null) {
      throw new ExecutionException("Module not found");
    }

    if(isNullOrEmpty(MAIN_CLASS_NAME)) {
      throw new ExecutionException(DefracBundle.message("facet.error.noMain"));
    }

    if(!RunConfigurationUtil.isEntryPoint(module, MAIN_CLASS_NAME)) {
      throw new ExecutionException(DefracBundle.message("run.error.illegalMainClass"));
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    assert facet != null : DefracBundle.message("facet.error.facetMissing", module.getName());

    if(!facet.getPlatform().isAvailableOnHostOS()) {
      throw new ExecutionException(DefracBundle.message("facet.error.unavailablePlatform", facet.getPlatform().displayName));
    }

    final DefracConfigOracle config = facet.getConfigOracle();

    if(config == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noSettings"));
    }

    final boolean isDebug = DefaultDebugExecutor.EXECUTOR_ID.equals(executor.getId());

    switch(facet.getPlatform()) {
      case JVM: return new JvmRunningState(environment, this, facet);
      case WEB: return new WebRunningState(environment, this);
      default:  return new DefracRunningState(environment, facet, isDebug);
    }
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    final JavaRunConfigurationModule configurationModule = getConfigurationModule();
    configurationModule.checkForWarning();

    final Module module = configurationModule.getModule();

    if(module == null) {
      throw new RuntimeConfigurationError("Module not found");
    }

    if(isNullOrEmpty(MAIN_CLASS_NAME)) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.noMain"));
    }

    if(!RunConfigurationUtil.isEntryPoint(module, MAIN_CLASS_NAME)) {
      throw new RuntimeConfigurationError(DefracBundle.message("run.error.illegalMainClass"));
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

    final DefracConfigOracle config = facet.getConfigOracle();

    if(config == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.noSettings"));
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    readModule(element);

    // le deprecated
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    writeModule(element);

    // le deprecated
    DefaultJDOMExternalizer.writeExternal(this, element);
  }

  @Override
  public String suggestedName() {
    final JavaRunConfigurationModule configurationModule =
        getConfigurationModule();

    final Module module = configurationModule.getModule();

    if(module == null) {
      return super.suggestedName();
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      return super.suggestedName();
    }

    final String name = module.getName();

    if(isNullOrEmpty(name)) {
      return super.suggestedName();
    }

    return name+" ("+facet.getConfiguration().getPlatform().displayName+')';
  }

  @Override
  public void setVMParameters(final String value) {
    VM_PARAMETERS = value;
  }

  @Override
  public String getVMParameters() {
    return VM_PARAMETERS;
  }

  @Override
  public boolean isAlternativeJrePathEnabled() {
    return ALTERNATIVE_JRE_PATH_ENABLED;
  }

  @Override
  public void setAlternativeJrePathEnabled(final boolean value) {
    ALTERNATIVE_JRE_PATH_ENABLED = value;
  }

  @Override
  public String getAlternativeJrePath() {
    return ALTERNATIVE_JRE_PATH;
  }

  @Override
  public void setAlternativeJrePath(final String value) {
    ALTERNATIVE_JRE_PATH = value;
  }

  @Nullable
  @Override
  public String getRunClass() {
    return MAIN_CLASS_NAME;
  }

  public void setRunClass(final String value) {
    MAIN_CLASS_NAME = value;
  }

  @Nullable
  @Override
  public String getPackage() {
    return null;
  }

  @Override
  public void setProgramParameters(@Nullable final String value) {
    PROGRAM_PARAMETERS = value;
  }

  @Nullable
  @Override
  public String getProgramParameters() {
    return PROGRAM_PARAMETERS;
  }

  @Override
  public void setWorkingDirectory(@Nullable final String value) {
    WORKING_DIRECTORY = ExternalizablePath.urlValue(value);
  }

  @Nullable
  @Override
  public String getWorkingDirectory() {
    return WORKING_DIRECTORY;
  }

  @Override
  public void setEnvs(@NotNull final Map<String, String> value) {
    envs.clear();
    envs.putAll(value);
  }

  @NotNull
  @Override
  public Map<String, String> getEnvs() {
    return envs;
  }

  @Override
  public void setPassParentEnvs(final boolean value) {
    PASS_PARENT_ENVS = value;
  }

  @Override
  public boolean isPassParentEnvs() {
    return PASS_PARENT_ENVS;
  }
}
