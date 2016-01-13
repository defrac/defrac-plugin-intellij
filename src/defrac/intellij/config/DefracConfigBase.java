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

import com.google.common.base.Function;
import defrac.intellij.DefracPlatform;
import defrac.json.JSONArray;
import defrac.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.collect.Iterators.*;

/**
 *
 */
public class DefracConfigBase {
  @NotNull
  JSONObject json;

  public DefracConfigBase() {
    this(new JSONObject());
  }

  public DefracConfigBase(@NotNull final JSONObject json) {
    this.json = json;
  }

  @NotNull
  public DefracConfigBase setName(@NotNull final String value) {
    return putString("name", value);
  }

  @NotNull
  public DefracConfigBase setPackage(@NotNull final String value) {
    return putString("package", value);
  }

  @NotNull
  public DefracConfigBase setVersion(@NotNull final String value) {
    return putString("version", value);
  }

  @NotNull
  public DefracConfigBase setMain(@NotNull final String value) {
    return putString("main", value);
  }

  @NotNull
  public DefracConfigBase setStrict(final boolean value) {
    json.put("strictMode", value);
    return this;
  }

  @NotNull
  public DefracConfigBase setMinify(final boolean value) {
    json.put("minify", value); // TODO: implement
    return this;
  }

  @NotNull
  public DefracConfigBase setDeployOnEmulator() {
    return setDeploy("emulator");
  }

  @NotNull
  public DefracConfigBase setDeployOnDevice() {
    return setDeploy("device");
  }

  @NotNull
  public DefracConfigBase setDeploy(@NotNull final String value) {
    json.put("deploy", value);
    return this;
  }

  @NotNull
  public DefracConfigBase setTargets(@NotNull final DefracPlatform[] value) {
    final JSONArray array = new JSONArray();

    for(final DefracPlatform target : value) {
      if(target == null || target.isGeneric()) {
        continue;
      }

      array.push(target.name);
    }

    json.put("targets", array);

    return this;
  }

  @NotNull
  public DefracConfigBase setTargets(@NotNull final String[] value) {
    return setTargets(
        toArray(transform(forArray(value), new Function<String, DefracPlatform>() {
          @Override
          public DefracPlatform apply(final String value) {
            return DefracPlatform.NAME_TO_PLATFORM.get(value);
          }
        }), DefracPlatform.class));
  }

  @NotNull
  private DefracConfigBase putString(@NotNull final String key, @NotNull final String value) {
    json.put(key, value);
    return this;
  }

  @Nullable
  private String getString(@NotNull final String key, @Nullable final String value) {
    return json.optString(key, value);
  }

  @NotNull
  public String getName() {
    return json.optString("name", "Unknown");
  }

  @NotNull
  public DefracConfigBase copy() {
    return new DefracConfigBase(json.copy());
  }

  @NotNull
  public JSONObject toJSON() {
    return json.copy();
  }

  @NotNull
  @Override
  public String toString() {
    return json.toString();
  }
}
