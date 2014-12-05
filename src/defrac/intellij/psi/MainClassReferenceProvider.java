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

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import defrac.intellij.DefracPlatform;
import defrac.intellij.fileType.DefracSettingsFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;

/**
 *
 */
public final class MainClassReferenceProvider extends PsiReferenceProvider {
  public MainClassReferenceProvider() {}

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element,
                                               @NotNull final ProcessingContext context) {
    if(!(element instanceof JsonProperty)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final JsonProperty property = (JsonProperty)element;

    if(!"main".equals(property.getName())) {
      return PsiReference.EMPTY_ARRAY;
    }

    final PsiFile file = element.getContainingFile();

    if(file == null || file.getFileType() != DefracSettingsFileType.getInstance()) {
      return PsiReference.EMPTY_ARRAY;
    }

    // (1) get literal
    final String value = getValue((JsonProperty)element);

    if(isNullOrEmpty(value)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final JsonObject object =
        getParentOfType(property, JsonObject.class, /*strict=*/true);

    if(object == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final JsonProperty scope =
        getParentOfType(object, JsonProperty.class, /*strict=*/true);

    final DefracPlatform targetPlatform;

    if(scope == null) {
      targetPlatform = DefracPlatform.GENERIC;
    } else {
      // note: could make sure parent is generic!
      //       otherwise we
      targetPlatform = DefracPlatform.byName(scope.getName());

      if(targetPlatform == null) {
        return PsiReference.EMPTY_ARRAY;
      }
    }

    return new PsiReference[] {
        new MainClassReference(
            value,
            checkNotNull((JsonStringLiteral) property.getValue()),
            targetPlatform)
    };
  }

  @Nullable
  private static String getValue(@NotNull final JsonProperty property) {
    final JsonValue value = property.getValue();

    if(!(value instanceof JsonStringLiteral)) {
      return null;
    }

    final JsonStringLiteral literal = (JsonStringLiteral)value;
    final String text = literal.getText();

    if(isNullOrEmpty(text) || text.length() < 3) {
      return null;
    }

    return text.substring(1, text.length() - 1);
  }
}
