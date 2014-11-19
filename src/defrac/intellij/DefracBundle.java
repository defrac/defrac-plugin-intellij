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

package defrac.intellij;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 *
 */
public final class DefracBundle {
  @NonNls @NotNull private static final String BUNDLE = "defrac.intellij.DefracBundle";

  @Nullable private static Reference<ResourceBundle> bundle;

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) final String key,
                               @NotNull final Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  @NotNull
  private static ResourceBundle getBundle() {
    ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(DefracBundle.bundle);

    if(bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      DefracBundle.bundle = new SoftReference<ResourceBundle>(bundle);
    }

    return bundle;
  }

  private DefracBundle() {}
}
