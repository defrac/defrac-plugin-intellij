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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.DefracDelegateClassReference;
import defrac.intellij.psi.DefracPsiUtil;
import defrac.intellij.psi.validation.DefracDelegateValidator;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracDelegateAnnotator implements Annotator {
  public DefracDelegateAnnotator() {}

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

    final PsiAnnotation annotation =
        PsiTreeUtil.getParentOfType(element, PsiAnnotation.class, /*strict=*/false);

    if(!DefracPsiUtil.isDelegateAnnotation(annotation)) {
      return;
    }

    final boolean isGeneric = Names.defrac_annotation_Delegate.equals(annotation.getQualifiedName());
    final PsiClass klass = PsiTreeUtil.getParentOfType(element, PsiClass.class, /*strict=*/false);

    if(klass == null) {
      return;
    }

    final PsiReference[] references =
        element.getReferences();

    final Set<DefracPlatform> platformImplementations = new HashSet<DefracPlatform>();

    for(final PsiReference reference : references) {
      if(!(reference instanceof DefracDelegateClassReference)) {
        continue;
      }

      final DefracDelegateClassReference defracRef = (DefracDelegateClassReference)reference;

      if(isNullOrEmpty(defracRef.getValue())) {
        holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.qualifiedName"));
        return;
      }

      final ResolveResult[] resolveResults = defracRef.multiResolve();

      if(resolveResults.length == 0) {
        final String value = defracRef.getValue();

        if(isNullOrEmpty(value)) {
          holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.qualifiedName"));
          return;
        } else {
          holder.createErrorAnnotation(element, DefracBundle.message("annotator.unresolved", defracRef.getValue()));
          return;
        }
      } else {
        for(final PsiElement result : PsiUtil.mapElements(resolveResults)) {
          final DefracFacet elementFacet = DefracFacet.getInstance(result);

          if(elementFacet != null) {
            if(!platformImplementations.add(elementFacet.getPlatform())) {
              holder.createErrorAnnotation(element, DefracBundle.message("annotator.ambiguous", defracRef.getValue(), elementFacet.getPlatform().displayName));
            }
          }

          DefracDelegateValidator.annotate(element, holder, klass, result);
        }
      }
    }

    if(isGeneric) {
      DefracAnnotatorUtil.reportMissingImplementations(
          element, holder,
          facet,
          klass, platformImplementations,
          DefracPlatform.DELEGATE_ANNOTATION_TO_PLATFORM);
    } else {
      DefracAnnotatorUtil.reportMoreGenericAnnotation(holder, annotation, klass);
    }
  }
}
