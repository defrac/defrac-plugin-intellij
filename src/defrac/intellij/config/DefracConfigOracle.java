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
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.json.JSON;
import defrac.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterators.*;

/**
 *
 */
public final class DefracConfigOracle {
  @NotNull
  private static final Splitter PATH_SPLITTER = Splitter.on('/').trimResults().omitEmptyStrings();

  @NotNull
  private static final Function<JSON, String> JSON_STRING_TO_STRING = new Function<JSON, String>() {
    @Override
    public String apply(@Nullable final JSON json) {
      return json == null
          ? null
          : json.stringValue();
    }
  };

  @NotNull
  private static final Function<JSON, String[]> JSON_ARRAY_TO_STRING_ARRAY = new Function<JSON, String[]>() {
    @Override
    public String[] apply(@Nullable final JSON json) {
      return json == null || !json.isArray()
          ? null
          : toArray(
              filter(
                transform(
                  checkNotNull(json.asArray()).iterator(),
                  JSON_STRING_TO_STRING
                ),
              Predicates.notNull()
            ), String.class);
    }
  };

  @NotNull
  private static final Function<JSON, DefracPlatform[]> JSON_ARRAY_TO_PLATFORM_ARRAY = new Function<JSON, DefracPlatform[]>() {
    @Override
    public DefracPlatform[] apply(@Nullable final JSON json) {
      final String[] strings = JSON_ARRAY_TO_STRING_ARRAY.apply(json);

      if(strings == null) {
        return null;
      }

      final ArrayList<DefracPlatform> platforms = Lists.newArrayListWithExpectedSize(strings.length);

      for(final String string : strings) {
        if("android".equalsIgnoreCase(string)) {
          platforms.add(DefracPlatform.ANDROID);
        } else if("ios".equalsIgnoreCase(string)) {
          platforms.add(DefracPlatform.IOS);
        } else if("jvm".equalsIgnoreCase(string)) {
          platforms.add(DefracPlatform.JVM);
        } else if("web".equalsIgnoreCase(string)) {
          platforms.add(DefracPlatform.WEB);
        }
      }

      return platforms.toArray(new DefracPlatform[platforms.size()]);
    }
  };

  @NotNull
  private static final Function<JSON, Boolean> JSON_BOOLEAN_TO_BOOLEAN = new Function<JSON, Boolean>() {
    @Override
    public Boolean apply(@Nullable final JSON json) {
      return "true".equals(JSON_STRING_TO_STRING.apply(json));
    }
  };

  public static DefracConfigOracle join(@NotNull final DefracPlatform platform,
                                        @NotNull final DefracConfig localConfig,
                                        @NotNull final DefracConfig globalConfig) {
    return new DefracConfigOracle(platform, localConfig, globalConfig);
  }

  @NotNull
  private final DefracPlatform platform;

  @NotNull
  private final DefracConfig localConfig;

  @NotNull
  private final DefracConfig globalConfig;

  private DefracConfigOracle(@NotNull final DefracPlatform platform,
                             @NotNull final DefracConfig localConfig,
                             @NotNull final DefracConfig globalConfig) {
    this.platform = platform;
    this.localConfig = localConfig;
    this.globalConfig = globalConfig;
  }

  public boolean isDebug() {
    return lookupBoolean("debug");
  }

  @NotNull
  public String[] getResources() {
    return lookupStringArray("resources");
  }

  @NotNull
  public DefracPlatform[] getTargets() {
    return lookupPlatformArray("targets");
  }

  @NotNull
  public String getPackage() {
    return lookupString("package");
  }

  @NotNull
  public String getName() {
    return lookupString("name");
  }

  @Nullable
  public VirtualFile getXCodeProject(@NotNull final DefracFacet facet) {
    final String build = lookupString("xcode/build");
    final String project = lookupString("xcode/project");

    VirtualFile result = null;

    if(!isNullOrEmpty(build)) {
      result = facet.findFileRelativeToSettings(build);
    }

    if(!isNullOrEmpty(project) && result == null) {
      result = facet.findFileRelativeToSettings(project);
    }

    if(result == null) {
      result = facet.findFileRelativeToSettings("target/ios/xcode/"+getDefaultNameOfXcodeProject());
    }

    return result;
  }

