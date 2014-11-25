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

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import defrac.intellij.DefracBundle;
import defrac.intellij.annotator.quickfix.RemoveReadOnlyQuickFix;
import defrac.intellij.annotator.quickfix.RemoveWriteOnlyQuickFix;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class IncompatibleReadWriteAnnotator implements Annotator {
  public IncompatibleReadWriteAnnotator() {}

  @Override
  public void annotate(@NotNull final PsiElement element,
                       @NotNull final AnnotationHolder holder) {
    if(!(element instanceof PsiField)) {
      return;
    }

    final PsiField field = (PsiField)element;

    if(DefracPsiUtil.isReadOnly(field) && DefracPsiUtil.isWriteOnly(field)) {
      final Annotation annotation =
          holder.
              createErrorAnnotation(element, DefracBundle.message("annotator.readWrite.both"));

      annotation.registerFix(new RemoveWriteOnlyQuickFix(field));
      annotation.registerFix(new RemoveReadOnlyQuickFix(field));
    }
  }
}
