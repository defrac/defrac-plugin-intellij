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
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 *
 */
public class JSONObject extends JSON {
  @NotNull
  public static final JSONObject EMPTY = new EmptyJSONObject();

  @NotNull
  private final LinkedHashMap<String, JSON> values;

  public JSONObject() {
    values = Maps.newLinkedHashMap();
  }

  public JSONObject(@NotNull final Map<? extends String, ? extends JSON> map) {
    values = Maps.newLinkedHashMap(map);
  }

  public JSONObject(final int initialCapacity, final float loadFactor) {
    values = new LinkedHashMap<String, JSON>(initialCapacity, loadFactor);
  }

  @Override
  public final boolean isObject() {
    return true;
  }

  @Override
  public void encode(@NotNull final JSONEncoder encoder) {
    encoder.openObject();
    for(final Map.Entry<String, JSON> entry : values.entrySet()) {
      encoder.key(entry.getKey());
      entry.getValue().encode(encoder);
    }
    encoder.closeObject();
  }

  @NotNull
  @Override
  public final JSONObject asObject() {
    return this;
  }

  public int length() {
    return values.size();
  }

  public boolean contains(@NotNull final String key) {
    return values.containsKey(key);
  }

  @NotNull
  public JSON remove(@NotNull final String key) {
    final JSON value = values.remove(key);

    if(null == value) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    return value;
  }

  @NotNull
  public JSON get(@NotNull final String key) {
    final JSON value = values.get(key);

    if(null == value) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    return value;
  }

  public boolean getBoolean(@NotNull final String key) {
    final JSON value = values.get(key);

    if(null == value) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    final JSONBoolean bool = value.asBoolean();

    if(null == bool) {
      throw new JSONException("Invalid type for key \""+key+"\"");
    }

    return bool.value;
  }

  public byte getByte(@NotNull final String key) {
    return (byte)getDouble(key);
  }

  public short getShort(@NotNull final String key) {
    return (short)getDouble(key);
  }

  public int getInt(@NotNull final String key) {
    return (int)getDouble(key);
  }

  public long getLong(@NotNull final String key) {
    return (long)getDouble(key);
  }

  public float getFloat(@NotNull final String key) {
    return (float)getDouble(key);
  }

  public double getDouble(@NotNull final String key) {
    final JSON value = values.get(key);

    if(null == value) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    final JSONNumber number = value.asNumber();

    if(null == number) {
      throw new JSONException("Invalid type for key \""+key+"\"");
    }

    return number.value;
  }

  @Nullable
  public String getString(@NotNull final String key) {
    final JSON value = values.get(key);

    if(null == value) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    if(value.isNull()) {
      return null;
    }

    final JSONString string = value.asString();

    if(null == string) {
      throw new JSONException("Invalid type for key \""+key+"\"");
    }

    return string.value;
  }

  @Nullable
  public JSONArray getArray(@NotNull final String key) {
    final JSON value = values.get(key);

    if(null == value) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    if(value.isNull()) {
      return null;
    }

    final JSONArray array = value.asArray();

    if(null == array) {
      throw new JSONException("Invalid type for key \""+key+"\"");
    }

    return array;
  }

  @Nullable
  public JSONObject getObject(@NotNull final String key) {
    final JSON value = values.get(key);

    if(null == value) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    if(value.isNull()) {
      return null;
    }

    final JSONObject object = value.asObject();

    if(null == object) {
      throw new JSONException("Invalid type for key \""+key+"\"");
    }

    return object;
  }

  @NotNull
  public JSON opt(@NotNull final String key) {
    return opt(key, JSONObject.EMPTY);
  }

  public JSON opt(@NotNull final String key, final JSON defaultValue) {
    final JSON value = values.get(key);

    if(null == value || value.isNull()) {
      return defaultValue;
    }

    return value;
  }

  public boolean optBoolean(@NotNull final String key) {
    return optBoolean(key, false);
  }

  public boolean optBoolean(@NotNull final String key, final boolean defaultValue) {
    final JSON value = values.get(key);

    if(null == value) {
      return defaultValue;
    }

    final JSONBoolean bool = value.asBoolean();

    return null == bool ? defaultValue : bool.value;
  }

  public byte optByte(@NotNull final String key) {
    return optByte(key, (byte)0);
  }

  public byte optByte(@NotNull final String key, final byte defaultValue) {
    return (byte)optDouble(key, defaultValue);
  }

  public short optShort(@NotNull final String key) {
    return optShort(key, (short)0);
  }

  public short optShort(@NotNull final String key, final short defaultValue) {
    return (short)optDouble(key, defaultValue);
  }

  public int optInt(@NotNull final String key) {
    return optInt(key, 0);
  }

  public int optInt(@NotNull final String key, final int defaultValue) {
    return (int)optDouble(key, defaultValue);
  }

  public long optLong(@NotNull final String key) {
    return optLong(key, 0L);
  }

  public long optLong(@NotNull final String key, final long defaultValue) {
    return (long)optDouble(key, defaultValue);
  }

  public float optFloat(@NotNull final String key) {
    return optFloat(key, 0.0f);
  }

  public float optFloat(@NotNull final String key, final float defaultValue) {
    return (float)optDouble(key, defaultValue);
  }

  public double optDouble(@NotNull final String key) {
    return optDouble(key, 0.0);
  }

