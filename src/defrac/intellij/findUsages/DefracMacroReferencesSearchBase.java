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

package defrac.intellij.findUsages;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
abstract class DefracMacroReferencesSearchBase<E extends PsiElement, T> extends DefracReferencesSearchBase<E, T> {
  DefracMacroReferencesSearchBase() {}

  @NotNull
  @Override
  protected SearchScope getSearchScope(@NotNull final T queryParameter,
                                       @NotNull final E element,
                                       @NotNull final DefracFacet thisFacet) {
    // Build the search scope for the class
    // ====================================
    // (1) @Macro class is only referenced in defrac projects
    // (2) @Macro class is never referenced in macro projects
    // (3) @Macro class is only referenced in generic or their platform
    // (4) @Macro class is never referenced in its own module

    final Module[] modules = ModuleManager.getInstance(element.getProject()).getModules();
    GlobalSearchScope scope = GlobalSearchScope.EMPTY_SCOPE;

    for(final Module module : modules) {
      final DefracFacet thatFacet = DefracFacet.getInstance(module);

      if(thatFacet == null) {
        continue; // (1)
      }

      if(thatFacet.isMacroLibrary()) {
        continue; // (2)
      }

      if(    !thisFacet.getPlatform().isGeneric()
          && !thatFacet.getPlatform().isGeneric()
          && thisFacet.getPlatform() != thatFacet.getPlatform()) {
        continue; // (3)
      }

      if(thatFacet == thisFacet) {
        continue; // (4)
      }

      scope = scope.uniteWith(GlobalSearchScope.moduleScope(module));
    }

    return scope;
  }
}
