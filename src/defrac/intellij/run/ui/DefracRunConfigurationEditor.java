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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.ui.AddEditDeleteListPanel;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.run.DefracRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import static defrac.intellij.run.DefracRunUtil.getCompileTimeQualifiedName;
import static defrac.intellij.run.DefracRunUtil.getRuntimeQualifiedName;

/**
 */
public final class DefracRunConfigurationEditor extends SettingsEditor<DefracRunConfiguration> {
  @NotNull
  private final Project project;

  private JPanel panel;
  private DefracModuleComboBox moduleComboBox;
  private DefracMainClassTextFieldWithBrowseButton mainClassTextField;
  private JRadioButton strictModeRadioButton;
  private JRadioButton minifyRadioButton;
  private JRadioButton runInEmulatorRadioButton;
  private JRadioButton runOnDeviceRadioButton;
  private AddEditDeleteListPanel keepSetPanel;

  public DefracRunConfigurationEditor(@NotNull final Project project) {
    this.project = project;

    moduleComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        validateState();
      }
    });

    runInEmulatorRadioButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        runOnDeviceRadioButton.setSelected(!runInEmulatorRadioButton.isSelected());
      }
    });

    runOnDeviceRadioButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        runInEmulatorRadioButton.setSelected(!runOnDeviceRadioButton.isSelected());
      }
    });

    final DefracMainClassBrowser classBrowser = new DefracMainClassBrowser(project, moduleComboBox.moduleSelector);
    classBrowser.setField(mainClassTextField);
  }

  private void validateState() {
    final Module selectedModule = moduleComboBox.getSelectedModule();

    mainClassTextField.setEnabled(selectedModule != null);

    final DefracFacet facet = DefracFacet.getInstance(selectedModule);

    if(facet != null) {
      final boolean supportLaunchMode = facet.getPlatform().isAndroid();

      runInEmulatorRadioButton.setEnabled(supportLaunchMode);
      runOnDeviceRadioButton.setEnabled(supportLaunchMode);

      final boolean supportStrictMode = facet.getPlatform().isWeb();

      strictModeRadioButton.setEnabled(supportStrictMode);

      final boolean supportMinify = facet.getPlatform().isWeb();

      minifyRadioButton.setEnabled(supportMinify);
    }
  }

  private void $$$setupUI$$$() {
    createUIComponents();
  }

  private void createUIComponents() {
    moduleComboBox = new DefracModuleComboBox(project);
    mainClassTextField = new DefracMainClassTextFieldWithBrowseButton(project, moduleComboBox.moduleSelector);
    keepSetPanel = new AddEditDeleteListPanel<String>(null, new ArrayList<String>()) {

      @Nullable
      @Override
      protected String editSelectedItem(final String o) {
        return showEditDialog(o);
      }

      @Nullable
      @Override
      protected String findItemToAdd() {
        return showEditDialog("");
      }

      @Nullable
      private String showEditDialog(String initialValue) {
        return Messages.showInputDialog(this, null, "Create Keep Pattern", null, initialValue, new InputValidatorEx() {
          public boolean checkInput(String inputString) {
            return !StringUtil.isEmpty(inputString);
          }

          public boolean canClose(String inputString) {
            return checkInput(inputString);
          }

          @Nullable
          public String getErrorText(String inputString) {
            // TODO: check pattern
            return !this.checkInput(inputString) ? "Pattern cannot be empty" : null;
          }
        });
      }
    };
  }

  @Override
  protected void resetEditorFrom(final DefracRunConfiguration configuration) {
    moduleComboBox.moduleSelector.reset(configuration);
    mainClassTextField.setText(getCompileTimeQualifiedName(configuration.getRunClass()));
    strictModeRadioButton.setSelected(configuration.isStrict());
    minifyRadioButton.setSelected(configuration.isMinify());

    runInEmulatorRadioButton.setSelected(configuration.launchInEmulator());
    runOnDeviceRadioButton.setSelected(configuration.launchOnDevice());

    validateState();
  }

  @Override
  protected void applyEditorTo(final DefracRunConfiguration configuration) throws ConfigurationException {
    final PsiClass mainClass = moduleComboBox.moduleSelector.findClass(mainClassTextField.getText());

    moduleComboBox.moduleSelector.applyTo(configuration);
    configuration.setMainClassName(getRuntimeQualifiedName(mainClass));
    configuration.setStrict(strictModeRadioButton.isSelected());
    configuration.setMinify(minifyRadioButton.isSelected());

    if(runInEmulatorRadioButton.isSelected()) {
      configuration.setLaunchInEmulator();
    } else if(runOnDeviceRadioButton.isSelected()) {
      configuration.setLaunchOnDevice();
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return panel;
  }

}
