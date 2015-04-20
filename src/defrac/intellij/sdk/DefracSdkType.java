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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.JavaDependentSdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.AnnotationOrderRootType;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Consumer;
import defrac.intellij.DefracBundle;
import defrac.intellij.sdk.ui.NewDefracSdkDialog;
import icons.DefracIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static defrac.intellij.sdk.JdkUtil.isApplicableJdk;

/**
 *
 */
public final class DefracSdkType extends JavaDependentSdkType implements JavaSdkType {
  @NotNull public static final String NAME = DefracBundle.message("sdk.name");
  @NotNull public static final String DEFAULT_DOCUMENTATION_URL = DefracBundle.message("sdk.javadoc.default");

  public DefracSdkType() {
    super(NAME);
  }

  @Override
  public String getBinPath(@NotNull Sdk sdk) {
    final Sdk internalJavaSdk = getInternalJavaSdk(sdk);
    return internalJavaSdk == null ? null : JavaSdk.getInstance().getBinPath(internalJavaSdk);
  }

  @Override
  @Nullable
  public String getToolsPath(@NotNull Sdk sdk) {
    final Sdk jdk = getInternalJavaSdk(sdk);
    return jdk == null || jdk.getVersionString() == null
        ? null
        : JavaSdk.getInstance().getToolsPath(jdk);
  }

  @Override
  @Nullable
  public String getVMExecutablePath(@NotNull Sdk sdk) {
    final Sdk internalJavaSdk = getInternalJavaSdk(sdk);
    return internalJavaSdk == null
        ? null
        : JavaSdk.getInstance().getVMExecutablePath(internalJavaSdk);
  }

  @Override
  public String getVersionString(@NotNull Sdk sdk) {
    final Sdk internalJavaSdk = getInternalJavaSdk(sdk);
    return internalJavaSdk == null
        ? null
        : internalJavaSdk.getVersionString();
  }

  @Override
  public String getPresentableName() {
    return NAME;
  }

  @Override
  public String suggestSdkName(String currentSdkName, String sdkHome) {
    return NAME;
  }

  @NotNull
  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isValidSdkHome(String path) {
    return DefracSdkUtil.isValidSdkHome(path);
  }

  @Nullable
  @Override
  public String suggestHomePath() {
    return Iterators.getNext(suggestHomePaths().iterator(), null);
  }

  @Override
  public void showCustomCreateUI(final SdkModel sdkModel,
                                 final JComponent parentComponent,
                                 final Consumer<Sdk> sdkCreatedCallback) {
    customImpl: {
      final Collection<String> homePaths = suggestHomePaths();

      if(homePaths.isEmpty()) {
        break customImpl;
      }

      final String home = homePaths.iterator().next();
      final File homePath = new File(home);

      if(!homePath.isDirectory()) {
        break customImpl;
      }

      if(!checkDependency(sdkModel)) {
        if(Messages.showOkCancelDialog(parentComponent,
            getUnsatisfiedDependencyMessage(), "Cannot Create SDK",
            Messages.getWarningIcon()) != Messages.OK) {
          return;
        }

        if(fixDependency(sdkModel, sdkCreatedCallback) == null) {
          return;
        }
      }

      final String newSdkName = SdkConfigurationUtil.createUniqueSdkName(this, home, Arrays.asList(sdkModel.getSdks()));
      final ProjectJdkImpl newJdk = new ProjectJdkImpl(newSdkName, this);

      newJdk.setHomePath(home);
      sdkCreatedCallback.consume(newJdk);

      return;
    }

    super.showCustomCreateUI(sdkModel, parentComponent, sdkCreatedCallback);
  }

  @Override
  public Collection<String> suggestHomePaths() {
    final ArrayList<String> homePaths = Lists.newArrayListWithCapacity(2);
    final String defracHome = System.getenv("DEFRAC_HOME");

    if(!isNullOrEmpty(defracHome)) {
      homePaths.add(defracHome);
    }

    final String userHome = System.getProperty("user.home", "");

    if(!isNullOrEmpty(userHome)) {
      homePaths.add(userHome+File.separatorChar+".defrac"+File.separatorChar);
    }

    return homePaths;
  }

  @Override
  public String adjustSelectedSdkHome(@Nullable final String homePath) {
    if(homePath == null) {
      return null;
    }

    final File actualHome = new File(homePath, ".defrac");

    if(actualHome.isDirectory()) {
      return actualHome.getAbsolutePath();
    }

    return super.adjustSelectedSdkHome(homePath);
  }