  public double optDouble(@NotNull final String key, final double defaultValue) {
    final JSON value = values.get(key);

    if(null == value) {
      return defaultValue;
    }

    final JSONNumber number = value.asNumber();

    return null == number ? defaultValue : number.value;
  }

  @NotNull
  public String optString(@NotNull final String key) {
    return optString(key, "");
  }

  public String optString(@NotNull final String key, final String defaultValue) {
    final JSON value = values.get(key);

    if(null == value) {
      return defaultValue;
    }

    final JSONString string = value.asString();

    return null == string ? defaultValue : string.value;
  }

  @NotNull
  public JSONArray optArray(@NotNull final String key) {
    return optArray(key, JSONArray.EMPTY);
  }

  public JSONArray optArray(@NotNull final String key, final JSONArray defaultValue) {
    final JSON value = values.get(key);

    if(null == value) {
      return defaultValue;
    }

    final JSONArray array = value.asArray();

    return null == array ? defaultValue : array;
  }

  @NotNull
  public JSONObject optObject(@NotNull final String key) {
    return optObject(key, EMPTY);
  }

  public JSONObject optObject(@NotNull final String key, final JSONObject defaultValue) {
    final JSON value = values.get(key);

    if(null == value) {
      return defaultValue;
    }

    final JSONObject object = value.asObject();

    return null == object ? defaultValue : object;
  }

  @NotNull
  public JSONObject put(@NotNull final String key, final boolean value) {
    return put(key, JSONBoolean.of(value));
  }

  @NotNull
  public JSONObject put(@NotNull final String key, final byte value) {
    return put(key, JSONNumber.of(value));
  }

  @NotNull
  public JSONObject put(@NotNull final String key, final short value) {
    return put(key, JSONNumber.of(value));
  }

  @NotNull
  public JSONObject put(@NotNull final String key, final int value) {
    return put(key, JSONNumber.of(value));
  }

  @NotNull
  public JSONObject put(@NotNull final String key, final long value) {
    return put(key, JSONNumber.of(value));
  }

  @NotNull
  public JSONObject put(@NotNull final String key, final float value) {
    return put(key, JSONNumber.of(value));
  }

  @NotNull
  public JSONObject put(@NotNull final String key, final double value) {
    return put(key, JSONNumber.of(value));
  }

  @NotNull
  public JSONObject put(@NotNull final String key, @Nullable final String value) {
    return put(key, null == value ? JSONNull.INSTANCE : JSONString.of(value));
  }

  @NotNull
  public JSONObject put(@NotNull final String key, @Nullable final JSON value) {
    values.put(key, convertNull(value));
    return this;
  }

  @NotNull
  public Iterator<String> keys() {
    return keySet().iterator();
  }

  @NotNull
  public Set<String> keySet() {
    return values.keySet();
  }

  @Override
  public String toString() {
    return JSON.stringify(this);
  }

  private static class EmptyJSONObject extends JSONObject {
    @Override
    public void encode(@NotNull final JSONEncoder encoder) {
      encoder.openObject().closeObject();
    }

    @Override
    public int length() {
      return 0;
    }

    @Override
    public boolean contains(@NotNull final String key) {
      return false;
    }

    @Override
    public boolean getBoolean(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Override
    public byte getByte(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Override
    public short getShort(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Override
    public int getInt(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Override
    public long getLong(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Override
    public float getFloat(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Override
    public double getDouble(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Nullable
    @Override
    public String getString(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Nullable
    @Override
    public JSONArray getArray(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Nullable
    @Override
    public JSONObject getObject(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @NotNull
    @Override
    public JSON get(@NotNull final String key) {
      throw new NoSuchElementException("No such element for key \""+key+'"');
    }

    @Override
    public boolean optBoolean(@NotNull final String key, final boolean defaultValue) {
      return defaultValue;
    }

    @Override
    public byte optByte(@NotNull final String key, final byte defaultValue) {
      return defaultValue;
    }

    @Override
    public short optShort(@NotNull final String key, final short defaultValue) {
      return defaultValue;
    }

    @Override
    public int optInt(@NotNull final String key, final int defaultValue) {
      return defaultValue;
    }

    @Override
    public long optLong(@NotNull final String key, final long defaultValue) {
      return defaultValue;
    }

    @Override
    public float optFloat(@NotNull final String key, final float defaultValue) {
      return defaultValue;
    }

    @Override
    public double optDouble(@NotNull final String key, final double defaultValue) {
      return defaultValue;
    }

    @NotNull
    @Override
    public String optString(@NotNull final String key, @NotNull final String defaultValue) {
      return defaultValue;
    }

    @Override
    public JSON opt(@NotNull final String key, final JSON defaultValue) {
      return defaultValue;
    }

    @NotNull
    @Override
    public JSONArray optArray(@NotNull final String key, @NotNull final JSONArray defaultValue) {
      return defaultValue;
    }

    @NotNull
    @Override
    public JSONObject optObject(@NotNull final String key, @NotNull final JSONObject defaultValue) {
      return defaultValue;
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, final boolean value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, final byte value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, final short value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, final int value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, final long value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, final float value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, final double value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, @Nullable final String value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public JSONObject put(@NotNull final String key, @Nullable final JSON value) {
      throw new UnsupportedOperationException("Cannot modify JSONObject.EMPTY");
    }

    @NotNull
    @Override
    public Iterator<String> keys() {
      return Iterators.emptyIterator();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
      return Collections.emptySet();
    }

    @Override
    public String toString() {
      return "{}";
    }
  }
}
