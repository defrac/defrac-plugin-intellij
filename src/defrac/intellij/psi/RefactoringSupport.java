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

package defrac.intellij.psi;

import com.intellij.lang.java.JavaRefactoringSupportProvider;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class RefactoringSupport extends JavaRefactoringSupportProvider {
  public RefactoringSupport() {}

  @Override
  public boolean isMemberInplaceRenameAvailable(@NotNull final PsiElement elementToRename,
                                                final PsiElement context) {
    // disable in-place rename for @Delegate and @Macro annotations
    // since it does weird stuff at the moment (qualified name is truncated)

    final PsiAnnotation annotation = PsiTreeUtil.getParentOfType(context, PsiAnnotation.class);

    //noinspection SimplifiableIfStatement
    if(    annotation == null
        || !(DefracPsiUtil.isMacroAnnotation(annotation) || DefracPsiUtil.isDelegateAnnotation(annotation))) {
      return super.isMemberInplaceRenameAvailable(elementToRename, context);
    } else {
      return false;
    }
  }
}
