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
public final class JSONEncoder {
  private static final int TYPE_ROOT   = 0x00010000;
  private static final int TYPE_OBJECT = 0x00020000;
  private static final int TYPE_ARRAY  = 0x00040000;
  private static final int MASK_EMPTY  = 0x00000001;

  @NotNull
  private final JSONPrinter out;
  private int[] state = {TYPE_ROOT, 0, 0, 0};
  private int stateIndex = 0;

  public JSONEncoder(@NotNull final JSONPrinter jsonPrinter) {
    this.out = jsonPrinter;
  }

  /**
   * Starts a new object, must be followed by {@link #closeObject()}
   *
   * <p><strong>Example:</strong>
   * <code><pre>
   * jsonEncoder.
   *   openObject().
   *   put("key", "value").
   *   put("anotherKey", "anotherValue").
   *   key("thirdKey").put("thirdValue").
   *   key("nestedObject").
   *     openObject().
   *     put("nestedKey", "value").
   *     closeObject().
   *   put("lastKey", "lastValue").
   *   closeObject();
   * </pre></code></p>
   *
   *
   *
   * @return The current encoder
   */
  @NotNull
  public JSONEncoder openObject() {
    if(insideArray()) {
      maybeAppend();
    }
    out.openObject();
    push(TYPE_OBJECT);
    return this;
  }

  /**
   * Closes an object, must be preceded by {@link #openObject()}
   *
   * @return The current encoder
   */
  @NotNull
  public JSONEncoder closeObject() {
    expectAndPop(TYPE_OBJECT);
    if(!currentlyEmpty()) {
      out.newLineOpt();
    }
    out.closeObject();
    return this;
  }

  /**
   * Starts a new array, must be followed by {@link #closeArray()}
   *
   * <p><strong>Example:</strong>
   * <code><pre>
   * jsonEncoder.
   *   openArray().
   *   put("value").
   *   put("anotherValue").
   *   closeArray();
   * </pre></code></p>
   * @return The current encoder
   */
  @NotNull
  public JSONEncoder openArray() {
    if(insideArray()) {
      maybeAppend();
    }
    out.openArray();
    push(TYPE_ARRAY);
    return this;
  }

  /**
   * Closes an array, must be preceded by {@link #openArray()}
   *
   * @return The current encoder
   */
  @NotNull
  public JSONEncoder closeArray() {
    expectAndPop(TYPE_ARRAY);
    if(!currentlyEmpty()) {
      out.newLineOpt();
    }
    out.closeArray();
    return this;
  }

  /**
   * Inserts a key for a value into an object
   *
   * <p>Calling key instead of one of the {@code put(key, value)} methods
   * makes sense when a child object should encode itself, since it does
   * not necessarily know that it is inside an object's context.
   *
   * <p><strong>Example:</strong>
   * <code><pre>
   * jsonEncoder.
   *   openObject().
   *   put("key1", knownValue);
   *
   * for(Map.Entry&lt;String, Child&gt; entry : children) {
   *   jsonEncoder.key(entry.getKey());
   *   entry.getValue().encodeJSON(jsonEncoder);
   * }
   *
   * jsonEncoder.
   *   closeObject();
   * </pre></code></p>
   * @param key The key of the value
   * @return The current encoder
   */
  @NotNull
  public JSONEncoder key(@NotNull final String key) {
    expectObject();
    maybeAppend();
    out.key(key);
    return this;
  }

  @NotNull
  public JSONEncoder put(@NotNull final String key, final boolean value) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(@NotNull final String key, final byte value) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(@NotNull final String key, final short value) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(@NotNull final String key, final int value) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(@NotNull final String key, final long value) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(@NotNull final String key, final double value) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(@NotNull final String key, final float value) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(@NotNull final String key, @Nullable final String value) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder putNull(@NotNull final String key) {
    expectObject();
    maybeAppend();
    out.key(key);
    out.literalNull();
    return this;
  }

  @NotNull
  public JSONEncoder put(final boolean value) {
    if(insideArray()) {
      maybeAppend();
    }
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(final byte value) {
    if(insideArray()) {
      maybeAppend();
    }
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(final short value) {
    if(insideArray()) {
      maybeAppend();
    }
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(final int value) {
    if(insideArray()) {
      maybeAppend();
    }
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(final long value) {
    if(insideArray()) {
      maybeAppend();
    }
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(final double value) {
    if(insideArray()) {
      maybeAppend();
    }
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(final float value) {
    if(insideArray()) {
      maybeAppend();
    }
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder put(@Nullable final String value) {
    if(insideArray()) {
      maybeAppend();
    }
    out.literal(value);
    return this;
  }

  @NotNull
  public JSONEncoder putNull() {
    if(insideArray()) {
      maybeAppend();
    }
    out.literalNull();
    return this;
  }

  /**
   * Creates and returns the final output string
   *
   * <p>A JSONException is thrown if an open object or array still exists
   *
   * @return The JSON data
   */
  @NotNull
  public String jsonString() {
    if(stateIndex > 0) {
      throw new JSONException("JSON has not been finished");
    }

    return out.toString();
  }

  private void maybeAppend() {
    if(currentlyEmpty()) {
      markNonEmpty();
    } else {
      out.comma();
    }
  }

  private void push(final int type) {
    ++stateIndex;
    if(stateIndex == state.length) {
      final int[] newState = new int[state.length << 1];
      System.arraycopy(state, 0, newState, 0, state.length);
      state = newState;
    }
    state[stateIndex] = type | MASK_EMPTY;

  }

  private int pop() {
    if(stateIndex < 0) {
      throw new JSONException("Unbalanced pop without push");
    }

    final int result = state[stateIndex];
    --stateIndex;
    return result;
  }

  private boolean currentlyEmpty() {
    return stateIndex < 1 || (state[stateIndex] & MASK_EMPTY) == MASK_EMPTY;
  }

  private void markNonEmpty() {
    if(stateIndex < 0) {
      return;
    }

    state[stateIndex] &= ~MASK_EMPTY;
  }

  private void expectObject() {
    if(stateIndex < 0 || ((state[stateIndex] & TYPE_OBJECT) != TYPE_OBJECT)) {
      throw new JSONException("Must be inside an object");
    }
  }

  private boolean insideArray() {
    if(stateIndex < 0) {
      return false;
    }

    final int current = state[stateIndex];
    return (current & TYPE_ARRAY) == TYPE_ARRAY;
  }

  private void expectAndPop(final int type) {
    final int current = pop();
    if((current & type) != type) {
      throw new JSONException("Illegal sequence");
    }
  }
}
