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

package defrac.intellij.psi.validation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateMethodQuickFix;
import com.intellij.codeInsight.daemon.impl.quickfix.ExtendsListFix;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.*;
import defrac.intellij.DefracBundle;
import defrac.intellij.annotator.quickfix.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static defrac.intellij.psi.DefracPsiUtil.*;
import static defrac.intellij.util.Grammar.buildGenitive;

/**
 *
 */
public final class DelegateValidator {
  public static void annotate(@NotNull final PsiElement element,
                              @NotNull final AnnotationHolder holder,
                              @NotNull final PsiClass thisClass,
                              @Nullable final PsiElement thatElement) {
    if(!(thatElement instanceof PsiClass)) {
      holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.class"));
      return;
    }

    final PsiClass thatClass = (PsiClass)thatElement;

    // Delegate Validation
    // ===================
    // (1) Check same base class
    // (2) Check all interfaces implemented
    // (3) Check all non-private fields exist with same signature
    // (4) Check all non-private method exists with same signature
    // (5) Check all constructors have a pendant with equal parameters

    // (1)
    final PsiClass thisSuper = thisClass.getSuperClass();
    final PsiClass thatSuper = thatClass.getSuperClass();

    if(thisSuper == null) {
      if(thatSuper != null) {
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.delegate.mustNotExtend", thatClass.getName(), thatSuper.getName())).
            registerFix(new ChangeSuperClassQuickFix(thatClass, null));
        return;
      }
    } else {
      if(thatSuper == null || !isQualifiedNameEqual(thisSuper, thatSuper)) {
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.delegate.mustExtend", thatClass.getName(), thisSuper.getName())).
            registerFix(new ChangeSuperClassQuickFix(thatClass, thisSuper));
        return;
      }
    }

    // (2)
    final PsiClass[] thisInterfaces = thisClass.getInterfaces();
    final Set<String> thatInterfaceQnames = ImmutableSet.copyOf(mapQualifiedName(thatClass.getInterfaces()));
    boolean interfacesHaveError = false;

    for(final PsiClass thisInterface : thisInterfaces) {
      if(!thatInterfaceQnames.contains(thisInterface.getQualifiedName())) {
        holder.createErrorAnnotation(element,
            DefracBundle.message("annotator.delegate.mustImplement", thatClass.getName(), thisInterface.getName())).
            registerFix(new ExtendsListFix(thatClass, thisInterface, true));
        interfacesHaveError = true;
      }
    }

    if(interfacesHaveError) {
      return;
    }

    final PsiClass[] thatInterfaces = thatClass.getInterfaces();
    final Set<String> thisInterfaceQnames = ImmutableSet.copyOf(mapQualifiedName(thisInterfaces));
    for(final PsiClass thatInterface: thatInterfaces) {
      if(!thisInterfaceQnames.contains(thatInterface.getQualifiedName())) {
        holder.createErrorAnnotation(element,
            DefracBundle.message("annotator.delegate.mustNotImplement", thatClass.getName(), thatInterface.getName())).
            registerFix(new ExtendsListFix(thatClass, thatInterface, false));
        interfacesHaveError = true;
      }
    }

    if(interfacesHaveError) {
      return;
    }

    // (3)
    final PsiField[] thisFields = thisClass.getFields();

    for(final PsiField thisField : thisFields) {
      final PsiModifierList modifierList = thisField.getModifierList();

      if(modifierList != null && modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
        continue;
      }

      final PsiField thatField =
          thatClass.findFieldByName(thisField.getName(), false);

      if(thatField == null) {
        /*
        why is this quickfix always creating a getter/setter/field trio?

            new CreateFieldOrPropertyFix(
                    thatClass,
                    thisField.getName(),
                    thisField.getType(),
                    PropertyMemberType.FIELD,
                    thisField.getModifierList() == null ? PsiAnnotation.EMPTY_ARRAY : thisField.getModifierList().getAnnotations())
         */
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.delegate.field.missing", thatClass.getName(), thisField.getName())).
            registerFix(new CreateFieldQuickFix(thatClass, thisField));
        continue;
      }

      if(!compareBytecodeTypes(thisField.getType(), thatField.getType())) {
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.delegate.field.type",
                    buildGenitive(thatClass.getName()), thisField.getName(), thisField.getType().getPresentableText())).
        registerFix(new ChangeVariableTypeQuickFix(thatField, thisField.getType()));
        continue;
      }

