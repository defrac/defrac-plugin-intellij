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
@SuppressWarnings("unused,MismatchedReadAndWriteOfArray")
public class DefracConfigurationBase {
  protected String name;
  @SerializedName("package") protected String package$;
  protected String main;
  protected String version;
  protected Boolean debug;
  @SerializedName("gen-sources") protected Boolean genSource;
  @SerializedName("gen-macros") protected Boolean genMacros;
  protected Boolean strictMode;
  protected String[] lib;
  protected String[] resources;
  protected EnvironmentConfig environment;
  protected JavaSettings java;
  protected MacroSettings macro;
  protected String[] keep;
  protected String optimize;

  @NotNull
  public String getName() {
    return nullToEmpty(name);
  }

  @NotNull
  public DefracConfigurationBase setName(@NotNull final String value) {
    name = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setPackage(@NotNull final String value) {
    package$ = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setMain(@NotNull final String value) {
    main = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setVersion(@NotNull final String value) {
    version = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setDebug(final boolean value) {
    debug = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setGenSource(final boolean value) {
    genSource = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setGenMacros(final boolean value) {
    genMacros = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setStrictMode(final boolean value) {
    strictMode = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setLib(@NotNull final String[] value) {
    lib = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setEnvironmentConfig(@NotNull final EnvironmentConfig value) {
    environment = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setJavaSettings(@NotNull final JavaSettings value) {
    java = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setMacroSettings(@NotNull final MacroSettings value) {
    macro = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setKeep(@NotNull final String[] value) {
    keep = value;
    return this;
  }

  @NotNull
  public DefracConfigurationBase setResources(@NotNull final String[] value) {
    resources = value;
    return this;
  }

  private static String nullToEmpty(@Nullable final String value) {
    return value == null ? "" : value;
  }
}
