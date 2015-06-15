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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.impl.quickfix.AddMethodFix;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateMethodQuickFix;
import com.intellij.codeInsight.daemon.impl.quickfix.ExtendsListFix;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import defrac.intellij.DefracBundle;
import defrac.intellij.annotator.quickfix.*;
import defrac.intellij.psi.AccessMethodDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static defrac.intellij.psi.DefracPsiUtil.*;
import static defrac.intellij.util.Grammar.buildGenitive;

/**
 *
 */
public final class MultiPlatformClassValidator {
  @NotNull
  private static final Logger LOG = Logger.getInstance(MultiPlatformClassValidator.class.getName());

  public static void annotate(@NotNull final PsiElement element,
                              @NotNull final AnnotationHolder holder,
                              @NotNull final PsiElement injectorElement,
                              @Nullable final PsiElement injectionElement) {
    if(!(injectorElement instanceof PsiClass)) {
      holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.class"));
      return;
    }

    if(!(injectionElement instanceof PsiClass)) {
      holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.class"));
      return;
    }

    final PsiClass injectorClass = (PsiClass)injectorElement;
    final PsiClass injectionClass = (PsiClass)injectionElement;

    // Multi-Platform Class Validation
    // ===============================
    // (1) Check same base class
    // (2) Check all interfaces implemented
    // (3) Check all non-private fields exist with same signature
    // (4) Check all non-private method exists with same signature
    // (5) Check all constructors have a pendant with equal parameters
    // (6) Check all declared non-private inner classes access non-private members of outer class

    // (1)
    final PsiClass injectorSuper = injectorClass.getSuperClass();
    final PsiClass injectionSuper = injectionClass.getSuperClass();

    if(injectorSuper == null) {
      if(injectionSuper != null) {
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.multiPlatformClass.mustNotExtend", injectionClass.getName(), injectionSuper.getName())).
            registerFix(new ChangeSuperClassQuickFix(injectionClass, null));
        return;
      }
    } else {
      if(injectionSuper == null || !isQualifiedNameEqual(injectorSuper, injectionSuper)) {
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.multiPlatformClass.mustExtend", injectionClass.getName(), injectorSuper.getName())).
            registerFix(new ChangeSuperClassQuickFix(injectionClass, injectorSuper));
        return;
      }
    }

    // (2)
    final PsiClass[] injectorInterfaces = injectorClass.getInterfaces();
    boolean interfacesHaveError = false;

    if(injectorInterfaces.length != 0) {
      final Set<String> injectionInterfaceQnames =
          ImmutableSet.copyOf(mapQualifiedName(injectionClass.getInterfaces()));

      for(final PsiClass injectorInterface : injectorInterfaces) {
        if(!injectionInterfaceQnames.contains(injectorInterface.getQualifiedName())) {
          holder.createErrorAnnotation(element,
              DefracBundle.message("annotator.multiPlatformClass.mustImplement", injectionClass.getName(), injectorInterface.getName())).
              registerFix(new ExtendsListFix(injectionClass, injectorInterface, true));
          interfacesHaveError = true;
        }
      }
    }

    if(interfacesHaveError) {
      return;
    }

    // (3)
    final PsiField[] injectorFields = injectorClass.getFields();

    for(final PsiField injectorField : injectorFields) {
      final PsiModifierList modifierList = injectorField.getModifierList();

      if(modifierList != null && modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
        continue;
      }

      final PsiField injectionField =
          injectionClass.findFieldByName(injectorField.getName(), false);

      if(injectionField == null) {
        /*
        why is this quickfix always creating a getter/setter/field trio?

            new CreateFieldOrPropertyFix(
                    injectionClass,
                    injectorField.getName(),
                    injectorField.getType(),
                    PropertyMemberType.FIELD,
                    injectorField.getModifierList() == null ? PsiAnnotation.EMPTY_ARRAY : injectorField.getModifierList().getAnnotations())
         */
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.multiPlatformClass.field.missing", injectionClass.getName(), injectorField.getName())).
            registerFix(new CreateFieldQuickFix(injectionClass, injectorField));
        continue;
      }

      if(!compareBytecodeTypes(injectorField.getType(), injectionField.getType())) {
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.multiPlatformClass.field.type",
                    buildGenitive(injectionClass.getName()), injectorField.getName(), injectorField.getType().getPresentableText())).
        registerFix(new ChangeVariableTypeQuickFix(injectionField, injectorField.getType()));
        continue;
      }

