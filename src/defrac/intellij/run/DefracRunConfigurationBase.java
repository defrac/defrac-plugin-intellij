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

package defrac.intellij.run;

import com.intellij.execution.configurations.ModuleRunConfiguration;
import com.intellij.openapi.module.Module;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfigBase;
import defrac.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 */
public interface DefracRunConfigurationBase extends ModuleRunConfiguration, Cloneable {
  @Nullable
  String getMain();

  void setMain(final String qualifiedName);

  @Nullable
  Module getModule();

  void setModule(final Module module);

  @NotNull
  DefracPlatform getPlatform();

  boolean isDebug();

  void setDebug(final boolean value);

  void setGeneratedName();

  @NotNull
  DefracConfigBase getAdditionalSettings();
}
