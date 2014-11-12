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
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

/**
 *
 */
public final class DefracSettingsStep extends ModuleWizardStep {
  @NotNull
  private final SettingsStep settingsStep;

  @NotNull
  private final DefracSettingsForm form;

  @NotNull
  private final DefracModuleBuilder moduleBuilder;

  public DefracSettingsStep(@NotNull final SettingsStep settingsStep, @NotNull final DefracModuleBuilder moduleBuilder) {
    //TODO(joa): see todo in DefracModuleType#modifySettingsStep
    assert settingsStep instanceof ModuleWizardStep;

    this.settingsStep = settingsStep;
    this.moduleBuilder = moduleBuilder;
    this.form = new DefracSettingsForm();

    form.updateFields(settingsStep.getModuleNameField().getText());

    settingsStep.addExpertPanel(form.getComponentPanel());
    settingsStep.getModuleNameField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final DocumentEvent event) {
        form.updateFields(settingsStep.getModuleNameField().getText());
      }
    });
  }

  @Override
  public JComponent getComponent() {
    return ((ModuleWizardStep)settingsStep).getComponent();
  }

  @Override
  public void updateDataModel() {
    moduleBuilder.setPackage(form.getPackage());
    moduleBuilder.setIdentifier(form.getIdentifier());
    moduleBuilder.setVersion(form.getVersion());
  }
}
