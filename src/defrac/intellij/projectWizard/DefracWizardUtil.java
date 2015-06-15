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

package defrac.intellij.projectWizard;

import com.google.common.base.Splitter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import defrac.intellij.DefracBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 */
public final class DefracWizardUtil {
  @NotNull @NonNls public static final String DEFAULT_APP_NAME = "myapp";
  @NotNull @NonNls public static final String DEFAULT_MAIN_CLASS_NAME = "com.example.Main";
  @NotNull @NonNls public static final String DEFAULT_MAIN_SCREEN_CLASS_NAME = "com.example.MainScreen";

  static final class ApplicationSettingsController {
    boolean packageTextFieldChangedByUser;

    ApplicationSettingsController(@NotNull final JTextField applicationNameTextField,
                                  @NotNull final JTextField packageNameTextField,
                                  @Nullable final JTextField mainClassTextField,
                                  @NotNull final JLabel errorLabel,
                                  @Nullable final String mainClassName) {
      applicationNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
        @Override
        protected void textChanged(final DocumentEvent documentEvent) {
          if(!packageTextFieldChangedByUser) {
            final String appName = applicationNameTextField.getText().trim();
            if(appName.length() > 0) {
              final String defaultPackageName = getDefaultPackageNameByModuleName(appName);
              packageNameTextField.setText(defaultPackageName);
              if(mainClassTextField != null) {
                mainClassTextField.setText(defaultPackageName + '.' + mainClassName);
              }
            }
            packageTextFieldChangedByUser = false;
          }
        }
      });

      packageNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
        @Override
        protected void textChanged(final DocumentEvent documentEvent) {
          packageTextFieldChangedByUser = true;
          errorLabel.setText(validatePackageName(packageNameTextField.getText()));
        }
      });
    }
  }

  static final class PlatformsSettingsController {
    @NotNull
    private final JCheckBox[] checkBoxes;

    public PlatformsSettingsController(final JCheckBox webCheckBox,
                                       final JCheckBox iosCheckBox,
                                       final JCheckBox androidCheckBox,
                                       final JCheckBox jvmCheckBox,
                                       final JLabel errorLabel) {
      checkBoxes = new JCheckBox[]{webCheckBox, iosCheckBox, androidCheckBox, jvmCheckBox};

      register(webCheckBox, errorLabel);
      register(iosCheckBox, errorLabel);
      register(androidCheckBox, errorLabel);
      register(jvmCheckBox, errorLabel);
    }

    private void register(final JCheckBox checkBox, final JLabel errorLabel) {
      checkBox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          errorLabel.setText(validatePlatforms(checkBoxes));
        }
      });
    }
  }

  public static void initializeApplicationSettingsInput(@NotNull final JTextField applicationNameTextField,
                                                        @NotNull final JTextField packageNameTextField,
                                                        @Nullable final String name) {
    final String defaultAppName = name != null ? name : DEFAULT_APP_NAME;
    applicationNameTextField.setText(defaultAppName);
    applicationNameTextField.selectAll();

    packageNameTextField.setText(getDefaultPackageNameByModuleName(defaultAppName));
  }

  public static void handleApplicationSettingsInput(@NotNull final JTextField applicationNameTextField,
                                                    @NotNull final JTextField packageNameTextField,
                                                    @Nullable final JTextField mainClassTextField,
                                                    @NotNull final JLabel errorLabel,
                                                    @Nullable final String mainClassName) {
    new ApplicationSettingsController(applicationNameTextField, packageNameTextField, mainClassTextField, errorLabel, mainClassName);
  }

  public static void handlePlatformsSettingsInput(final JCheckBox webCheckBox,
                                                  final JCheckBox iosCheckBox,
                                                  final JCheckBox androidCheckBox,
                                                  final JCheckBox jvmCheckBox,
                                                  final JLabel errorLabel) {
    new PlatformsSettingsController(webCheckBox, iosCheckBox, androidCheckBox, jvmCheckBox, errorLabel);
  }

  public static void handleMainClassSettingsInput(final JTextField mainClassTextField,
                                                  final JLabel errorLabel) {
    mainClassTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final DocumentEvent documentEvent) {
        errorLabel.setText(validateMainClassName(mainClassTextField.getText()));
      }
    });
  }

  @NotNull
  public static String getDefaultPackageNameByModuleName(@NotNull final String moduleName) {
    return "com.example."+toIdentifier(moduleName);
  }

  @NotNull
  public static String validatePackageName(@NotNull final String value) {
    final String name = value.trim();

    if(name.isEmpty()) {
      return DefracBundle.message("projectWizard.error.noPackageName");
    }

    if(name.indexOf('.') == -1) {
      return DefracBundle.message("projectWizard.error.unqualifiedPackageName");
    }

    if(!isValidQualifiedName(name)) {
      return DefracBundle.message("projectWizard.error.invalidPackageName");
    }

    return "";
  }

  @NotNull
  public static String validateMainClassName(@NotNull final String value) {
    final String name = value.trim();

    if(name.isEmpty()) {
      return DefracBundle.message("projectWizard.error.noMainClass");
    }

    if(!isValidQualifiedName(name)) {
      return DefracBundle.message("projectWizard.error.invalidMainClass");
    }

    return "";
  }

  @NotNull
  public static String validatePlatforms(final JCheckBox... checkBoxes) {
    for(final JCheckBox checkBox : checkBoxes) {
      if(checkBox.isSelected()) {
        return "";
      }
    }

    return DefracBundle.message("projectWizard.error.noPlatform");
  }

  public static boolean isValidQualifiedName(@NotNull final String name) {
    for(final String s : Splitter.on('.').split(name)) {
      if(!StringUtil.isJavaIdentifier(s)) {
        return false;
      }
    }
    return true;
  }

  @NotNull
  public static String toIdentifier(@NotNull final String s) {
    final StringBuilder result = new StringBuilder();
    for(int i = 0, n = s.length(); i < n; i++) {
      final char c = s.charAt(i);

      if(Character.isJavaIdentifierPart(c)) {
        if(i == 0) {
          if(!Character.isJavaIdentifierStart(c)) {
            result.append('_');
          } else {
            result.append(Character.toLowerCase(c));
          }
        } else {
          result.append(c);
        }
      } else {
        result.append('_');
      }
    }
    return result.toString();
  }
}
