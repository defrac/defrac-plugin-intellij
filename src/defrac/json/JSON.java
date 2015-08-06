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

import com.google.common.base.Function;
import com.google.common.io.Closeables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.StringReader;

/**
 * The JSON class is the base class of different JSON data types
 *
 * <p>The JSON format supports {@link defrac.json.JSONArray arrays},
 * {@link defrac.json.JSONBoolean booleans}, {@link defrac.json.JSONNumber numbers},
 * {@link defrac.json.JSONObject objects} and {@link defrac.json.JSONString strings}.
 * Each of these data types is implemented in a distinct class with methods specific
 * to the data type ({@link defrac.json.JSONArray} for instance provides access to its elements).
 *
 * <p>The JSON class is used to stringify or to parse data. The resulting type
 * of the various {@code parse} methods is always JSON because it is not known
 * what kind of data is parsed. It could be a simple string.
 *
 * <p>If the format of the data is known, it is possible to use the {@link #asObject()}
 * method for instance or to perform a simple Java cast.
 *
 * <p><strong>Example:</strong>
 * <code><pre>
 *   JSONObject object = JSON.parse(myJsonString).asObject();
 *   System.out.println(object.getString("foo"));
 * </pre></code>
 */
public abstract class JSON {
  @NotNull
  private static final String[] JSON_ESCAPE_TABLE = {
      "\\u0000", "\\u0001", "\\u0002", "\\u0003", "\\u0004", "\\u0005", "\\u0006", "\\u0007",
      "\\b"    , "\\t"    , "\\n"    , "\\u000b", "\\f"    , "\\r"    , "\\u000e", "\\u000f",
      "\\u0010", "\\u0011", "\\u0012", "\\u0013", "\\u0014", "\\u0015", "\\u0016", "\\u0017",
      "\\u0018", "\\u0019", "\\u001a", "\\u001b", "\\u001c", "\\u001d", "\\u001e", "\\u001f"
  };

  /**
   * Creates and returns a valid and JSON string for a given input
   *
   * <p>This method adheres to RFC 4627 and performs necessary
   * escaping of characters.
   *
   * <p>If the given input is null, the result will be the
   * empty string {@code ""}.
   *
   * <p>Please note that this method will quote the resulting string with
   * the necessary "-characters.
   *
   * @param input The unescaped input; may be null
   * @return An escaped JSON string according to RFC 4627
   * @see <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>
   */
  @NotNull
  static String escapeJSON(@Nullable String input) {
    if(null == input || input.isEmpty()) {
      return "\"\"";
    }

    final StringBuilder builder = new StringBuilder(input.length() + 2);
    escapeJSON(builder, input);
    return builder.toString();
  }

  static void escapeJSON(@NotNull final StringBuilder stringBuilder,
                         @Nullable String input) {
    if(null == input || input.isEmpty()) {
      stringBuilder.append("\"\"");
      return;
    }

    final char[] chars = input.toCharArray();

    stringBuilder.append('"');
    for(final char c : chars) {
      if(c == '\\' || c == '"') {
        // we have to encode either '\', '"', or '/'
        // so we prepend a '\'
        stringBuilder.append('\\').append(c);
      } else if(c < ' ') {
        // anything below 0x20 is encoded using the JSON_ESCAPE_TABLE
        // which will result in either \\u00XY or a predefined escape
        // code like \b, \t, \n, \f or \r
        stringBuilder.append(JSON_ESCAPE_TABLE[c]);
      } else {
        // a character without special escaping requirements
        stringBuilder.append(c);
      }
    }
    stringBuilder.append('"');
  }

  /** Function to convert from String to JSON */
  @NotNull
  public static final Function<String, JSON> STRING_TO_JSON =
      new Function<String, JSON>() {
        @Override
        public JSON apply(final String value) {
          return parse(value);
        }
      };

  /** Function to convert JSONNull to null */
  @NotNull
  public static final Function<JSON, JSON> JSON_TO_NULL =
      new Function<JSON, JSON>() {
        @Override
        public JSON apply(final JSON value) {
          return value == null || value.isNull() ? null : value;
        }
      };

