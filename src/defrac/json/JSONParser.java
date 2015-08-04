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

import static defrac.json.JSONTokens.*;

/**
 *
 */
public final class JSONParser {
  // Implementation of a JSON parser according to ECMA-404
  // See http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf
  @NotNull
  private final JSONScanner scanner;

  private int currentToken = EOF;

  public JSONParser(@NotNull final JSONScanner scanner) {
    this.scanner = scanner;
    advance();
  }

  @NotNull
  public JSON parseJSON() throws JSONException {
    switch(currentToken) {
      case LCURL: return parseObject();
      case LBRAC: return parseArray();
      case STRING: {
        final String value = scanner.tokenValue();
        advance();
        return JSONString.of(value);
      }
      case NULL: advance(); return JSONNull.INSTANCE;
      case TRUE: advance(); return JSONBoolean.TRUE;
      case FALSE: advance(); return JSONBoolean.FALSE;
      case NUMBER:
        try {
          final String value = scanner.tokenValue();
          advance();
          return JSONNumber.of(value);
        } catch(final NumberFormatException numberFormatException) {
          // The scanner should already avoid any pitfalls here but
          // just to be safe, we do not want exceptions to leak
          // out of the JSON parsing that are not typed JSONException
          throw new JSONException(numberFormatException);
        }
      default:
        throw new JSONException("Unexpected token "+JSONTokens.tokenToString(currentToken));
    }
  }

  @NotNull
  public JSONObject parseObject() {
    expect(LCURL);
    final JSONObject result = new JSONObject();
    do {
      if(peek(STRING)) {
        final String key = scanner.tokenValue();
        advance();
        expect(COLON);
        final JSON value = parseJSON();
        result.put(key, value);
      }
    } while(poll(COMMA));
    expect(RCURL);
    return result;
  }

  @NotNull
  public JSONArray parseArray() {
    expect(LBRAC);
    final JSONArray result = new JSONArray();
    do {
      if(!peek(RBRAC)) {
        result.push(parseJSON());
      }
    } while(poll(COMMA));
    expect(RBRAC);
    result.trimToSize();
    return result;
  }

  @NotNull
  public JSONNumber parseNumber() {
    if(!peek(NUMBER)) {
      throw new JSONException("Unexpected token "+JSONTokens.tokenToString(currentToken));
    }

    try {
      final String value = scanner.tokenValue();
      advance();
      return JSONNumber.of(value);
    } catch(final NumberFormatException numberFormatException) {
      // The scanner should already avoid any pitfalls here but
      // just to be safe, we do not want exceptions to leak
      // out of the JSON parsing that are not typed JSONException
      throw new JSONException(numberFormatException);
    }
  }

  @NotNull
  public JSONString parseString() {
    if(!peek(STRING)) {
      throw new JSONException("Unexpected token "+JSONTokens.tokenToString(currentToken));
    }

    final String value = scanner.tokenValue();
    advance();
    return JSONString.of(value).asString(value);
  }

  @NotNull
  public JSONNull parseNull() {
    expect(NULL);
    return JSONNull.INSTANCE;
  }

  @NotNull
  public JSONBoolean parseBoolean() {
    if(poll(TRUE)) {
      return JSONBoolean.TRUE;
    } else if(poll(FALSE)) {
      return JSONBoolean.FALSE;
    } else {
      throw new JSONException("Unexpected token "+JSONTokens.tokenToString(currentToken));
    }
  }

  private void expect(final int tok) throws JSONException {
    if(!poll(tok)) {
      throw new JSONException("Unexpected token "+JSONTokens.tokenToString(currentToken));
    }
  }

  private boolean peek(final int token) {
    return (currentToken == token);
  }

  private boolean poll(final int token) {
    if(peek(token)) {
      advance();
      return true;
    }

    return false;
  }

  private void advance() {
    currentToken = nextToken();
  }

  private int nextToken() {
    int tok;
    do {
      tok = scanner.nextToken();
    } while(tok == WHITESPACE);
    return tok;
  }
}
