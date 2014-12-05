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
import com.intellij.openapi.util.Condition;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.MainClassReference;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public final class MainClassReferencesSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  @NotNull
  public static final Condition<PsiReference> REFERENCE_FILTER = new Condition<PsiReference>() {
    @Override
    public boolean value(final PsiReference reference) {
      return reference instanceof MainClassReference;
    }
  };

  public MainClassReferencesSearch() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull final ReferencesSearch.SearchParameters queryParameters,
                           @NotNull final Processor<PsiReference> consumer) {
    if(queryParameters.getEffectiveSearchScope() instanceof LocalSearchScope) {
      return;
    }

    final PsiElement elementToSearch = queryParameters.getElementToSearch();
    final DefracFacet facet = DefracFacet.getInstance(elementToSearch);

    if(elementToSearch instanceof PsiTypeParameter) {
      return;
    }

    if(facet == null) {
      return;
    }

    if(facet.isMacroLibrary() || !(elementToSearch instanceof PsiClass)) {
      return;
    }

    final String qualifiedName = checkNotNull(((PsiClass)elementToSearch).getQualifiedName(), "Qualified name of %s must not be null", elementToSearch);
    final GlobalSearchScope scope = GlobalSearchScopes.directoryScope(elementToSearch.getProject(), facet.getVirtualSettingsFile().getParent(), false);//GlobalSearchScope.fileScope(elementToSearch.getProject(), facet.getVirtualSettingsFile());
    final PsiSearchHelper helper = PsiSearchHelper.SERVICE.getInstance(elementToSearch.getProject());

    // processAllFilesWithWord
    helper.processAllFilesWithWord(qualifiedName, scope,
        new Processor<PsiFile>() {
          @Override
          public boolean process(final PsiFile psiFile) {
            System.out.println(psiFile);
            return true;
          }
        }, true);

    helper.processAllFilesWithWord(qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1), scope,
        new Processor<PsiFile>() {
          @Override
          public boolean process(final PsiFile psiFile) {
            System.out.println(psiFile);
            return true;
          }
        }, true);

    // processUsagesInNonJavaFiles
    helper.processUsagesInNonJavaFiles(qualifiedName, new PsiNonJavaFileReferenceProcessor() {
      @Override
      public boolean process(final PsiFile file, final int startOffset, final int endOffset) {
        System.out.println(file + " " + startOffset + " " + endOffset);
        return true;
      }
    }, scope);

    helper.processUsagesInNonJavaFiles(null, qualifiedName, new PsiNonJavaFileReferenceProcessor() {
      @Override
      public boolean process(final PsiFile file, final int startOffset, final int endOffset) {
        System.out.println(file + " " + startOffset + " " + endOffset);
        return true;
      }
    }, scope);

    // processElementsWithWord
    helper.processElementsWithWord(
        new TextOccurenceProcessor() {
          @Override
          public boolean execute(@NotNull final PsiElement element, final int offsetInElement) {
            System.out.println(element);
            System.out.println(offsetInElement);
            return true;
          }
        },
        scope,
        qualifiedName,
        UsageSearchContext.ANY,
        /*caseSensitive=*/true
    );

    helper.processElementsWithWord(
        new TextOccurenceProcessor() {
          @Override
          public boolean execute(@NotNull final PsiElement element, final int offsetInElement) {
            System.out.println(element);
            System.out.println(offsetInElement);
            return true;
          }
        },
        scope,
        qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1),
        UsageSearchContext.ANY,
        /*caseSensitive=*/true
    );
  }
}
