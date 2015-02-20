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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.ValidatableSdkAdditionalData;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static defrac.intellij.sdk.JdkUtil.isApplicableJdk;

/**
 *
 */
public final class DefracSdkAdditionalData implements ValidatableSdkAdditionalData {
  @NonNls
  private static final String ATTR_JDK = "jdk";

  @NonNls
  private static final String ATTR_VER = "ver";

  @NotNull
  private final Sdk defracSdk;

  @Nullable
  private Sdk javaSdk;

  @Nullable
  private String javaSdkName;

  @Nullable
  private String defracVersionName;

  @Nullable
  private DefracVersion defracVersion;

  public DefracSdkAdditionalData(@NotNull final Sdk defracSdk, @Nullable final Sdk javaSdk) {
    this.defracSdk = defracSdk;
    this.javaSdk = javaSdk;
  }

  public DefracSdkAdditionalData(@NotNull final Sdk defracSdk, @NotNull final Element element) {
    this.defracSdk = defracSdk;
    this.javaSdkName = element.getAttributeValue(ATTR_JDK);
    this.defracVersionName = element.getAttributeValue(ATTR_VER);
  }

  @Override
  public void checkValid(@Nullable final SdkModel sdkModel) throws ConfigurationException {
    if(getJavaSdk() == null) {
      throw new ConfigurationException("TODO");//TODO(joa): message
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    final DefracSdkAdditionalData data = (DefracSdkAdditionalData)super.clone();
    final Sdk jdk = getJavaSdk();
    data.setJavaSdk(checkNotNull(jdk));
    data.defracVersion = defracVersion;
    data.defracVersionName = defracVersionName;
    return data;
  }

  @Nullable
  public Sdk getJavaSdk() {
    if(javaSdk != null) {
      return javaSdk;
    }

    final ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();

    if(!isNullOrEmpty(javaSdkName)) {
      javaSdk = jdkTable.findJdk(javaSdkName);
      javaSdkName = null;
    } else {
      for(final Sdk jdk : jdkTable.getAllJdks()) {
        if(isApplicableJdk(jdk)) {
          javaSdk = jdk;
          break;
        }
      }
    }

    return javaSdk;
  }

  public void setJavaSdk(@Nullable final Sdk javaSdk) {
    this.javaSdk = javaSdk;
  }

  public void save(@NotNull final Element element) {
    final Sdk sdk = getJavaSdk();

    if(sdk != null) {
      element.setAttribute(ATTR_JDK, sdk.getName());
    }

    final DefracVersion version = getDefracVersion();

    if(version != null) {
      element.setAttribute(ATTR_VER, version.getName());
    }
  }

  public void setDefracVersion(@Nullable final DefracVersion value) {
    defracVersion = value;
    defracVersionName = null;
  }

  @Nullable
  public DefracVersion getDefracVersion() {
    if(!isNullOrEmpty(defracVersionName)) {
      defracVersion = DefracSdkUtil.findVersion(defracSdk, defracVersionName);
      defracVersionName = null;
    }

    return defracVersion;
  }

  @Nullable
  public String getGlobalSettings() {
    return defracSdk.getHomePath()+File.separatorChar+"global.settings";
  }
}
