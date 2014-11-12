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
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 *
 */
public final class DefracNewSdkDialog extends DialogWrapper {
  private JPanel contentPanel;
  private JComboBox internalJdkComboBox;
  private JComboBox defracVersionComboBox;

  public DefracNewSdkDialog(@Nullable final Project project,
                            @NotNull final List<String> javaSdkNames,
                            @NotNull final String selectedJavaSdk,
                            @NotNull final List<String> defracVersionNames,
                            @NotNull final String selectedDefracVersion) {
    super(project);

    setTitle("Create New Defrac SDK");

    internalJdkComboBox.setModel(new CollectionComboBoxModel(javaSdkNames, selectedJavaSdk));
    defracVersionComboBox.setModel(new CollectionComboBoxModel(defracVersionNames, selectedDefracVersion));

    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return contentPanel;
  }

  public int getSelectedJavaSdkIndex() {
    return internalJdkComboBox.getSelectedIndex();
  }

  public int getSelectedDefracVersionIndex() {
    return defracVersionComboBox.getSelectedIndex();
  }
}
