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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.sdk.DefracSdkUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracQuickFixUtil {
  public static boolean isAvailable(@NotNull final PsiElement element,
                                    @NotNull final PsiFile file) {
    return DefracFacet.getInstance(element) != null
        && element.getManager().isInProject(file)
        && !DefracSdkUtil.isInDefracSdk(element);
  }

  private DefracQuickFixUtil() {}
}
