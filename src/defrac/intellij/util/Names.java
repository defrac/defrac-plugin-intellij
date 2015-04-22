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
  @NotNull @NonNls public static final String defrac_annotation_Delegate = "defrac.annotation.Delegate";
  @NotNull @NonNls public static final String defrac_annotation_DelegateA5D = "defrac.annotation.DelegateA5D";
  @NotNull @NonNls public static final String defrac_annotation_DelegateIOS = "defrac.annotation.DelegateIOS";
  @NotNull @NonNls public static final String defrac_annotation_DelegateJVM = "defrac.annotation.DelegateJVM";
  @NotNull @NonNls public static final String defrac_annotation_DelegateWeb = "defrac.annotation.DelegateWeb";
  @NotNull @NonNls public static final String defrac_annotation_Macro = "defrac.annotation.Macro";
  @NotNull @NonNls public static final String defrac_annotation_MacroA5D = "defrac.annotation.MacroA5D";
  @NotNull @NonNls public static final String defrac_annotation_MacroIOS = "defrac.annotation.MacroIOS";
  @NotNull @NonNls public static final String defrac_annotation_MacroJVM = "defrac.annotation.MacroJVM";
  @NotNull @NonNls public static final String defrac_annotation_MacroWeb = "defrac.annotation.MacroWeb";
  @NotNull @NonNls public static final String defrac_annotation_UnsupportedA5D = "defrac.annotation.UnsupportedA5D";
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

  @NotNull public static final Set<String> ALL_DELEGATES = ImmutableSet.of(
      defrac_annotation_Delegate,
      defrac_annotation_DelegateA5D,
      defrac_annotation_DelegateIOS,
      defrac_annotation_DelegateJVM,
      defrac_annotation_DelegateWeb
  );

  @NotNull public static final Set<String> ALL_MACROS = ImmutableSet.of(
      defrac_annotation_Macro,
      defrac_annotation_MacroA5D,
      defrac_annotation_MacroIOS,
      defrac_annotation_MacroJVM,
      defrac_annotation_MacroWeb
  );

  @NotNull public static final Set<String> ALL_UNSUPPORTED = ImmutableSet.of(
      defrac_annotation_UnsupportedA5D,
      defrac_annotation_UnsupportedIOS,
      defrac_annotation_UnsupportedJVM,
      defrac_annotation_UnsupportedWeb
  );

  private Names() {}
}
