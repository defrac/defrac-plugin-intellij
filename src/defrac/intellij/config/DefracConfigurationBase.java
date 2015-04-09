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

package defrac.intellij.config;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class DefracConfigurationBase {
  protected String name;
  protected String identifier;
  @SerializedName("package") protected String package$;
  protected String main;
  protected String version;
  protected Boolean debug;
  @SerializedName("gen-sources") protected Boolean genSource;
  @SerializedName("gen-macros") protected Boolean genMacros;
  protected Boolean strictMode;
  @SuppressWarnings("MismatchedReadAndWriteOfArray")
  protected String[] lib;
  @SuppressWarnings("MismatchedReadAndWriteOfArray")
  protected String[] resources;
  protected EnvironmentConfig environment;
  protected JavaSettings java;
  protected MacroSettings macro;
  @SuppressWarnings("MismatchedReadAndWriteOfArray")
  protected String[] keep;

  @NotNull
  public String getName() {
    return nullToEmpty(name);
  }

  private static String nullToEmpty(@Nullable final String value) {
    return value == null ? "" : value;
  }
}
