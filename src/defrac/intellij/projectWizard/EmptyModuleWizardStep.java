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

package defrac.intellij.projectWizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import defrac.intellij.projectWizard.ui.EmptyModuleWizard;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 */
public final class EmptyModuleWizardStep extends ModuleWizardStep {
  @NotNull
  private final DefracModuleBuilder moduleBuilder;

  @NotNull
  private final EmptyModuleWizard wizard;

  public EmptyModuleWizardStep(@NotNull final DefracModuleBuilder moduleBuilder) {
    this.moduleBuilder = moduleBuilder;
    this.wizard = new EmptyModuleWizard(moduleBuilder.getName());
  }

  @Override
  public boolean validate() throws ConfigurationException {
    return wizard.validate();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return wizard.getPreferredFocusedComponent();
  }

  @Override
  public JComponent getComponent() {
    return wizard.getComponent();
  }

  @Override
  public void updateDataModel() {
    moduleBuilder.setApplicationName(wizard.getApplicationName());
    moduleBuilder.setPackageName(wizard.getPackageName());
    moduleBuilder.setWebSupported(wizard.isWebSupported());
    moduleBuilder.setIOSSupported(wizard.isIOSSupported());
    moduleBuilder.setJVMSupported(wizard.isJVMSupported());
    moduleBuilder.setAndroidSupported(wizard.isAndroidSupported());
  }
}