  public VirtualFile getIndexHtml(@NotNull final DefracFacet facet) {
    final String html = lookupString("html", "index.html");

    if(!isNullOrEmpty(html)) {
      return facet.findFileRelativeToSettings("target/web/"+html);
    }

    return null;
  }

  @NotNull
  private String getDefaultNameOfXcodeProject() {
    return getName()+".xcodeproj";
  }

  @NotNull
  public List<VirtualFile> getResources(@NotNull final DefracFacet facet) {
    final String[] resources = getResources();
    final ArrayList<VirtualFile> result = Lists.newArrayListWithCapacity(resources.length);

    for(final String resource : resources) {
      final VirtualFile file = facet.findFileRelativeToSettings(resource);

      if(file != null) {
        result.add(file);
      }
    }

    return result;
  }

  public String getMain() {
    return lookupString("main");
  }

  public String getBrowser() {
    return lookupString("browser");
  }

  @NotNull
  private String lookupString(@NotNull final String fieldName) {
    return lookupString(fieldName, "");
  }

  @NotNull
  private String lookupString(@NotNull final String fieldName, @NotNull final String defaultValue) {
    return lookup(fieldName, String.class, defaultValue, JSON_STRING_TO_STRING);
  }

  @NotNull
  private String[] lookupStringArray(@NotNull final String fieldName) {
    return lookup(fieldName, String[].class, ArrayUtil.EMPTY_STRING_ARRAY, JSON_ARRAY_TO_STRING_ARRAY);
  }

  @NotNull
  private DefracPlatform[] lookupPlatformArray(@NotNull final String fieldName) {
    return lookup(fieldName, DefracPlatform[].class, DefracPlatform.EMPTY_ARRAY, JSON_ARRAY_TO_PLATFORM_ARRAY);
  }

  private boolean lookupBoolean(@NotNull final String fieldName) {
    return lookup(fieldName, Boolean.class, false, JSON_BOOLEAN_TO_BOOLEAN);
  }

  @NotNull
  private <A, B extends A> A lookup(
      @NotNull final String fieldPath,
      @NotNull Class<A> typeOfValue,
      @NotNull final B defaultValue,
      @NotNull final Function<JSON, A> flattener) {
    try {
      A firstTry = extract(fieldPath, typeOfValue, flattener, localConfig.json);

      if(firstTry != null) {
        return firstTry;
      }

      A secondTry = extract(fieldPath, typeOfValue, flattener, globalConfig.json);

      if(secondTry != null) {
        return secondTry;
      }

      return defaultValue;
    } catch(final NoSuchFieldException e) {
      return defaultValue;
    } catch(final IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  private <A> A extract(@NotNull final String fieldPath,
                        @NotNull final Class<A> typeOfValue,
                        @NotNull final Function<JSON, A> flattener,
                        @NotNull final JSON genericConfig) throws NoSuchFieldException, IllegalAccessException {
    final JSON platformConfig =
        genericConfig.asObject(JSONObject.EMPTY).optObject(platform.name);

    if(platformConfig != JSONObject.EMPTY) {
      try {
        final A value = walkPath(fieldPath, typeOfValue, flattener, platformConfig.asObject(JSONObject.EMPTY));

        if(value != null) {
          return value;
        }
      } catch(final NoSuchFieldException noSuchFieldInPlatform) {
        // ignore
      }
    }

    return walkPath(fieldPath, typeOfValue, flattener, genericConfig.asObject(JSONObject.EMPTY));
  }


  @Nullable
  private <A> A walkPath(@NotNull final String fieldPath,
                         @NotNull final Class<A> typeOfValue,
                         @NotNull final Function<JSON, A> flattener,
                         @NotNull final JSONObject config) throws NoSuchFieldException, IllegalAccessException {
    final Iterator<String> pathElements = PATH_SPLITTER.split(fieldPath).iterator();
    JSON current = config;

    while(pathElements.hasNext()) {
      final String pathElement = pathElements.next();

      if(current instanceof JSONObject) {
        final JSONObject obj = (JSONObject)current;

        if(!obj.contains(pathElement)) {
          return null;
        }

        current = obj.get(pathElement);
      } else {
        return null;
      }
    }

    return typeOfValue.cast(flattener.apply(current));
  }
}
