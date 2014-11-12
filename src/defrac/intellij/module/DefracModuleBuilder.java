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

package defrac.intellij.module;

import defrac.intellij.DefracPlatform;
import defrac.intellij.util.DefaultSettings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static defrac.intellij.sdk.DefracSdkUtil.isDefracSdk;

/**
 *
 */
public final class DefracModuleBuilder extends ModuleBuilder implements SourcePathsBuilder {
  @Nullable
  private List<Pair<String, String>> sourcePaths;

  @Nullable
  private Set<DefracPlatform> platforms;

  @NotNull
  private String package$ = "foo.bar";

  @NotNull
  private String identifier = "foo";

  @NotNull
  private String version = "1.0";

  @Override
  public void setupRootModel(@NotNull final ModifiableRootModel rootModel) throws ConfigurationException {
    if(getModuleJdk() != null) {
      rootModel.setSdk(getModuleJdk());
    } else {
      rootModel.inheritSdk();
    }

    final ContentEntry contentEntry = doAddContentEntry(rootModel);

    if(contentEntry != null) {
      for(final Pair<String, String> sourcePathPair : getSourcePaths()) {
        final String sourcePath = sourcePathPair.getFirst();

        //noinspection ResultOfMethodCallIgnored
        new File(sourcePath).mkdirs();

        final VirtualFile sourceRoot =
            LocalFileSystem.getInstance().
                refreshAndFindFileByPath(FileUtil.toSystemIndependentName(sourcePath));

        if(null != sourceRoot) {
          //TODO(joa): how to add platform as scope of source root?
          contentEntry.addSourceFolder(sourceRoot, false, sourcePathPair.getSecond());
        }
      }

      writeDefaultSettings(contentEntry);
    }
  }

  private void writeDefaultSettings(@NotNull final ContentEntry contentEntry) throws ConfigurationException {
    final VirtualFile file = contentEntry.getFile();

    if(file == null) {
      return;
    }

    final File rootDir = new File(file.getPath());

    try {
      DefaultSettings.write(
          getName(),
          package$,
          identifier,
          version,
          getPlatforms(),
          new File(rootDir, "default.settings"));
    } catch(final IOException e) {
      throw new ConfigurationException("Unable to create default.settings");
    }
  }

  @Override
  public ModuleType getModuleType() {
    return DefracModuleType.getInstance();
  }

  public void setPlatforms(@NotNull final Set<DefracPlatform> value) {
    if(platforms == null) {
      platforms = Sets.newHashSet(value);
      return;
    }

    platforms.clear();
    platforms.addAll(value);
  }

  public Set<DefracPlatform> getPlatforms() {
    if(platforms != null) {
      return platforms;
    }

    return Collections.emptySet();
  }

  @Override
  @NotNull
  public List<Pair<String, String>> getSourcePaths() throws ConfigurationException {
    if(sourcePaths != null) {
      return sourcePaths;
    }

    final ArrayList<Pair<String, String>> paths = Lists.newArrayList();
    final String sourceRoot = getContentEntryPath()+File.separator+"src";
    final String macroRoot = getContentEntryPath()+File.separator+"macro";

    addAllPlatformsToPaths(paths, sourceRoot);
    addAllPlatformsToPaths(paths, macroRoot);

    return paths;
  }

  private void addAllPlatformsToPaths(@NotNull final List<Pair<String, String>> paths,
                                      @NotNull final String root) {
    //noinspection ResultOfMethodCallIgnored
    new File(root).mkdirs();

    for(final DefracPlatform platform : getPlatforms()) {
      final String path = root+File.separator+platform.applySuffix("java");
      //noinspection ResultOfMethodCallIgnored
      (new File(path)).mkdirs();
      paths.add(Pair.create(path, ""));
    }
  }

  @Override
  public void setSourcePaths(@Nullable final List<Pair<String, String>> value) {
    sourcePaths = value == null ? null : Lists.newArrayList(value);
  }

  @Override
  public void addSourcePath(@NotNull final Pair<String, String> pair) {
    if(sourcePaths == null) {
      sourcePaths = Lists.newArrayList();
    }

    sourcePaths.add(pair);
  }

  @Override
  public boolean isSuitableSdk(@Nullable final Sdk sdk) {
    return isDefracSdk(sdk);
  }

  @Override
  public boolean isSuitableSdkType(@Nullable final SdkTypeId sdkType) {
    return isDefracSdk(sdkType);
  }

  @Override
  public int getWeight() {
    return 100;
  }

  public void setPackage(@NotNull final String value) {
    this.package$ = value;
  }

  public void setIdentifier(@NotNull final String value) {
    this.identifier = value;
  }

  public void setVersion(@NotNull final String value) {
    version = value;
  }
}
