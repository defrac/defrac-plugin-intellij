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

import com.intellij.ide.util.projectWizard.SdkSettingsStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.util.Condition;

/**
 */
public final class DefracModifiedSettingsStep extends SdkSettingsStep {
  private DefracModuleBuilder moduleBuilder;

  public DefracModifiedSettingsStep(final SettingsStep settingsStep, final DefracModuleBuilder moduleBuilder) {
    super(settingsStep, moduleBuilder, new Condition<SdkTypeId>() {
      @Override
      public boolean value(final SdkTypeId sdkTypeId) {
        return moduleBuilder.isSuitableSdkType(sdkTypeId);
      }
    });

    this.moduleBuilder = moduleBuilder;

    if(myJdkComboBox.getSelectedJdk() != null) {
      OnSdkSelected(myJdkComboBox.getSelectedJdk());
    }
  }

  @Override
  protected void OnSdkSelected(final Sdk sdk) {
    moduleBuilder.setDefracSdk(sdk);
  }
}