      if(!isEqualVisibility(thisField, thatField)) {
        final String visibility = getVisibility(thisField);

        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.delegate.field.visibility",
                    buildGenitive(thatClass.getName()), thisField.getName(), visibility)).
            registerFix(new ChangeVisibilityQuickFix(thatField, visibility));
      }
    }

    // (4)
    final PsiMethod[] thisMethods = thisClass.getMethods();
    Set<PsiMethod> visibleConstructors = null;

    for(final PsiMethod thisMethod : thisMethods) {
      final PsiModifierList modifierList = thisMethod.getModifierList();

      if(modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
        continue;
      }

      if(thisMethod.isConstructor()) {
        if(visibleConstructors == null) {
          visibleConstructors = Sets.newHashSet();
        }

        visibleConstructors.add(thisMethod);
        continue;
      }

      final PsiMethod[] thatMethods =
          thatClass.findMethodsByName(thisMethod.getName(), false);

      if(thatMethods.length == 0) {
        final Annotation annotation = holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.delegate.method.missing", thatClass.getName(), thisMethod.getName()));

        final CreateMethodQuickFix fix = CreateMethodQuickFix.createFix(thatClass, getMethodSignature(thisMethod), "");

        if(fix != null) {
          annotation.registerFix(fix);
        }

        continue;
      }

      boolean isSignatureEqual = false;

      for(final PsiMethod thatMethod : thatMethods) {
        if(isSignatureEqual(thisMethod, thatMethod)) {
          isSignatureEqual = true;

          final PsiType thisReturnType = thisMethod.getReturnType();

          if(!compareBytecodeTypes(thisReturnType, thatMethod.getReturnType())) {
            final Annotation annotation = holder.
                createErrorAnnotation(element,
                    DefracBundle.message("annotator.delegate.method.returnType",
                        buildGenitive(thatClass.getName()), thisMethod.getName(),
                        thisReturnType == null ? "?" : thisReturnType.getPresentableText()));
            if(thisReturnType != null) {
              annotation.
                  registerFix(new ChangeReturnTypeQuickFix(thatMethod, thisReturnType));
            }
            break;
          }

          if(!isEqualVisibility(thisMethod, thatMethod)) {
            final String visibility = getVisibility(thisMethod);
            holder.
                createErrorAnnotation(element,
                    DefracBundle.message("annotator.delegate.method.visibility",
                        buildGenitive(thatClass.getName()), thisMethod.getName(), visibility)).
                registerFix(new ChangeVisibilityQuickFix(thatMethod, visibility));
          }

          break;
        }
      }

      if(!isSignatureEqual) {
        final Annotation annotation = holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.delegate.method.signature",
                    buildGenitive(thatClass.getName()), thisMethod.getName()));

        if(thatMethods.length == 1) {
          annotation.registerFix(new ChangeMethodSignatureQuickFix(thatMethods[0], thisMethod));
        }
      }
    }

    // (5)
    if(visibleConstructors != null) {
      assert !visibleConstructors.isEmpty();

      final PsiMethod[] thatConstructors = thatClass.findMethodsByName(thatClass.getName(), false);
      final boolean thatHasNoConstructors = thatConstructors.length == 0;

      for(final PsiMethod thisConstructor : visibleConstructors) {
        if(thatHasNoConstructors) {
          final Annotation annotation = holder.
              createErrorAnnotation(element,
                  DefracBundle.message("annotator.delegate.constructor.missing",
                      thatClass.getName(), getParameterTypes(thisConstructor)));

          final CreateMethodQuickFix fix = CreateMethodQuickFix.
              createFix(thatClass, getConstructorSignature(thatClass, thisConstructor), "");

          if(fix != null) {
            annotation.registerFix(fix);
          }

          continue;
        }

        boolean isSignatureEqual = false;

        for(final PsiMethod thatConstructor : thatConstructors) {
          if(isSignatureEqual(thisConstructor, thatConstructor)) {
            isSignatureEqual = true;

            if(!isEqualVisibility(thisConstructor, thatConstructor)) {
              final String visibility = getVisibility(thisConstructor);
              holder.
                  createErrorAnnotation(element,
                      DefracBundle.message("annotator.delegate.constructor.visibility",
                          buildGenitive(thatClass.getName()), thisConstructor.getName(),
                          getMethodSignature(thatConstructor),
                          visibility)).
                  registerFix(new ChangeVisibilityQuickFix(thatConstructor, visibility));
            }

            break;
          }
        }

        if(!isSignatureEqual) {
          final Annotation annotation = holder.
              createErrorAnnotation(element,
                  DefracBundle.message("annotator.delegate.constructor.missing",
                      thatClass.getName(), getParameterTypes(thisConstructor)));

          final CreateMethodQuickFix fix = CreateMethodQuickFix.
              createFix(thatClass, getConstructorSignature(thatClass, thisConstructor), "");

          if(fix != null) {
            annotation.registerFix(fix);
          }
        }
      }
    }
  }

  @NotNull
  private static String getMethodSignature(@NotNull final PsiMethod method) {
    final PsiParameter[] parameters = method.getParameterList().getParameters();
    final StringBuilder signatureBuilder = new StringBuilder(getVisibility(method));
    final PsiType returnType = method.getReturnType();

    signatureBuilder.
        append(' ').append(returnType == null ? "" : returnType.getPresentableText()).
        append(' ').append(method.getName()).
        append('(');

    boolean peelMe = true;

    for(final PsiParameter parameter : parameters) {
      if(!peelMe) {
        signatureBuilder.append(", ");
      }

      signatureBuilder.append(parameter.getType().getPresentableText()).append(' ').append(parameter.getName());
      peelMe = false;
    }

    signatureBuilder.
        append(')');

    return signatureBuilder.toString();
  }

  @NotNull
  private static String getConstructorSignature(@NotNull final PsiClass targetClass,
                                                @NotNull final PsiMethod method) {
    final PsiParameter[] parameters = method.getParameterList().getParameters();
    final StringBuilder signatureBuilder = new StringBuilder(getVisibility(method));

    signatureBuilder.
        append(' ').append(targetClass.getName()).
        append('(');

    boolean peelMe = true;

    for(final PsiParameter parameter : parameters) {
      if(!peelMe) {
        signatureBuilder.append(", ");
      }

      signatureBuilder.append(parameter.getType().getPresentableText()).append(' ').append(parameter.getName());
      peelMe = false;
    }

    signatureBuilder.
        append(')');

    return signatureBuilder.toString();
  }

  @NotNull
  private static String getParameterTypes(@NotNull final PsiMethod method) {
    final PsiParameter[] parameters = method.getParameterList().getParameters();
    final StringBuilder signatureBuilder = new StringBuilder();

    signatureBuilder.
        append('(');

    boolean peelMe = true;

    for(final PsiParameter parameter : parameters) {
      if(!peelMe) {
        signatureBuilder.append(", ");
      }

      signatureBuilder.append(parameter.getType().getPresentableText());
      peelMe = false;
    }

    signatureBuilder.
        append(')');

    return signatureBuilder.toString();
  }

  private DelegateValidator() {}
}
