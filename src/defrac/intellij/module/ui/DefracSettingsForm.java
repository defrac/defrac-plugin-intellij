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

import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 */
public final class DefracSettingsForm {
  private JPanel componentPanel;
  private JTextField packageField;
  private JTextField identifierField;
  private JTextField versionField;

  public DefracSettingsForm() {}

  public String getPackage() {
    return packageField.getText();
  }

  public String getIdentifier() {
    return identifierField.getText();
  }

  public String getVersion() {
    return versionField.getText();
  }

  public JComponent getComponentPanel() {
    return componentPanel;
  }

  public void updateFields(@Nullable final String name) {
    final String actualName = Strings.isNullOrEmpty(name) ? "Untitled Project" : name;
    final String identifier = generatedIdentifierFromName(actualName);

    //nameField.setText(actualName);
    packageField.setText("com."+identifier);
    identifierField.setText(identifier);
    versionField.setText("1.0");
  }

  @NotNull
  private static String generatedIdentifierFromName(@NotNull final String value) {
    // 1) replace all invalid chars with '_'
    // 2) replace consecutive '_'+ with '_'
    // 3) replace surrounding '_' with ''

    final StringBuilder string1Builder = new StringBuilder(value.length());
    boolean isStart = true;

    for(final char c : value.toCharArray()) {
      final boolean valid;

      if(isStart) {
        valid = Character.isJavaIdentifierStart(c);
        isStart = false;
      } else {
        valid = Character.isJavaIdentifierPart(c);
      }

      string1Builder.append(valid ? c : '_');
    }

    final String string1 = string1Builder.toString();
    final String string2 = string1.replaceAll("/_{2,}/", "_");
    final String string3 = defrac.intellij.util.Strings.trim(string2, '_');
    final int n = string3.length();

    final StringBuilder result = new StringBuilder(n);
    boolean convertNextCharToUpperCase = false;

    for(int i = 0; i < n; ++i) {
      final char c = string3.charAt(i);

      if(c == '_') {
        convertNextCharToUpperCase = true;
      } else {
        if(convertNextCharToUpperCase) {
          result.append(Character.toUpperCase(c));
          convertNextCharToUpperCase = false;
        } else {
          result.append(c);
        }
      }
    }

    return result.toString();
  }
}
