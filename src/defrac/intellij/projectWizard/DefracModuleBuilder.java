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

import com.google.common.base.Suppliers;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import defrac.intellij.DefracFileTemplateProvider;
import defrac.intellij.DefracIcons;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfig;
import defrac.intellij.sdk.DefracSdkType;
import defrac.intellij.util.DefracCommandLineBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public abstract class DefracModuleBuilder extends ModuleBuilder {
  public static final class Generic extends DefracModuleBuilder {
    @Override
    public String getBuilderId() {
      return "defrac.generic";
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull final WizardContext wizardContext, @NotNull final ModulesProvider modulesProvider) {
      return new ModuleWizardStep[]{
          new GenericModuleWizardStep(this)
      };
    }

    @Override
    public void createTemplate(@NotNull final Project project, @NotNull final File baseDir, @NotNull final String packageName, @NotNull final String className) throws IOException {
      final File sourceDir = new File(baseDir, "src" + File.separator + "java");
      final File packageDir = new File(sourceDir, packageName.replace('.', File.separatorChar));

      write(project, new File(packageDir, className + ".java"), DefracFileTemplateProvider.GENERIC_APP, packageName, className);
    }
  }

  public static final class IOS extends DefracModuleBuilder {
    @Override
    public String getBuilderId() {
      return "defrac.ios";
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull final WizardContext wizardContext, @NotNull final ModulesProvider modulesProvider) {
      return new ModuleWizardStep[]{
          new IOSModuleWizardStep(this)
      };
    }

    @Override
    public void createTemplate(@NotNull final Project project, @NotNull final File baseDir, @NotNull final String packageName, @NotNull final String className) throws IOException {
      final File sourceDir = new File(baseDir, "src" + File.separator + "java.ios");
      final File packageDir = new File(sourceDir, packageName.replace('.', File.separatorChar));

      write(project, new File(packageDir, className + ".java"), DefracFileTemplateProvider.IOS_APP, packageName, className);
      write(project, new File(packageDir, className + "Controller.java"), DefracFileTemplateProvider.IOS_APP_CONTROLLER, packageName, className);
      write(project, new File(packageDir, className + "Delegate.java"), DefracFileTemplateProvider.IOS_APP_DELEGATE, packageName, className);
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
    public void createTemplate(@NotNull final Project project, @NotNull final File baseDir, @NotNull final String packageName, @NotNull final String className) throws IOException {
      // no template
    }
  }

  @NotNull
  private String myApplicationName = "";
  @NotNull
  private String myPackageName = "";
  @NotNull
  private String myMainClassName = "";
  @NotNull
  private String myVersion = "";
  @Nullable
  private Sdk myDefracSdk;

  private boolean myWebSupported;
  private boolean myIOSSupported;
  private boolean myJVMSupported;
  private boolean myAndroidSupported;

  @Override
  public Icon getNodeIcon() {
    return DefracIcons.DEFRAC;
  }

  @Override
  public Icon getBigIcon() {
    return DefracIcons.DEFRAC;
  }

  @Override
  public abstract String getBuilderId();

  public void setApplicationName(@NotNull final String value) {
    myApplicationName = value;
  }

  public void setPackageName(@NotNull final String value) {
    myPackageName = value;
  }

  public void setMainClassName(@NotNull final String value) {
    myMainClassName = value;
  }

  public void setWebSupported(final boolean value) {
    myWebSupported = value;
  }

  public void setIOSSupported(final boolean value) {
    myIOSSupported = value;
  }

  public void setJVMSupported(final boolean value) {
    myJVMSupported = value;
  }

  public void setAndroidSupported(final boolean value) {
    myAndroidSupported = value;
  }

  public void setDefracSdk(final Sdk value) {
    myDefracSdk = value;
  }

  @NotNull
  public String getJavaPackageName() {
    final int dot = myMainClassName.lastIndexOf('.');
    return dot == -1 ? "" : myMainClassName.substring(0, dot);
  }

  @NotNull
  public String getJavaClassName() {
    final int dot = myMainClassName.lastIndexOf('.');
    return dot == -1 ? myMainClassName : myMainClassName.substring(dot + 1);
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
  public final List<Module> commit(@NotNull final Project project, final ModifiableModuleModel model, final ModulesProvider modulesProvider) {
    createProjectStructure(project);
    createDefaultSettings(project);
    createIntelliProject(project);

    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      @Override
      public void run() {
        createTemplate(project);
      }
    });
    return null;
  }

  private void createProjectStructure(@NotNull final Project project) {
    final File baseDir = VfsUtilCore.virtualToIoFile(project.getBaseDir());

    createDefracDirectory(baseDir, "bin", "java");
    createDefracDirectory(baseDir, "bin", "macro");

    createDefracDirectory(baseDir, "macro", "java");

    createDirectory(baseDir, "resources");

    createDefracDirectory(baseDir, "src", "java");

    createDefracDirectory(baseDir, "target", "");

    createDefracDirectory(baseDir, "test", "java");
  }

  private void createIntelliProject(@NotNull final Project project) {
    final GeneralCommandLine cmdLine =
        DefracCommandLineBuilder.forSdk(checkNotNull(myDefracSdk)).
            command("ide-intellij").
            workingDirectory(VfsUtilCore.virtualToIoFile(project.getBaseDir())).
            build();

    try {
      final OSProcessHandler handler = new OSProcessHandler(cmdLine);
      handler.startNotify();
      handler.waitFor();
    } catch(ExecutionException e) {
      e.printStackTrace();
    }
  }

  private void createTemplate(@NotNull final Project project) {
    final File baseDir = VfsUtilCore.virtualToIoFile(project.getBaseDir());

    try {
      createTemplate(project, baseDir, getJavaPackageName(), getJavaClassName());
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  public abstract void createTemplate(@NotNull final Project project, @NotNull final File baseDir, @NotNull final String packageName, @NotNull final String className) throws IOException;

  private static void write(@NotNull final Project project, @NotNull final File file, @NotNull final String template, @NotNull final String packageName, @NotNull final String className) throws IOException {
    FileUtil.writeToFile(file, DefracFileTemplateProvider.getText(project, template, templateProperties(project, packageName, className)));
  }

  @NotNull
  private static Properties templateProperties(@NotNull final Project project, @NotNull final String packageName, @NotNull final String className) {
    final Properties properties = new Properties(FileTemplateManager.getInstance(project).getDefaultProperties());
    properties.put("NAME", className);
    properties.put("PACKAGE_NAME", packageName);
    return properties;
  }

  @NotNull
  private static File createDefracDirectory(@NotNull final File parent, @NotNull final String name, @NotNull final String childPrefix) {
    final File dir = createDirectory(parent, name);
    if(!childPrefix.isEmpty()) {
      createDirectory(dir, childPrefix);
    }
    createDirectory(dir, directoryName(childPrefix, "android"));
    if(DefracPlatform.IOS.isAvailableOnHostOS()) {
      createDirectory(dir, directoryName(childPrefix, "ios"));
    }
    createDirectory(dir, directoryName(childPrefix, "jvm"));
    createDirectory(dir, directoryName(childPrefix, "web"));
    return dir;
  }

  @NotNull
  private static String directoryName(@NotNull final String scope, @NotNull final String name) {
    if(scope.isEmpty()) {
      return name;
    } else {
      return scope + '.' + name;
    }
  }

  @NotNull
  private static File createDirectory(@NotNull final File parent, @NotNull final String name) {
    final File dir = new File(parent, name);
    dir.mkdirs();
    return dir;
  }

  private void createDefaultSettings(@NotNull final Project project) {
    final File baseDir = VfsUtilCore.virtualToIoFile(project.getBaseDir());
    final File settingsFile = new File(baseDir, "default.settings");

    final DefracConfig config = new DefracConfig();
    config.setName(myApplicationName);
    config.setPackage(myPackageName);
    config.setVersion(myVersion);

    if(!myMainClassName.isEmpty()) {
      config.setMain(myMainClassName);
    }

    config.setTargets(targets());

    try {
      settingsFile.createNewFile();

      config.write(Suppliers.<OutputStream>ofInstance(new FileOutputStream(settingsFile)));
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  @NotNull
  private String[] targets() {
    final List<String> list = new ArrayList<String>();

    if(myWebSupported) {
      list.add("web");
    }

    if(myIOSSupported) {
      list.add("ios");
    }

    if(myJVMSupported) {
      list.add("jvm");
    }

    if(myAndroidSupported) {
      list.add("android");
    }

    return list.toArray(new String[list.size()]);
  }
}
