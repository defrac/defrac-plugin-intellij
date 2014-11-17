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

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import defrac.intellij.psi.DefracPsiReference;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracReferencesSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  public DefracReferencesSearch() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull final ReferencesSearch.SearchParameters queryParameters,
                           @NotNull final Processor<PsiReference> consumer) {
    final PsiElement element = queryParameters.getElementToSearch();

    if(!(element instanceof PsiClass)) {
      return;
    }

    //TODO(joa): split this for @Macro and @Delegate, continue with @Macro only if extends defrac.macro.Macro

    final PsiClass classToSearch = (PsiClass)element;
    final SearchScope scope = queryParameters.getEffectiveSearchScope();
    final PsiSearchHelper helper = PsiSearchHelper.SERVICE.getInstance(element.getProject());
    final TextOccurenceProcessor processor = new TextOccurenceProcessor() {
      @Override
      public boolean execute(@NotNull final PsiElement element, final int offsetInElement) {
        final PsiLiteralExpression literalExpression = PsiTreeUtil.getParentOfType(element, PsiLiteralExpression.class, false);

        if(literalExpression == null) {
          return true;
        }

        for(final PsiReference ref : literalExpression.getReferences()) {
          if(!(ref instanceof DefracPsiReference)) {
            continue;
          }

          if(!ref.getRangeInElement().contains(offsetInElement)) {
            continue;
          }

          if(ref.isReferenceTo(classToSearch)) {
            return consumer.process(ref);
          }
        }

        return true;
      }
    };

    helper.processElementsWithWord(processor, scope, classToSearch.getName(), UsageSearchContext.IN_STRINGS, true);
  }
}
