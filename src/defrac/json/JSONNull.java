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
public final class JSONNull extends JSON {
  @NotNull
  public static final JSONNull INSTANCE = new JSONNull();

  private JSONNull() {}

  public boolean isNull() {
    return true;
  }

  @Override
  public void encode(@NotNull final JSONEncoder encoder) {
    encoder.putNull();
  }

  @Override
  public boolean equals(final Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return -1234;
  }

  @Override
  public String toString() {
    return "null";
  }

  @Override
  public double doubleValue() {
    return 0.0;
  }

  @Override
  public float floatValue() {
    return 0.0f;
  }

  @Override
  public long longValue() {
    return 0L;
  }

  @Override
  public int intValue() {
    return 0;
  }

  @Override
  public short shortValue() {
    return (short)0;
  }

  @Override
  public char charValue() {
    return (char)0;
  }

  @Override
  public byte byteValue() {
    return (byte)0;
  }

  @Override
  public boolean booleanValue() {
    return false;
  }

  @NotNull
  @Override
  public String stringValue() {
    return "null";
  }
}
