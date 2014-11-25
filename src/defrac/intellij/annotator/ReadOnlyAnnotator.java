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

package defrac.intellij.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.*;
import defrac.intellij.DefracBundle;
import defrac.intellij.annotator.quickfix.RemoveReadOnlyQuickFix;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class ReadOnlyAnnotator implements Annotator {
  public ReadOnlyAnnotator() {}

  @Override
  public void annotate(@NotNull final PsiElement element,
                       @NotNull final AnnotationHolder holder) {
    if(!(element instanceof PsiAssignmentExpression)) {
      return;
    }

    final PsiAssignmentExpression assignmentExpression =
        (PsiAssignmentExpression)element;

    final PsiExpression lhs = assignmentExpression.getLExpression();

    if(!(lhs instanceof PsiReferenceExpression)) {
      return;
    }

    final PsiReferenceExpression referenceExpression = (PsiReferenceExpression)lhs;
    final PsiElement referencedElement = referenceExpression.resolve();

    if(!(referencedElement instanceof PsiField)) {
      return;
    }

    final PsiField field = (PsiField)referencedElement;

    if(DefracPsiUtil.isReadOnly(field)) {
      holder.
          createErrorAnnotation(element,
              DefracBundle.message("annotator.readWrite.readOnly", field.getName())).
          registerFix(new RemoveReadOnlyQuickFix(field));
    }
  }
}
