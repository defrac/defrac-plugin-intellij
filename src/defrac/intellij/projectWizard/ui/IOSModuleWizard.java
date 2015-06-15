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

package defrac.intellij.projectWizard.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.JBColor;
import defrac.intellij.projectWizard.DefracWizardUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 */
public final class IOSModuleWizard {
  private JPanel panel;
  private JTextField applicationNameTextField;
  private JTextField mainClassNameTextField;
  private JTextField packageNameTextField;
  private JLabel errorLabel;

  public IOSModuleWizard(@Nullable final String name) {
    DefracWizardUtil.initializeApplicationSettingsInput(applicationNameTextField, packageNameTextField, name);
    DefracWizardUtil.handleApplicationSettingsInput(applicationNameTextField, packageNameTextField, mainClassNameTextField, errorLabel, "Main");
    DefracWizardUtil.handleMainClassSettingsInput(mainClassNameTextField, errorLabel);

    errorLabel.setForeground(JBColor.RED);

    mainClassNameTextField.setText(DefracWizardUtil.DEFAULT_MAIN_CLASS_NAME);
  }

  @NotNull
  public String getPackageName() {
    return packageNameTextField.getText().trim();
  }

  @NotNull
  public String getApplicationName() {
    return applicationNameTextField.getText().trim();
  }

  @NotNull
  public String getMainClassName() {
    return mainClassNameTextField.getText().trim();
  }

  @NotNull
  public JComponent getComponent() {
    return panel;
  }

  @NotNull
  public JComponent getPreferredFocusedComponent() {
    return applicationNameTextField;
  }

  public boolean validate() throws ConfigurationException {
    String message = DefracWizardUtil.validatePackageName(getPackageName());

    if(!message.isEmpty()) {
      throw new ConfigurationException(message);
    }

    message = DefracWizardUtil.validateMainClassName(getMainClassName());

    if(!message.isEmpty()) {
      throw new ConfigurationException(message);
    }
    return true;
  }
}
