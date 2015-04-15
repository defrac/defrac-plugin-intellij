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

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.android.AndroidSettings;
import defrac.intellij.config.ios.IOSSettings;
import defrac.intellij.config.jvm.JVMSettings;
import defrac.intellij.config.web.WebSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracConfig extends DefracConfigurationBase {
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

  @SuppressWarnings("MismatchedReadAndWriteOfArray")
  private String[] targets;
  private GenericSettings gen;
  private AndroidSettings android;
  private IOSSettings ios;
  private JVMSettings jvm;
  private WebSettings web;

  @Nullable
  public DefracConfigurationBase getPlatform(@NotNull final DefracPlatform platform) {
    switch(platform) {
      case ANDROID:
        return android;
      case GENERIC:
        return this;
      case IOS:
        return ios;
      case JVM:
        return jvm;
      case WEB:
        return web;
    }

    throw new IllegalArgumentException("Unknown platform " + platform);
  }

  @NotNull
  public DefracPlatform[] getTargets() {
    final ArrayList<DefracPlatform> platforms =
        Lists.newArrayListWithExpectedSize(targets.length);

    for(final String target : targets) {
      if(isNullOrEmpty(target)) {
        continue;
      }

      final DefracPlatform platform = DefracPlatform.byName(target);

      if(platform == null) {
        continue;
      }

      platforms.add(platform);
    }

    return platforms.toArray(new DefracPlatform[platforms.size()]);
  }

  @NotNull
  public DefracConfig setTargets(@NotNull final String[] value) {
    targets = value;
    return this;
  }

  @NotNull
  public DefracConfig setGenericSettings(@NotNull final GenericSettings value) {
    gen = value;
    return this;
  }

  @NotNull
  public DefracConfig setAndroidSettings(@NotNull final AndroidSettings value) {
    android = value;
    return this;
  }

  @NotNull
  public DefracConfig setIOSSettings(@NotNull final IOSSettings value) {
    ios = value;
    return this;
  }

  @NotNull
  public DefracConfig setJVMSettings(@NotNull final JVMSettings value) {
    jvm = value;
    return this;
  }

  @NotNull
  public DefracConfig setWebSettings(@NotNull final WebSettings value) {
    web = value;
    return this;
  }

  public void write(@NotNull final Supplier<OutputStream> supplier) throws IOException {
    BufferedWriter out = null;

    try {
      out = new BufferedWriter(new PrintWriter(supplier.get()));

      final Gson gson = new Gson();
      gson.toJson(this, out);
    } finally {
      Closeables.close(out, true);
    }
  }

  public DefracConfig() {
  }
}
