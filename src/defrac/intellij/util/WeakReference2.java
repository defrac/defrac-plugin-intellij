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

import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 *
 */
public final class WeakReference2<T> extends WeakReference<T> {
  @NotNull
  public static <T> WeakReference2<T> create(@NotNull final T value) {
    return new WeakReference2<T>(value);
  }

  private final int hashCode;

  private WeakReference2(@NotNull final T value) {
    super(value);
    this.hashCode = value.hashCode();
  }

  @Override
  public boolean equals(final Object that) {
    if(this == that) {
      return true;
    }

    final T thisValue = get();

    if(that instanceof Reference<?>) {
      final Object thatValue = ((Reference<?>)that).get();
      return Comparing.equal(thisValue, thatValue);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}
