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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.DefracDelegateClassReference;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public final class DefracDelegateClassReferencesSearch extends DefracReferencesSearch<PsiElement, ReferencesSearch.SearchParameters> {
  public DefracDelegateClassReferencesSearch() {}

  @NotNull
  @Override
  protected PsiElement getElement(@NotNull final ReferencesSearch.SearchParameters queryParameter) {
    return queryParameter.getElementToSearch();
  }

  @Override
  protected boolean isSearchCandidate(@NotNull final PsiElement candidate, @NotNull final DefracFacet facet) {
    return !facet.isMacroLibrary() && candidate instanceof PsiClass;
  }

  @NotNull
  @Override
  protected String getSearchString(@NotNull final PsiElement candidate, @NotNull final DefracFacet facet) {
    return checkNotNull(((PsiClass)candidate).getQualifiedName(), "Qualified name of %s must not be null", candidate);
  }

  @Override
  protected boolean isReferenceCandidate(@NotNull final PsiReference reference) {
    return reference instanceof DefracDelegateClassReference;
  }

  @NotNull
  protected SearchScope getSearchScope(@NotNull final ReferencesSearch.SearchParameters queryParameter,
                                       @NotNull final PsiElement element,
                                       @NotNull final DefracFacet thisFacet) {
    final SearchScope defaultScope = queryParameter.getEffectiveSearchScope();

    if(useProvidedSearchScope(defaultScope)) {
      return defaultScope;
    }

    // Build the search scope for the class
    // ====================================
    // (1) @Delegate class is only referenced in defrac projects
    // (2) @Delegate class is never referenced in macro projects
    // (3) @Delegate class is only referenced in generic or their platform

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

      scope = scope.uniteWith(GlobalSearchScope.moduleScope(module));
    }

    return scope;
  }
}
