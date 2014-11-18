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
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import defrac.intellij.DefracBundle;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.run.ui.DefracRunConfigurationEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracRunConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule> {
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
  public DefracRunningState getState(@NotNull final Executor executor,
                                  @NotNull final ExecutionEnvironment environment) throws ExecutionException {

    final Module module = getConfigurationModule().getModule();

    if(module == null) {
      throw new ExecutionException("Module not found");
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.facetMissing", module.getName()));
    }

    if(facet.getDefracVersion() == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noVersion"));
    }

    if(!facet.getSettingsFile().canRead()) {
      throw new ExecutionException(DefracBundle.message("facet.error.fileDoesNotExist", facet.getSettingsFile()));
    }

    if(facet.getPlatform().isGeneric()) {
      throw new ExecutionException(DefracBundle.message("facet.error.genericPlatform", module.getName()));
    }

    final boolean isDebug = DefaultDebugExecutor.EXECUTOR_ID.equals(executor.getId());

    if(isDebug) {
      // no debug support at the moment
      return null;
    }

    return new DefracRunningState(
        environment,
        facet,
        isDebug
    );
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    final JavaRunConfigurationModule configurationModule = getConfigurationModule();
    configurationModule.checkForWarning();

    final Module module = configurationModule.getModule();

    if(module == null) {
      return;
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.facetMissing", module.getName()));
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
}
