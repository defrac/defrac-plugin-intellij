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

package defrac.intellij.projectWizard;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.java.LanguageLevel;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfig;
import defrac.intellij.fileTemplate.DefracFileTemplateProvider;
import defrac.intellij.sdk.DefracSdkType;
import defrac.intellij.util.DefracCommandLineBuilder;
import icons.DefracIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 */
public abstract class DefracModuleBuilder extends ModuleBuilder {
  @NotNull
  private static final Logger LOG = Logger.getInstance(DefracModuleBuilder.class.getName());

  public static final class Generic extends DefracModuleBuilder {
    @Override
    public String getBuilderId() {
      return "defrac.generic";
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull final WizardContext wizardContext,
                                                @NotNull final ModulesProvider modulesProvider) {
      return new ModuleWizardStep[]{
          new GenericModuleWizardStep(this)
      };
    }

    @Override
    public void createTemplate(@NotNull final Project project,
                               @NotNull final File baseDir,
                               @NotNull final String packageName,
                               @NotNull final String mainScreenName,
                               final boolean isLambdaSupported) throws IOException {
      final String packagePath = packageName.replace('.', File.separatorChar);

      final File genericSrcDir = new File(baseDir, "src" + File.separator + "java");
      final File genericPackageDir = new File(genericSrcDir, packagePath);

      write(project,
          new File(genericPackageDir, mainScreenName + ".java"),
          isLambdaSupported
              ? DefracFileTemplateProvider.MAIN_SCREEN_8
              : DefracFileTemplateProvider.MAIN_SCREEN
          , packageName, mainScreenName, getApplicationName());

      write(project,
          new File(genericPackageDir, "DisplayListController.java"),
          isLambdaSupported
              ? DefracFileTemplateProvider.DISPLAY_LIST_CONTROLLER_8
              : DefracFileTemplateProvider.DISPLAY_LIST_CONTROLLER
          , packageName, mainScreenName, getApplicationName());

      createMain(androidSupported, project, baseDir, packagePath, packageName, mainScreenName, DefracPlatform.ANDROID, DefracFileTemplateProvider.MAIN_ANDROID);
      createMain(iosSupported, project, baseDir, packagePath, packageName, mainScreenName, DefracPlatform.IOS, DefracFileTemplateProvider.MAIN_IOS);

      if(jvmSupported) {
        final File jvmSrcDir = new File(baseDir, "src" + File.separator + "java." + DefracPlatform.JVM.name);
        final File jvmPackageDir = new File(jvmSrcDir, packagePath);

        write(project,
            new File(jvmPackageDir, "Main.java"),
            isLambdaSupported
                ? DefracFileTemplateProvider.MAIN_JVM_8
                : DefracFileTemplateProvider.MAIN_JVM,
            packageName, mainScreenName, getApplicationName());
      }

      createMain(webSupported, project, baseDir, packagePath, packageName, mainScreenName, DefracPlatform.WEB, DefracFileTemplateProvider.MAIN_WEB);
    }

    private void createMain(final boolean supported,
                            @NotNull final Project project,
                            @NotNull final File baseDir,
                            @NotNull final String packagePath,
                            @NotNull final String packageName,
                            @NotNull final String mainScreenName,
                            @NotNull final DefracPlatform platform,
                            @NotNull final String template) throws IOException {
      if(supported) {
        final File srcDir = new File(baseDir, "src" + File.separator + "java." + platform.name);
        final File packageDir = new File(srcDir, packagePath);

        write(project,
            new File(packageDir, "Main.java"), template, packageName, mainScreenName, getApplicationName());
      }
    }
  }

  public static final class IOS extends DefracModuleBuilder {
    @Override
    public String getBuilderId() {
      return "defrac.ios";
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull final WizardContext wizardContext,
                                                @NotNull final ModulesProvider modulesProvider) {
      return new ModuleWizardStep[]{
          new IOSModuleWizardStep(this)
      };
    }

    @Override
    public void createTemplate(@NotNull final Project project,
                               @NotNull final File baseDir,
                               @NotNull final String packageName,
                               @NotNull final String mainScreenName,
                               final boolean isLambdaSupported) throws IOException {
      final File sourceDir = new File(baseDir, "src" + File.separator + "java.ios");
      final File packageDir = new File(sourceDir, packageName.replace('.', File.separatorChar));

      write(project, new File(packageDir, mainScreenName + ".java"), DefracFileTemplateProvider.IOS_APP, packageName, mainScreenName, getApplicationName());
      write(project, new File(packageDir, mainScreenName + "Controller.java"), DefracFileTemplateProvider.IOS_APP_CONTROLLER, packageName, mainScreenName, getApplicationName());
      write(project, new File(packageDir, mainScreenName + "Delegate.java"), DefracFileTemplateProvider.IOS_APP_DELEGATE, packageName, mainScreenName, getApplicationName());
    }
  }

