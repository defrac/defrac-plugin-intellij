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

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class DefracPsiUtil {
  @SuppressWarnings("SimplifiableIfStatement")
  @Contract("null -> false")
  public static boolean isMacro(@Nullable final PsiElement element) {
    if(!(element instanceof PsiClass)) {
      return false;
    }

    return isMacro((PsiClass)element);
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Contract("null -> false")
  public static boolean isMacro(@Nullable final PsiClass klass) {
    if(klass == null) {
      return false;
    }

    final PsiClass macro =
        JavaPsiFacade.
            getInstance(klass.getProject()).
            findClass(Names.defrac_compiler_macro_Macro, GlobalSearchScope.allScope(klass.getProject()));


    if(macro == null) {
      return false;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    final Boolean cachedValue = CachedValuesManager.getCachedValue(klass, new CachedValueProvider<Boolean>() {
      @Nullable
      public Result<Boolean> compute() {
        return Result.create(
            klass.isInheritor(macro, true),
            klass, PsiModificationTracker.JAVA_STRUCTURE_MODIFICATION_COUNT
        );
      }
    });

    return cachedValue;
  }

  private DefracPsiUtil() {}
}
