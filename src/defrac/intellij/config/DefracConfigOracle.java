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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracConfigOracle {
  private static final Splitter PATH_SPLITTER = Splitter.on('/').trimResults().omitEmptyStrings();

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
      result = facet.findFileRelativeToSettings(getDefaultNameOfXcodeProject());
    }

    return result;
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
    return lookup(fieldName, String.class, "");
  }

  @NotNull
  private String[] lookupStringArray(@NotNull final String fieldName) {
    return lookup(fieldName, String[].class, ArrayUtil.EMPTY_STRING_ARRAY);
  }

  private boolean lookupBoolean(@NotNull final String fieldName) {
    return lookup(fieldName, Boolean.class, false);
  }

  @NotNull
  private <A, B extends A> A lookup(
      @NotNull final String fieldPath,
      @NotNull Class<A> typeOfValue,
      @NotNull final B defaultValue) {
    try {
      A firstTry = extract(fieldPath, typeOfValue, localConfig);

      if(firstTry != null) {
        return firstTry;
      }

      A secondTry = extract(fieldPath, typeOfValue, globalConfig);

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
                        @NotNull final DefracConfig genericConfig) throws NoSuchFieldException, IllegalAccessException {
    final DefracConfigurationBase platformConfig =
        genericConfig.getPlatform(platform);

    if(platformConfig != null) {
      try {
        final A value = walkPath(fieldPath, typeOfValue, platformConfig);

        if(value != null) {
          return value;
        }
      } catch(final NoSuchFieldException noSuchFieldInPlatform) {
        // ignore
      }
    }

    return walkPath(fieldPath, typeOfValue, genericConfig);
  }


  @Nullable
  private <A> A walkPath(@NotNull final String fieldPath,
                         @NotNull final Class<A> typeOfValue,
                         @NotNull final Object config) throws NoSuchFieldException, IllegalAccessException {
    final Iterator<String> pathElements = PATH_SPLITTER.split(fieldPath).iterator();
    Object current = config;

    while(pathElements.hasNext()) {
      final String pathElement = pathElements.next();
      final Field field = getField(current, pathElement);

      current = field.get(current);

      if(current == null) {
        return null;
      }
    }

    return typeOfValue.cast(current);
  }

  @NotNull
  private Field getField(@NotNull Object config,
                         @NotNull String name) throws NoSuchFieldException {
    Class<?> klass = config.getClass();

    while(klass != Object.class) {
      try {
        return makeAccessible(klass.getField(name));
      } catch(final NoSuchFieldException ignored) {
        try {
          return makeAccessible(klass.getDeclaredField(name));
        } catch(final NoSuchFieldException alsoIgnored) {
          // nada
        }
      }
      klass = klass.getSuperclass();
    }

    throw new NoSuchFieldException("No such field: "+name);
  }

  @NotNull
  private static Field makeAccessible(@NotNull final Field field) {
    field.setAccessible(true);
    return field;
  }
}
