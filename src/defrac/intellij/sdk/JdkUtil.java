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

package defrac.intellij.sdk;

import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class JdkUtil {
  @NotNull private static final LanguageLevel DEFAULT_LANG_LEVEL = LanguageLevel.JDK_1_7;

  public static boolean isApplicableJdk(@NotNull final Sdk jdk) {
    return isApplicableJdk(jdk, null);
  }

  public static boolean isApplicableJdk(@NotNull final Sdk jdk,
                                        @Nullable final LanguageLevel langLevel) {
    if(!isJavaSdk(jdk)) {
      return false;
    }

    final JavaSdkVersion version =
        JavaSdk.getInstance().getVersion(jdk);

    return
        version != null &&
        hasMatchingLangLevel(version, langLevel == null ? DEFAULT_LANG_LEVEL : langLevel);
  }

  private static boolean hasMatchingLangLevel(@NotNull final JavaSdkVersion jdkVersion,
                                              @NotNull final LanguageLevel langLevel) {
    final LanguageLevel maxLangLevel = jdkVersion.getMaxLanguageLevel();
    return maxLangLevel.isAtLeast(langLevel);
  }

  public static boolean isJavaSdk(@Nullable final Sdk sdk) {
    return sdk != null && sdk.getSdkType() instanceof JavaSdk;
  }
}
