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

package defrac.intellij.sdk;

import defrac.intellij.sdk.ui.DefracSdkConfigurableForm;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static defrac.intellij.sdk.DefracSdkUtil.isDefracSdk;
import static defrac.intellij.sdk.JdkUtil.isJavaSdk;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public final class DefracSdkConfigurable implements AdditionalDataConfigurable {
  @NotNull
  private final DefracSdkConfigurableForm form;

  @Nullable
  private Sdk sdk;

  @NotNull
  private final SdkModel sdkModel;

  @NotNull
  private final SdkModel.Listener listener;

  public DefracSdkConfigurable(@NotNull final SdkModel sdkModel,
                               @NotNull final SdkModificator sdkModificator) {
    this.sdkModel = sdkModel;

    form = new DefracSdkConfigurableForm(sdkModel, sdkModificator);

    listener = new SdkModel.Listener() {
      @Override
      public void sdkAdded(Sdk sdk) {
        if(isJavaSdk(sdk)) {
          form.addJavaSdk(sdk);
        }
      }

      @Override
      public void beforeSdkRemove(Sdk sdk) {
        if(isJavaSdk(sdk)) {
          form.removeJavaSdk(sdk);
        }
      }

      @Override
      public void sdkChanged(Sdk sdk, String previousName) {
        if(isJavaSdk(sdk)) {
          form.updateJdks(sdk, previousName);
        }
      }

      @Override
      public void sdkHomeSelected(Sdk sdk, String newSdkHome) {
        if(sdk != null && isDefracSdk(sdk)) {
          form.internalJdkUpdate(sdk);
        }
      }
    };

    sdkModel.addListener(listener);
  }

  @Override
  public void setSdk(@Nullable final Sdk value) {
    sdk = value;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return form.getContentPanel();
  }

  @Override
  public boolean isModified() {
    final DefracSdkAdditionalData data = sdk == null ? null : (DefracSdkAdditionalData)sdk.getSdkAdditionalData();
    final Sdk javaSdk = data == null ? null : data.getJavaSdk();
    final String javaSdkHomePath = javaSdk == null ? null : javaSdk.getHomePath();
    final Sdk selectedSdk = form.getSelectedSdk();
    final String selectedSdkHomePath = selectedSdk == null ? null : selectedSdk.getHomePath();

    return !FileUtil.pathsEqual(javaSdkHomePath, selectedSdkHomePath);
  }

  @Override
  public void apply() throws ConfigurationException {
    final Sdk javaSdk = form.getSelectedSdk();
    final DefracSdkAdditionalData newData = new DefracSdkAdditionalData(checkNotNull(sdk), javaSdk);
    final SdkModificator modificator = sdk.getSdkModificator();

    newData.setDefracVersion(form.getSelectedDefracVersion());

    modificator.setVersionString(javaSdk == null ? null : javaSdk.getVersionString());
    modificator.setSdkAdditionalData(newData);

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        modificator.commitChanges();
      }
    });
  }

  @Override
  public void reset() {
    if(sdk == null) {
      return;
    }

    final SdkAdditionalData arbitraryData = sdk.getSdkAdditionalData();

    if(!(arbitraryData instanceof DefracSdkAdditionalData)) {
      return;
    }

    final DefracSdkAdditionalData data = (DefracSdkAdditionalData)arbitraryData;

    form.init(data.getJavaSdk(), sdk, data.getDefracVersion());
  }

  @Override
  public void disposeUIResources() {
    sdkModel.removeListener(listener);
  }
}
