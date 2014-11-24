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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class RemoveReadOnlyQuickFix extends RemoveAnnotationQuickFix {
  public RemoveReadOnlyQuickFix(@NotNull final PsiField field) {
    super(field);
  }

  @NotNull
  @Override
  public String getText() {
    return "Remove @ReadOnly";
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return super.isAvailable(project, editor, file)
        && DefracPsiUtil.isReadOnly(element);
  }

  @Override
  protected boolean isAnnotation(@NotNull final PsiAnnotation annotation) {
    return DefracPsiUtil.isReadOnlyAnnotation(annotation);
  }
}
