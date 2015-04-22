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

package defrac.intellij.completion.ios;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.java.JavaLanguage;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 *
 */
public final class IOSCompletionContributor extends CompletionContributor {
  public IOSCompletionContributor() {
    // joa: not working, maybe find some time to figure out why
    /*PsiJavaPatterns.psiExpression().methodCall(PsiJavaPatterns.psiMethod().with(new PatternCondition<PsiMethod>("representedBy") {
      @Override
      public boolean accepts(@NotNull final PsiMethod method, final ProcessingContext context) {
        for(final PsiParameter parameter : method.getParameterList().getParameters()) {
          final PsiModifierList modifierList = parameter.getModifierList();
          if(modifierList != null && modifierList.findAnnotation(Names.defrac_dni_RepresentedBy) != null) {
            return true;
          }
        }
        return false;
      }
    }));*/

    extend(
        CompletionType.BASIC,
        psiElement().withLanguage(JavaLanguage.INSTANCE),
        new RepresentedByCompletionProvider()
    );
  }
}
