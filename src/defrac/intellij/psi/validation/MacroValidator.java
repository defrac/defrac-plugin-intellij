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

import com.intellij.codeInsight.daemon.impl.quickfix.ExtendsListFix;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import defrac.intellij.DefracBundle;
import defrac.intellij.annotator.quickfix.ChangeMacroSignatureQuickFix;
import defrac.intellij.annotator.quickfix.ChangeReturnTypeQuickFix;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static defrac.intellij.psi.DefracPsiUtil.compareBytecodeTypes;

/**
 *
 */
public final class MacroValidator {
  public static void annotate(@NotNull final PsiElement element,
                              @NotNull final AnnotationHolder holder,
                              @NotNull final PsiMethod thisMethod,
                              @NotNull final PsiMethod thatMethod) {
    // Macro Validation
    // ===================
    // (1) Check same arity
    // (2) Check all parameters of type "Parameter"
    // (3) Check return type is MethodBody
    // (4) Check enclosing class extends Macro


    // (1)
    final int arity = thisMethod.getParameterList().getParametersCount();

    if(arity != thatMethod.getParameterList().getParametersCount()) {
      holder.
          createErrorAnnotation(element,
              DefracBundle.message("annotator.macro.arity", arity, arity == 1 ? "" : "s")).
          registerFix(new ChangeMacroSignatureQuickFix(thatMethod, thisMethod));
      return;
    }

    // --

    final Project project = element.getProject();
    final PsiParameterList parameterList = thatMethod.getParameterList();
    final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    final PsiClass classOfParameter = javaPsiFacade.findClass(Names.defrac_compiler_macro_Parameter, scope);

    if(classOfParameter == null) {
      return;
    }

    final PsiClassType typeOfParameter = PsiTypesUtil.getClassType(classOfParameter);
    final PsiClass classOfMethodBody = checkNotNull(javaPsiFacade.findClass(Names.defrac_compiler_macro_MethodBody, scope));

    if(classOfMethodBody == null) {
      return;
    }

    // (2)
    final PsiClassType typeOfMethodBody = PsiTypesUtil.getClassType(classOfMethodBody);

    for(final PsiParameter parameter : parameterList.getParameters()) {
      if(!compareBytecodeTypes(parameter.getType(), typeOfParameter)) {
        holder.
            createErrorAnnotation(element,
                DefracBundle.message("annotator.macro.parameterType",
                    parameter.getName(), classOfParameter.getName(), thatMethod.getName())).
            registerFix(new ChangeMacroSignatureQuickFix(thatMethod, thisMethod));
      }
    }

    // (3)
    if(!compareBytecodeTypes(thatMethod.getReturnType(), typeOfMethodBody)) {
      holder.
          createErrorAnnotation(element,
              DefracBundle.message("annotator.macro.returnType", thatMethod.getName(), classOfMethodBody.getName())).
          registerFix(new ChangeReturnTypeQuickFix(thatMethod, typeOfMethodBody));
    }

    // (4)
    final PsiClass classOfMacro =  javaPsiFacade.findClass(Names.defrac_compiler_macro_Macro, scope);

    if(classOfMacro == null) {
      return;
    }

    final PsiClass thatClass = thatMethod.getContainingClass();

    if(thatClass != null && !thatClass.isInheritor(classOfMacro, true)) {
      holder.
          createErrorAnnotation(element,
              DefracBundle.message("annotator.macro.mustExtend", thatClass.getName(), classOfMacro.getName())).
          registerFix(new ExtendsListFix(thatClass, classOfMacro, true));
    }
  }

  private MacroValidator() {}
}
