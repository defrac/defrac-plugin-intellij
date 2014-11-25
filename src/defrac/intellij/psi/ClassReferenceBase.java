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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import defrac.intellij.DefracPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
abstract class ClassReferenceBase extends PsiReferenceBase<PsiLiteralExpression> implements PsiPolyVariantReference, DefracReference {
  @NotNull
  static final Object[] NO_VARIANTS = new Object[0];

  @NotNull
  protected final DefracPlatform platform;

  public ClassReferenceBase(@NotNull final PsiLiteralExpression element,
                            @NotNull final TextRange range,
                            @NotNull final DefracPlatform platform) {
    super(element, range, false);
    this.platform = platform;
  }

  @Override
  public final PsiElement bindToElement(@NotNull final PsiElement klass) throws IncorrectOperationException {
    // We always want the fully qualified class name here so we have
    // to implement bindToElement since it is the only method that
    // doesn't receive just a string that represents the new unqualified name
    if(!(klass instanceof PsiClass)) {
      throw new IncorrectOperationException();
    }

    final PsiLiteralExpression element = getElement();
    final ElementManipulator<PsiLiteralExpression> manipulator = ElementManipulators.getManipulator(getElement());

    return manipulator.
        handleContentChange(element, getRangeInElement(), ((PsiClass) klass).getQualifiedName());
  }

  @Nullable
  @Override
  public final PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve();
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @NotNull
  public final ResolveResult[] multiResolve() {
    return multiResolve(false);
  }

  @NotNull
  protected abstract GlobalSearchScope getSearchScope(@NotNull final Project project);

  @NotNull
  @Override
  public final ResolveResult[] multiResolve(final boolean incompleteCode) {
    final Project project = getElement().getProject();
    final GlobalSearchScope scope = getSearchScope(project);
    final PsiElement[] results = JavaPsiFacade.getInstance(project).findClasses(getValue(), scope);
    final ResolveResult[] resolveResult = new ResolveResult[results.length];

    for(int i = 0; i < results.length; i++) {
      final PsiElement result = results[i];
      resolveResult[i] = new PsiElementResolveResult(result);
    }

    return resolveResult;
  }

  @NotNull
  public final DefracPlatform getPlatform() {
    return platform;
  }

  @Override
  public final boolean isReferenceTo(@NotNull final PsiElement element) {
    final PsiManager psiManager = getElement().getManager();

    for(final ResolveResult result : multiResolve()) {
      if(psiManager.areElementsEquivalent(result.getElement(), element)) {
        return true;
      }
    }

    return false;
  }
}
