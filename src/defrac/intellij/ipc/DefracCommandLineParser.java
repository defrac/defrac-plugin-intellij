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

package defrac.intellij.ipc;

import com.intellij.openapi.compiler.CompilerMessageCategory;
import org.jetbrains.annotations.NotNull;

/**
 */
public class DefracCommandLineParser {

  public static final class Message {
    @NotNull
    public final CompilerMessageCategory category;
    @NotNull
    public final String text;

    protected Message(@NotNull final CompilerMessageCategory category,
                      @NotNull final String text) {
      this.category = category;
      this.text = text;
    }

    public boolean isError() {
      return category == CompilerMessageCategory.ERROR;
    }
  }

  private static final int DEFAULT = 0;
  private static final int INFO = 1;
  private static final int DEBUG = 2;
  private static final int WARN = 4;
  private static final int ERROR = 3;

  public static final String INFO_PATTERN = "[info] ";
  public static final String DEBUG_PATTERN = "[debug] ";
  public static final String WARN_PATTERN = "[warn] ";
  public static final String ERROR_PATTERN = "[error] ";

  private int mode = DEFAULT;

  @NotNull
  public synchronized final Message parse(@NotNull final String text) {
    if(text.startsWith(INFO_PATTERN)) {
      mode = INFO;
      return parseInfo(text.substring(INFO_PATTERN.length()));
    }

    if(text.startsWith(DEBUG_PATTERN)) {
      mode = DEBUG;
      return parseDebug(text.substring(DEBUG_PATTERN.length()));
    }

    if(text.startsWith(WARN_PATTERN)) {
      mode = WARN;
      return parseWarning(text.substring(WARN_PATTERN.length()));
    }

    if(text.startsWith(ERROR_PATTERN)) {
      mode = ERROR;
      return parseError(text.substring(ERROR_PATTERN.length()));
    }

    return parse(mode, text);
  }

  @NotNull
  private Message parse(final int mode, final String text) {
    switch(mode) {
      case INFO:
        return parseInfo(text);
      case DEBUG:
        return parseDebug(text);
      case WARN:
        return parseWarning(text);
      case ERROR:
        return parseError(text);
      case DEFAULT:
        return parseText(text);
      default:
        throw new IllegalArgumentException("Illegal mode " + mode);
    }
  }

  @NotNull
  protected Message parseText(@NotNull final String text) {
    return new Message(CompilerMessageCategory.INFORMATION, text);
  }

  @NotNull
  protected Message parseInfo(@NotNull final String text) {
    return new Message(CompilerMessageCategory.INFORMATION, text);
  }

  @NotNull
  protected Message parseDebug(@NotNull final String text) {
    return new Message(CompilerMessageCategory.INFORMATION, text);
  }

  @NotNull
  protected Message parseWarning(@NotNull final String text) {
    return new Message(CompilerMessageCategory.WARNING, text);
  }

  @NotNull
  protected Message parseError(@NotNull final String text) {
    return new Message(CompilerMessageCategory.ERROR, text);
  }
}
