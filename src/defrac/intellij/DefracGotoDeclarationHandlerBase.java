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

package defrac.intellij;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.intellij.psi.PsiElement.EMPTY_ARRAY;

/**
 *
 */
abstract class DefracGotoDeclarationHandlerBase implements GotoDeclarationHandler {
  @NotNull private final String[] allowedQualifiedNames;
  private final boolean onlyInDefracModule;

  DefracGotoDeclarationHandlerBase(@NotNull final String[] allowedQualifiedNames,
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

    final String value = getValue(literal);

    if(isNullOrEmpty(value)) {
      return EMPTY_ARRAY;
    }

    final Project project = element.getProject();
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    final PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(value, scope);

    if(psiClass == null) {
      return EMPTY_ARRAY;
    }

    return new PsiElement[] { psiClass };
  }

  private boolean isDefracAnnotation(@Nullable final PsiAnnotation annotation) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName =
        annotation.getQualifiedName();

    if(qualifiedName == null) {
      return false;
    }

    for(final String allowedQualifiedName : allowedQualifiedNames) {
      if(allowedQualifiedName.equals(qualifiedName)) {
        return true;
      }
    }

    return false;
  }

  @Nullable
  private static String getValue(@NotNull final PsiLiteralExpression literalExp) {
    final Object value = literalExp.getValue();
    return value instanceof String ? (String)value : null;
  }
}
