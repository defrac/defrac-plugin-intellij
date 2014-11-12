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

package defrac.intellij.util;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class Strings {
  @NotNull
  public static String trim(@NotNull final String value, final char c) {
    final char[] chars = value.toCharArray();
    final int n = chars.length;

    int startIndex = 0;
    int endIndex = n - 1;

    while(startIndex < n && chars[startIndex] == c) ++startIndex;

    if(startIndex == n) {
      return "";
    }

    while(endIndex > 0 && chars[endIndex] == c) --endIndex;

    return new String(chars, startIndex, endIndex - startIndex + 1);
  }

  private Strings() {
  }
}
