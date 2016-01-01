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
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.projectRoot.JdkListConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.facet.DefracRootUtil;
import defrac.intellij.sdk.DefracSdkType;
import defrac.intellij.ui.DefracPlatformRenderer;
import defrac.intellij.ui.SdkRenderer;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
  private JCheckBox macroLibraryCheckBox;
  private JButton newSdkButton;
  private JComboBox defracSdkComboBox;

  @NotNull
  private final DefaultComboBoxModel defracSDKModel = new DefaultComboBoxModel();
  @NotNull
  private final DefaultComboBoxModel platformModel = new DefaultComboBoxModel();

  private final Condition<SdkTypeId> sdkCreationFilter = new Condition<SdkTypeId>() {
    @Override
    public boolean value(final SdkTypeId sdkTypeId) {
      return sdkTypeId == DefracSdkType.getInstance();
    }
  };

  public DefracFacetEditorForm(@NotNull final FacetEditorContext context,
                               @NotNull final DefracFacet facet,
                               @NotNull final ProjectSdksModel sdkModel) {
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

    defracSdkComboBox.setRenderer(new SdkRenderer());
    defracSdkComboBox.setModel(defracSDKModel);

    newSdkButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final DefaultActionGroup group = new DefaultActionGroup();

        sdkModel.createAddActions(group, defracSdkComboBox, new Consumer<Sdk>() {
          @Override
          public void consume(final Sdk sdk) {
            final JdkListConfigurable configurable = JdkListConfigurable.getInstance(context.getProject());
            configurable.addJdkNode(sdk, false);

            addDefracSdk(sdk);
            setSelectedSdk(sdk);
          }
        }, sdkCreationFilter);

        final DataContext dataContext = DataManager.getInstance().getDataContext(defracSdkComboBox);

        if(group.getChildrenCount() > 1) {
          JBPopupFactory.getInstance().createActionGroupPopup("Create New defrac SDK", group, dataContext, JBPopupFactory.ActionSelectionAid.MNEMONICS, false).showUnderneathOf(newSdkButton);
        } else {
          final AnActionEvent event = new AnActionEvent(null, dataContext, "unknown", new Presentation(""), ActionManager.getInstance(), 0);
          group.getChildren(event)[0].actionPerformed(event);
        }
      }
    });
  }

  public void init(@NotNull final List<Sdk> defracSdks, @Nullable final Sdk currentDefracSdk) {
    addPlatforms(DefracPlatform.values());
    setDefracSdks(defracSdks);
    setSelectedSdk(currentDefracSdk);
  }

  public void addDefracSdk(@NotNull final Sdk sdk) {
    if(!containsDefracSdk(sdk)) {
      defracSDKModel.addElement(sdk);
    }
  }

  public void removeDefracSdk(@NotNull final Sdk sdk) {
    final int index = defracSdkIndex(sdk);

    if(index != -1) {
      defracSdkComboBox.removeItemAt(index);
    }
  }

  private int defracSdkIndex(@NotNull final Sdk sdk) {
    for(int i = 0; i < defracSDKModel.getSize(); ++i) {
      if(sdk.getName().equals(((Sdk)defracSDKModel.getElementAt(i)).getName())) {
        return i;
      }
    }
    return -1;
  }

  public boolean containsDefracSdk(@NotNull final Sdk sdk) {
    return defracSdkIndex(sdk) != -1;
  }

  public void setDefracSdks(@NotNull final List<Sdk> sdks) {
    defracSDKModel.removeAllElements();

    for(final Sdk sdk : sdks) {
      defracSDKModel.addElement(sdk);
    }
  }

  public void setSelectedSdk(@Nullable final Sdk sdk) {
    if(sdk != null) {
      final int index = defracSdkIndex(sdk);

      if(index != -1) {
        defracSdkComboBox.setSelectedIndex(index);
      }
    }
  }

  @Nullable
  public Sdk getSelectedSdk() {
    return (Sdk) defracSdkComboBox.getSelectedItem();
  }

  public void addPlatform(@NotNull final DefracPlatform platform) {
    platformModel.addElement(platform);
  }

  public DefracPlatform getSelectedPlatform() {
    return (DefracPlatform) platformComboBox.getSelectedItem();
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

        if(p != null) {
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

  private enum DefaultSettingsFilter implements Condition<VirtualFile> {
    INSTANCE;

    @Override
    public boolean value(@NotNull final VirtualFile file) {
      return file.getName().endsWith(Names.settingsSuffix);
    }
  }
}
