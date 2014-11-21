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

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
abstract class DefracReferencesSearch<E extends PsiElement, T> extends QueryExecutorBase<PsiReference, T> {
  public static boolean useProvidedSearchScope(@NotNull final SearchScope scope) {
    return scope instanceof LocalSearchScope;
  }

  DefracReferencesSearch() {
    super(true);
  }

  @NotNull
  protected abstract E getElement(@NotNull final T queryParameter);

  protected abstract boolean isSearchCandidate(@NotNull final E candidate,
                                               @NotNull final DefracFacet facet);

  @NotNull
  protected abstract String getSearchString(@NotNull final E candidate,
                                            @NotNull final DefracFacet facet);

  @NotNull
  protected abstract SearchScope getSearchScope(@NotNull final T queryParameter,
                                                @NotNull final E element,
                                                @NotNull final DefracFacet thisFacet);

  protected abstract boolean isReferenceCandidate(@NotNull final PsiReference reference);

  @Override
  public final void processQuery(@NotNull final T queryParameter,
                                 @NotNull final Processor<PsiReference> consumer) {
    final E elementToSearch= getElement(queryParameter);
    final DefracFacet facet = DefracFacet.getInstance(elementToSearch);

    if(facet == null) {
      return;
    }

    if(!isSearchCandidate(elementToSearch, facet)) {
      return;
    }

    final SearchScope scope = getSearchScope(queryParameter, elementToSearch, facet);
    final PsiSearchHelper helper = PsiSearchHelper.SERVICE.getInstance(elementToSearch.getProject());
    final TextOccurenceProcessor processor = new DefracTextOccurenceProcessor(elementToSearch, consumer);

    helper.processElementsWithWord(
        processor,
        scope,
        getSearchString(elementToSearch, facet),
        UsageSearchContext.IN_STRINGS,
        /*caseSensitive=*/true
    );
  }

  private class DefracTextOccurenceProcessor implements TextOccurenceProcessor {
    private final E elementToSearch;
    private final Processor<PsiReference> consumer;

    public DefracTextOccurenceProcessor(final E elementToSearch, final Processor<PsiReference> consumer) {
      this.elementToSearch = elementToSearch;
      this.consumer = consumer;
    }

    @Override
    public boolean execute(@NotNull final PsiElement element, final int offsetInElement) {
      final PsiLiteralExpression literalExpression =
          PsiTreeUtil.getParentOfType(element, PsiLiteralExpression.class, false);

      if(literalExpression == null) {
        return true;
      }

      final PsiReference[] references = literalExpression.getReferences();

      for(final PsiReference reference : references) {
        if(!isReferenceCandidate(reference)) {
          continue;
        }

        if(!reference.getRangeInElement().contains(offsetInElement)) {
          continue;
        }

        if(reference.isReferenceTo(elementToSearch)) {
          return consumer.process(reference);
        }
      }

      return true;
    }
  }
}
