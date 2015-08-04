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
public final class JSONTokens {
  //////////////////////////////////////////////////////////////////////////////
  //                      TOKEN         ID    DESCRIPTION
  //////////////////////////////////////////////////////////////////////////////
  public static final int LCURL      = 0;   // {
  public static final int RCURL      = 1;   // }
  public static final int LBRAC      = 2;   // [
  public static final int RBRAC      = 3;   // ]
  public static final int COMMA      = 4;   // ,
  public static final int COLON      = 5;   // :
  public static final int TRUE       = 6;   // true
  public static final int FALSE      = 7;   // false
  public static final int NULL       = 8;   // null
  public static final int NUMBER     = 9;   // Numerical value
  public static final int STRING     = 10;  // "" or "value"
  public static final int EOF        = 11;  // EOF
  public static final int WHITESPACE = 12;  // Whitespace or linefeed
  //////////////////////////////////////////////////////////////////////////////

  @NotNull
  public static String tokenToString(final int token) {
    switch(token) {
      case LCURL: return "{";
      case RCURL: return "}";
      case LBRAC: return "[";
      case RBRAC: return "]";
      case COMMA: return ",";
      case COLON: return ":";
      case TRUE: return "TRUE";
      case FALSE: return "FALSE";
      case NULL: return "NULL";
      case NUMBER: return "NUMBER";
      case STRING: return "STRING";
      case EOF: return "EOF";
      case WHITESPACE: return "WHITESPACE";
      default: return "ILLEGAL";
    }
  }

  private JSONTokens() {}
}
