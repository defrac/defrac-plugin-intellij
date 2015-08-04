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

import java.util.Arrays;

/**
 *
 */
public final class JSONPrinter {
  @NotNull
  private final StringBuilder out;
  @NotNull
  private char[][] indents = {{}};
  private final int indentation;
  private final char indentChar;
  private int indent;
  private boolean newLine;
  private final boolean prettyPrint;

  public JSONPrinter() {
    this(new StringBuilder());
  }

  public JSONPrinter(@NotNull final StringBuilder out) {
    this(out, ' ', 0, false);
  }

  public JSONPrinter(final char indentChar) {
    this(new StringBuilder(), indentChar);
  }

  public JSONPrinter(@NotNull final StringBuilder out,
                     final char indentChar) {
    this(out, indentChar, 2, true);
  }

  public JSONPrinter(@NotNull final StringBuilder out,
                     final char indentChar,
                     final int indentation,
                     final boolean prettyPrint) {
    if(indentation < 0) {
      throw new IllegalArgumentException("indentation < 0");
    }

    this.out = out;
    this.indentChar = indentChar;
    this.indentation = indentation;
    this.prettyPrint = prettyPrint;
  }

  public void openObject() {
    println('{');
    if(prettyPrint) {
      pushIndent();
    }
  }

  public void closeObject() {
    if(prettyPrint) {
      popIndent();
    }
    print('}');
  }

  public void openArray() {
    println('[');
    if(prettyPrint) {
      pushIndent();
    }
  }

  public void closeArray() {
    if(prettyPrint) {
      popIndent();
    }
    print(']');
  }

  public void key(@NotNull final String value) {
    if(prettyPrint) {
      maybeIndent();
    }
    JSON.escapeJSON(out, value);
    out.append(':');
    space();
  }

  public void comma() {
    print(',');
    if(prettyPrint) {
      newLine();
    }
  }

  public void literal(final boolean value) {
    print(value ? "true" : "false");
  }

  public void literal(final byte value) {
    print(String.valueOf(value));
  }

  public void literal(final short value) {
    print(String.valueOf(value));
  }

  public void literal(final int value) {
    print(String.valueOf(value));
  }

  public void literal(final long value) {
    print(String.valueOf(value));
  }

  public void literal(final float value) {
    print(String.valueOf((double) value));
  }

  public void literal(final double value) {
    print(String.valueOf(value));
  }

  public void literal(@Nullable final String value) {
    if(prettyPrint) {
      maybeIndent();
    }
    JSON.escapeJSON(out, value);
  }

  public void literalNull() {
    print("null");
  }

  public void space() {
    if(prettyPrint) {
      print(' ');
    }
  }

  //

  private void println(char c) {
    print(c);
    if(prettyPrint) {
      newLine();
    }
  }

  private void print(char c) {
    if(prettyPrint) {
      maybeIndent();
    }

    out.append(c);
  }

  private void print(@NotNull final CharSequence chars) {
    if(prettyPrint) {
      maybeIndent();
    }

    out.append(chars);
  }

  //

  private void pushIndent() {
    indent++;
    checkIndentSize();
  }

  private void popIndent() {
    if(--indent < 0)
      throw new IllegalStateException("Indent cannot be smaller than zero");
  }

  public void newLine() {
    newLine = true;
    out.append('\n');
  }

  public void newLineOpt() {
    if(prettyPrint) {
      newLine = true;
      out.append('\n');
    }
  }

  @Override
  @NotNull
  public String toString() {
    return out.toString();
  }

  private void maybeIndent() {
    if(newLine) {
      printIndent();
      newLine = false;
    }
  }

  private void printIndent() {
    out.append(indents[indent]);
  }

  private void checkIndentSize() {
    if(indent >= indents.length) {
      final char[] spaces = new char[indentation * indent];
      Arrays.fill(spaces, indentChar);

      final char[][] newIndents = new char[indents.length + 1][];
      System.arraycopy(indents, 0, newIndents, 0, indents.length);

      indents = newIndents;
      indents[indent] = spaces;
    }
  }
}
