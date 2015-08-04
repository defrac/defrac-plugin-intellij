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
import defrac.json.JSON;
import defrac.json.JSONArray;
import defrac.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import static com.google.common.collect.Iterators.forArray;
import static com.google.common.collect.Iterators.toArray;
import static com.google.common.collect.Iterators.transform;

/**
 *
 */
public class DefracConfigBase {
  @NotNull
  JSON json;

  DefracConfigBase() {
    this(new JSONObject());
  }

  DefracConfigBase(@NotNull final JSON json) {
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
  public DefracConfigBase setTargets(@NotNull final DefracPlatform[] value) {
    if(json instanceof JSONObject) {
      final JSONArray array = new JSONArray();

      for(final DefracPlatform target : value) {
        if(target == null || target.isGeneric()) {
          continue;
        }

        array.push(target.name);
      }

      ((JSONObject)json).put("targets", array);
    }

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
    if(json instanceof JSONObject) {
      ((JSONObject)json).put(key, value);
    }

    return this;
  }

  @NotNull
  public String getName() {
    if(json instanceof JSONObject) {
      return ((JSONObject)json).optString("name", "Unknown");
    }

    return "";
  }
}
