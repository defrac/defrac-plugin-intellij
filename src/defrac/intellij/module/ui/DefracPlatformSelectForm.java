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

package defrac.intellij.module.ui;

import defrac.intellij.DefracPlatform;
import defrac.intellij.util.OS;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Set;

/**
 *
 */
public final class DefracPlatformSelectForm {
  private JPanel componentPanel;
  private JCheckBox androidCheckBox;
  private JLabel androidRequirementsLabel;
  private JCheckBox iosCheckBox;
  private JLabel iosRequirementsLabel;
  private JCheckBox javaCheckBox;
  private JCheckBox webCheckBox;

  public DefracPlatformSelectForm() {
    //TODO(joa): check global.settings for default targets
    //TODO(joa): check if xcode is installed
    //TODO(joa): check if android sdk is configured
    androidCheckBox.setSelected(true);
    androidRequirementsLabel.setVisible(false);

    if(OS.isMac()) {
      iosCheckBox.setSelected(true);
      iosRequirementsLabel.setVisible(false);
    } else {
      iosCheckBox.setEnabled(false);
      iosRequirementsLabel.setVisible(true);
    }

    javaCheckBox.setSelected(true);
    webCheckBox.setSelected(true);
  }

  @NotNull
  public Set<DefracPlatform> getSelectedPlatforms() {
    final Set<DefracPlatform> selectedPlatforms = Sets.newHashSet();

    if(androidCheckBox.isSelected()) {
      selectedPlatforms.add(DefracPlatform.ANDROID);
    }

    if(iosCheckBox.isSelected()) {
      selectedPlatforms.add(DefracPlatform.IOS);
    }

    if(javaCheckBox.isSelected()) {
      selectedPlatforms.add(DefracPlatform.JVM);
    }

    if(webCheckBox.isSelected()) {
      selectedPlatforms.add(DefracPlatform.WEB);
    }

    return selectedPlatforms;
  }

  public JPanel getComponentPanel() {
    return componentPanel;
  }
}
