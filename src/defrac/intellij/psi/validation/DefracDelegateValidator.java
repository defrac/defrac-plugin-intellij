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
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static defrac.intellij.psi.DefracPsiUtil.mapQualifiedName;

/**
 *
 */
public final class DefracDelegateValidator {
  public static void annotate(@NotNull final PsiElement element,
                              @NotNull final AnnotationHolder holder,
                              @NotNull final PsiClass thisClass,
                              @Nullable final PsiElement thatElement) {
    if(!(thatElement instanceof PsiClass)) {
      holder.createErrorAnnotation(element, "Class expected");
      return;
    }

    final PsiClass thatClass = (PsiClass)thatElement;

    // Delegate Validation
    // ===================
    // (1) Check same base class
    // (2) Check all interfaces implemented
    // (3) Check all non-private fields exist with same signature
    // (4) Check all non-private method exists with same signature

    // (1)
    final PsiClass thisSuper = thisClass.getSuperClass();
    final PsiClass thatSuper = thatClass.getSuperClass();

    if(thisSuper == null) {
      if(thatSuper != null) {
        holder.createErrorAnnotation(element, thatClass.getName()+" must not extend "+thatSuper.getName());
        return;
      }
    } else {
      if(thatSuper == null || !DefracPsiUtil.isQualifiedNameEqual(thisSuper, thatSuper)) {
        holder.createErrorAnnotation(element, thatClass.getName()+" must extend "+thisSuper.getName());
        return;
      }
    }

    // (2)
    final PsiClass[] thisInterfaces = thisClass.getInterfaces();
    final Set<String> thatInterfaces = ImmutableSet.copyOf(mapQualifiedName(thatClass.getInterfaces()));
    boolean interfacesHaveError = false;

    for(final PsiClass thisInterface : thisInterfaces) {
      if(!thatInterfaces.contains(thisInterface.getQualifiedName())) {
        holder.createErrorAnnotation(element, thatClass.getName()+" must implement "+thisInterface.getName());
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
        holder.createErrorAnnotation(element, thatClass.getName()+" must contain field "+thisField.getName());
        continue;
      }

      if(!PsiTypesUtil.compareTypes(thisField.getType(), thatField.getType(), true)) {
        holder.createErrorAnnotation(element, thatClass.getName()+"'s field '"+thisField.getName()+"' must be of type '"+thisField.getType().getPresentableText()+'\'');
        continue;
      }

      if(!DefracPsiUtil.isEqualVisibility(thisField, thatField)) {
        holder.createErrorAnnotation(element, thatClass.getName()+"'s field '"+thisField.getName()+"' must be declared "+DefracPsiUtil.getVisibility(thisField));
      }
    }

    // (4)
    final PsiMethod[] thisMethods = thisClass.getMethods();

    for(final PsiMethod thisMethod : thisMethods) {
      final PsiModifierList modifierList = thisMethod.getModifierList();

      if(modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
        continue;
      }

      final PsiMethod[] thatMethods =
          thatClass.findMethodsByName(thisMethod.getName(), false);

      if(thatMethods.length == 0) {
        holder.createErrorAnnotation(element, thatClass.getName()+" must implement method "+thisMethod.getName());
        continue;
      }

      boolean isSignatureEqual = false;

      for(final PsiMethod thatMethod : thatMethods) {
        if(DefracPsiUtil.isSignatureEqual(thisMethod, thatMethod)) {
          isSignatureEqual = true;

          final PsiType thisReturnType = thisMethod.getReturnType();

          if(!PsiTypesUtil.compareTypes(thisReturnType, thatMethod.getReturnType(), true)) {
            holder.createErrorAnnotation(element, thatClass.getName()+"'s method '"+thisMethod.getName()+"' must return type '"+(thisReturnType == null ? '?' : thisReturnType.getPresentableText())+'\'');
            break;
          }

          if(!DefracPsiUtil.isEqualVisibility(thisMethod, thatMethod)) {
            holder.createErrorAnnotation(element, thatClass.getName()+"'s method '"+thisMethod.getName()+"' must be declared "+DefracPsiUtil.getVisibility(thisMethod));
          }

          break;
        }
      }

      if(!isSignatureEqual) {
        holder.createErrorAnnotation(element, thatClass.getName()+"'s method '"+thisMethod.getName()+"'has wrong parameter types");
      }
    }
  }

  private DefracDelegateValidator() {}
}