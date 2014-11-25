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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;
import com.intellij.util.IncorrectOperationException;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class ChangeVisibilityQuickFix extends BaseIntentionAction {
  @NotNull
  private final PsiMember member;

  @NotNull
  private final String newVisibility;

  public ChangeVisibilityQuickFix(@NotNull final PsiMember member,
                                  @NotNull final String newVisibility) {
    this.member = member;
    this.newVisibility = newVisibility;
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return "defrac";
  }

  @NotNull
  @Override
  public String getText() {
    return "Make '"+ member.getName()+"' "+newVisibility;
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return DefracQuickFixUtil.isAvailable(member, file)
        && member.isValid();
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if(FileModificationService.getInstance().preparePsiElementForWrite(member)) {
      if(FileModificationService.getInstance().prepareFileForWrite(member.getContainingFile())) {
        CommandProcessor.getInstance().executeCommand(member.getProject(), new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              @Override
              public void run() {
                DefracPsiUtil.setVisibility(member, newVisibility);
              }
            });
          }
        }, "Changing visibility", null);
      }
    }
  }
}
