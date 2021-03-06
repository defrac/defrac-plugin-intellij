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

package defrac.intellij.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfigOracle;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static defrac.intellij.psi.DefracPsiUtil.isUnsupported;

/**
 *
 */
public final class UnsupportedAnnotator implements Annotator {
  public UnsupportedAnnotator() {}

  @Override
  public void annotate(@NotNull final PsiElement element,
                       @NotNull final AnnotationHolder holder) {
    final DefracFacet facet = DefracFacet.getInstance(element);

    if(facet == null) {
      return;
    }

    final DefracPlatform platform  = facet.getPlatform();

   if(element instanceof PsiClass) {
      annotateClass(element, (PsiClass)element, holder, platform);
    } else if(element instanceof PsiMethod) {
      final PsiMethod method = (PsiMethod)element;

      if(method.isConstructor()) {
        annotateClass(element, method.getContainingClass(), holder, platform);
      } else if(isUnsupported(method, platform)) {
        holder.
            createErrorAnnotation(element, DefracBundle.message("annotator.unsupported.method"));
      }
    } else if(element instanceof PsiReferenceExpression) {
      final PsiReferenceExpression referenceExpression = (PsiReferenceExpression)element;
      final PsiElement referencedElement = referenceExpression.resolve();
      annotateReference(element, holder, facet, referencedElement);
    } else if(element instanceof PsiJavaCodeReferenceElement) {
      final PsiJavaCodeReferenceElement referenceElement = (PsiJavaCodeReferenceElement)element;
      final JavaResolveResult[] results = referenceElement.multiResolve(false);
      for(final PsiElement resolvedElement : PsiUtil.mapElements(results)) {
        annotateReference(element, holder, facet, resolvedElement);
      }
    }
  }

  private void annotateClass(@NotNull final PsiElement element,
                             @Nullable final PsiClass klass,
                             @NotNull final AnnotationHolder holder,
                             @NotNull final DefracPlatform platform) {
    if(klass == null) {
      return;
    }

    if(isUnsupported(klass, platform)) {
      holder.
          createErrorAnnotation(element,
              DefracBundle.message("annotator.unsupported.class"));
    }
  }

  private void annotateReference(@NotNull final PsiElement element,
                                 @NotNull final AnnotationHolder holder,
                                 @NotNull final DefracFacet facet,
                                 @Nullable final PsiElement referencedElement) {
    if(!(referencedElement instanceof PsiModifierListOwner)) {
      return;
    }

    final PsiModifierListOwner owner = (PsiModifierListOwner)referencedElement;
    final DefracPlatform platform = facet.getPlatform();

    if(platform.isGeneric()) {
      final DefracConfigOracle config = facet.getConfigOracle();

      if(config != null) {
        for(final DefracPlatform targetPlatform : config.getTargets()) {
          if(isUnsupported(owner, targetPlatform)) {
            holder.
                createWarningAnnotation(element,
                    DefracBundle.message("annotator.unsupported.refInGeneric",
                        getPresentableKind(owner),
                        getPresentableName(owner),
                        targetPlatform.displayName));
          }
        }
      }
    } else {
      if(isUnsupported(owner, platform)) {
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.unsupported.ref",
                    getPresentableKind(owner),
                    getPresentableName(owner)));
      }
    }
  }

  @NotNull
  private String getPresentableKind(@NotNull final PsiElement element) {
    if(element instanceof PsiClass) {
      return "class";
    } else if(element instanceof PsiMethod) {
      return "method";
    } else if(element instanceof PsiVariable) {
      return "variable";
    } else {
      return "";
    }
  }

  @NotNull
  private String getPresentableName(@NotNull final PsiElement element) {
    if(!(element instanceof PsiNamedElement)) {
      return "";
    }

    final String name = ((PsiNamedElement)element).getName();

    if(name == null) {
      return "";
    }

    return name;
  }
}
