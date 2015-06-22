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

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class AccessMethodDetector extends PsiRecursiveElementVisitor  {
  @NotNull
  private final PsiClass klass;

  private boolean accessMethodFound;

  public AccessMethodDetector(@NotNull final PsiClass klass) {
    this.klass = klass;
  }

  public boolean requiresAccessMethod() {
    accessMethodFound = false;
    klass.accept(this);
    return accessMethodFound;
  }

  @Override
  public void visitElement(final PsiElement element) {
    // Maybe there is an actual visitor API hidden somewhere :/
    if(element instanceof PsiReferenceExpression) {
      visitReferenceExpression((PsiReferenceExpression)element);
    }

    if(!accessMethodFound) {
      super.visitElement(element);
    }
  }

  private void visitReferenceExpression(final PsiReferenceExpression expression) {
    final PsiElement element = expression.resolve();

    if(!(element instanceof PsiMember)) {
      return;
    }

    final PsiMember member = (PsiMember)element;

    final PsiModifierList modifiers = member.getModifierList();

    if(modifiers == null || !modifiers.hasModifierProperty(PsiModifier.PRIVATE)) {
      return;
    }

    if(member.getContainingClass() != klass) {
      accessMethodFound = true;
    }
  }
}
