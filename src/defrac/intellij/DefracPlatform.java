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

package defrac.intellij;

import com.google.common.collect.ImmutableMap;
import defrac.intellij.util.OS;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public enum DefracPlatform {
  GENERIC("generic", "Generic", ""),
  ANDROID("android", "Android", "a5d"),
  IOS("ios", "iOS", "ios"),
  JVM("jvm", "JVM", "jvm"),
  WEB("web", "Web", "web");

  @NotNull
  private static final Map<String, DefracPlatform> NAME_TO_PLATFORM = ImmutableMap.of(
      GENERIC.name, GENERIC,
      ANDROID.name, ANDROID,
      IOS.name, IOS,
      JVM.name, JVM,
      WEB.name, WEB
  );

  @NotNull public final String name;
  @NotNull public final String displayName;
  @NotNull public final String abbreviation;

  private DefracPlatform(@NotNull final String name,
                         @NotNull final String displayName,
                         @NotNull final String abbreviation) {
    this.name = name;
    this.displayName = displayName;
    this.abbreviation = abbreviation;
  }

  public boolean isGeneric() {
    return this == GENERIC;
  }

  @NotNull
  public static DefracPlatform byName(@NotNull final String value) {
    return checkNotNull(NAME_TO_PLATFORM.get(value));
  }

  public boolean isAvailableOnHostOS() {
    return this != IOS || OS.isMac();
  }
}