  public static final class Empty extends DefracModuleBuilder {
    @Override
    public String getBuilderId() {
      return "defrac.empty";
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull final WizardContext wizardContext, @NotNull final ModulesProvider modulesProvider) {
      return new ModuleWizardStep[]{
          new EmptyModuleWizardStep(this)
      };
    }

    @Override
    public void createTemplate(@NotNull final Project project,
                               @NotNull final File baseDir,
                               @NotNull final String packageName,
                               @NotNull final String mainScreenName,
                               final boolean isLambdaSupported) throws IOException {
      // no template
    }
  }

  @NotNull
  private String applicationName = "";
  @NotNull
  private String packageName = "";
  @NotNull
  private String mainScreenName = "";
  @NotNull
  private String version = "1.0";
  @Nullable
  private Sdk defracSdk;

  boolean webSupported;
  boolean iosSupported;
  boolean jvmSupported;
  boolean androidSupported;

  @Override
  public Icon getNodeIcon() {
    return DefracIcons.Defrac16x16;
  }

  @Override
  public Icon getBigIcon() {
    return DefracIcons.Defrac16x16;
  }

  @Override
  public abstract String getBuilderId();

  public void setApplicationName(@NotNull final String value) {
    applicationName = value;
  }

  @NotNull
  public String getApplicationName() { return applicationName; }

  public void setPackageName(@NotNull final String value) {
    packageName = value;
  }

  public void setMainScreenName(@NotNull final String value) {
    mainScreenName = value;
  }

  public void setWebSupported(final boolean value) {
    webSupported = value;
  }

  public void setIOSSupported(final boolean value) {
    iosSupported = value;
  }

  public void setJVMSupported(final boolean value) {
    jvmSupported = value;
  }

  public void setAndroidSupported(final boolean value) {
    androidSupported = value;
  }

  public void setDefracSdk(@Nullable final Sdk value) {
    defracSdk = value;
  }

  @NotNull
  public String getJavaPackageName() {
    final int dot = mainScreenName.lastIndexOf('.');
    return dot == -1 ? "" : mainScreenName.substring(0, dot);
  }

  @NotNull
  public String getJavaMainScreenName() {
    final int dot = mainScreenName.lastIndexOf('.');
    return dot == -1 ? mainScreenName : mainScreenName.substring(dot + 1);
  }

  @NotNull
  public String getJavaMainName() {
    final String packageName = getJavaPackageName();
    return packageName.isEmpty() ? "Main" : packageName + ".Main";
  }

  @Override
  public boolean isSuitableSdkType(final SdkTypeId sdkType) {
    return sdkType == DefracSdkType.getInstance();
  }

  @Override
  public abstract ModuleWizardStep[] createWizardSteps(@NotNull final WizardContext wizardContext, @NotNull final ModulesProvider modulesProvider);

  @Override
  public final void setupRootModel(final ModifiableRootModel rootModel) throws ConfigurationException {
    // intellij does not support multi-module project building
    // so we create the project structure manually and let the defrac console do the rest (see commit(...))
  }

  @Nullable
  @Override
  public ModuleWizardStep modifySettingsStep(@NotNull final SettingsStep settingsStep) {
    return new DefracModifiedSettingsStep(settingsStep, this);
  }

  @Override
  public ModuleType getModuleType() {
    return StdModuleTypes.JAVA;
  }

