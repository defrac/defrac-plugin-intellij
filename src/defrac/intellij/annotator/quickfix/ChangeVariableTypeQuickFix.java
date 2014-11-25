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
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class ChangeVariableTypeQuickFix extends BaseIntentionAction {
  @NotNull
  private final PsiVariable variable;

  @NotNull
  private final PsiType newType;

  public ChangeVariableTypeQuickFix(@NotNull final PsiVariable variable,
                                    @NotNull final PsiType newType) {
    this.variable = variable;
    this.newType = newType;
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return "defrac";
  }

  @NotNull
  @Override
  public String getText() {
    return "Change type of '"+variable.getName()+"' to '"+newType.getPresentableText()+'\'';
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return DefracQuickFixUtil.isAvailable(variable, file)
        && variable.isValid()
        && newType.isValid();
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if(FileModificationService.getInstance().preparePsiElementForWrite(variable)) {
      if(FileModificationService.getInstance().prepareFileForWrite(variable.getContainingFile())) {
        CommandProcessor.getInstance().executeCommand(variable.getProject(), new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              @Override
              public void run() {
                invokeInWriteAction();
              }
            });
          }
        }, "Changing type", null);
      }
    }
  }

  private void invokeInWriteAction() {
    final PsiTypeElement typeElement = variable.getTypeElement();
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(variable.getProject());
    final PsiTypeElement newTypeElement = factory.createTypeElement(newType);

    if(typeElement != null) {
      typeElement.replace(newTypeElement);
    }
  }
}
