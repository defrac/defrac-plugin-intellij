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

/**
 *
 */
public final class JSONBoolean extends JSON implements Comparable<JSONBoolean> {
  @NotNull
  public static JSONBoolean of(final boolean value) {
    return value ? TRUE : FALSE;
  }

  @NotNull
  public static final JSONBoolean TRUE = new JSONBoolean(true);

  @NotNull
  public static final JSONBoolean FALSE = new JSONBoolean(false);

  public final boolean value;

  private JSONBoolean(final boolean value) {
    this.value = value;
  }

  @Override
  public int compareTo(@NotNull final JSONBoolean that) {
    return this.value == that.value ? 0 : this.value ? 1 : -1;
  }

  @Override
  public boolean isBoolean() {
    return true;
  }

  @Override
  public void encode(@NotNull final JSONEncoder encoder) {
    encoder.put(value);
  }

  @NotNull
  @Override
  public JSONBoolean asBoolean() {
    return this;
  }

  @Override
  public boolean equals(final Object other) {
    if(this == other) {
      return true;
    }

    if(!(other instanceof JSONBoolean)) {
      return false;
    }

    final JSONBoolean that = (JSONBoolean)other;
    return this.value == that.value;
  }

  @Override
  public int hashCode() {
    return value ? 1 : 0;
  }

  @Override
  public String toString() {
    return value ? "true" : "false";
  }

  @Override
  public double doubleValue() {
    return value ? 1.0 : 0.0;
  }

  @Override
  public float floatValue() {
    return value ? 1.0f : 0.0f;
  }

  @Override
  public long longValue() {
    return value ? 1L : 0L;
  }

  @Override
  public int intValue() {
    return value ? 1 : 0;
  }

  @Override
  public short shortValue() {
    return value ? (short)1 : (short)0;
  }

  @Override
  public char charValue() {
    return value ? (char)1 : (char)0;
  }

  @Override
  public byte byteValue() {
    return value ? (byte)1 : (byte)0;
  }

  @Override
  public boolean booleanValue() {
    return value;
  }

  @NotNull
  @Override
  public String stringValue() {
    return String.valueOf(value);
  }
}
