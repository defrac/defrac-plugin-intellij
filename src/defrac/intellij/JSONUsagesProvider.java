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

package defrac.intellij;

import com.intellij.lang.cacheBuilder.WordOccurrence;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class JSONUsagesProvider implements FindUsagesProvider {
  public JSONUsagesProvider() {}

  @Nullable
  @Override
  public WordsScanner getWordsScanner() {
    return new WordsScanner() {
      @Override
      public void processWords(final CharSequence fileText, final Processor<WordOccurrence> processor) {
        //TODO(joa): implement me
      }
    };
  }

  @Override
  public boolean canFindUsagesFor(@NotNull final PsiElement element) {
    return element instanceof PsiClass && !(element instanceof PsiTypeParameter);
  }

  @Nullable
  @Override
  public String getHelpId(@NotNull final PsiElement element) {
    return null;
  }

  @NotNull
  @Override
  public String getType(@NotNull final PsiElement element) {
    return null; //TODO(joa): implement me
  }

  @NotNull
  @Override
  public String getDescriptiveName(@NotNull final PsiElement element) {
    return null; //TODO(joa): implement me
  }

  @NotNull
  @Override
  public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
    return null; //TODO(joa): implement me
  }
}
