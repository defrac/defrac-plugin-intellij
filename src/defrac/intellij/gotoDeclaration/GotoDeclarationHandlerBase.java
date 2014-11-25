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

package defrac.intellij.gotoDeclaration;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;

import static com.intellij.psi.PsiElement.EMPTY_ARRAY;

/**
 *
 */
abstract class GotoDeclarationHandlerBase implements GotoDeclarationHandler {
  @NotNull
  private final Set<String> allowedQualifiedNames;

  private final boolean onlyInDefracModule;

  GotoDeclarationHandlerBase(@NotNull final Set<String> allowedQualifiedNames,
                             final boolean onlyInDefracModule) {
    this.allowedQualifiedNames = allowedQualifiedNames;
    this.onlyInDefracModule = onlyInDefracModule;
  }

  @Nullable
  @Override
  public String getActionText(final DataContext context) {
    return null;
  }

  @Nullable
  @Override
  public PsiElement[] getGotoDeclarationTargets(@Nullable final PsiElement element,
                                                final int offset,
                                                final Editor editor) {
    if(element == null) {
      return EMPTY_ARRAY;
    }

    if(onlyInDefracModule && DefracFacet.getInstance(element) == null) {
      return EMPTY_ARRAY;
    }

    final PsiLiteralExpression literal =
        PsiTreeUtil.getParentOfType(element, PsiLiteralExpression.class, /*strict=*/false);

    if(literal == null) {
      return EMPTY_ARRAY;
    }

    final PsiAnnotation annotation =
        PsiTreeUtil.getParentOfType(literal, PsiAnnotation.class, /*strict=*/false);

    if(!isDefracAnnotation(annotation)) {
      return EMPTY_ARRAY;
    }

    final PsiReference[] references = literal.getReferences();
    final ArrayList<PsiPolyVariantReference> defracReferences =
        Lists.newArrayListWithExpectedSize(references.length);

    for(final PsiReference reference : references) {
      if(isDefracReference(reference)) {
        if(!reference.getRangeInElement().contains(offset)) {
          continue;
        }

        defracReferences.add((PsiPolyVariantReference)reference);
      }
    }

    final ArrayList<PsiElement> result = Lists.newArrayListWithExpectedSize(defracReferences.size());

    for(final PsiPolyVariantReference reference : defracReferences) {
      final ResolveResult[] resolveResults = reference.multiResolve(false);

      for(final PsiElement resolveResult : PsiUtil.mapElements(resolveResults)) {
        if(resolveResult == null) {
          continue;
        }

        result.add(resolveResult);
      }
    }

    return result.toArray(new PsiElement[result.size()]);
  }

  private boolean isDefracAnnotation(@Nullable final PsiAnnotation annotation) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName =
        annotation.getQualifiedName();

    return allowedQualifiedNames.contains(qualifiedName);
  }

  protected abstract boolean isDefracReference(@NotNull final PsiReference reference);
}
