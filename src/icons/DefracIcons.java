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

package icons;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 */
public final class DefracIcons {
  // don't move this class .. intellij has some hard-coded logic
  // which requires an icon specified in plugins.xml to exist in
  // a class that ends in "Icon" and is part of the "icons" package.

  @NotNull
  private static Icon load(@NotNull final String path) {
    return IconLoader.getIcon(path, DefracIcons.class);
  }

  @NotNull public static final Icon Defrac16x16 = load("/icons/defrac16x16.png");

  @NotNull public static final Icon Defrac13x13 = load("/icons/defrac13x13.png");

  @NotNull public static final Icon ToolWindow = Defrac13x13;

  @NotNull public static final Icon AndroidModule = load("/icons/module.android.png");

  @NotNull public static final Icon NewDelegate = Defrac13x13;

  @NotNull public static final Icon NewMacro = Defrac13x13;

  @NotNull public static final Icon SwitchToXCode = Defrac13x13;

  private DefracIcons() {}
}
