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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.MacroClassReference;
import defrac.intellij.psi.MacroMethodReference;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static defrac.intellij.psi.DefracPsiUtil.isMacro;

/**
 *
 */
public final class MacroClassReferencesSearch extends MacroReferencesSearchBase<PsiElement, ReferencesSearch.SearchParameters> {
  public MacroClassReferencesSearch() {}

  @NotNull
  @Override
  protected PsiElement getElement(@NotNull final ReferencesSearch.SearchParameters queryParameter) {
    return queryParameter.getElementToSearch();
  }

  @Override
  protected boolean isSearchCandidate(@NotNull final PsiElement candidate,
                                      @NotNull final DefracFacet facet) {
    if(!facet.isMacroLibrary()) {
      return false;
    }

    if(candidate instanceof PsiClass) {
      final PsiClass classToSearch = (PsiClass)candidate;
      return isMacro(classToSearch);
    } else if(candidate instanceof PsiMethod) {
      final PsiMethod methodToSearch = (PsiMethod)candidate;
      return isMacro(methodToSearch.getParent());
    } else {
      return false;
    }
  }

  @NotNull
  @Override
  protected String getSearchString(@NotNull final PsiElement candidate, @NotNull final DefracFacet facet) {
    if(candidate instanceof PsiClass) {
      final PsiClass classToSearch = (PsiClass)candidate;
      return checkNotNull(classToSearch.getQualifiedName(), "Qualified name of %s must nut be null", classToSearch);
    } else if(candidate instanceof PsiMethod) {
      final PsiMethod methodToSearch = (PsiMethod)candidate;
      return methodToSearch.getName();
    } else {
      throw new IllegalStateException();
    }
  }

  @Override
  protected boolean isReferenceCandidate(@NotNull final PsiReference reference) {
    return (reference instanceof MacroClassReference)
        || (reference instanceof MacroMethodReference);
  }

  @NotNull
  @Override
  protected SearchScope getSearchScope(@NotNull final ReferencesSearch.SearchParameters queryParameter,
                                       @NotNull final PsiElement element,
                                       @NotNull final DefracFacet facet) {
    final SearchScope defaultScope = queryParameter.getEffectiveSearchScope();

    if(useProvidedSearchScope(defaultScope)) {
      return defaultScope;
    }

    return super.getSearchScope(queryParameter, element, facet);
  }
}
