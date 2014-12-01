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
import com.intellij.codeInsight.daemon.impl.quickfix.CreateFieldFromUsageHelper;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.util.PsiUtil.setModifierProperty;
import static defrac.intellij.psi.DefracPsiUtil.getVisibility;

/**
 *
 */
public final class CreateFieldQuickFix extends BaseIntentionAction {
  @NotNull
  private final PsiClass klass;
  @NotNull
  private final PsiField field;

  public CreateFieldQuickFix(@NotNull final PsiClass klass,
                             @NotNull final PsiField field) {
    this.klass = klass;
    this.field = field;
  }

  @NotNull
  @Override
  public String getText() {
    return "Create Field "+field.getName();
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return "defrac";
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return DefracQuickFixUtil.isAvailable(klass, file)
        && field.isValid();
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if(FileModificationService.getInstance().preparePsiElementForWrite(klass)) {
      if(FileModificationService.getInstance().prepareFileForWrite(klass.getContainingFile())) {
        CommandProcessor.getInstance().executeCommand(klass.getProject(), new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              public void run() {
                invokeInWriteAction();
              }
            });
          }
        }, "Adding field", null);
      }
    }
  }

  private void invokeInWriteAction() {
    final PsiElementFactory factory =
        JavaPsiFacade.getInstance(klass.getProject()).getElementFactory();
    final PsiField newField = factory.createField(field.getName(), field.getType());

    // equal visibility
    setModifierProperty(newField, getVisibility(field), true);

    CreateFieldFromUsageHelper.insertField(klass, newField, klass);
  }
}
