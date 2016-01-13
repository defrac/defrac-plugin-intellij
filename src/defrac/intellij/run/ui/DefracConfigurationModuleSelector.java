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

package defrac.intellij.run.ui;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;

/**
 */
public final class DefracConfigurationModuleSelector extends ConfigurationModuleSelector {
  @NotNull
  private final Condition<Module> condition;

  public DefracConfigurationModuleSelector(@NotNull final Project project,
                                           @NotNull final ModulesComboBox modulesComboBox) {
    super(project, modulesComboBox);

    this.condition = new Condition<Module>() {
      @Override
      public boolean value(final Module module) {
        final DefracFacet facet = DefracFacet.getInstance(module);

        return facet != null
            && !facet.getPlatform().isGeneric()
            && !facet.isMacroLibrary();
      }
    };
  }

  @Override
  public boolean isModuleAccepted(final Module module) {
    return condition.value(module) && super.isModuleAccepted(module);
  }
}
