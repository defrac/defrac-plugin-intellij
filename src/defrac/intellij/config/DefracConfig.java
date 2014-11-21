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

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import defrac.intellij.DefracPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracConfig {
  public static DefracConfig fromJson(@NotNull final VirtualFile file) throws IOException {
    final Gson gson = new Gson();
    Reader reader = null;

    try {
      reader = new InputStreamReader(file.getInputStream());
      return gson.fromJson(reader, DefracConfig.class);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }

  public static DefracConfig fromJson(@NotNull final PsiFile file) throws IOException {
    final AtomicReference<IOException> exceptionRef = new AtomicReference<IOException>(null);
    final DefracConfig config = CachedValuesManager.getCachedValue(file, new CachedValueProvider<DefracConfig>() {
      @Nullable
      public Result<DefracConfig> compute() {

        final Gson gson = new Gson();
        Reader reader = null;

        try {
          reader = new InputStreamReader(file.getVirtualFile().getInputStream());
          return Result.create(
              gson.fromJson(reader, DefracConfig.class),
              file, PsiModificationTracker.MODIFICATION_COUNT);
        } catch(final IOException exception) {
          exceptionRef.set(exception);
          return null;
        } finally {
          Closeables.closeQuietly(reader);
        }
      }
    });

    final IOException exception = exceptionRef.get();

    if(exception != null) {
      assert config == null;
      throw exception;
    }

    return config;
  }

  private String name;
  @SerializedName("package") private String package$;
  private String identifier;
  private String main;
  private String version;
  @SuppressWarnings("MismatchedReadAndWriteOfArray") //false positive, this field is written by GSON
  private String[] targets;
  private boolean debug;

  @NotNull
  public String getName() {
    return nullToEmpty(name);
  }

  public String getPackage() {
    return nullToEmpty(package$);
  }

  public String getIdentifier() {
    return nullToEmpty(identifier);
  }

  public String getMain() {
    return nullToEmpty(main);
  }

  public String getVersion() {
    return nullToEmpty(version);
  }

  public DefracPlatform[] getTargets() {
    final ArrayList<DefracPlatform> platforms =
        Lists.newArrayListWithExpectedSize(targets.length);

    for(final String target : targets) {
      if(isNullOrEmpty(target)) {
        continue;
      }

      final DefracPlatform platform = DefracPlatform.byName(target);

      platforms.add(platform);
    }

    return platforms.toArray(new DefracPlatform[platforms.size()]);
  }

  public boolean isDebug() {
    return debug;
  }

  DefracConfig() {}

  private static String nullToEmpty(@Nullable final String value) {
    return value == null ? "" : value;
  }
}