  /**
   * Parses the given data into a JSON structure
   *
   * @param json The JSON data to parse
   * @return The JSON object
   * @throws JSONException If the syntax is invalid
   */
  @Nullable
  public static JSON parse(@NotNull final String json) throws JSONException {
    StringReader reader = null;

    try {
      reader = new StringReader(json);
      return parse(reader);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }


  /**
   * Parses the given data into a JSON structure
   *
   * <p>This method will not close the given reader.
   *
   * @param reader The JSON data to parse
   * @return The JSON object
   * @throws JSONException If the syntax is invalid
   */
  @Nullable
  public static JSON parse(@NotNull final Reader reader) throws JSONException {
    final JSONScanner scanner = new JSONScanner(reader);
    final JSONParser parser = new JSONParser(scanner);
    final JSON json = parser.parseJSON();
    return json.isNull() ? null : json;
  }

  /**
   * Converts the JSON object into a string representation
   *
   * <p>This method is not using pretty printing by default.
   *
   * @param json The JSON object to encode
   * @return The string representation of the JSON object
   * @throws JSONException If encoding failed due to an implementation error
   */
  @NotNull
  public static String stringify(@NotNull final JSON json) throws JSONException {
    return stringify(json, false);
  }

  /**
   * Converts the JSON object into a string representation
   *
   * <p>The output will contain new-lines and indentation if {@code prettyPrint}
   * is set to {@literal true}.
   *
   * @param json The JSON object to encode
   * @param prettyPrint Whether or not pretty printing should be used
   * @return The string representation of the JSON object
   * @throws JSONException If encoding failed due to an implementation error
   */
  @NotNull
  public static String stringify(@NotNull final JSON json, final boolean prettyPrint) throws JSONException {
    final StringBuilder builder = new StringBuilder();
    final JSONPrinter jsonPrinter = new JSONPrinter(builder, ' ', 2, prettyPrint);
    final JSONEncoder jsonEncoder = new JSONEncoder(jsonPrinter);
    json.encode(jsonEncoder);
    return jsonEncoder.jsonString();
  }

  JSON() {}

  /**
   * Whether or not this value is {@literal null}
   *
   * <p>JSON objects encode null values as an instance of {@link defrac.json.JSONNull}.
   * This is an implementation detail and the {@code isNull} method does not need to
   * be checked by the user.
   */
  public boolean isNull() {
    return false;
  }

  /**
   * Returns this JSON object as a JSONBoolean; null if it is not a JSONBoolean
   *
   * @return The object itself; {@literal null} if it is not a JSONBoolean
   */
  @Nullable
  public JSONBoolean asBoolean() {
    return null;
  }

  /**
   * Returns this JSON object as a JSONBoolean; {@code defaultValue} if it is not a JSONBoolean
   *
   * @return The object itself; {@code defaultValue} if it is not a JSONBoolean
   */
  @NotNull
  public final JSONBoolean asBoolean(final boolean defaultValue) {
    final JSONBoolean result = asBoolean();

    if(null == result) {
      return JSONBoolean.of(defaultValue);
    }

    return result;
  }

  /** Whether or not this object is a JSONBoolean */
  public boolean isBoolean() {
    return false;
  }

  /**
   * Returns this JSON object as a JSONNumber; null if it is not a JSONNumber
   *
   * @return The object itself; {@literal null} if it is not a JSONNumber
   */
  @Nullable
  public JSONNumber asNumber() {
    return null;
  }

  /**
   * Returns this JSON object as a JSONNumber; {@code defaultValue} if it is not a JSONNumber
   *
   * @return The object itself; {@code defaultValue} if it is not a JSONNumber
   */
  @NotNull
  public final JSONNumber asNumber(double defaultValue) {
    final JSONNumber result = asNumber();

    if(null == result) {
      return JSONNumber.of(defaultValue);
    }

    return result;
  }

  /** Whether or not this object is a JSONNumber */
  public boolean isNumber() {
    return false;
  }

  /**
   * Returns this JSON object as a JSONString; null if it is not a JSONString
   *
   * @return The object itself; {@literal null} if it is not a JSONString
   */
  @Nullable
  public JSONString asString() {
    return null;
  }

  /**
   * Returns this JSON object as a JSONString; {@code defaultValue} if it is not a JSONString
   *
   * @return The object itself; {@code defaultValue} if it is not a JSONString
   */
  @NotNull
  public JSONString asString(@NotNull final String defaultValue) {
    final JSONString result = asString();

    if(null == result) {
      return JSONString.of(defaultValue).asString(defaultValue);
    }

    return result;
  }

  /** Whether or not this object is a JSONString */
  public boolean isString() {
    return false;
  }

  /**
   * Returns this JSON object as a JSONArray; null if it is not a JSONArray
   *
   * @return The object itself; {@literal null} if it is not a JSONArray
   */
  @Nullable
  public JSONArray asArray() {
    return null;
  }

  /**
   * Returns this JSON object as a JSONArray; {@code defaultValue} if it is not a JSONArray
   *
   * @return The object itself; {@code defaultValue} if it is not a JSONArray
   */
  public JSONArray asArray(@NotNull final JSON[] defaultValue) {
    final JSONArray result = asArray();

    if(null == result) {
      final JSONArray r = new JSONArray(defaultValue.length);

      for(final JSON json : defaultValue) {
        r.push(json);
      }

      return r;
    }

    return result;
  }

  /**
   * Returns this JSON object as a JSONArray; {@code defaultValue} if it is not a JSONArray
   *
   * @return The object itself; {@code defaultValue} if it is not a JSONArray
   */
  @NotNull
  public JSONArray asArray(@NotNull final JSONArray defaultValue) {
    final JSONArray result = asArray();

    if(null == result) {
      return defaultValue;
    }

    return result;
  }

  /** Whether or not this object is a JSONArray */
  public boolean isArray() {
    return false;
  }

  /**
   * Returns this JSON object as a JSONObject; null if it is not a JSONObject
   *
   * @return The object itself; {@literal null} if it is not a JSONObject
   */
  @Nullable
  public JSONObject asObject() {
    return null;
  }

  /**
   * Returns this JSON object as a JSONObject; {@code defaultValue} if it is not a JSONObject
   *
   * @return The object itself; {@code defaultValue} if it is not a JSONObject
   */
  @NotNull
  public JSONObject asObject(@NotNull final JSONObject defaultValue) {
    final JSONObject result = asObject();

    if(null == result) {
      return defaultValue;
    }

    return result;
  }

  /** Whether or not this object is a JSONObject */
  public boolean isObject() {
    return false;
  }

  /**
   * Encodes this object using the given encoder
   *
   * @param encoder The encoder to use
   */
  public abstract void encode(@NotNull final JSONEncoder encoder);

  /**
   * Converts this object to a number and returns its double value
   * @return This object's double value; {@literal 0.0} if it can't be converted to a number
   */
  public double doubleValue() {
    return asNumber(0).doubleValue();
  }

  /**
   * Converts this object to a number and returns its float value
   * @return This object's float value; {@literal 0.0f} if it can't be converted to a number
   */
  public float floatValue() {
    return asNumber(0).floatValue();
  }

  /**
   * Converts this object to a number and returns its long value
   * @return This object's long value; {@literal 0L} if it can't be converted to a number
   */
  public long longValue() {
    return asNumber(0).longValue();
  }

  /**
   * Converts this object to a number and returns its integer value
   * @return This object's integer value; {@literal 0} if it can't be converted to a number
   */
  public int intValue() {
    return asNumber(0).intValue();
  }

  /**
   * Converts this object to a number and returns its short value
   * @return This object's short value; {@literal 0} if it can't be converted to a number
   */
  public short shortValue() {
    return asNumber(0).shortValue();
  }

  /**
   * Converts this object to a number and returns its char value
   * @return This object's char value; {@literal '0'} if it can't be converted to a number
   */
  public char charValue() {
    return asNumber(0).charValue();
  }

  /**
   * Converts this object to a number and returns its byte value
   * @return This object's byte value; {@literal 0} if it can't be converted to a number
   */
  public byte byteValue() {
    return asNumber(0).byteValue();
  }

  /**
   * Converts this object to a boolean and returns its value
   * @return This object's boolean value; {@literal false} if it can't be converted to a boolean
   */
  public boolean booleanValue() {
    return asBoolean(false).value;
  }

  /**
   * Converts this object to a String and returns its value
   * @return This object's String value; {@literal ""} if it can't be converted to a boolean
   */
  @NotNull
  public String stringValue() {
    return asString("").value;
  }

  /**
   * Converts the given nullable value to a JSON object
   *
   * @param value The value to convert
   * @return {@link JSONNull#INSTANCE} if {@literal null} is given; otherwise the value
   */
  @NotNull
  static JSON convertNull(@Nullable JSON value) {
    return value == null ? JSONNull.INSTANCE : value;
  }
}
