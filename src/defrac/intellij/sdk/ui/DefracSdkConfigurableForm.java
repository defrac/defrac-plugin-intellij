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

package defrac.intellij.sdk.ui;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.util.Comparing;
import defrac.intellij.sdk.DefracSdkAdditionalData;
import defrac.intellij.sdk.DefracVersion;
import defrac.intellij.ui.SdkRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static defrac.intellij.sdk.DefracSdkUtil.isDefracSdk;
import static defrac.intellij.sdk.JdkUtil.isApplicableJdk;

/**
 *
 */
public final class DefracSdkConfigurableForm {
  @NotNull
  private final SdkModel sdkModel;

  @NotNull
  private final DefaultComboBoxModel javaSdkModel = new DefaultComboBoxModel();

  @Nullable
  private DefracVersion selectedDefracVersion;

  private JComponent contentPanel;
  private JComboBox internalJdkComboBox;
  private JLabel internalJdkLabel;

  public DefracSdkConfigurableForm(@NotNull final SdkModel sdkModel,
                                   @NotNull final SdkModificator sdkModificator) {
    this.sdkModel = sdkModel;

    internalJdkLabel.setLabelFor(internalJdkComboBox);

    //noinspection unchecked
    internalJdkComboBox.setRenderer(new SdkRenderer());
    internalJdkComboBox.setModel(javaSdkModel);
  }

  public void addJavaSdk(@NotNull final Sdk sdk) {
    javaSdkModel.addElement(sdk);
  }

  public void removeJavaSdk(@NotNull final Sdk sdk) {
    javaSdkModel.removeElement(sdk);
  }

  private void updateJdks() {
    javaSdkModel.removeAllElements();
    for(final Sdk sdk : sdkModel.getSdks()) {
      if(isApplicableJdk(sdk)) {
        javaSdkModel.addElement(sdk);
      }
    }
  }

  public void updateJdks(@NotNull final Sdk sdk, @Nullable final String previousName) {
    final Sdk[] sdks = sdkModel.getSdks();

    for(final Sdk currentSdk : sdks) {
      if(currentSdk != null && isDefracSdk(currentSdk)) {
        final DefracSdkAdditionalData data = (DefracSdkAdditionalData)currentSdk.getSdkAdditionalData();
        final Sdk internalJava = data == null ? null : data.getJavaSdk();

        if(internalJava != null && Comparing.equal(internalJava.getName(), previousName)) {
          data.setJavaSdk(sdk);
        }
      }
    }

    updateJdks();
  }

  public void internalJdkUpdate(@NotNull final Sdk sdk) {
    final DefracSdkAdditionalData data = (DefracSdkAdditionalData)sdk.getSdkAdditionalData();

    if(data == null) {
      return;
    }

    final Sdk javaSdk = data.getJavaSdk();

    if(javaSdkModel.getIndexOf(javaSdk) == -1) {
      javaSdkModel.addElement(javaSdk);
    } else {
      javaSdkModel.setSelectedItem(javaSdk);
    }
  }

  public JComponent getContentPanel() {
    return contentPanel;
  }

  public Sdk getSelectedSdk() {
    return (Sdk)internalJdkComboBox.getSelectedItem();
  }

  public void init(@Nullable final Sdk jdk, @Nullable final Sdk defracSdk, @Nullable final DefracVersion defracVersion) {
    updateJdks();

    final String jdkName = jdk == null ? null : jdk.getName();

    if(defracSdk != null) {
      for(int i = 0; i < javaSdkModel.getSize(); i++) {
        final Sdk sdk = (Sdk)javaSdkModel.getElementAt(i);

        if(Comparing.strEqual(sdk.getName(), jdkName)) {
          internalJdkComboBox.setSelectedIndex(i);
          break;
        }
      }
    }

    //TODO(joa): let user choose between latest and distinct version
    //sdkLocation = defracSdk == null ? null : defracSdk.getHomePath();
    //DefracSdkData defracSdkData = sdkLocation == null ? null : DefracSdkData.getSdkData(sdkLocation);
    //freeze = true;
    //updateDefracVersion(defracSdkData, defracVersion);
    //freeze = false;

    selectedDefracVersion = defracVersion;
  }

  @Nullable
  public DefracVersion getSelectedDefracVersion() {
    return selectedDefracVersion;
  }
}
