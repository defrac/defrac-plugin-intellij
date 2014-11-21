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

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

/**
*
*/
enum DefracQualifiedClassNameInsertHandler implements InsertHandler<LookupElement> {
  INSTANCE;

  @Override
  public void handleInsert(@NotNull final InsertionContext context,
                           @NotNull final LookupElement item) {
    // When auto-completion happens for @Macro or @Delegate we always need the
    // fully qualified class name. IntelliJ IDEA inserts the class name by default
    // and I couldn't find a way to prevent this from happening.
    //
    // This insert handler therefore prefixes the name inserted by IntelliJ IDEA
    // with the package of the class.

    final Object object = item.getObject();

    if(!(object instanceof PsiClass)) {
      return;
    }

    PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments();

    final PsiClass klass = (PsiClass)object;
    final int caretOffset = context.getEditor().getCaretModel().getOffset();
    final String qualifiedName = klass.getQualifiedName();

    if(qualifiedName == null) {
      // This smells like an error but we simply bail out.
      return;
    }

    final String name = klass.getName();
    final int lastIndexOfDot = qualifiedName.lastIndexOf('.');

    if(lastIndexOfDot == -1) {
      // This is a top-level class and therefore nothing to prepend
      return;
    }

    context.
        getEditor().
        getDocument().
        insertString(
            // We move backwards by the name of the class that IntelliJ IDEA inserts
            caretOffset - name.length(),
            // We prepend the qualified name
            // Note: we add 1 because we want the last '.' char
            qualifiedName.substring(0, lastIndexOfDot + 1)
        );
  }
}
