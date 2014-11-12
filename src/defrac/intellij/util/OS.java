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

package defrac.intellij.util;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class OS {
  @NotNull
  public static String name() {
    return System.getProperty("os.name", "<UNKNOWN>");
  }

  @NotNull
  public static String arch() {
    return System.getProperty("os.arch", "<UNKNOWN>");
  }

  @NotNull
  public static String version() {
    return System.getProperty("os.version", "<UNKNOWN>");
  }

  public static boolean isWindows() {
    return name().toLowerCase().contains("windows");
  }

  public static boolean isLinux() {
    return name().toLowerCase().contains("linux");
  }

  public static boolean isMac() {
    return name().toLowerCase().contains("mac");
  }

  public static boolean is64Bit() {
    return arch().toLowerCase().contains("64");
  }

  public static boolean is32Bit() {
    return !is64Bit();
  }

  private OS() {}
}
