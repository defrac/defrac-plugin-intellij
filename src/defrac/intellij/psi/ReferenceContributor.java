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

import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 *
 */
public final class ReferenceContributor extends PsiReferenceContributor {
  public ReferenceContributor() {}

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    // TODO(joa): withReference of a multi-platform / macro class
    registrar.registerReferenceProvider(
        psiElement(PsiLiteralExpression.class).
            withSuperParent(3, psiElement(PsiAnnotation.class)),
        new InjectionReferenceProvider()
    );

    registrar.registerReferenceProvider(
        psiElement(PsiLiteralExpression.class).
            withSuperParent(3, psiElement(PsiAnnotation.class)),
        new InjectorReferenceProvider()
    );

    registrar.registerReferenceProvider(
        psiElement(PsiLiteralExpression.class).
            withLanguage(JavaLanguage.INSTANCE).
            withSuperParent(3, psiElement(PsiAnnotation.class)),
        new MacroReferenceProvider()
    );

    registrar.registerReferenceProvider(
        psiElement(JsonProperty.class).withLanguage(JsonLanguage.INSTANCE),
        new MainClassReferenceProvider()
    );
  }
}
