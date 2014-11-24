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

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.util.IncorrectOperationException;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.sdk.DefracSdkUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
abstract class RemoveAnnotationQuickFix extends BaseIntentionAction {
  @NotNull
  final PsiModifierListOwner element;

  RemoveAnnotationQuickFix(@NotNull final PsiModifierListOwner element) {
    this.element = element;
  }

  @NotNull
  @Override
  public final String getFamilyName() {
    return "defrac";
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return DefracFacet.getInstance(element) != null
        && element.getManager().isInProject(file)
        && element.getModifierList() != null
        && !DefracSdkUtil.isInDefracSdk(element);
  }

  @Override
  public final void invoke(@NotNull final Project project,
                     final Editor editor,
                     final PsiFile file) throws IncorrectOperationException {
    final PsiModifierList modifierList = element.getModifierList();

    if(modifierList == null) {
      return;
    }

    for(final PsiAnnotation annotation : modifierList.getAnnotations()) {
      if(isAnnotation(annotation)) {
        annotation.delete();
      }
    }
  }

  protected abstract boolean isAnnotation(@NotNull final PsiAnnotation annotation);

  @Override
  public final boolean startInWriteAction() {
    return true;
  }
}
