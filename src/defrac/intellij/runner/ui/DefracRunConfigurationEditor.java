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

package defrac.intellij.runner.ui;

import defrac.intellij.runner.DefracRunConfiguration;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static defrac.intellij.DefracPlatform.*;

/**
 *
 */
public final class DefracRunConfigurationEditor extends SettingsEditor<DefracRunConfiguration> {
  @NotNull public static final String[] DISPLAY_NAMES = {
      ANDROID.displayName,
      IOS.displayName,
      JVM.displayName,
      WEB.displayName
  };

  @NotNull
  private final Project project;

  public DefracRunConfigurationEditor(@NotNull final Project project) {
    this.project = project;
  }

  @Override
  protected void resetEditorFrom(DefracRunConfiguration configuration) {
    //TODO(joa): implement me
  }

  @Override
  protected void applyEditorTo(DefracRunConfiguration configuration) throws ConfigurationException {
    //TODO(joa): implement me
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    final ComboBox platform = new ComboBox(DISPLAY_NAMES);

    return FormBuilder.
        createFormBuilder().
        addLabeledComponent("Platform", platform).
        getPanel();
  }
}
