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

package defrac.intellij.fileType;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import defrac.intellij.DefracBundle;
import icons.DefracIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 */
public final class DefracSettingsFileType extends LanguageFileType {
  @NotNull private static final DefracSettingsFileType INSTANCE = new DefracSettingsFileType();
  @NotNull @NonNls public static final String EXTENSION = "settings";
  @NotNull private static final String NAME = DefracBundle.message("fileType.settings.name");
  @NotNull private static final String DESCRIPTION = DefracBundle.message("fileType.settings.description");

  @NotNull
  public static FileType getInstance() {
    return INSTANCE;
  }

  @NotNull
  public static FileNameMatcher[] fileNameMatchers() {
    return new FileNameMatcher[] {
        new ExtensionFileNameMatcher(EXTENSION)
    };
  }

  private DefracSettingsFileType() {
    super(JsonLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return NAME;
  }

  @NotNull
  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return EXTENSION;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return DefracIcons.Defrac16x16;
  }
}
