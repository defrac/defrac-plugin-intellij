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

package defrac.intellij.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import defrac.intellij.DefracPlatform;
import defrac.intellij.jps.model.impl.JpsDefracModuleProperties;
import defrac.intellij.sdk.DefracSdkAdditionalData;
import defrac.intellij.sdk.DefracVersion;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static defrac.intellij.sdk.DefracSdkUtil.isDefracSdk;

/**
 *
 */
public final class DefracFacetConfiguration implements FacetConfiguration, PersistentStateComponent<JpsDefracModuleProperties> {
  @Nullable
  private DefracFacet facet = null;

  @Nullable
  private JpsDefracModuleProperties state = new JpsDefracModuleProperties();

  public DefracFacetConfiguration() {
  }

  @Nullable
  public Sdk getDefracSdk() {
    if(facet == null) {
      return null;
    }

    final String version = getState().DEFRAC_VERSION;

    for(final Sdk sdk : ProjectJdkTable.getInstance().getAllJdks()) {
      if(!isDefracSdk(sdk)) {
        continue;
      }

      final DefracSdkAdditionalData data = (DefracSdkAdditionalData)sdk.getSdkAdditionalData();

      if(data == null) {
        continue;
      }

      final DefracVersion defracVersion = data.getDefracVersion();

      if(defracVersion == null) {
        continue;
      }

      if(defracVersion.getName().equals(version)) {
        return sdk;
      }
    }

    return null;
  }

  @Nullable
  public DefracVersion getDefracVersion() {
    final Sdk sdk = getDefracSdk();

    if(sdk == null) {
      return null;
    }

    final DefracSdkAdditionalData data = (DefracSdkAdditionalData)sdk.getSdkAdditionalData();

    if(data == null) {
      return null;
    }

    return data.getDefracVersion();
  }

  public boolean isMacroLibrary() {
    return getState().IS_MACRO_LIBRARY;
  }

  public boolean skipJavac() {
    return getState().SKIP_JAVAC;
  }

  @NotNull
  public DefracPlatform getPlatform() {
    return DefracPlatform.byName(getState().PLATFORM);
  }

  @Override
  @NotNull
  public FacetEditorTab[] createEditorTabs(final FacetEditorContext editorContext,
                                           final FacetValidatorsManager validatorsManager) {
    return new FacetEditorTab[] {
        new DefracFacetEditorTab(editorContext, this)
    };
  }

  @Override
  public void readExternal(final Element element) throws InvalidDataException {}

  @Override
  public void writeExternal(final Element element) throws WriteExternalException {}

  public void setFacet(@Nullable final DefracFacet facet) {
    this.facet = facet;
  }

  @Nullable
  public DefracFacet getFacet() {
    return facet;
  }

  @NotNull
  @Override
  public JpsDefracModuleProperties getState() {
    return checkNotNull(state);
  }

  @Override
  public void loadState(final JpsDefracModuleProperties value) {
    state = value;
  }
}
