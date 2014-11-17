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

package defrac.intellij.runner;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracConfigurationType extends ConfigurationTypeBase {
  @NotNull @NonNls public static final String ID = "DEFRAC";

  @NotNull public static final String DISPLAY_NAME = DefracBundle.message("defrac.config.name");
  @NotNull public static final String DESCRIPTION = DefracBundle.message("defrac.config.description");

  public DefracConfigurationType() {
    super(ID, DISPLAY_NAME, DESCRIPTION, DefracIcons.DEFRAC);
    addFactory(new DefracConfigurationFactory(this));
  }
}
