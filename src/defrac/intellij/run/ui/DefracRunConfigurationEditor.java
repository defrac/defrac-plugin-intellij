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

import com.google.common.base.Strings;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.run.DefracRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 */
public final class DefracRunConfigurationEditor extends SettingsEditor<DefracRunConfiguration> {

  @NotNull
  private final ConfigurationModuleSelector moduleSelector;

  private JPanel componentPanel;
  private JLabel moduleLabel;
  private JComboBox moduleComboBox;
  private JTextField mainClassTextField;

  public DefracRunConfigurationEditor(@NotNull final Project project) {
    this.moduleSelector = new ConfigurationModuleSelector(project, moduleComboBox) {
      @Override
      public boolean isModuleAccepted(final Module module) {
        if(module == null || !super.isModuleAccepted(module)) {
          return false;
        }

        final DefracFacet facet = DefracFacet.getInstance(module);

        return facet != null
            && !facet.getPlatform().isGeneric()
            && !facet.isMacroLibrary();
      }

      @Override
      public void applyTo(final ModuleBasedConfiguration configurationModule) {
        super.applyTo(configurationModule);
      }
    };

    moduleLabel.setLabelFor(moduleComboBox);
  }

  @Override
  protected void resetEditorFrom(final DefracRunConfiguration configuration) {
    mainClassTextField.setText(Strings.nullToEmpty(configuration.getRunClass()));
    moduleSelector.reset(configuration);
  }

  @Override
  protected void applyEditorTo(final DefracRunConfiguration configuration) throws ConfigurationException {
    configuration.setRunClass(mainClassTextField.getText());
    moduleSelector.applyTo(configuration);
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return componentPanel;
  }
}
