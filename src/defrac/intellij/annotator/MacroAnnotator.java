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

import com.google.common.collect.Lists;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateMethodQuickFix;
import com.intellij.codeInsight.daemon.quickFix.CreateClassOrPackageFix;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassKind;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.annotator.quickfix.ChangeMacroSignatureQuickFix;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.MacroClassReference;
import defrac.intellij.psi.MacroMethodReference;
import defrac.intellij.psi.validation.DefracMacroValidator;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static defrac.intellij.psi.DefracPsiUtil.isMacroAnnotation;
import static defrac.intellij.psi.DefracPsiUtil.mapToContainingClasses;

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

    if(!isMacroAnnotation(annotation)) {
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
    String target = null;

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
            final Annotation errorAnnotation = holder.createErrorAnnotation(element, DefracBundle.message("annotator.unresolved", defracRef.getValue()));
            final String qualifiedClassName = MacroMethodReference.getQualifiedClassName(value);
            final CreateClassOrPackageFix fix = DefracAnnotatorUtil.createCreateClassOrPackageFix(
                qualifiedClassName == null ? "" : qualifiedClassName,
                checkNotNull(DefracFacet.getInstance(method)).
                    getMacroSearchScope(DefracPlatform.byMacroAnnotation(annotation.getQualifiedName())),
                element,
                ClassKind.CLASS,
                Names.defrac_compiler_macro_Macro,
                null);

            if(fix != null) {
              errorAnnotation.registerFix(fix);
            }
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

        target = defracRef.getValue();

        final ResolveResult[] resolveResults = defracRef.multiResolve();

        if(resolveResults.length == 0) {
          final String value = defracRef.getValue();

          if(isNullOrEmpty(value)) {
            holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.identifier"));
            return;
          } else {
            final Annotation errorAnnotation = holder.
                createErrorAnnotation(element, DefracBundle.message("annotator.unresolved", value));

            final ResolveResult[] classes = defracRef.getClassReference().multiResolve();

            for(final PsiElement klass : PsiUtil.mapElements(classes)) {
              if(!(klass instanceof PsiClass)) {
                continue;
              }

              if(((PsiClass)klass).findMethodsByName(value, true).length == 0) {
                final CreateMethodQuickFix fix = CreateMethodQuickFix.createFix(
                    (PsiClass)klass,
                    getMacroSignature(value, method),
                    "return MethodBody(Return());"
                );

                if(fix != null) {
                  errorAnnotation.registerFix(fix);
                }
              }
            }

            return;
          }
        } else {
          final int arity = method.getParameterList().getParametersCount();
          final ArrayList<PsiMethod> candidates = Lists.newArrayListWithCapacity(2);

          boolean found = false;

          for(final PsiElement result : PsiUtil.mapElements(resolveResults)) {
            if(!(result instanceof PsiMethod)) {
              continue;
            }

            final PsiMethod thatMethod = (PsiMethod)result;

            if(arity == thatMethod.getParameterList().getParametersCount()) {
              DefracMacroValidator.annotate(element, holder, method, thatMethod);
              found = true;
              break;
            } else {
              candidates.add(thatMethod);
            }
          }

          if(!found) {
            final Annotation errorAnnotation = holder.
                createErrorAnnotation(element,
                    DefracBundle.message("annotator.macro.arity", arity, arity == 1 ? "" : "s"));

            if(candidates.size() == 1) {
              errorAnnotation.
                  registerFix(new ChangeMacroSignatureQuickFix(candidates.get(0), method));
            }

            final Set<PsiClass> containingClasses = mapToContainingClasses(candidates);

            for(final PsiClass klass : containingClasses) {
              final CreateMethodQuickFix fix = CreateMethodQuickFix.createFix(
                  klass,
                  getMacroSignature(method.getName(), method),
                  "return MethodBody(Return());"
              );

              if(fix != null) {
                errorAnnotation.registerFix(fix);
              }
            }
          }
        }
      }
    }

    if(isGeneric) {
      DefracAnnotatorUtil.reportMissingImplementations(
          element,
          holder,
          facet, method,
          platformImplementations,
          DefracPlatform.MACRO_ANNOTATION_TO_PLATFORM,
          target,
          /*isDelegate=*/false);
    } else {
      DefracAnnotatorUtil.reportMoreGenericAnnotation(
          holder, annotation, method,
          Names.defrac_annotation_Macro,
          DefracPlatform.byMacroAnnotation(checkNotNull(annotation.getQualifiedName())),
          /*isDelegate=*/false);
    }
  }

  private static String getMacroSignature(final String name, final PsiMethod method) {
    final PsiParameter[] parameters = method.getParameterList().getParameters();
    final StringBuilder signatureBuilder = new StringBuilder("@Nonnull ");
    signatureBuilder.
        append(PsiModifier.PUBLIC).append(' ').
        append(PsiModifier.FINAL).
        append(" MethodBody ").append(name).
        append('(');

    boolean peelMe = true;

    for(final PsiParameter parameter : parameters) {
      if(!peelMe) {
        signatureBuilder.append(", ");
      }

      signatureBuilder.append("@Nonnull final Parameter ").append(parameter.getName());
      peelMe = false;
    }

    signatureBuilder.
        append(')');

    return signatureBuilder.toString();
  }
}
