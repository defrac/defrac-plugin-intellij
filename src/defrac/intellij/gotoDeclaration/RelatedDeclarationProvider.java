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
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.psi.*;
import defrac.intellij.psi.DelegateClassReference;
import defrac.intellij.psi.MacroMethodReference;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.psi.util.PsiUtil.mapElements;
import static defrac.intellij.psi.DefracPsiUtil.isDelegateAnnotation;
import static defrac.intellij.psi.DefracPsiUtil.isMacroAnnotation;

/**
 *
 */
public final class RelatedDeclarationProvider extends GotoRelatedProvider {
  public RelatedDeclarationProvider() {}

  @NotNull
  @Override
  public List<? extends GotoRelatedItem> getItems(@NotNull final PsiElement element) {
    if(element instanceof PsiIdentifier) {
      final PsiElement parent = element.getParent();

      if(parent instanceof PsiMethod) {
        return getItems((PsiMethod)parent);
      } else if(parent instanceof PsiClass) {
        return getItems((PsiClass)parent);
      }
    } else if(element instanceof PsiMethod) {
      return getItems((PsiMethod)element);
    } else if(element instanceof PsiClass) {
      return getItems((PsiClass)element);
    } else if(element instanceof PsiJavaReference) {
      final PsiElement resolved = ((PsiJavaReference)element).resolve();

      if(resolved instanceof PsiMethod) {
        return getItems((PsiMethod)resolved);
      } else if(resolved instanceof PsiClass) {
        return getItems((PsiClass)resolved);
      }
    }

    return super.getItems(element);
  }

  @NotNull
  public List<GotoRelatedItem> getItems(@NotNull final PsiMethod method) {
    final PsiModifierList modifiers = method.getModifierList();
    final ArrayList<GotoRelatedItem> items = Lists.newArrayListWithCapacity(0);

    for(final PsiAnnotation annotation : modifiers.getAnnotations()) {
      if(!isMacroAnnotation(annotation)) {
        continue;
      }

      final MacroMethodReference reference =
          MacroMethodReference.getInstance(annotation);

      if(reference == null) {
        continue;
      }

      for(final PsiElement element : mapElements(reference.multiResolve())) {
        items.add(new GotoRelatedItem(element, "Macro"));
      }
    }

    return items;
  }

  @NotNull
  public List<GotoRelatedItem> getItems(@NotNull final PsiClass klass) {
    final PsiModifierList modifiers = klass.getModifierList();

    if(modifiers == null) {
      return Collections.emptyList();
    }

    final ArrayList<GotoRelatedItem> items = Lists.newArrayListWithCapacity(0);

    for(final PsiAnnotation annotation : modifiers.getAnnotations()) {
      if(!isDelegateAnnotation(annotation)) {
        continue;
      }

      final DelegateClassReference reference =
          DelegateClassReference.getInstance(annotation);

      if(reference == null) {
        continue;
      }

      for(final PsiElement element : mapElements(reference.multiResolve())) {
        items.add(new GotoRelatedItem(element, "Delegate"));
      }
    }

    return items;
  }
}
