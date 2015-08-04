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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The JSONArray class represents a JSON array
 */
public class JSONArray extends JSON implements Iterable<JSON> {
  /** The empty JSON array */
  @NotNull
  public static final JSONArray EMPTY = new EmptyJSONArray();

  @NotNull
  private final ArrayList<JSON> values;

  /**
   * Creates and returns a new JSONArray object
   *
   * @param initialCapacity The initial capacity of the JSONArray
   */
  public JSONArray(final int initialCapacity) {
    values = Lists.newArrayListWithCapacity(initialCapacity);
  }

  /**
   * Creates and returns a new JSONArray object
   */
  public JSONArray() {
    values = Lists.newArrayList();
  }

  /**
   * Creates and returns a new JSONArray object
   *
   * <p>This constructor will use the given collection without making a copy.
   *
   * @param values The list of values to use
   */
  private JSONArray(@NotNull final ArrayList<JSON> values) {
    this.values = values;
  }

  /**
   * Trims the JSONArray to its actual size
   */
  public void trimToSize() {
    values.trimToSize();
  }

  /** Returns always true */
  @Override
  public final boolean isArray() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void encode(@NotNull final JSONEncoder encoder) {
    encoder.openArray();
    final int n = values.size();

    // We do not want IntelliJ IDEA to report this since
    // we know it is an ArrayList and do not want to allocate
    // an iterator here

    //noinspection ForLoopReplaceableByForEach
    for(int i = 0; i < n; ++i) {
      values.get(i).encode(encoder);
    }
    encoder.closeArray();
  }

