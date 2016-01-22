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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionComboBoxModel;
import defrac.intellij.sdk.DefracVersion;
import defrac.intellij.ui.DefracVersionRenderer;
import defrac.intellij.ui.SdkRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 *
 */
public final class NewDefracSdkDialog extends DialogWrapper {
  private JPanel contentPanel;
  private JComboBox jdkComboBox;
  private JComboBox defracVersionComboBox;
  private JLabel jdkLabel;
  private JLabel defracVersionLabel;

  public NewDefracSdkDialog(@Nullable final Project project,
                            @NotNull final List<Sdk> jdks,
                            @NotNull final Sdk selectedJdk,
                            @NotNull final List<DefracVersion> defracVersions,
                            @NotNull final DefracVersion selectedDefracVersion) {
    super(project);

    setTitle("Create New Defrac SDK");

    jdkLabel.setLabelFor(jdkComboBox);
    defracVersionLabel.setLabelFor(defracVersionComboBox);

    //noinspection unchecked
    jdkComboBox.setModel(new CollectionComboBoxModel(jdks, selectedJdk));
    jdkComboBox.setRenderer(new SdkRenderer());

    //noinspection unchecked
    defracVersionComboBox.setModel(new CollectionComboBoxModel(defracVersions, selectedDefracVersion));
    defracVersionComboBox.setRenderer(new DefracVersionRenderer());

    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return contentPanel;
  }

  @NotNull
  public Sdk getSelectedJdk() {
    return (Sdk) jdkComboBox.getSelectedItem();
  }

  @NotNull
  public DefracVersion getSelectedDefracVersion() {
    return (DefracVersion) defracVersionComboBox.getSelectedItem();
  }
}
