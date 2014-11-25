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
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class ChangeSuperClassQuickFix extends BaseIntentionAction {
  @NotNull
  private final PsiClass klass;

  @Nullable
  private final PsiClass newSuperClass;

  public ChangeSuperClassQuickFix(@NotNull final PsiClass klass,
                                  @Nullable final PsiClass newSuperClass) {
    this.klass = klass;
    this.newSuperClass = newSuperClass;
  }


  @NotNull
  @Override
  public String getFamilyName() {
    return "defrac";
  }

  @NotNull
  @Override
  public String getText() {
    if(newSuperClass == null) {
      final PsiClass oldSuperClass = klass.getSuperClass();
      if(oldSuperClass == null) {
        return "?";
      } else {
        return "Don't extend "+oldSuperClass.getName();
      }
    } else {
      return "Extend "+newSuperClass.getName();
    }
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return DefracQuickFixUtil.isAvailable(klass, file);
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
        }, "Changing inheritance", null);
      }
    }
  }

  private void invokeInWriteAction() {
    final PsiElementFactory factory =
        JavaPsiFacade.getInstance(klass.getProject()).getElementFactory();

    if(klass instanceof PsiAnonymousClass) {
      final PsiJavaCodeReferenceElement baseClassReference = ((PsiAnonymousClass) klass).getBaseClassReference();

      if(newSuperClass == null) {
        baseClassReference.delete();
      } else {
        baseClassReference.replace(factory.createClassReferenceElement(newSuperClass));
      }
    } else {
      final PsiReferenceList extendsList = klass.getExtendsList();
      if(extendsList != null && extendsList.getReferenceElements().length == 1) {
        final PsiElement oldExtends = extendsList.getReferenceElements()[0];
        oldExtends.delete();

        if(newSuperClass != null) {
          final PsiElement newExtends = extendsList.add(factory.createClassReferenceElement(newSuperClass));
          JavaCodeStyleManager.getInstance(klass.getProject()).shortenClassReferences(newExtends);
        }
      }
    }
  }
}
