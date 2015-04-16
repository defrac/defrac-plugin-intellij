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

package defrac.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import icons.DefracIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 */
public final class DefracFacetType extends FacetType<DefracFacet, DefracFacetConfiguration> {
  @NotNull @NonNls public static final String TYPE_ID = "DEFRAC";

  public DefracFacetType() {
    super(DefracFacet.ID, TYPE_ID, "defrac");
  }

  @Override
  public Icon getIcon() {
    return DefracIcons.Defrac16x16;
  }

  @Override
  public DefracFacetConfiguration createDefaultConfiguration() {
    return new DefracFacetConfiguration();
  }

  @Override
  public DefracFacet createFacet(@NotNull final Module module,
                                 final String name,
                                 @NotNull final DefracFacetConfiguration configuration,
                                 final Facet underlyingFacet) {
    // Note from the ADT plugin:
    //
    // DO NOT COMMIT MODULE-ROOT MODELS HERE!
    // modules are not initialized yet, so some data may be lost

    return new DefracFacet(module, name, configuration);
  }

  @Override
  public boolean isSuitableModuleType(@Nullable final ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }
}
