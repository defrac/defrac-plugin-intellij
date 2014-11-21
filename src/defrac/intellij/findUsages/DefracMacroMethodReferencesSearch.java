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

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.DefracMacroMethodReference;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracMacroMethodReferencesSearch extends DefracMacroReferencesSearch<PsiMethod, MethodReferencesSearch.SearchParameters> {
  public DefracMacroMethodReferencesSearch() {}

  @NotNull
  @Override
  protected PsiMethod getElement(@NotNull final MethodReferencesSearch.SearchParameters queryParameter) {
    return queryParameter.getMethod();
  }

  @Override
  protected boolean isSearchCandidate(@NotNull final PsiMethod candidate,
                                      @NotNull final DefracFacet facet) {
    return facet.isMacroLibrary() && DefracPsiUtil.isMacro(candidate.getParent());
  }

  @Override
  protected boolean isReferenceCandidate(@NotNull final PsiReference reference) {
    return reference instanceof DefracMacroMethodReference;
  }

  @NotNull
  @Override
  protected String getSearchString(@NotNull final PsiMethod candidate,
                                   @NotNull final DefracFacet facet) {
    return '#'+candidate.getName();
  }

  @NotNull
  @Override
  protected SearchScope getSearchScope(@NotNull final MethodReferencesSearch.SearchParameters queryParameter,
                                       @NotNull final PsiMethod element,
                                       @NotNull final DefracFacet facet) {
    final SearchScope defaultScope = queryParameter.getEffectiveSearchScope();

    if(useProvidedSearchScope(defaultScope)) {
      return defaultScope;
    }

    return super.getSearchScope(queryParameter, element, facet);
  }
}
