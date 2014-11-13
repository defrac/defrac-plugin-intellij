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

package defrac.intellij.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class DefracModuleUtil {
  public static boolean isDefracModule(@Nullable final Module module) {
    return module != null && ModuleType.get(module) == DefracModuleType.getInstance();
  }


  @Nullable
  public static Module findDefracModule(@Nullable final PsiElement psiElement) {
    return null == psiElement ? null : findDefracModule(psiElement.getContainingFile());
  }

  @Nullable
  public static Module findDefracModule(@Nullable PsiFile psiFile) {
    if(psiFile == null) {
      return null;
    }

    Module module = ModuleUtil.findModuleForPsiElement(psiFile);

    if(module == null) {
      final PsiDirectory directory = psiFile.getParent();

      if(directory != null) {
        module = ModuleUtil.findModuleForPsiElement(directory);
      }
    }

    return isDefracModule(module) ? module : null;
  }


  private DefracModuleUtil() {}
}
