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
import com.google.common.base.Suppliers;
import com.google.common.io.Closeables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import defrac.intellij.DefracPlatform;
import defrac.json.JSON;
import defrac.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collections;

/**
 *
 */
public final class DefracConfig extends DefracConfigBase {
  @NotNull
  public static DefracConfig fromJson(@NotNull final PsiFile file) throws IOException {
    return new DefracConfig(ConfigCache.getInstance().get(file.getVirtualFile()));
  }

  @NotNull
  public static DefracConfig fromJson(@NotNull final VirtualFile file) throws IOException {
    return new DefracConfig(ConfigCache.getInstance().get(file));
  }

  @NotNull
  public static DefracConfig fromJson(@NotNull final String url) throws IOException {
    return new DefracConfig(ConfigCache.getInstance().get(url));
  }

  public void commit(@NotNull final Project project) throws IOException {
    final File baseDir = VfsUtilCore.virtualToIoFile(project.getBaseDir());
    final File settingsFile = new File(baseDir, "default.settings");

    write(Suppliers.<OutputStream>ofInstance(new FileOutputStream(settingsFile)));

    LocalFileSystem.getInstance().refreshIoFiles(Collections.singletonList(settingsFile));
  }

  public void write(@NotNull final Supplier<OutputStream> supplier) throws IOException {
    BufferedWriter out = null;

    try {
      out = new BufferedWriter(new PrintWriter(supplier.get()));
      out.write(JSON.stringify(json, /*prettyPrint=*/true));
    } finally {
      Closeables.close(out, true);
    }
  }

  public DefracConfig() {
    super();
  }

  public DefracConfig(@NotNull final JSON json) {
    super(json);
  }

  @Nullable
  public DefracConfigBase getOrCreatePlatform(final DefracPlatform platform) {
    if(platform.isGeneric() || !(json instanceof JSONObject)) {
      return null;
    }

    final JSONObject obj = (JSONObject)json;

    if(obj.contains(platform.name)) {
      final JSON platformObject = obj.get(platform.name);

      if(platformObject.isObject()) {
        return new DefracConfigBase(platformObject);
      }

      return null;
    } else {
      final JSONObject platformObject = new JSONObject();
      obj.put(platform.name, platformObject);
      return new DefracConfigBase(platformObject);
    }
  }
}
