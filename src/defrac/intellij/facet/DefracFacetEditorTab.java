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

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.PathUtil;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.ui.DefracFacetEditorForm;
import defrac.intellij.sdk.DefracSdkAdditionalData;
import defrac.intellij.sdk.DefracVersion;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.jgoodies.common.base.Strings.isEmpty;

/**
 *
 */
public final class DefracFacetEditorTab extends FacetEditorTab {
  @NotNull
  private final FacetEditorContext context;

  @NotNull
  private final DefracFacetConfiguration configuration;

  @NotNull
  private final DefracFacetEditorForm form;

  public DefracFacetEditorTab(@NotNull final FacetEditorContext context,
                              @NotNull final DefracFacetConfiguration configuration) {
    this.context = context;
    this.configuration = configuration;
    this.form = new DefracFacetEditorForm(
        context,
        (DefracFacet)context.getFacet());

    form.addPlatforms(DefracPlatform.values());
  }

  @NotNull
  private DefracFacet getFacet() {
    return (DefracFacet)context.getFacet();
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    return form.getComponentPanel();
  }

  @Override
  public boolean isModified() {
    return checkRelativePath(configuration.getState().SETTINGS_FILE_RELATIVE_PATH, form.getSettingsPath())
        || form.getSelectedPlatform() != configuration.getPlatform()
        || form.getSelectedSdk() != configuration.getDefracSdk()
        || form.getSkipJavac() != configuration.skipJavac()
        || form.isMacroLibrary() != configuration.isMacroLibrary();
  }

  @Override
  public void reset() {
    form.setSelectedPlatform(configuration.getPlatform());
    form.setSettingsPath(getFacet().getSettingsFile().getAbsolutePath());
    form.setSelectedSdk(configuration.getDefracSdk());
    form.setMacroLibrary(configuration.isMacroLibrary());
    form.setSkipJavac(configuration.skipJavac());
  }

  @Override
  public void disposeUIResources() {
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "defrac SDK Settings";
  }

  @Override
  public void apply() throws ConfigurationException {
    if(!isModified()) {
      return;
    }

    //

    configuration.getState().PLATFORM = form.getSelectedPlatform().name;
    configuration.getState().IS_MACRO_LIBRARY = form.isMacroLibrary();
    configuration.getState().SKIP_JAVAC = form.getSkipJavac();

    //

    final Sdk sdk = form.getSelectedSdk();
    final SdkAdditionalData arbitraryData = sdk.getSdkAdditionalData();

    if(!(arbitraryData instanceof DefracSdkAdditionalData)) {
      throw new ConfigurationException("Illegal SDK");
    }

    final DefracSdkAdditionalData data = (DefracSdkAdditionalData)arbitraryData;
    final DefracVersion version = data.getDefracVersion();

    if(version == null) {
      throw new ConfigurationException("SDK is not configured");
    }

    configuration.getState().DEFRAC_VERSION = version.getName();

    //

    final String absSettingsPath = form.getSettingsPath();

    if(isEmpty(absSettingsPath)) {
      throw new ConfigurationException("Settings file not specified");
    }

    final String relSettingsPath = getAndCheckRelativePath(absSettingsPath, true);

    configuration.getState().SETTINGS_FILE_RELATIVE_PATH = '/'+relSettingsPath;
  }

  private boolean checkRelativePath(@Nullable final String relativePathFromConfig,
                                    @NotNull final String absPathFromTextField) {
    final String pathFromConfig = isNullOrEmpty(relativePathFromConfig)
        ? ""
        : toAbsolutePath(relativePathFromConfig);

    final String pathFromTextField = absPathFromTextField.trim();

    return !FileUtil.pathsEqual(pathFromConfig, pathFromTextField);
  }

  @Nullable
  private String toRelativePath(@NotNull final String absPath) {
    final String independentAbsPath = FileUtil.toSystemIndependentName(absPath);
    String projectBasePath = DefracRootUtil.getBasePath(context);

    if(projectBasePath != null) {
      projectBasePath = FileUtil.toSystemIndependentName(projectBasePath);
      return FileUtil.getRelativePath(projectBasePath, independentAbsPath, '/');
    }

    return null;
  }

  @Nullable
  private String toAbsolutePath(String genRelativePath) {
    if(genRelativePath == null) {
      return null;
    }

    if(isEmpty(genRelativePath)) {
      return "";
    }

    final String projectBasePath = DefracRootUtil.getBasePath(context);

    if(projectBasePath == null) {
      return null;
    }

    final String path = PathUtil.getCanonicalPath(new File(projectBasePath, genRelativePath).getPath());
    return PathUtil.getLocalPath(path);
  }

  private String getAndCheckRelativePath(String absPath, boolean checkExists) throws ConfigurationException {
    if(absPath.indexOf('/') < 0 && absPath.indexOf(File.separatorChar) < 0) {
      throw new ConfigurationException(DefracBundle.message("facet.error.fileNotPartOfProject", FileUtil.toSystemDependentName(absPath)));
    }

    final String relativePath = toRelativePath(absPath);

    if(isNullOrEmpty(relativePath)) {
      throw new ConfigurationException(DefracBundle.message("facet.error.fileNotPartOfProject", FileUtil.toSystemDependentName(absPath)));
    }

    if(checkExists && LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(absPath)) == null) {
      throw new ConfigurationException(DefracBundle.message("facet.error.fileDoesNotExist", FileUtil.toSystemDependentName(absPath)));
    }

    return relativePath;
  }
}
