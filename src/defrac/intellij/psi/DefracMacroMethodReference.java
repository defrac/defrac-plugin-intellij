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

import com.google.common.collect.Lists;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public final class DefracMacroMethodReference extends PsiReferenceBase<PsiLiteralExpression> implements PsiPolyVariantReference {
  @NotNull
  private final DefracMacroClassReference parent;

  public DefracMacroMethodReference(@NotNull final DefracMacroClassReference parent,
                                    @NotNull final PsiLiteralExpression element,
                                    final int offset,
                                    final int length) {
    super(element, new TextRange(offset, offset + length));
    this.parent = parent;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final ResolveResult[] parentResults = parent.multiResolve(false);
    final List<LookupElement> variants = new LinkedList<LookupElement>();

    for(final ResolveResult parentResult : parentResults) {
      final PsiElement parentElement = parentResult.getElement();

      if(!(parentElement instanceof PsiClass)) {
        continue;
      }

      final PsiClass klass = (PsiClass)parentElement;

      for(final PsiMethod method : klass.getMethods()) {
        variants.add(LookupElementBuilder.create(method));
      }
    }

    return variants.toArray(new Object[variants.size()]);
  }

  @Nullable
  @Override
  public final PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @NotNull
  @Override
  public final ResolveResult[] multiResolve(final boolean incompleteCode) {
    final ResolveResult[] parentResults = parent.multiResolve(incompleteCode);
    final ArrayList<ResolveResult> result = Lists.newArrayListWithExpectedSize(parentResults.length);
    final String value = getValue();

    for(final ResolveResult parentResult : parentResults) {
      final PsiElement parentElement = parentResult.getElement();

      if(!(parentElement instanceof PsiClass)) {
        continue;
      }

      final PsiClass klass = (PsiClass)parentElement;
      final PsiMethod[] methods = klass.findMethodsByName(value, true);

      for(final PsiMethod method : methods) {
        result.add(new PsiElementResolveResult(method));
      }
    }

    return result.toArray(new ResolveResult[result.size()]);
  }
}
