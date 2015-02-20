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

package defrac.intellij.sdk;

import com.google.common.collect.Lists;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import defrac.intellij.DefracBundle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public final class DefracVersion {
  @NotNull @NonNls public static final String LATEST = "Latest";
  @NotNull @NonNls public static final String CURRENT = "current";

  @Contract("!null -> !null")
  @Nullable
  public static String mapToCurrent(@Nullable final String value) {
    return LATEST.equals(value) ? CURRENT : value;
  }

  @NotNull public static final DefracVersion[] EMPTY_ARRAY = new DefracVersion[0];

  @NotNull public static final String FILE_LIB = DefracBundle.message("sdk.file.sdk.ver.lib");
  @NotNull public static final String FILE_NATIVE = DefracBundle.message("sdk.file.sdk.ver.native");

  @NotNull private static final String[] LIBRARIES = {
      /*"defrac.sdk.file.sdk.ver.lib.defrac",
      "defrac.sdk.file.sdk.ver.lib.defrac.a5d",
      "defrac.sdk.file.sdk.ver.lib.defrac.cpp",
      "defrac.sdk.file.sdk.ver.lib.defrac.ios",
      "defrac.sdk.file.sdk.ver.lib.defrac.jvm",
      "defrac.sdk.file.sdk.ver.lib.defrac.web",*/
      "sdk.file.sdk.ver.lib.runtime",
  };

  @NotNull private static final String[] ANNOTATIONS = {
      "sdk.file.sdk.ver.lib.annotations",
  };

  @NotNull private static final String[] MACROS = {
      "sdk.file.sdk.ver.lib.macro.a5d",
      "sdk.file.sdk.ver.lib.macro.cpp",
      "sdk.file.sdk.ver.lib.macro.ios",
      "sdk.file.sdk.ver.lib.macro.jvm",
      "sdk.file.sdk.ver.lib.macro.web",
  };

  @NotNull private static final String[] RUNTIME_JARS = {
      "sdk.file.sdk.ver.lib.defrac.jvm",
      "sdk.file.sdk.ver.lib.lwjgl",
      "sdk.file.sdk.ver.lib.audio.mp3",
      "sdk.file.sdk.ver.lib.audio.ogg",
      "sdk.file.sdk.ver.lib.audio.tritonus",
      "sdk.file.sdk.ver.lib.audio.jlayer",
      "sdk.file.sdk.ver.lib.audio.jorbis",
  };

  @NotNull
  private final String name;

  @NotNull
  private final File location;

  private final boolean isCurrent;

  public DefracVersion(@NotNull final String name,
                       @NotNull final File location) {
    this.isCurrent = CURRENT.equals(name);
    this.name = isCurrent ? LATEST : name;
    this.location = location;
  }

  public boolean isCurrent() {
    return isCurrent;
  }

  @NotNull
  public File getLocation() {
    return location;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @Nullable
  public VirtualFile getRoot() {
    return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(getLocation());
  }

  @Nullable
  public VirtualFile getLib() {
    final VirtualFile root = getRoot();
    return root == null ? null : root.findChild(FILE_LIB);
  }

  @Nullable
  public VirtualFile getNative() {
    final VirtualFile root = getRoot();
    return root == null ? null : root.findChild(FILE_NATIVE);
  }

  @NotNull
  public List<VirtualFile> getLibraries() {
    return librariesOf(LIBRARIES);
  }

  @NotNull
  public List<VirtualFile> getAnnotations() {
    return librariesOf(ANNOTATIONS);
  }

  @NotNull
  public List<VirtualFile> getMacros() {
    return librariesOf(MACROS);
  }

  @NotNull
  public List<VirtualFile> getRuntimeJars() {
    return librariesOf(RUNTIME_JARS);
  }

  @NotNull
  private List<VirtualFile> librariesOf(@NotNull final String[] names) {
    final VirtualFile lib = getLib();

    if(lib == null) {
      return Collections.emptyList();
    }

    final List<VirtualFile> result = Lists.newLinkedList();

    for(final String name : names) {
      final VirtualFile file = lib.findChild(DefracBundle.message(name));

      if(file == null) {
        continue;
      }

      final VirtualFile jarRoot =
          JarFileSystem.getInstance().findFileByPath(file.getPath()+JarFileSystem.JAR_SEPARATOR);

      if(jarRoot == null) {
        continue;
      }

      result.add(jarRoot);
    }

    return result;
  }
}
