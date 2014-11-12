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

import defrac.intellij.DefracBundle;
import com.google.common.collect.Lists;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public final class DefracSdkData {
  @NotNull private static final List<SoftReference<DefracSdkData>> instances = Lists.newArrayList();
  @NotNull public static final String FILE_SDK = DefracBundle.message("defrac.sdk.file.sdk");

  @Nullable
  public static DefracSdkData getSdkData(@NotNull final File sdkLocation) {
    return getSdkData(sdkLocation, false);
  }

  @Nullable
  public static DefracSdkData getSdkData(@NotNull final File sdkLocation, final boolean forceReparse) {
    final File canonicalLocation =
        new File(FileUtil.toCanonicalPath(sdkLocation.getPath()));

    if(!forceReparse) {
      for(final Iterator<SoftReference<DefracSdkData>> iterator = instances.iterator(); iterator.hasNext();) {
        final DefracSdkData sdkData = iterator.next().get();

        // Lazily remove stale soft references
        if(sdkData == null) {
          iterator.remove();
          continue;
        }

        if(FileUtil.filesEqual(sdkData.getLocation(), canonicalLocation)) {
          return sdkData;
        }
      }
    }

    if(!DefracSdkUtil.isValidSdkHome(canonicalLocation)) {
      return null;
    }

    final DefracSdkData sdkData = new DefracSdkData(canonicalLocation);
    instances.add(0, new SoftReference<DefracSdkData>(sdkData));
    return instances.get(0).get();
  }

  @Nullable
  public static DefracSdkData getSdkData(@NotNull final String sdkPath) {
    final File file = new File(FileUtil.toSystemDependentName(sdkPath));
    return getSdkData(file);
  }

  @Nullable
  public static DefracSdkData getSdkData(@NotNull final Sdk sdk) {
    final String sdkHomePath = sdk.getHomePath();
    return sdkHomePath == null
        ? null
        : getSdkData(sdk.getHomePath());
  }

  @Nullable
  public static DefracSdkData getSdkData(@NotNull final Project project) {
    final Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
    return sdk == null
        ? null
        : getSdkData(sdk);
  }

  @Nullable
  public static DefracSdkData getSdkData(@NotNull final Module module) {
    return getSdkData(module.getProject());
  }

  @NotNull
  private final File location;

  private DefracSdkData(@NotNull File location) {
    this.location = location;
  }

  @NotNull
  public File getLocation() {
    return location;
  }

  @NotNull
  public DefracVersion[] getVersions() {
    final File sdk = new File(location, FILE_SDK);

    if(!sdk.isDirectory()) {
      return DefracVersion.EMPTY_ARRAY;
    }

    final LinkedList<DefracVersion> versions = Lists.newLinkedList();
    final File[] files = sdk.listFiles();

    if(files == null) {
      return DefracVersion.EMPTY_ARRAY;
    }

    for(final File file : files) {
      if(!file.isDirectory()) {
        continue;
      }

      versions.add(new DefracVersion(file.getName(), file));
    }

    return versions.toArray(new DefracVersion[versions.size()]);
  }
}
