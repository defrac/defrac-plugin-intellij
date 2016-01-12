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
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.ui.ClassBrowser;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import com.intellij.uiDesigner.core.GridConstraints;
import defrac.intellij.DefracPlatform;
import defrac.intellij.run.DefracRunConfigurationBase;
import defrac.intellij.run.DefracRunUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 *
 */
public class DefracRunConfigurationEditor<T extends ModuleBasedConfiguration & DefracRunConfigurationBase> extends SettingsEditor<T> {
  @NotNull
  private final Project project;
  @NotNull
  private final ConfigurationModuleSelector moduleSelector;

  private JPanel componentPanel;
  private ModulesComboBox moduleComboBox;
  private EditorTextFieldWithBrowseButton mainClassTextField;
  private JLabel moduleLabel;
  private SettingsEditor<T> customSettingsEditor;

  @SuppressWarnings("unchecked")
  public DefracRunConfigurationEditor(@NotNull final Project project, @NotNull final DefracPlatform platform) {
    this.project = project;
    this.moduleSelector = new DefracConfigurationModuleSelector(project, moduleComboBox, platform);

    moduleLabel.setLabelFor(moduleComboBox);

    moduleComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        // we need a module to select the main class
        mainClassTextField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });

    // disabled per default. Will be enabled when module is selected
    mainClassTextField.setEnabled(false);

    final ClassBrowser classBrowser = new DefracMainClassBrowser(project, moduleSelector);
    classBrowser.setField(mainClassTextField);
  }

  public void addCustomSettingsEditor(@NotNull final SettingsEditor<T> editor) {
    customSettingsEditor = editor;

    final GridConstraints constraints = new GridConstraints();
    constraints.setRow(2);
    constraints.setColumn(2);
    constraints.setFill(GridConstraints.FILL_BOTH);

    componentPanel.add(editor.getComponent(), constraints);
    componentPanel.invalidate();
  }

  private void $$$setupUI$$$() {
    createUIComponents();
  }

  private void createUIComponents() {
    mainClassTextField = new EditorTextFieldWithBrowseButton(project, true, new JavaCodeFragment.VisibilityChecker() {
      public Visibility isDeclarationVisible(PsiElement declaration, PsiElement place) {
        if(declaration instanceof PsiClass) {
          final PsiClass aClass = (PsiClass) declaration;

          if(isValidMainClass(aClass) || place.getParent() != null && isValidMainClass(moduleSelector.findClass(aClass.getQualifiedName()))) {
            return Visibility.VISIBLE;
          }
        }

        return Visibility.NOT_VISIBLE;
      }

      private boolean isValidMainClass(@Nullable final PsiClass cls) {
        return DefracRunUtil.isValidMainClass(moduleSelector.getModule(), cls);
      }
    });
  }

  @Override
  protected void resetEditorFrom(final T configuration) {
    moduleSelector.reset(configuration);
    mainClassTextField.setText(DefracRunUtil.getCompileTimeQualifiedName(configuration.getMain()));

    if(customSettingsEditor != null) {
      customSettingsEditor.resetFrom(configuration);
    }
  }

  @Override
  protected void applyEditorTo(final T configuration) throws ConfigurationException {
    moduleSelector.applyTo(configuration);
    configuration.setMain(DefracRunUtil.getRuntimeQualifiedName(moduleSelector.findClass(mainClassTextField.getText())));

    if(customSettingsEditor != null) {
      customSettingsEditor.applyTo(configuration);
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return componentPanel;
  }
}
