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

package defrac.intellij.annotator.quickfix;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTypesUtil.getClassType;

/**
 *
 */
public final class ChangeMacroSignatureQuickFix extends BaseIntentionAction {
  @NotNull
  private final PsiMethod macro;

  @NotNull
  private final PsiMethod reference;

  @Nullable
  private final PsiClass parameter;

  public ChangeMacroSignatureQuickFix(@NotNull final PsiMethod macro,
                                      @NotNull final PsiMethod reference) {
    final Project project = macro.getProject();
    final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);

    this.macro = macro;
    this.reference = reference;
    this.parameter = javaPsiFacade.findClass(Names.defrac_compiler_macro_Parameter, scope);
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return "defrac";
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @NotNull
  @Override
  public String getText() {
    return "Change signature of "+macro.getName();
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return parameter != null
        && DefracQuickFixUtil.isAvailable(macro, file)
        && reference.isValid();
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if(parameter == null) {
      return;
    }

    if(FileModificationService.getInstance().preparePsiElementForWrite(macro)) {
      if(FileModificationService.getInstance().prepareFileForWrite(macro.getContainingFile())) {
        CommandProcessor.getInstance().executeCommand(macro.getProject(), new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              public void run() {invokeInWriteAction();
              }
            });
          }
        }, "Changing signature", null);
      }
    }
  }

  private void invokeInWriteAction() {
    assert parameter != null;

    final PsiElementFactory factory = JavaPsiFacade.getInstance(macro.getProject()).getElementFactory();
    final PsiParameterList methodParameterList = macro.getParameterList();
    final PsiParameterList refParameterList = reference.getParameterList();
    final PsiParameter[] methodParameters = methodParameterList.getParameters();
    final PsiParameter[] refParameters = refParameterList.getParameters();
    final int methodParameterCount = methodParameters.length;
    final int referenceParameterCount = refParameters.length;
    final PsiClassType parameterType = getClassType(parameter);

    // change types of existing parameters, keep names
    for(int i = 0; i < Math.min(methodParameterCount, referenceParameterCount); ++i) {
      final PsiParameter methodParameter = methodParameters[i];
      final PsiTypeElement typeElement = methodParameter.getTypeElement();
      final PsiTypeElement newTypeElement = factory.createTypeElement(parameterType);

      if(typeElement != null) {
        typeElement.replace(newTypeElement);
      }
    }

    if(methodParameterCount > referenceParameterCount) {
      // drop illegal parameters
      for(int i = 0; i < methodParameterCount - referenceParameterCount; ++i) {
        methodParameters[methodParameterCount - 1 - i].delete();
      }
    } else if(methodParameterCount < referenceParameterCount) {
      // add missing parameters with names from reference
      final int diff = referenceParameterCount - methodParameterCount;
      for(int i = methodParameterCount; i < methodParameterCount + diff; ++i) {
        final PsiParameter refParameter = refParameters[i];
        final PsiParameter parameter =
            factory.createParameter(refParameter.getName(), parameterType);
        methodParameterList.add(parameter);
      }
    }
  }
}
