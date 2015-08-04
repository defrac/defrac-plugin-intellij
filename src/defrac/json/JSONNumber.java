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
public final class JSONNumber extends JSON implements Comparable<JSONNumber> {
  @NotNull
  public static JSONNumber of(final double value) {
    return new JSONNumber(value);
  }

  @NotNull
  public static JSONNumber of(@NotNull final String value) throws NumberFormatException {
    return of(Double.parseDouble(value));
  }

  final double value;

  private JSONNumber(final double value) {
    this.value = value;
  }

  @Override
  public int compareTo(@NotNull final JSONNumber that) {
    return Double.compare(this.value, that.value);
  }

  @Override
  public double doubleValue() {
    return value;
  }

  @Override
  public float floatValue() { return (float)value; }

  @Override
  public long longValue() { return (long)value; }

  @Override
  public int intValue() { return (int)value; }

  @Override
  public short shortValue() { return (short)value; }

  @Override
  public char charValue() { return (char)value; }

  @Override
  public byte byteValue() { return (byte)value; }

  @Override
  public boolean booleanValue() {
    return value != 0.0;
  }

  @NotNull
  @Override
  public String stringValue() {
    return String.valueOf(value);
  }

  @Override
  public boolean isNumber() {
    return true;
  }

  @Override
  public void encode(@NotNull final JSONEncoder encoder) {
    final int intValue = (int)value;
    if(intValue == value) {
      encoder.put(intValue);
    } else {
      encoder.put(value);
    }
  }

  @NotNull
  @Override
  public JSONNumber asNumber() {
    return this;
  }

  @Override
  public boolean equals(final Object other) {
    if(this == other) {
      return true;
    }

    if(!(other instanceof JSONNumber)) {
      return false;
    }

    final JSONNumber that = (JSONNumber)other;
    return this.value == that.value;
  }

  @Override
  public int hashCode() {
    final long temp = Double.doubleToLongBits(value);
    return (int)(temp ^ (temp >>> 32));
  }

  @Override
  public String toString() {
    final int intValue = (int)value;
    if(intValue == value) {
      return Integer.toString(intValue);
    } else {
      return Double.toString(value);
    }
  }
}
