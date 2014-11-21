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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracMacroReferenceProvider extends PsiReferenceProvider {
  public DefracMacroReferenceProvider() {}

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element,
                                               @Nullable final ProcessingContext context) {
    final DefracFacet facet = DefracFacet.getInstance(element);

    if(facet == null || facet.isMacroLibrary()) {
      return PsiReference.EMPTY_ARRAY;
    }

    final PsiFile file = element.getContainingFile();

    if(file == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    // (1) get literal
    final String value = getValue((PsiLiteralExpression)element);

    if(isNullOrEmpty(value)) {
      return PsiReference.EMPTY_ARRAY;
    }

    // (2) get annotation
    final PsiAnnotation annotation =
        PsiTreeUtil.getParentOfType(element, PsiAnnotation.class, /*strict=*/false);

    if(annotation == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    // (3) is this our annotation?
    final String qualifiedName = annotation.getQualifiedName();

    if(qualifiedName == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    if(!matchType(qualifiedName, Names.ALL_MACROS)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final int indexOfHash = value.lastIndexOf('#');

    if(indexOfHash == -1) {
      return new PsiReference[] {
          new DefracMacroClassReference(
              (PsiLiteralExpression)element,
              // We ignore the first ", so start-offset is 1
              1,
              // We ignore the last ", so length is fine (end is exclusive)
              value.length()
          )
      };
    }

    final DefracMacroClassReference classReference = new DefracMacroClassReference(
        (PsiLiteralExpression) element,
        // We ignore the first "
        1,
        // We ignore the # so the length is the index of the hash
        // minus one for the first " we ignore
        //
        // Example: "abc#foo" the indexOfHash is 4 and the length is 3, but exclusive
        indexOfHash
    );

    /*if(indexOfHash >= value.length() - 2) {
      // "foo.bar.Baz#"
      return new PsiReference[] { classReference };
    }*/

    return new PsiReference[] {
        classReference,
        new DefracMacroMethodReference(
            classReference,
            (PsiLiteralExpression)element,
            // We start without the #
            indexOfHash + 2,
            // We ignore the # so the length is the index of the hash
            // minus one for the first " we ignore
            //
            // We ignore the last ", so use length minus one for the hash
            value.length() - indexOfHash - 1
        )
    };
  }

  private boolean matchType(@NotNull final String qname,
                                           @NotNull final String[] availableNames) {
    for(final String name : availableNames) {
      if(name.equals(qname)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean acceptsTarget(@NotNull final PsiElement target) {
    return target instanceof PsiLiteralExpression;
  }

  @Nullable
  private static String getValue(@NotNull final PsiLiteralExpression literalExp) {
    final Object value = literalExp.getValue();
    return value instanceof String ? (String)value : null;
  }
}
