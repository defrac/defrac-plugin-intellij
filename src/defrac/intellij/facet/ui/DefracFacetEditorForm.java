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

package defrac.intellij.facet.ui;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.facet.DefracRootUtil;
import defrac.intellij.sdk.DefracSdkUtil;
import defrac.intellij.ui.DefracPlatformRenderer;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.jgoodies.common.base.Strings.isEmpty;

/**
 *
 */
public final class DefracFacetEditorForm {
  @NotNull
  private final FacetEditorContext context;

  private JPanel componentPanel;
  private JComboBox platformComboBox;
  private TextFieldWithBrowseButton settingsField;
  private JLabel platformLabel;
  private JLabel settingsLabel;
  private JLabel defracSdkLabel;
  private JdkComboBox defracSdkComboBox;
  private JCheckBox macroLibraryCheckBox;
  private JCheckBox skipJavacCheckBox;
  private JButton newSdkButton;
  private ProjectSdksModel projectSdksModel;

  @NotNull
  private final DefaultComboBoxModel platformModel = new DefaultComboBoxModel();

  public DefracFacetEditorForm(@NotNull final FacetEditorContext context,
                               @NotNull final DefracFacet facet) {
    this.context = context;

    platformLabel.setLabelFor(platformComboBox);
    settingsLabel.setLabelFor(settingsField);
    defracSdkLabel.setLabelFor(defracSdkComboBox);

    //noinspection unchecked
    platformComboBox.setRenderer(new DefracPlatformRenderer());
    platformComboBox.setModel(platformModel);

    settingsField.
        getButton().
        addActionListener(
            new FolderFieldListener(
                settingsField,
                DefracRootUtil.getBaseDir(facet),
                DefaultSettingsFilter.INSTANCE));

    defracSdkComboBox.setSetupButton(
        newSdkButton, null, projectSdksModel, new JdkComboBox.NoneJdkComboBoxItem(), null, false);
  }

  public void addPlatform(@NotNull final DefracPlatform platform) {
    platformModel.addElement(platform);
  }

  public DefracPlatform getSelectedPlatform() {
    return (DefracPlatform)platformComboBox.getSelectedItem();
  }

  public void setSelectedPlatform(@NotNull final DefracPlatform platform) {
    platformComboBox.setSelectedItem(platform);
  }

  public void setMacroLibrary(final boolean value) {
    macroLibraryCheckBox.setSelected(value);
  }

  public boolean isMacroLibrary() {
    return macroLibraryCheckBox.isSelected();
  }

  public void setSkipJavac(final boolean value) {
    skipJavacCheckBox.setSelected(value);
  }

  public boolean getSkipJavac() {
    return skipJavacCheckBox.isSelected();
  }

  public void setSelectedSdk(@Nullable final Sdk sdk) {
    defracSdkComboBox.setSelectedJdk(sdk);
  }

  @Nullable
  public Sdk getSelectedSdk() {
    return defracSdkComboBox.getSelectedJdk();
  }

  public JPanel getComponentPanel() {
    return componentPanel;
  }

  @NotNull
  public String getSettingsPath() {
    return settingsField.getText().trim();
  }

  public void setSettingsPath(@Nullable final String path) {
    if(isNullOrEmpty(path)) {
      return;
    }

    settingsField.setText(path);
  }

  public void addPlatforms(final DefracPlatform[] platforms) {
    for(final DefracPlatform platform : platforms) {
      addPlatform(platform);
    }
  }

  private void createUIComponents() {
    projectSdksModel = DefracSdkUtil.getSdkModel(null);
    defracSdkComboBox = new JdkComboBox(
        projectSdksModel,
        DefracSdkUtil.IS_DEFRAC_VERSION);
  }

  private class FolderFieldListener implements ActionListener {
    @NotNull
    private final TextFieldWithBrowseButton field;

    @NotNull
    private final VirtualFile defaultDir;

    @NotNull
    private final Condition<VirtualFile> filter;

    private FolderFieldListener(@NotNull final TextFieldWithBrowseButton field,
                                @NotNull final VirtualFile defaultDir,
                                @NotNull final Condition<VirtualFile> filter) {
      this.field = field;
      this.defaultDir = defaultDir;
      this.filter = filter;
    }

    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
      final VirtualFile initialFile;
      String path = field.getText().trim();

      if(isEmpty(path)) {
        path = defaultDir.getPath();
      }

      initialFile = LocalFileSystem.getInstance().findFileByPath(path);

      VirtualFile[] files = chooserDirsUnderModule(initialFile, filter);

      if(files.length > 0) {
        assert files.length == 1;
        field.setText(FileUtil.toSystemDependentName(files[0].getPath()));
      }
    }

    private VirtualFile[] chooserDirsUnderModule(@Nullable VirtualFile initialFile,
                                                 @Nullable final Condition<VirtualFile> filter) {
      if(initialFile == null) {
        initialFile = context.getModule().getModuleFile();
      }

      if(initialFile == null) {
        String p = DefracRootUtil.getBasePath(context);

        if (p != null) {
          initialFile = LocalFileSystem.getInstance().findFileByPath(p);
        }
      }

      final FileChooserDescriptor descriptor =
          new FileChooserDescriptor(
              /*chooseFiles=*/true,
              /*chooseFolders=*/false,
              /*chooseJars=*/false,
              /*chooseJarsAsFiles=*/false,
              /*chooseJarContents=*/false,
              /*chooseMultiple=*/false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
              return super.isFileVisible(file, showHiddenFiles)
                  && (file.isDirectory() || (filter == null || filter.value(file)));

            }
          };

      return FileChooser.chooseFiles(descriptor, getComponentPanel(), context.getProject(), initialFile);
    }
  }

  private static enum DefaultSettingsFilter implements Condition<VirtualFile> {
    INSTANCE;

    @Override
    public boolean value(@NotNull final VirtualFile file) {
      return file.getName().endsWith(Names.settingsSuffix);
    }
  }
}