      if(!isEqualVisibility(injectorField, injectionField)) {
        final String visibility = getVisibility(injectorField);

        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.multiPlatformClass.field.visibility",
                    buildGenitive(injectionClass.getName()), injectorField.getName(), visibility)).
            registerFix(new ChangeVisibilityQuickFix(injectionField, visibility));
      }
    }

    // (4)
    final PsiMethod[] injectorMethods = injectorClass.getMethods();
    Set<PsiMethod> visibleConstructors = null;

    for(final PsiMethod injectorMethod : injectorMethods) {
      final PsiModifierList modifierList = injectorMethod.getModifierList();

      if(modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
        continue;
      }

      if(injectorMethod.isConstructor()) {
        if(visibleConstructors == null) {
          visibleConstructors = Sets.newHashSet();
        }

        visibleConstructors.add(injectorMethod);
        continue;
      }

      final PsiMethod[] injectionMethods =
          injectionClass.findMethodsByName(injectorMethod.getName(), false);

      if(injectionMethods.length == 0) {
        final Annotation annotation = holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.multiPlatformClass.method.missing",
                    injectionClass.getName(), injectorMethod.getName()));

        annotation.registerFix(
            new AddMethodFix(injectorMethod, injectionClass));

        continue;
      }

      boolean isSignatureEqual = false;

      for(final PsiMethod injectionMethod : injectionMethods) {
        if(isSignatureEqual(injectorMethod, injectionMethod)) {
          isSignatureEqual = true;

          final PsiType thisReturnType = injectorMethod.getReturnType();

          if(!compareBytecodeTypes(thisReturnType, injectionMethod.getReturnType())) {
            final Annotation annotation = holder.
                createErrorAnnotation(element,
                    DefracBundle.message("annotator.multiPlatformClass.method.returnType",
                        buildGenitive(injectionClass.getName()), injectorMethod.getName(),
                        thisReturnType == null ? "?" : thisReturnType.getPresentableText()));
            if(thisReturnType != null) {
              annotation.
                  registerFix(new ChangeReturnTypeQuickFix(injectionMethod, thisReturnType));
            }
            break;
          }

          if(!isEqualVisibility(injectorMethod, injectionMethod)) {
            final String visibility = getVisibility(injectorMethod);
            holder.
                createErrorAnnotation(element,
                    DefracBundle.message("annotator.multiPlatformClass.method.visibility",
                        buildGenitive(injectionClass.getName()), injectorMethod.getName(), visibility)).
                registerFix(new ChangeVisibilityQuickFix(injectionMethod, visibility));
          }

          break;
        }
      }

      if(!isSignatureEqual) {
        final Annotation annotation = holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.multiPlatformClass.method.signature",
                    buildGenitive(injectionClass.getName()), injectorMethod.getName()));

        if(injectionMethods.length == 1) {
          annotation.registerFix(new ChangeMethodSignatureQuickFix(injectionMethods[0], injectorMethod));
        }
      }
    }

    // (5)
    if(visibleConstructors != null) {
      assert !visibleConstructors.isEmpty();

      final PsiMethod[] injectionConstructors = injectionClass.findMethodsByName(injectionClass.getName(), false);
      final boolean injectionHasNoConstructors = injectionConstructors.length == 0;

      for(final PsiMethod injectorConstructor : visibleConstructors) {
        if(injectionHasNoConstructors) {
          final Annotation annotation = holder.
              createErrorAnnotation(element,
                  DefracBundle.message("annotator.multiPlatformClass.constructor.missing",
                      injectionClass.getName(), getParameterTypes(injectorConstructor)));

          final CreateMethodQuickFix fix = CreateMethodQuickFix.
              createFix(injectionClass, getConstructorSignature(injectionClass, injectorConstructor), "");

          if(fix != null) {
            annotation.registerFix(fix);
          }

          continue;
        }

        boolean isSignatureEqual = false;

        for(final PsiMethod injectionConstructor : injectionConstructors) {
          if(isSignatureEqual(injectorConstructor, injectionConstructor)) {
            isSignatureEqual = true;

            if(!isEqualVisibility(injectorConstructor, injectionConstructor)) {
              final String visibility = getVisibility(injectorConstructor);
              holder.
                  createErrorAnnotation(element,
                      DefracBundle.message("annotator.multiPlatformClass.constructor.visibility",
                          buildGenitive(injectionClass.getName()), injectorConstructor.getName(),
                          visibility)).
                  registerFix(new ChangeVisibilityQuickFix(injectionConstructor, visibility));
            }

            break;
          }
        }

        if(!isSignatureEqual) {
          final Annotation annotation = holder.
              createErrorAnnotation(element,
                  DefracBundle.message("annotator.multiPlatformClass.constructor.missing",
                      injectionClass.getName(), getParameterTypes(injectorConstructor)));

          final CreateMethodQuickFix fix = CreateMethodQuickFix.
              createFix(injectionClass, getConstructorSignature(injectionClass, injectorConstructor), "");

          if(fix != null) {
            annotation.registerFix(fix);
          }
        }
      }
    }

    // (6)
    final PsiClass[] injectorInnerClasses = injectorClass.getInnerClasses();

    if(injectorInnerClasses.length != 0) {
      final PsiClass[] injectionInnerClasses = injectionClass.getInnerClasses();
      final ImmutableMap.Builder<String, PsiClass> builder = ImmutableMap.builder();

      for(final PsiClass injectionInnerClass : injectionInnerClasses) {
        builder.put(injectionInnerClass.getName(), injectionInnerClass);
      }

      final Map<String, PsiClass> injectionClassByName = builder.build();

      for(final PsiClass injectorInnerClass : injectorInnerClasses) {
        final PsiModifierList modifierList = injectorInnerClass.getModifierList();

        if(modifierList == null || modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
          continue;
        }

        final PsiClass injectionInnerClass = injectionClassByName.
            get(injectorInnerClass.getName());

        if(injectionInnerClass != null) {
          MultiPlatformClassValidator.
              annotate(
                  element instanceof PsiLiteralExpression ? element : injectionInnerClass,
                  holder, injectorInnerClass, injectionInnerClass);
          continue;
        }

        final AccessMethodDetector accessMethodDetector =
            new AccessMethodDetector(injectorInnerClass);

        if(!accessMethodDetector.requiresAccessMethod()) {
          continue;
        }

        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.multiPlatformClass.innerClass.missing",
                    injectionClass.getName(), injectorInnerClass.getName()));

        //TODO: provide fix to create inner class
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

  private MultiPlatformClassValidator() {}
}
