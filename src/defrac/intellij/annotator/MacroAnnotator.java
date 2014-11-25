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
import defrac.intellij.psi.DefracPsiUtil;
import defrac.intellij.psi.MacroClassReference;
import defrac.intellij.psi.MacroMethodReference;
import defrac.intellij.psi.validation.DefracMacroValidator;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class MacroAnnotator implements Annotator {
  public MacroAnnotator() {}

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

    if(!DefracPsiUtil.isMacroAnnotation(annotation)) {
      return;
    }

    final boolean isGeneric = Names.defrac_annotation_Macro.equals(annotation.getQualifiedName());
    final PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class, /*strict=*/false);

    if(method == null) {
      return;
    }

    final PsiReference[] references =
        element.getReferences();

    final Set<DefracPlatform> platformImplementations = new HashSet<DefracPlatform>();

    for(final PsiReference reference : references) {
      if(reference instanceof MacroClassReference) {
        final MacroClassReference defracRef = (MacroClassReference)reference;

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
          }
        }
      } else if(reference instanceof MacroMethodReference) {
        final MacroMethodReference defracRef = (MacroMethodReference)reference;

        if(isNullOrEmpty(defracRef.getValue())) {
          holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.identifier"));
          return;
        }

        final ResolveResult[] resolveResults = defracRef.multiResolve();

        if(resolveResults.length == 0) {
          final String value = defracRef.getValue();

          if(isNullOrEmpty(value)) {
            holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.identifier"));
            return;
          } else {
            holder.createErrorAnnotation(element, DefracBundle.message("annotator.unresolved", defracRef.getValue()));
            return;
          }
        } else {
          for(final PsiElement result : PsiUtil.mapElements(resolveResults)) {
            DefracMacroValidator.annotate(element, holder, method, result);
          }
        }
      }
    }

    if(isGeneric) {
      DefracAnnotatorUtil.reportMissingImplementations(
          element, holder,
          facet, method,
          platformImplementations, DefracPlatform.MACRO_ANNOTATION_TO_PLATFORM,
          /*isDelegate=*/true);
    } else {
      DefracAnnotatorUtil.reportMoreGenericAnnotation(
          holder, annotation, method,
          Names.defrac_annotation_Macro,
          DefracPlatform.byMacroAnnotation(checkNotNull(annotation.getQualifiedName())),
          /*isDelegate=*/true);
    }
  }
}
