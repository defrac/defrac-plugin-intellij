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

package defrac.intellij.psi;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static defrac.intellij.psi.DefracPsiUtil.isDelegateAnnotation;

/**
 *
 */
public final class DelegateReferenceProvider extends PsiReferenceProvider {
  public DelegateReferenceProvider() {}

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element,
                                               @Nullable final ProcessingContext context) {
    final DefracFacet facet = DefracFacet.getInstance(element);

    if(facet == null || facet.isMacroLibrary()) {
      return PsiReference.EMPTY_ARRAY;
    }

    final PsiFile file = element.getContainingFile();

    if(file == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    // (1) get literal
    final String value = getValue((PsiLiteralExpression)element);

    if(isNullOrEmpty(value)) {
      return PsiReference.EMPTY_ARRAY;
    }

    // (2) get delegate annotation
    final PsiAnnotation annotation =
        getParentOfType(element, PsiAnnotation.class, /*strict=*/false);

    if(!isDelegateAnnotation(annotation)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final DefracPlatform targetPlatform =
        DefracPlatform.byDelegateAnnotation(checkNotNull(annotation.getQualifiedName()));

    return new PsiReference[] {
        new DelegateClassReference(
            value,
            (PsiLiteralExpression)element,
            targetPlatform)
    };
  }

  @Override
  public boolean acceptsTarget(@NotNull final PsiElement target) {
    return target instanceof PsiLiteralExpression;
  }

  @Nullable
  private static String getValue(@NotNull final PsiLiteralExpression literalExp) {
    final Object value = literalExp.getValue();
    return value instanceof String ? (String)value : null;
  }
}
