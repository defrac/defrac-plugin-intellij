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

import java.io.IOException;
import java.io.Reader;

import static defrac.json.JSONTokens.*;

/**
 *
 */
public final class JSONScanner {
  // Implementation of a JSON parser according to ECMA-404
  // See http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf
  //
  @NotNull
  private final Reader in;

  @NotNull
  private final StringBuilder buffer = new StringBuilder();

  private boolean hasChar;

  private int currentChar;

  public JSONScanner(@NotNull final Reader reader) {
    this.in = reader;
  }

  @NotNull
  public String tokenValue() {
    return buffer.toString();
  }

  public int nextToken() {
    try {
      final int c = nextChar();

      if(c == -1) {
        return EOF;
      } else if(c == '{') {
        advance();
        return LCURL;
      } else if(c == '}') {
        advance();
        return RCURL;
      } else if(c == '[') {
        advance();
        return LBRAC;
      } else if(c == ']') {
        advance();
        return RBRAC;
      } else if(c == ',') {
        advance();
        return COMMA;
      } else if(c == ':') {
        advance();
        return COLON;
      } else if(c == 'n') {
        advance();
        if(nextChar() == 'u') {
          advance();
          if(nextChar() == 'l') {
            advance();
            if(nextChar() == 'l') {
              advance();
              return NULL;
            }
          }
        }
        if(nextChar() == -1) {
          throw new JSONException("Unexpected end of input");
        } else {
          throw new JSONException("Unexpected token "+(char)nextChar());
        }
      } else if(c == 't') {
        advance();
        if(nextChar() == 'r') {
          advance();
          if(nextChar() == 'u') {
            advance();
            if(nextChar() == 'e') {
              advance();
              return TRUE;
            }
          }
        }
        if(nextChar() == -1) {
          throw new JSONException("Unexpected end of input");
        } else {
          throw new JSONException("Unexpected token "+(char)nextChar());
        }
      } else if(c == 'f') {
        advance();
        if(nextChar() == 'a') {
          advance();
          if(nextChar() == 'l') {
            advance();
            if(nextChar() == 's') {
              advance();
              if(nextChar() == 'e') {
                advance();
                return FALSE;
              }
            }
          }
        }
        if(nextChar() == -1) {
          throw new JSONException("Unexpected end of input");
        } else {
          throw new JSONException("Unexpected token "+(char)nextChar());
        }
      } else if(c == ' ' || c == '\t' || c == '\r' || c == '\n') {
        int cc;
        do {
          advance();
          cc = nextChar();
        } while(cc == ' ' || cc == '\t' || cc == '\r' || cc == '\n');
        return WHITESPACE;
      } else if(c == '"') {
        advance();
        return string();
      } else if(c == '-') {
        advance();
        return number(/*negative=*/true);
      } else if(c >= '0' && c <= '9') {
        return number(/*negative=*/false);
      } else {
        throw new JSONException("Unexpected token "+(char)nextChar());
      }
    } catch(final IOException ioException) {
      throw new JSONException(ioException);
    }
  }

  private int nextChar() throws IOException {
    if(!hasChar) {
      hasChar = true;
      currentChar = in.read();
    }

    return currentChar;
  }

  private void advance() {
    hasChar = false;
  }

  private int string() throws IOException {
    buffer.setLength(0); // clear the buffer

    int c = nextChar();

    while(c != '"') {
      if(c == '\\') {
        advance(); //consume '\'-character
        final int cc = nextChar();
        if(cc == '"') {
          buffer.append('"');
        } else if(cc == '\\') {
          buffer.append('\\');
        } else if(cc == '/') {
          buffer.append('/');
        } else if(cc == 'b') {
          buffer.append('\b');
        } else if(cc == 'f') {
          buffer.append('\f');
        } else if(cc == 'n') {
          buffer.append('\n');
        } else if(cc == 'r') {
          buffer.append('\r');
        } else if(cc == 't') {
          buffer.append('\t');
        } else if(cc == 'u') {
          advance(); //consume 'u'-character
          final int hex0 = nextChar();
          boolean isIllegalUnicodeEscape = true;
          if(isHexDigit(hex0)) {
            advance(); //consume first digit
            final int hex1 = nextChar();
            if(isHexDigit(hex1)) {
              advance(); //consume second digit
              final int hex2 = nextChar();
              if(isHexDigit(hex2)) {
                advance(); //consume third digit
                final int hex3 = nextChar();
                if(isHexDigit(hex3)) {
                  // do not consume fourth digit, we do it in the
                  // normal fall-through case
                  isIllegalUnicodeEscape = false;
                  buffer.append(
                      (char)hexToDecimal(hex0, hex1, hex2, hex3)
                  );
                }
              }
            }
          }
          if(isIllegalUnicodeEscape) {
            throw new JSONException("Illegal unicode escape sequence");
          }
        } else {
          throw new JSONException("Unexpected escape code "+(char)cc);
        }
      } else {
        buffer.append((char)c);
      }

      advance();
      c = nextChar();

      if(c == -1) {
        throw new JSONException("Unexpected end of input");
      }
    }

    // consume the "-character
    advance();

    return STRING;
  }

  private int number(final boolean isNegative) throws IOException {
    buffer.setLength(0); // clear the buffer

    // int part
    if(isNegative) {
      buffer.append('-');
    }

    int c = nextChar();

    if(c == '0') {
      buffer.append('0');
      advance();
      c = nextChar();
    } else if(c >= '1' && c <= '9') {
      buffer.append((char)c);
      advance();

      int nextDigit = nextChar();
      while(nextDigit >= '0' && nextDigit <= '9') {
        buffer.append((char)nextDigit);
        advance();
        nextDigit = nextChar();
      }
      c = nextDigit;
    } else {
      throw new JSONException("Unexpected token "+(char)c);
    }

    // fraction part
    if(c == '.') {
      buffer.append('.');
      advance();

      int nextDigit = nextChar();
      while(nextDigit >= '0' && nextDigit <= '9') {
        buffer.append((char)nextDigit);
        advance();
        nextDigit = nextChar();
      }
      c = nextDigit;
    }


    // exponent part
    if(c == 'e' || c == 'E') {
      buffer.append((char)c);

      advance();

      int cc = nextChar();

      if(cc == '-' || cc == '+') {
        buffer.append((char)cc);
        advance();
        cc = nextChar();
      }

      while(cc >= '0' && cc <= '9') {
        buffer.append((char)cc);
        advance();
        cc = nextChar();
      }
    }

    return NUMBER;
  }

  private static boolean isHexDigit(final int c) {
    return (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F');
  }

  private static int hexToDecimal(
      final int a, final int b, final int c, final int d) {
    // convert the hexadecimal digit abcd to its decimal
    // representation
    //
    // 'result << 4' is the same as 'result * 16'
    //
    // one could also write this as 16 * 16 * 16 * a + 16 * 16 * b + 16 * c + d
    // but in that case we have significantly more multiplications

    int
    result =                 hexDigitToDecimal(a);
    result = (result << 4) + hexDigitToDecimal(b);
    result = (result << 4) + hexDigitToDecimal(c);
    result = (result << 4) + hexDigitToDecimal(d);
    return result;
  }

  private static int hexDigitToDecimal(final int c) {
    // The invariant is, that this hex-digit is either in [A,F], [a,f] or [0,9]
    // which means the isHexDigit function returns 'true' for the given
    // character 'c'
    if(c >= 'A') {
      return 10 + (c - 'A');
    } else if(c >= 'a') {
      return 10 + (c - 'a');
    } else {
      return c - '0';
    }
  }
}
