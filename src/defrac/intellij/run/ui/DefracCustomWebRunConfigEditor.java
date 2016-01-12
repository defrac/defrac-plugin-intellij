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

package defrac.intellij.run.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import defrac.intellij.run.DefracWebRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 */
public final class DefracCustomWebRunConfigEditor extends SettingsEditor<DefracWebRunConfiguration> {
  private JRadioButton minifyRadioButton;
  private JRadioButton strictModeRadioButton;
  private JPanel panel;

  @Override
  protected void resetEditorFrom(final DefracWebRunConfiguration runConfiguration) {
    strictModeRadioButton.setSelected(runConfiguration.isStrict());
    minifyRadioButton.setSelected(runConfiguration.minify());
  }

  @Override
  protected void applyEditorTo(final DefracWebRunConfiguration runConfiguration) throws ConfigurationException {
    runConfiguration.isStrict(strictModeRadioButton.isSelected());
    runConfiguration.minify(minifyRadioButton.isSelected());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return panel;
  }
}