  /** Returns this object */
  @NotNull
  @Override
  public final JSONArray asArray() {
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<JSON> iterator() {
    return Iterators.transform(values.iterator(), JSON.JSON_TO_NULL);
  }

  /** The length of the array */
  public int length() {
    return values.size();
  }

  /**
   * Returns the JSON value at the given index
   * @param index The index
   * @return The value at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   */
  @Nullable
  public JSON get(final int index) {
    if(index < 0 || index > values.size()) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    return JSON_TO_NULL.apply(values.get(index));
  }

  /**
   * Returns the boolean at the given index
   * @param index The index
   * @return The boolean at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type boolean
   */
  public boolean getBoolean(final int index) {
    if(index < 0 || index > values.size()) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    final JSON value = values.get(index);

    if(!value.isBoolean()) {
      throw new JSONException("Invalid type for index "+index);
    }

    return ((JSONBoolean)value).value;
  }

  /**
   * Returns the byte at the given index
   * @param index The index
   * @return The byte at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type number
   */
  public byte getByte(final int index) {
    return (byte)getDouble(index);
  }

  /**
   * Returns the short at the given index
   * @param index The index
   * @return The short at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type number
   */
  public short getShort(final int index) {
    return (short)getDouble(index);
  }

  /**
   * Returns the integer at the given index
   * @param index The index
   * @return The integer at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type number
   */
  public int getInt(final int index) {
    return (int)getDouble(index);
  }

  /**
   * Returns the long at the given index
   * @param index The index
   * @return The long at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type number
   */
  public long getLong(final int index) {
    return (long)getDouble(index);
  }

  /**
   * Returns the float at the given index
   * @param index The index
   * @return The float at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type number
   */
  public float getFloat(final int index) {
    return (float)getDouble(index);
  }

  /**
   * Returns the double at the given index
   * @param index The index
   * @return The double at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type number
   */
  public double getDouble(final int index) {
    if(index < 0 || index > values.size()) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    final JSONNumber number = values.get(index).asNumber();

    if(null == number) {
      throw new JSONException("Invalid type for index "+index);
    }

    return number.value;
  }

  /**
   * Returns the String at the given index
   *
   * <p>This method won't raise an exception if the data at the index is {@literal null}
   * and return {@literal null} instead.
   *
   * @param index The index
   * @return The String at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type string
   */
  @Nullable
  public String getString(final int index) {
    if(index < 0 || index > values.size()) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    final JSON value = values.get(index);

    if(value.isNull()) {
      return null;
    }

    final JSONString string = value.asString();

    if(null == string) {
      throw new JSONException("Invalid type for index "+index);
    }

    return string.value;
  }

  /**
   * Returns the object at the given index
   *
   * <p>This method won't raise an exception if the data at the index is {@literal null}
   * and return {@literal null} instead.
   *
   * @param index The index
   * @return The object at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type object
   */
  @Nullable
  public JSONObject getObject(final int index) {
    if(index < 0 || index > values.size()) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    final JSON value = values.get(index);

    if(value.isNull()) {
      return null;
    }

    final JSONObject object = value.asObject();

    if(null == object) {
      throw new JSONException("Invalid type for index "+index);
    }

    return object;
  }

  /**
   * Returns the array at the given index
   *
   * <p>This method won't raise an exception if the data at the index is {@literal null}
   * and return {@literal null} instead.
   *
   * @param index The index
   * @return The array at the given index
   * @throws java.util.NoSuchElementException If the index is out of bounds
   * @throws defrac.json.JSONException If the data is not of type array
   */
  @Nullable
  public JSONArray getArray(final int index) {
    if(index < 0 || index > values.size()) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    final JSON value = values.get(index);

    if(value.isNull()) {
      return null;
    }

    final JSONArray array = value.asArray();

    if(null == array) {
      throw new JSONException("Invalid type for index "+index);
    }

    return array;
  }

  public boolean optBoolean(final int index) {
    return optBoolean(index, false);
  }

  public boolean optBoolean(final int index, final boolean defaultValue) {
    if(index < 0 || index > values.size()) {
      return defaultValue;
    }

    final JSONBoolean bool = values.get(index).asBoolean();
    if(null == bool) {
      return defaultValue;
    }

    return bool.value;
  }

  public byte optByte(final int index) {
    return optByte(index, (byte)0);
  }

  public byte optByte(final int index, final byte defaultValue) {
    return (byte)optDouble(index, defaultValue);
  }

  public short optShort(final int index) {
    return optShort(index, (short)0);
  }

  public short optShort(final int index, final short defaultValue) {
    return (short)optDouble(index, defaultValue);
  }

  public int optInt(final int index) {
    return optInt(index, 0);
  }

  public int optInt(final int index, final int defaultValue) {
    return (int)optDouble(index, defaultValue);
  }

  public long optLong(final int index) {
    return optLong(index, 0);
  }

  public long optLong(final int index, final long defaultValue) {
    return (long)optDouble(index, defaultValue);
  }

  public float optFloat(final int index) {
    return optFloat(index, 0.0f);
  }

  public float optFloat(final int index, final float defaultValue) {
    return (float)optDouble(index, defaultValue);
  }

  public double optDouble(final int index) {
    return optDouble(index, 0.0);
  }

  public double optDouble(final int index, final double defaultValue) {
    if(index < 0 || index > values.size()) {
      return defaultValue;
    }

    final JSONNumber number = values.get(index).asNumber();
    if(null == number) {
      return defaultValue;
    }

    return number.value;
  }

  @NotNull
  public String optString(final int index) {
    return optString(index, "");
  }

  public String optString(final int index, final String defaultValue) {
    if(index < 0 || index > values.size()) {
      return defaultValue;
    }

    final JSONString string = values.get(index).asString();
    if(null == string) {
      return defaultValue;
    }

    return string.value;
  }

  @NotNull
  public JSONArray optArray(final int index) {
    return optArray(index, JSONArray.EMPTY);
  }

  public JSONArray optArray(final int index, final JSONArray defaultValue) {
    if(index < 0 || index > values.size()) {
      return defaultValue;
    }

    final JSONArray array = values.get(index).asArray();
    if(null == array) {
      return defaultValue;
    }

    return array;
  }

  @NotNull
  public JSONObject optObject(final int index) {
    return optObject(index, JSONObject.EMPTY);
  }

  public JSONObject optObject(final int index, final JSONObject defaultValue) {
    if(index < 0 || index > length()) {
      return defaultValue;
    }

    final JSONObject object = values.get(index).asObject();
    if(null == object) {
      return defaultValue;
    }

    return object;
  }

  @NotNull
  public JSONArray set(final int index, final boolean value) {
    return set(index, JSONBoolean.of(value));
  }

  @NotNull
  public JSONArray set(final int index, final byte value) {
    return set(index, JSONNumber.of(value));
  }

  @NotNull
  public JSONArray set(final int index, final short value) {
    return set(index, JSONNumber.of(value));
  }

  @NotNull
  public JSONArray set(final int index, final int value) {
    return set(index, JSONNumber.of(value));
  }

  @NotNull
  public JSONArray set(final int index, final long value) {
    return set(index, JSONNumber.of(value));
  }

  @NotNull
  public JSONArray set(final int index, final float value) {
    return set(index, JSONNumber.of(value));
  }

  @NotNull
  public JSONArray set(final int index, final double value) {
    return set(index, JSONNumber.of(value));
  }

  @NotNull
  public JSONArray set(final int index, @Nullable final String value) {
    return set(index, null == value ? JSONNull.INSTANCE : JSONString.of(value));
  }

  @NotNull
  public JSONArray set(final int index, @Nullable final JSON value) {
    if(index < 0) {
      throw new IllegalArgumentException("index < 0");
    }

    final int n = values.size();

    if(index >= n) {
      final int m = (index + 1) - n;
      for(int i = 0; i < m; ++i) {
        values.add(JSONNull.INSTANCE);
      }
    }

    values.set(index, convertNull(value));
    return this;
  }

  public int indexOf(final boolean value) {
    return indexOf(JSONBoolean.of(value));
  }

  public int indexOf(final byte value) {
    return indexOf(JSONNumber.of(value));
  }

  public int indexOf(final short value) {
    return indexOf(JSONNumber.of(value));
  }

  public int indexOf(final int value) {
    return indexOf(JSONNumber.of(value));
  }

  public int indexOf(final long value) {
    return indexOf(JSONNumber.of(value));
  }

  public int indexOf(final float value) {
    return indexOf(JSONNumber.of(value));
  }

  public int indexOf(final double value) {
    return indexOf(JSONNumber.of(value));
  }

  public int indexOf(@NotNull final String value) {
    return indexOf(JSONString.of(value));
  }

  public int indexOf(@Nullable final JSON json) {
    return indexOf(json, 0);
  }

  public int indexOf(@Nullable final JSON json, final int startIndex) {
    final JSON needle = convertNull(json);
    final int n = values.size();

    for(int i = startIndex; i < n; ++i) {
      if(needle.equals(values.get(i))) {
        return i;
      }
    }

    return -1;
  }

  @NotNull
  public List<JSON> toList() {
    return Lists.newArrayList(values);
  }

  @NotNull
  public JSONArray concat(@NotNull final JSONArray...that) {
    int initialCapacity = this.length();
    for(final JSONArray array : that) {
      initialCapacity += array.length();
    }

    final ArrayList<JSON> newValues = Lists.newArrayListWithCapacity(initialCapacity);

    newValues.addAll(this.values);

    int index = this.length();

    for(final JSONArray array : that) {
      newValues.addAll(index, array.values);
      index += array.length();
    }

    return new JSONArray(newValues);
  }

  @NotNull
  public String join(@NotNull final String separator) {
    final StringBuilder result = new StringBuilder();
    boolean separate = false;

    for(final JSON value : values) {
      result.append(value.toString());

      if(separate) {
        result.append(separator);
      } else {
        separate = true;
      }
    }

    return result.toString();
  }

  @NotNull
  public JSONArray push(final boolean value) {
    values.add(JSONBoolean.of(value));
    return this;
  }

  @NotNull
  public JSONArray push(final byte value) {
    return push((double)value);
  }

  @NotNull
  public JSONArray push(final short value) {
    return push((double)value);
  }

  @NotNull
  public JSONArray push(final int value) {
    return push((double)value);
  }

  @NotNull
  public JSONArray push(final long value) {
    return push((double)value);
  }

  @NotNull
  public JSONArray push(final float value) {
    return push((double)value);
  }

  @NotNull
  public JSONArray push(final double value) {
    values.add(JSONNumber.of(value));
    return this;
  }

  @NotNull
  public JSONArray push(@Nullable final String value) {
    values.add(JSONString.of(value));
    return this;
  }

  @NotNull
  public JSONArray push(@Nullable final JSON value) {
    values.add(convertNull(value));
    return this;
  }

  @NotNull
  public JSON pop() {
    if(values.isEmpty()) {
      throw new NoSuchElementException("JSONArray is empty");
    }

    return values.remove(values.size() - 1);
  }

  @NotNull
  public JSONArray reverse() {
    Collections.reverse(values);
    return this;
  }

  @NotNull
  public JSON shift() {
    if(values.isEmpty()) {
      throw new NoSuchElementException("JSONArray is empty");
    }

    return values.remove(0);
  }

  @NotNull
  public JSON remove(final int index) {
    return values.remove(index);
  }

  @NotNull
  public JSONArray slice(final int startIndex) {
    return slice(startIndex, length());
  }

  @NotNull
  public JSONArray slice(final int startIndex, final int endIndex) {
    final int actualEndIndex = endIndex < 0 ? length() - endIndex : endIndex;

    if(startIndex > actualEndIndex) {
      throw new IllegalArgumentException("startIndex > actualIndex");
    }

    final ArrayList<JSON> result = Lists.newArrayListWithExpectedSize(actualEndIndex - startIndex);

    for(int i = startIndex; i < actualEndIndex; ++i) {
      result.add(values.get(i));
    }

    return new JSONArray(result);
  }

  //splice

  //sort

  @NotNull
  public JSONArray unshift(@Nullable final JSON value) {
    values.add(0, value);
    return this;
  }

  @Override
  public String toString() {
    return JSON.stringify(this);
  }

  private static class EmptyJSONArray extends JSONArray {
    @Override
    public void trimToSize() {
      //noop
    }

    @Override
    public void encode(@NotNull final JSONEncoder encoder) {
      encoder.openArray().closeArray();
    }

    @Override
    public Iterator<JSON> iterator() {
      return Iterators.emptyIterator();
    }

    @Override
    public int length() {
      return 0;
    }

    @NotNull
    @Override
    public JSON get(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Override
    public boolean getBoolean(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Override
    public byte getByte(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Override
    public short getShort(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Override
    public int getInt(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Override
    public long getLong(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Override
    public float getFloat(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Override
    public double getDouble(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Nullable
    @Override
    public String getString(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Nullable
    @Override
    public JSONObject getObject(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Nullable
    @Override
    public JSONArray getArray(final int index) {
      throw new NoSuchElementException("No such element at index '"+index+"'");
    }

    @Override
    public boolean optBoolean(final int index, final boolean defaultValue) {
      return defaultValue;
    }

    @Override
    public byte optByte(final int index, final byte defaultValue) {
      return defaultValue;
    }

    @Override
    public short optShort(final int index, final short defaultValue) {
      return defaultValue;
    }

    @Override
    public int optInt(final int index, final int defaultValue) {
      return defaultValue;
    }

    @Override
    public long optLong(final int index, final long defaultValue) {
      return defaultValue;
    }

    @Override
    public float optFloat(final int index, final float defaultValue) {
      return defaultValue;
    }

    @Override
    public double optDouble(final int index, final double defaultValue) {
      return defaultValue;
    }

    @NotNull
    @Override
    public String optString(final int index, @NotNull final String defaultValue) {
      return defaultValue;
    }

    @NotNull
    @Override
    public JSONArray optArray(final int index, @NotNull final JSONArray defaultValue) {
      return defaultValue;
    }

    @NotNull
    @Override
    public JSONObject optObject(final int index, @NotNull final JSONObject defaultValue) {
      return defaultValue;
    }

    @NotNull
    @Override
    public JSONArray set(final int index, final boolean value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray set(final int index, final byte value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray set(final int index, final short value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray set(final int index, final int value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray set(final int index, final long value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray set(final int index, final float value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray set(final int index, final double value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray set(final int index, @Nullable final String value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray set(final int index, @Nullable final JSON value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @Override
    public int indexOf(final boolean value) {
      return -1;
    }

    @Override
    public int indexOf(final byte value) {
      return -1;
    }

    @Override
    public int indexOf(final short value) {
      return -1;
    }

    @Override
    public int indexOf(final int value) {
      return -1;
    }

    @Override
    public int indexOf(final long value) {
      return -1;
    }

    @Override
    public int indexOf(final float value) {
      return -1;
    }

    @Override
    public int indexOf(final double value) {
      return -1;
    }

    @Override
    public int indexOf(@NotNull final String value) {
      return -1;
    }

    @Override
    public int indexOf(@Nullable final JSON json) {
      return -1;
    }

    @Override
    public int indexOf(@Nullable final JSON json, final int startIndex) {
      return -1;
    }

    @NotNull
    @Override
    public List<JSON> toList() {
      return Lists.newLinkedList();
    }

    @NotNull
    @Override
    public JSONArray concat(@NotNull final JSONArray... that) {
      int initialCapacity = 0;
      for(final JSONArray array : that) {
        initialCapacity += array.length();
      }

      final ArrayList<JSON> newValues = Lists.newArrayListWithExpectedSize(initialCapacity);
      int index = 0;

      for(final JSONArray array : that) {
        newValues.addAll(index, array.values);
        index += array.length();
      }

      return new JSONArray(newValues);
    }

    @NotNull
    @Override
    public String join(@NotNull final String separator) {
      return "";
    }

    @NotNull
    @Override
    public JSONArray push(@Nullable final JSON value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSON pop() {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray reverse() {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSON shift() {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @NotNull
    @Override
    public JSONArray slice(final int startIndex) {
      return new JSONArray();
    }

    @NotNull
    @Override
    public JSONArray slice(final int startIndex, final int endIndex) {
      return new JSONArray();
    }

    @NotNull
    @Override
    public JSONArray unshift(@Nullable final JSON value) {
      throw new UnsupportedOperationException("Cannot modify JSONArray.EMPTY");
    }

    @Override
    public String toString() {
      return "[]";
    }
  }
}
