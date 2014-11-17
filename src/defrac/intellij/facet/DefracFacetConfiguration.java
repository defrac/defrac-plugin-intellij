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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.ui.DefracFacetEditorTab;
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
  public DefracVersion getDefracVersion() {
    if(facet == null) {
      return null;
    }

    final Module module = facet.getModule();
    final Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();

    if(moduleSdk != null && isDefracSdk(moduleSdk)) {
      DefracSdkAdditionalData data = (DefracSdkAdditionalData)moduleSdk.getSdkAdditionalData();
      return data != null ? data.getDefracVersion() : null;
    }

    return null;
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