  @SuppressWarnings("Contract")
  @Override
  public boolean setupSdkPaths(@NotNull final Sdk sdk, @NotNull final SdkModel sdkModel) {
    final List<String> javaSdks = new ArrayList<String>();
    final Sdk[] sdks = sdkModel.getSdks();

    // (1) search for applicable jdk
    for(final Sdk jdk : sdks) {
      if(isApplicableJdk(jdk)) {
        javaSdks.add(jdk.getName());
      }
    }

    if(javaSdks.isEmpty()){
      Messages.showErrorDialog(
          DefracBundle.message("sdk.error.jdkNotFound.message"),
          DefracBundle.message("sdk.error.jdkNotFound.title"));
      return false;
    }

    // (2) search fro valid defrac sdk
    final DefracSdkData sdkData = DefracSdkData.getSdkData(sdk);

    if(sdkData == null) {
      Messages.showErrorDialog(
          DefracBundle.message("sdk.error.parse.message"),
          DefracBundle.message("sdk.error.parse.title"));
      return false;
    }

    final DefracVersion[] versions = sdkData.getVersions();

    if(versions.length == 0) {
      Messages.showErrorDialog("No defrac version found. Please run \"defrac --force-update\" first.", "Defrac Not Installed");
      //TODO(joa): allow user to run from idea
      return false;
    }

    // (3) convert defrac versions to names, find <current>
    final ArrayList<String> versionNames = Lists.newArrayListWithCapacity(versions.length);
    DefracVersion currentVersion = null;

    for(final DefracVersion version : versions) {
      if(version.isCurrent()) {
        currentVersion = version;
      }

      versionNames.add(version.getName());
    }

    // (4) let user select jdk and defrac version
    final NewDefracSdkDialog dialog = new NewDefracSdkDialog(
        null, javaSdks, javaSdks.get(0),
        versionNames,
        currentVersion == null ? versionNames.get(0) : currentVersion.getName());

    dialog.show();

    if(!dialog.isOK()) {
      return false;
    }

    // (5) feed sdk
    final String name = javaSdks.get(dialog.getSelectedJavaSdkIndex());
    final Sdk jdk = sdkModel.findSdk(name);
    final DefracVersion version = versions[dialog.getSelectedDefracVersionIndex()];
    final String sdkName = DefracSdkUtil.chooseNameForNewLibrary(version);

    DefracSdkUtil.setupSdk(sdk, sdkName, sdks, version, jdk, /*addRoots=*/true);

    return true;
  }

  @Override
  public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel,
                                                                     @NotNull final SdkModificator sdkModificator) {
    return new DefracSdkConfigurable(sdkModel, sdkModificator);
  }

  @Override
  public void saveAdditionalData(@NotNull final SdkAdditionalData data, @NotNull final Element element) {
    if(data instanceof DefracSdkAdditionalData) {
      ((DefracSdkAdditionalData)data).save(element);
    }
  }

  @Override
  public SdkAdditionalData loadAdditionalData(@NotNull final Sdk currentSdk, @NotNull final Element additional) {
    return new DefracSdkAdditionalData(currentSdk, additional);
  }

  @Override
  @Nullable
  public Icon getIcon() {
    return DefracIcons.Defrac16x16;
  }

  @Override
  @Nullable
  public Icon getIconForAddAction() {
    return getIcon();
  }

  @Nullable
  @Override
  public String getDefaultDocumentationUrl(@Nullable final Sdk sdk) {
    return DEFAULT_DOCUMENTATION_URL;
  }

  @Override
  public boolean isRootTypeApplicable(@Nullable final OrderRootType type) {
    return
        type == OrderRootType.CLASSES ||
        type == OrderRootType.SOURCES ||
        type == JavadocOrderRootType.getInstance() ||
        type == AnnotationOrderRootType.getInstance();
  }

  @Nullable
  private static Sdk getInternalJavaSdk(@NotNull final Sdk sdk) {
    final SdkAdditionalData data = sdk.getSdkAdditionalData();
    return data instanceof DefracSdkAdditionalData
        ? ((DefracSdkAdditionalData)data).getJavaSdk()
        : null;
  }

  @NotNull
  public static DefracSdkType getInstance() {
    return checkNotNull(SdkType.findInstance(DefracSdkType.class));
  }
}
