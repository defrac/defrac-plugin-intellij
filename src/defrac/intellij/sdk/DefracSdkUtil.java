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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracSdkUtil {
  @NotNull public static final String FILE_LAUNCHER = DefracBundle.message("sdk.file.launcher");
  @NotNull public static final String FILE_SDK = DefracBundle.message("sdk.file.sdk");
  @NotNull public static final String FILE_SDK_CURRENT = DefracBundle.message("sdk.file.sdk.current");
  @NotNull public static final String FILE_SDK_CURRENT_VERSION = DefracBundle.message("sdk.file.sdk.current.version");

  public static boolean isValidSdkHome(@Nullable final String path) {
    return !isNullOrEmpty(path) && isValidSdkHome(new File(path));
  }

  @SuppressWarnings("RedundantIfStatement")
  public static boolean isValidSdkHome(@Nullable final File home) {
    if(home == null || !home.isDirectory()) {
      return false;
    }

    final File launcherJar = new File(home, FILE_LAUNCHER);

    if(!launcherJar.canRead()) {
      return false;
    }

    final File sdk = new File(home, FILE_SDK);

    if(!sdk.isDirectory()) {
      return false;
    }

    final File currentSdk = new File(sdk, FILE_SDK_CURRENT);

    if(!currentSdk.isDirectory()) {
      return false;
    }

    final File version = new File(currentSdk, FILE_SDK_CURRENT_VERSION);

    if(!version.canRead()) {
      return false;
    }

    return true;
  }

  private DefracSdkUtil() {}

  public static void setupSdk(@NotNull final Sdk defracSdk,
                              @NotNull final String sdkName,
                              @NotNull final Sdk[] allSdks,
                              @NotNull final DefracVersion defracVersion,
                              @Nullable final Sdk javaSdk,
                              final boolean addRoots) {
    final DefracSdkAdditionalData data = new DefracSdkAdditionalData(defracSdk, javaSdk);
    final String name = SdkConfigurationUtil.createUniqueSdkName(sdkName, Arrays.asList(allSdks));
    final SdkModificator sdkModificator = defracSdk.getSdkModificator();

    data.setDefracVersion(defracVersion);
    sdkModificator.setName(name);

    if(javaSdk != null) {
      sdkModificator.setVersionString(javaSdk.getVersionString());
    }

    sdkModificator.setSdkAdditionalData(data);

    if(addRoots) {
      sdkModificator.removeAllRoots();

      attachLibraries(defracVersion, sdkModificator);
      attachAnnotations(defracVersion, sdkModificator);
    }

    sdkModificator.commitChanges();
  }

  private static void attachLibraries(@Nullable final DefracVersion defracVersion,
                                      @NotNull final SdkModificator sdkModificator) {
    if(defracVersion == null) {
      return;
    }

    for(final OrderRoot orderRoot : getLibraryRootsForVersion(defracVersion)) {
      sdkModificator.addRoot(orderRoot.getFile(), orderRoot.getType());
    }
  }

  private static void attachAnnotations(@Nullable final DefracVersion defracVersion,
                                        @NotNull final SdkModificator sdkModificator) {
    if(defracVersion == null) {
      return;
    }

    for(final VirtualFile library : defracVersion.getAnnotations()) {
      sdkModificator.addRoot(library, AnnotationOrderRootType.getInstance());
    }
  }

  @NotNull
  private static List<OrderRoot> getLibraryRootsForVersion(@Nullable final DefracVersion defracVersion) {
    if(defracVersion == null) {
      return Collections.emptyList();
    }

    final List<OrderRoot> result = Lists.newLinkedList();

    for(final VirtualFile library : defracVersion.getLibraries()) {
      result.add(new OrderRoot(library, OrderRootType.CLASSES));
    }

    for(final DefracPlatform platform : DefracPlatform.values()) {
      final VirtualFile javadoc =
          VirtualFileManager.getInstance().
              findFileByUrl(getJavadocUrl(defracVersion, platform));

      if(null != javadoc) {
        result.add(new OrderRoot(javadoc, JavadocOrderRootType.getInstance()));
      }
    }

    return result;
  }

  @NotNull
  private static String getJavadocUrl(@NotNull final DefracVersion defracVersion,
                                      @NotNull final DefracPlatform defracPlatform) {
    return DefracBundle.message(
        "sdk.javadoc.withVersion",
        defracVersion.isCurrent() ? "latest" : defracVersion.getName(),
        defracPlatform.name);
  }

  public static boolean isDefracSdk(@Nullable final Sdk sdk) {
    return sdk != null && sdk.getSdkType() == DefracSdkType.getInstance();
  }

  public static boolean isDefracSdk(@Nullable final SdkTypeId sdkTypeId) {
    return sdkTypeId != null && sdkTypeId.getName().equals(DefracSdkType.NAME);
  }

  @NotNull
  public static String chooseNameForNewLibrary(@NotNull final DefracVersion version) {
    return "defrac ("+version.getName()+')';
  }

  @Nullable
  public static DefracVersion findVersion(@Nullable final Sdk sdk, @Nullable final String versionName) {
    if(!isDefracSdk(sdk) || isNullOrEmpty(versionName)) {
      return null;
    }

    final File sdkDir = new File(sdk.getHomePath(), FILE_SDK);

    if(!sdkDir.isDirectory()) {
      return null;
    }

    final File location = new File(sdkDir, DefracVersion.mapToCurrent(versionName));

    if(!location.isDirectory()) {
      return null;
    }

    return new DefracVersion(versionName, location);
  }

  @NotNull
  public static ProjectSdksModel getSdkModel() {
    // TODO(joa): this can't be correct
    return getSdkModel(ProjectManager.getInstance().getOpenProjects()[0]);
  }

  @NotNull
  public static ProjectSdksModel getSdkModel(@NotNull final Project project) {
    return ProjectStructureConfigurable.
        getInstance(project).getProjectJdksModel();
  }

  public static boolean isInDefracSdk(@NotNull final PsiElement element) {
    final PsiFile psiFile = element.getContainingFile();

    if(psiFile == null) {
      return false;
    }

    final VirtualFile file = psiFile.getVirtualFile();

    if(file == null) {
      return false;
    }

    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(element.getProject()).getFileIndex();
    final List<OrderEntry> entries = projectFileIndex.getOrderEntriesForFile(file);

    for(final OrderEntry entry : entries) {
      if(entry instanceof JdkOrderEntry) {
        final Sdk sdk = ((JdkOrderEntry)entry).getJdk();

        if(isDefracSdk(sdk)) {
          return true;
        }
      }
    }

    return false;
  }

  @NotNull
  public static final Condition<SdkTypeId> IS_DEFRAC_VERSION = new Condition<SdkTypeId>() {
    @Override
    public boolean value(final SdkTypeId sdkTypeId) {
      return isDefracSdk(sdkTypeId);
    }
  };
}
