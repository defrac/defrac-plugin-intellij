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

package defrac.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class JSONString extends JSON implements Comparable<JSONString> {
  @NotNull
  public static final JSONString EMPTY = new JSONString("");

  public static JSON of(@Nullable final String value) {
    if(null == value) {
      return JSONNull.INSTANCE;
    }

    return value.isEmpty() ? EMPTY : new JSONString(value);
  }

  @NotNull
  final String value;

  private JSONString(@NotNull final String value) {
    this.value = value;
  }

  @Override
  public int compareTo(@NotNull final JSONString that) {
    return this.value.compareTo(that.value);
  }

  @Override
  public void encode(@NotNull final JSONEncoder encoder) {
    encoder.put(value);
  }

  @Override
  public double doubleValue() {
    return Double.valueOf(value);
  }

  @Override
  public float floatValue() {
    return Float.valueOf(value);
  }

  @Override
  public long longValue() {
    return Long.valueOf(value);
  }

  @Override
  public int intValue() {
    return Integer.valueOf(value);
  }

  @Override
  public short shortValue() {
    return Short.valueOf(value);
  }

  @Override
  public char charValue() {
    return value.charAt(0);
  }

  @Override
  public byte byteValue() {
    return Byte.valueOf(value);
  }

  @Override
  public boolean booleanValue() {
    return Boolean.valueOf(value);
  }

  @NotNull
  @Override
  public String stringValue() {
    return value;
  }

  @Override
  public boolean equals(final Object other) {
    if(this == other) {
      return true;
    }

    if(other instanceof String) {
      return this.value.equals(other);
    }

    if(!(other instanceof JSONString)) {
      return false;
    }

    final JSONString that = (JSONString)other;
    return this.value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return JSON.escapeJSON(value);
  }

  @Nullable
  @Override
  public JSONString asString() {
    return this;
  }

  @Override
  public boolean isString() {
    return true;
  }
}
