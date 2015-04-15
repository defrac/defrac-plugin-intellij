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

package defrac.intellij.run.web;

import com.intellij.openapi.util.SystemInfo;
import defrac.intellij.config.DefracConfigOracle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracBrowserUtil {
  @NotNull
  private static final String[] CHROME_WINDOWS = {
      "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
      "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
  };

  @NotNull
  private static final String[] CHROME_MAC = {
      "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
      "/Applications/Chromium.app/Contents/MacOS/Chromium"
  };

  @NotNull
  private static final String[] CHROME_LINUX = {
      "/usr/bin/google-chrome",
      "/usr/bin/chromium-browser",
  };

  @Contract(value="null -> null", pure=true)
  @Nullable
  public static File getBrowser(@Nullable final DefracConfigOracle config) {
    if(config == null) {
      return null;
    }

    final String value = config.getBrowser();

    if(!isNullOrEmpty(value)) {
      final File file = new File(value);

      if(file.canExecute()) {
        return file;
      }
    }

    final String[] browserDefaults;

    if(SystemInfo.isWindows) {
      browserDefaults = CHROME_WINDOWS;
    } else if(SystemInfo.isMac) {
      browserDefaults = CHROME_MAC;
    } else if(SystemInfo.isLinux) {
      browserDefaults = CHROME_LINUX;
    } else {
      return null;
    }

    for(final String executable : browserDefaults) {
      final File file = new File(executable);

      if(file.canExecute()) {
        return file;
      }
    }

    return null;
  }

  @Contract(value="null -> false", pure=true)
  public static boolean isChromium(@Nullable final File file) {
    if(file == null) {
      return false;
    }

    final String name = file.getName().toLowerCase();
    return name.contains("chrome") || name.contains("chromium");
  }

  private DefracBrowserUtil() {}
}
