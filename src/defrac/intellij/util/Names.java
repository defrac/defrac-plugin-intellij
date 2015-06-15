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

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 *
 */
public final class Names {
  @NotNull @NonNls public static final String defrac_annotation_Inject = "defrac.annotation.Inject";
  @NotNull @NonNls public static final String defrac_annotation_InjectAndroid = "defrac.annotation.InjectAndroid";
  @NotNull @NonNls public static final String defrac_annotation_InjectIOS = "defrac.annotation.InjectIOS";
  @NotNull @NonNls public static final String defrac_annotation_InjectJVM = "defrac.annotation.InjectJVM";
  @NotNull @NonNls public static final String defrac_annotation_InjectWeb = "defrac.annotation.InjectWeb";
  @NotNull @NonNls public static final String defrac_annotation_Injector = "defrac.annotation.Injector";
  @NotNull @NonNls public static final String defrac_annotation_Macro = "defrac.annotation.Macro";
  @NotNull @NonNls public static final String defrac_annotation_MacroAndroid = "defrac.annotation.MacroAndroid";
  @NotNull @NonNls public static final String defrac_annotation_MacroIOS = "defrac.annotation.MacroIOS";
  @NotNull @NonNls public static final String defrac_annotation_MacroJVM = "defrac.annotation.MacroJVM";
  @NotNull @NonNls public static final String defrac_annotation_MacroWeb = "defrac.annotation.MacroWeb";
  @NotNull @NonNls public static final String defrac_annotation_UnsupportedAndroid = "defrac.annotation.UnsupportedAndroid";
  @NotNull @NonNls public static final String defrac_annotation_UnsupportedIOS = "defrac.annotation.UnsupportedIOS";
  @NotNull @NonNls public static final String defrac_annotation_UnsupportedJVM = "defrac.annotation.UnsupportedJVM";
  @NotNull @NonNls public static final String defrac_annotation_UnsupportedWeb = "defrac.annotation.UnsupportedWeb";


  @NotNull @NonNls public static final String defrac_compiler_macro_Macro = "defrac.compiler.macro.Macro";
  @NotNull @NonNls public static final String defrac_compiler_macro_Parameter = "defrac.compiler.macro.Parameter";
  @NotNull @NonNls public static final String defrac_compiler_macro_MethodBody = "defrac.compiler.macro.MethodBody";

  @NotNull @NonNls public static final String settingsSuffix = ".settings";
  @NotNull @NonNls public static final String default_settings = "default"+settingsSuffix;

  @NotNull @NonNls public static final String defrac_dni_ReadOnly = "defrac.dni.ReadOnly";
  @NotNull @NonNls public static final String defrac_dni_WriteOnly = "defrac.dni.WriteOnly";

  @NotNull @NonNls public static final String defrac_dni_Intrinsic = "defrac.dni.Intrinsic";
  @NotNull @NonNls public static final String defrac_dni_RepresentedBy = "defrac.dni.RepresentedBy";

  @NotNull public static final Set<String> ALL_INJECTS = ImmutableSet.of(
      defrac_annotation_Inject,
      defrac_annotation_InjectAndroid,
      defrac_annotation_InjectIOS,
      defrac_annotation_InjectJVM,
      defrac_annotation_InjectWeb
  );

  @NotNull public static final Set<String> ALL_MACROS = ImmutableSet.of(
      defrac_annotation_Macro,
      defrac_annotation_MacroAndroid,
      defrac_annotation_MacroIOS,
      defrac_annotation_MacroJVM,
      defrac_annotation_MacroWeb
  );

  @NotNull public static final Set<String> ALL_UNSUPPORTED = ImmutableSet.of(
      defrac_annotation_UnsupportedAndroid,
      defrac_annotation_UnsupportedIOS,
      defrac_annotation_UnsupportedJVM,
      defrac_annotation_UnsupportedWeb
  );

  private Names() {}
}
