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

import javax.swing.*;

/**
 */
public final class EmptyModuleWizard {
  private JPanel myPanel;
  private JTextField myApplicationNameTextField;
  private JTextField myPackageNameTextField;
  private JLabel myErrorLabel;
  private JCheckBox myWebCheckBox;
  private JCheckBox myIOSCheckBox;
  private JCheckBox myAndroidCheckBox;
  private JCheckBox myJVMCheckBox;

  public EmptyModuleWizard(String name) {
    DefracWizardUtil.initializeApplicationSettingsInput(myApplicationNameTextField, myPackageNameTextField, name);
    DefracWizardUtil.handleApplicationSettingsInput(myApplicationNameTextField, myPackageNameTextField, myErrorLabel);
    DefracWizardUtil.handlePlatformsSettingsInput(myWebCheckBox, myIOSCheckBox, myAndroidCheckBox, myJVMCheckBox, myErrorLabel);

    myErrorLabel.setForeground(JBColor.RED);
  }

  @NotNull
  public String getPackageName() {
    return myPackageNameTextField.getText().trim();
  }

  @NotNull
  public String getApplicationName() {
    return myApplicationNameTextField.getText().trim();
  }

  public boolean isWebSupported() {
    return myWebCheckBox.isSelected();
  }

  public boolean isIOSSupported() {
    return myIOSCheckBox.isSelected();
  }

  public boolean isAndroidSupported() {
    return myAndroidCheckBox.isSelected();
  }

  public boolean isJVMSupported() {
    return myJVMCheckBox.isSelected();
  }

  @NotNull
  public JComponent getComponent() {
    return myPanel;
  }

  @NotNull
  public JComponent getPreferredFocusedComponent() {
    return myApplicationNameTextField;
  }

  public boolean validate() throws ConfigurationException {
    String message = DefracWizardUtil.validatePackageName(getPackageName());

    if(!message.isEmpty()) {
      throw new ConfigurationException(message);
    }

    message = DefracWizardUtil.validatePlatforms(myWebCheckBox, myIOSCheckBox, myAndroidCheckBox, myJVMCheckBox);

    if(!message.isEmpty()) {
      throw new ConfigurationException(message);
    }
    return true;
  }
}
