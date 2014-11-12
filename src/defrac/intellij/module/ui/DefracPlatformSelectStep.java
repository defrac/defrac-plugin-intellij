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

package defrac.intellij.module.ui;

import defrac.intellij.module.DefracModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 */
public final class DefracPlatformSelectStep extends ModuleWizardStep {
  @NotNull
  private final DefracPlatformSelectForm form;

  @NotNull
  private final DefracModuleBuilder moduleBuilder;

  public DefracPlatformSelectStep(@NotNull final DefracModuleBuilder moduleBuilder) {
    this.moduleBuilder = moduleBuilder;
    this.form = new DefracPlatformSelectForm();
  }

  @Override
  public JComponent getComponent() {
    return form.getComponentPanel();
  }

  @Override
  public void updateDataModel() {
    moduleBuilder.setPlatforms(form.getSelectedPlatforms());
  }

  @Override
  public boolean validate() throws ConfigurationException {
    return !form.getSelectedPlatforms().isEmpty();
  }

  @Override
  public String getName() {
    return "Defrac Target Platforms";
  }


}
