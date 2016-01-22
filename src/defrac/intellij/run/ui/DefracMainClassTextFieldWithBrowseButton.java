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

package defrac.intellij.run.ui;

import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import defrac.intellij.run.DefracRunUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 */
public final class DefracMainClassTextFieldWithBrowseButton extends EditorTextFieldWithBrowseButton {

  public DefracMainClassTextFieldWithBrowseButton(@NotNull final Project project,
                                                  @NotNull final ConfigurationModuleSelector moduleSelector) {
    super(project, true, new JavaCodeFragment.VisibilityChecker() {
      public Visibility isDeclarationVisible(PsiElement declaration, PsiElement place) {
        if(declaration instanceof PsiClass) {
          final PsiClass aClass = (PsiClass) declaration;

          if(isValidMainClass(aClass) || place.getParent() != null && isValidMainClass(moduleSelector.findClass(aClass.getQualifiedName()))) {
            return Visibility.VISIBLE;
          }
        }

        return Visibility.NOT_VISIBLE;
      }

      private boolean isValidMainClass(@Nullable final PsiClass cls) {
        return DefracRunUtil.isValidMainClass(moduleSelector.getModule(), cls);
      }
    });
  }
}
