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

package defrac.intellij.ipc;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracCommands {
  @NotNull @NonNls public static final String COMPILE = "compile";
  @NotNull @NonNls public static final String RUN = "run";
  @NotNull @NonNls public static final String DEBUG = "debug";
  @NotNull @NonNls public static final String OPEN = "open";
  @NotNull @NonNls public static final String CLOSE = "close";
  @NotNull @NonNls public static final String COMPILE_RESULT = "\\[info\\] Compiled (\\d+) units? in (\\d+(s|ms)) \\((\\d+) errors?, (\\d+) warnings?\\)";

  @NotNull @NonNls public static final String PACKAGE = "package";
  @NotNull @NonNls public static final String GEN_MACROS = "gen-macros";

  private DefracCommands() {}
}
