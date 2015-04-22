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
import defrac.intellij.DefracBundle;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static defrac.intellij.psi.DefracPsiUtil.getValue;
import static defrac.intellij.psi.DefracPsiUtil.isIntrinsicAnnotation;

/**
 *
 */
public final class IntrinsicAnnotator implements Annotator {
  public IntrinsicAnnotator() {
  }

  @Override
  public void annotate(@NotNull final PsiElement element,
                       @NotNull final AnnotationHolder holder) {
    if(!(element instanceof PsiLiteralExpression)) {
      return;
    }

    final DefracFacet facet = DefracFacet.getInstance(element);

    if(facet == null || facet.isMacroLibrary()) {
      return;
    }

    if(!(facet.getPlatform().isIOS() || facet.getPlatform().isWeb())) {
      return;
    }

    final PsiAnnotation annotation =
        getParentOfType(element, PsiAnnotation.class, /*strict=*/false);

    if(annotation == null || !isIntrinsicAnnotation(annotation)) {
      return;
    }

    final String text = getValue((PsiLiteralExpression) element);

    if(text == null) {
      return;
    }

    final PsiMethod method =
        getParentOfType(annotation, PsiMethod.class, /*strict=*/false);

    if(method == null) {
      final PsiClass klass = getParentOfType(annotation, PsiClass.class, /*strict=*/false);

      if(klass == null) {
        return;
      }

      validateClass(holder, klass, annotation, text, facet);
      return;
    }

    validateMethod(holder, method, annotation, text, facet);
  }

  private void validateClass(@NotNull final AnnotationHolder holder,
                             @NotNull final PsiClass klass,
                             @NotNull final PsiAnnotation annotation,
                             @NotNull final String value,
                             @NotNull final DefracFacet facet) {
    if(!PsiNameHelper.getInstance(facet.getModule().getProject()).isIdentifier(value)) {
      holder.createErrorAnnotation(annotation, DefracBundle.message("annotator.intrinsic.invalidClassName"));
    }
  }

  private void validateMethod(@NotNull final AnnotationHolder holder,
                              @NotNull final PsiMethod method,
                              @NotNull final PsiAnnotation annotation,
                              @NotNull final String value,
                              @NotNull final DefracFacet facet) {
    if(facet.getPlatform().isWeb()) {
      if(!PsiNameHelper.getInstance(facet.getModule().getProject()).isIdentifier(value)) {
        holder.createErrorAnnotation(annotation, DefracBundle.message("annotator.intrinsic.invalidMethodName"));
      }
    } else if(facet.getPlatform().isIOS()) {
      boolean valid = true;
      int actualParameters = 0;

      if(value.length() < 1) {
        valid = false;
      } else {
        if(!Character.isJavaIdentifierStart(value.charAt(0))) {
          valid = false;
        } else {
          final int n = value.length();
          for(int i = 1; i < n; ++i) {
            final char c = value.charAt(i);
            if(c == ':') {
              actualParameters++;
            } else if(!Character.isJavaIdentifierPart(c)) {
              valid = false;
              break;
            }
          }
        }
      }

      if(!valid) {
        holder.createErrorAnnotation(annotation, DefracBundle.message("annotator.intrinsic.invalidMethodName"));
        return;
      }

      final int expectedParameters =
          method.getParameterList().getParametersCount();

      if(actualParameters != expectedParameters) {
        holder.createErrorAnnotation(annotation,
            DefracBundle.message("annotator.intrinsic.invalidMethodParameters", actualParameters, expectedParameters));
      }
    }
  }
}
