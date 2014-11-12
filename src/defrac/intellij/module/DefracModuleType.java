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

package defrac.intellij.module;

import defrac.intellij.DefracBundle;
import defrac.intellij.DefracIcons;
import defrac.intellij.module.ui.DefracPlatformSelectStep;
import defrac.intellij.module.ui.DefracSettingsStep;
import defrac.intellij.sdk.DefracSdkType;
import com.google.common.collect.Lists;
import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

import static defrac.intellij.sdk.DefracSdkUtil.isDefracSdk;

/**
 *
 */
public final class DefracModuleType extends ModuleType<DefracModuleBuilder> {
  @NotNull @NonNls public static final String ID = "DEFRAC_MODULE";
  @NotNull public static final String NAME = DefracBundle.message("defrac.module.name");
  @NotNull public static final String DESCRIPTION = DefracBundle.message("defrac.module.description");

  @NotNull
  public static ModuleType getInstance() {
    return ModuleTypeManager.getInstance().findByID(ID);
  }

  public DefracModuleType() {
    super(ID);
  }

  @NotNull
  @Override
  public DefracModuleBuilder createModuleBuilder() {
    return new DefracModuleBuilder();
  }

  @NotNull
  @Override
  public String getName() {
    return NAME;
  }

  @NotNull
  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public Icon getBigIcon() {
    return DefracIcons.DEFRAC;
  }

  @Override
  public Icon getNodeIcon(@Deprecated boolean isOpened) {
    return DefracIcons.DEFRAC;
  }

  @Override
  public boolean isValidSdk(@NotNull Module module, Sdk projectSdk) {
    return isDefracSdk(projectSdk);
  }

  @NotNull
  @Override
  public ModuleWizardStep[] createWizardSteps(@NotNull final WizardContext wizardContext,
                                              @NotNull final DefracModuleBuilder moduleBuilder,
                                              @NotNull final ModulesProvider modulesProvider) {
    final ArrayList<ModuleWizardStep> wizardSteps = Lists.newArrayListWithCapacity(2);

    //TODO(joa): do this only if project sdk is not defrac
    if(!isDefracSdk(moduleBuilder.getModuleJdk())) {
      wizardSteps.add(new ProjectJdkForModuleStep(wizardContext, DefracSdkType.getInstance()) {
        @Override
        public void updateDataModel() {
          super.updateDataModel();
          moduleBuilder.setModuleJdk(getJdk());
        }
      });
    }

    wizardSteps.add(new DefracPlatformSelectStep(moduleBuilder));

    final ModuleWizardStep[] wizardStepsArray =
        wizardSteps.toArray(new ModuleWizardStep[wizardSteps.size()]);

    return ArrayUtil.mergeArrays(
        wizardStepsArray, super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider));
  }

  @Nullable
  @Override
  public ModuleWizardStep modifySettingsStep(@NotNull final SettingsStep settingsStep,
                                             @NotNull final ModuleBuilder moduleBuilder) {
    //TODO(joa): seems fishy, how to integrate with updateModel of settings step?
    if(isModuleWizardStep(settingsStep) && isDefracModuleBuilder(moduleBuilder)) {
      //noinspection Contract
      return new DefracSettingsStep(settingsStep, (DefracModuleBuilder)moduleBuilder);
    } else {
      return super.modifySettingsStep(settingsStep, moduleBuilder);
    }
  }

  private static boolean isDefracModuleBuilder(@Nullable final ModuleBuilder moduleBuilder) {
    return moduleBuilder instanceof DefracModuleBuilder;
  }

  private static boolean isModuleWizardStep(@Nullable final SettingsStep settingsStep) {
    return settingsStep instanceof ModuleWizardStep;
  }
}
