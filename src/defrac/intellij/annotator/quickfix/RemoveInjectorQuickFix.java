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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import static defrac.intellij.psi.DefracPsiUtil.isInjectorAnnotation;

/**
 *
 */
public final class RemoveInjectorQuickFix extends RemoveAnnotationQuickFix {
  public RemoveInjectorQuickFix(@NotNull final PsiClass klass) {
    super(klass);
  }

  @NotNull
  @Override
  public String getText() {
    return "Remove @Injector";
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return super.isAvailable(project, editor, file);
  }

  @Override
  protected boolean isAnnotation(@NotNull final PsiAnnotation annotation) {
    return isInjectorAnnotation(annotation);
  }
}