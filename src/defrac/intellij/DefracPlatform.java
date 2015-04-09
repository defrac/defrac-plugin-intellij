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
import com.intellij.openapi.util.SystemInfo;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  public static final Map<String, DefracPlatform> MACRO_ANNOTATION_TO_PLATFORM = ImmutableMap.of(
      Names.defrac_annotation_Macro   , GENERIC,
      Names.defrac_annotation_MacroA5D, ANDROID,
      Names.defrac_annotation_MacroIOS, IOS,
      Names.defrac_annotation_MacroJVM, JVM,
      Names.defrac_annotation_MacroWeb, WEB
  );

  @NotNull
  public static final Map<DefracPlatform, String> PLATFORM_TO_MACRO_ANNOTATION = ImmutableMap.of(
      GENERIC, Names.defrac_annotation_Macro,
      ANDROID, Names.defrac_annotation_MacroA5D,
      IOS    , Names.defrac_annotation_MacroIOS,
      JVM    , Names.defrac_annotation_MacroJVM,
      WEB    , Names.defrac_annotation_MacroWeb
  );

  @NotNull
  public static final Map<String, DefracPlatform> UNSUPPORTED_ANNOTATION_TO_PLATFORM = ImmutableMap.of(
      Names.defrac_annotation_UnsupportedA5D, ANDROID,
      Names.defrac_annotation_UnsupportedIOS, IOS,
      Names.defrac_annotation_UnsupportedJVM, JVM,
      Names.defrac_annotation_UnsupportedWeb, WEB
  );

  @NotNull
  public static final Map<DefracPlatform, String> PLATFORM_TO_UNSUPPORTED_ANNOTATION = ImmutableMap.of(
      ANDROID, Names.defrac_annotation_UnsupportedA5D,
      IOS    , Names.defrac_annotation_UnsupportedIOS,
      JVM    , Names.defrac_annotation_UnsupportedJVM,
      WEB    , Names.defrac_annotation_UnsupportedWeb
  );

  @NotNull
  public static final Map<String, DefracPlatform> DELEGATE_ANNOTATION_TO_PLATFORM = ImmutableMap.of(
      Names.defrac_annotation_Delegate   , GENERIC,
      Names.defrac_annotation_DelegateA5D, ANDROID,
      Names.defrac_annotation_DelegateIOS, IOS,
      Names.defrac_annotation_DelegateJVM, JVM,
      Names.defrac_annotation_DelegateWeb, WEB
  );

  @NotNull
  public static final Map<DefracPlatform, String> PLATFORM_TO_DELEGATE_ANNOTATION = ImmutableMap.of(
      GENERIC, Names.defrac_annotation_Delegate,
      ANDROID, Names.defrac_annotation_DelegateA5D,
      IOS    , Names.defrac_annotation_DelegateIOS,
      JVM    , Names.defrac_annotation_DelegateJVM,
      WEB    , Names.defrac_annotation_DelegateWeb
  );

  @NotNull
  private static final Map<String, DefracPlatform> NAME_TO_PLATFORM = ImmutableMap.of(
      GENERIC.name, GENERIC,
      ANDROID.name, ANDROID,
      IOS.name    , IOS,
      JVM.name    , JVM,
      WEB.name    , WEB
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

  @Nullable
  public static DefracPlatform byName(@NotNull final String value) {
    return NAME_TO_PLATFORM.get(value);
  }

  @NotNull
  public static DefracPlatform byDelegateAnnotation(@NotNull final String qualifiedName) {
    return checkNotNull(DELEGATE_ANNOTATION_TO_PLATFORM.get(qualifiedName));
  }

  @NotNull
  public static DefracPlatform byMacroAnnotation(@NotNull final String qualifiedName) {
    return checkNotNull(MACRO_ANNOTATION_TO_PLATFORM.get(qualifiedName));
  }

  public boolean isAvailableOnHostOS() {
    return this != IOS || SystemInfo.isMac;
  }
}