  @Nullable
  @Override
  public final List<Module> commit(@NotNull final Project project,
                                   final ModifiableModuleModel model,
                                   final ModulesProvider modulesProvider) {
    try {
      createProjectStructure(project);
      createDefaultSettings(project);
    } catch(final IOException exception) {
      LOG.error(exception);
    }

    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      @Override
      public void run() {
        try {
          createIntelliProject(project);
          createTemplate(project);

          // have to reload here since intellij caches the project
          // and because it is not capable of creating multi-module
          // projects we have to use "defrac ide-intellij" which
          // creates the modules and facet which is a change intellij
          // won't notice otherwise ...
          reloadProject(project);
        } catch(final IOException exception) {
          LOG.error(exception);
        }
      }
    });
    return null;
  }

  private static void reloadProject(@NotNull final Project project) {
    VirtualFileManager.getInstance().asyncRefresh(new Runnable() {
      @Override
      public void run() {
        ProjectManager.getInstance().reloadProject(project);
      }
    });
  }

  private void createProjectStructure(@NotNull final Project project) throws IOException {
    final File baseDir = VfsUtilCore.virtualToIoFile(project.getBaseDir());

    createDirectory(baseDir, "resources");

    createDefracDirectory(baseDir, "src", "java");
    createDefracDirectory(baseDir, "test", "java");
    createDefracDirectory(baseDir, "macro", "java");

    createDefracDirectory(baseDir, "bin", "java");
    createDefracDirectory(baseDir, "bin", "macro");

    createDefracDirectory(baseDir, "target", "");
  }

  private void createIntelliProject(@NotNull final Project project) {
    final GeneralCommandLine cmdLine =
        DefracCommandLineBuilder.forSdk(checkNotNull(defracSdk)).
            command("ide-intellij").
            workingDirectory(VfsUtilCore.virtualToIoFile(project.getBaseDir())).
            build();

    try {
      final OSProcessHandler handler = new OSProcessHandler(cmdLine);
      handler.startNotify();
      handler.waitFor();
    } catch(final ExecutionException exception) {
      LOG.error(exception);
    }
  }

  private void createTemplate(@NotNull final Project project) throws IOException {
    final File baseDir = VfsUtilCore.virtualToIoFile(project.getBaseDir());
    final LanguageLevel languageLevel =
        LanguageLevelProjectExtension.getInstance(project).getLanguageLevel();

    createTemplate(project, baseDir, getJavaPackageName(), getJavaMainScreenName(), languageLevel.isAtLeast(LanguageLevel.JDK_1_8));
  }

  public abstract void createTemplate(@NotNull final Project project,
                                      @NotNull final File baseDir,
                                      @NotNull final String packageName,
                                      @NotNull final String className,
                                      final boolean lambdaSupported) throws IOException;

  private static void write(@NotNull final Project project,
                            @NotNull final File file,
                            @NotNull final String template,
                            @NotNull final String packageName,
                            @NotNull final String className,
                            @NotNull final String applicationName) throws IOException {
    FileUtil.writeToFile(
        file,
        DefracFileTemplateProvider.getText(project, template, templateProperties(project, packageName, className, applicationName)));
  }

  @NotNull
  private static Properties templateProperties(@NotNull final Project project,
                                               @NotNull final String packageName,
                                               @NotNull final String className,
                                               @NotNull final String applicationName) {
    final Properties properties = new Properties(FileTemplateManager.getInstance(project).getDefaultProperties());
    properties.put("NAME", className);
    properties.put("PACKAGE_NAME", packageName);
    properties.put("APPLICATION_NAME", applicationName);
    return properties;
  }

  @NotNull
  private File createDefracDirectory(@NotNull final File parent,
                                     @NotNull final String name,
                                     @NotNull final String childPrefix) throws IOException {
    final File dir = createDirectory(parent, name);

    if(!childPrefix.isEmpty()) {
      createDirectory(dir, childPrefix);
    }

    for(final String target : targets()) {
      if(isNullOrEmpty(target)) {
        continue;
      }

      createDirectory(dir, directoryName(childPrefix, target));
    }

    return dir;
  }

  @NotNull
  private static String directoryName(@NotNull final String scope, @NotNull final String name) {
    if(scope.isEmpty()) {
      return name;
    } else {
      return scope+'.'+name;
    }
  }

  @NotNull
  private static File createDirectory(@NotNull final File parent, @NotNull final String name) throws IOException {
    final File dir = new File(parent, name);

    if(!dir.exists()) {
      LOG.info("Creating directory \""+parent.getAbsolutePath()+'"');

      if(!dir.mkdirs()) {
        throw new IOException("Couldn't create "+dir.getAbsolutePath());
      }
    }

    return dir;
  }

  private void createDefaultSettings(@NotNull final Project project) throws IOException {
    final DefracConfig config = new DefracConfig();

    config.
            setName(applicationName).
            setPackage(packageName).
            setVersion(isNullOrEmpty(version) ? "1.0" : version);

    if(!mainScreenName.isEmpty()) {
      config.setMain(getJavaMainName());
    }

    config.setTargets(targets());

    final File settingsFile = config.commit(project);

    LocalFileSystem.getInstance().refreshIoFiles(Collections.singletonList(settingsFile));
  }

  @NotNull
  private String[] targets() {
    final List<String> list = new ArrayList<String>();

    if(webSupported) {
      list.add("web");
    }

    if(iosSupported) {
      list.add("ios");
    }

    if(jvmSupported) {
      list.add("jvm");
    }

    if(androidSupported) {
      list.add("android");
    }

    return list.toArray(new String[list.size()]);
  }
}
