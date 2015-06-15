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

package defrac.intellij.fileTemplate;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import icons.DefracIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 */
public final class DefracFileTemplateProvider implements FileTemplateGroupDescriptorFactory {
  @NotNull @NonNls public static final String MAIN_ANDROID = "MainAndroid.java";
  @NotNull @NonNls public static final String MAIN_IOS = "MainIOS.java";
  @NotNull @NonNls public static final String MAIN_JVM = "MainJVM.java";
  @NotNull @NonNls public static final String MAIN_JVM_8= "MainJVM_JDK8.java";
  @NotNull @NonNls public static final String MAIN_WEB = "MainWeb.java";

  @NotNull @NonNls public static final String MAIN_SCREEN = "MainScreen.java";
  @NotNull @NonNls public static final String MAIN_SCREEN_8 = "MainScreen_JDK8.java";

  @NotNull @NonNls public static final String DISPLAY_LIST_CONTROLLER = "DisplayListController.java";
  @NotNull @NonNls public static final String DISPLAY_LIST_CONTROLLER_8 = "DisplayListController_JDK8.java";

  @NotNull @NonNls public static final String IOS_APP = "IOSApplication.java";
  @NotNull @NonNls public static final String IOS_APP_CONTROLLER = "IOSApplicationController.java";
  @NotNull @NonNls public static final String IOS_APP_DELEGATE = "IOSApplicationDelegate.java";
  @NotNull @NonNls public static final String MULTI_PLATFORM_CLASS = "MultiPlatformClass.java";
  @NotNull @NonNls public static final String MULTI_PLATFORM_CLASS_INJECTION = "MultiPlatformClassInjection.java";
  @NotNull @NonNls public static final String MACRO = "Macro.java";
  @NotNull @NonNls public static final String MACRO_IMPLEMENTATION = "MacroImplementation.java";

  @NotNull
  private static final String[] ALL_TEMPLATES = {
      MAIN_ANDROID,
      MAIN_IOS,
      MAIN_JVM,
      MAIN_JVM_8,
      MAIN_WEB,
      MAIN_SCREEN,
      MAIN_SCREEN_8,
      DISPLAY_LIST_CONTROLLER,
      DISPLAY_LIST_CONTROLLER_8,
      IOS_APP,
      IOS_APP_CONTROLLER,
      IOS_APP_DELEGATE,
      MULTI_PLATFORM_CLASS,
      MULTI_PLATFORM_CLASS_INJECTION,
      MACRO,
      MACRO_IMPLEMENTATION
  };

  @Override
  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Defrac", DefracIcons.Defrac16x16);
    for(final String template : ALL_TEMPLATES) {
      group.addTemplate(new FileTemplateDescriptor(template, StdFileTypes.JAVA.getIcon()));
    }
    return group;
  }

  public static void write(@NotNull final Project project,
                           @NotNull final File file,
                           @NotNull final String template,
                           @NotNull final Properties properties) throws IOException {
    FileUtil.writeToFile(file, getText(project, template, properties));
  }

  @NotNull
  public static String getText(@NotNull final Project project,
                               @NotNull final String template,
                               @NotNull final Properties properties) throws IOException {
    final FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
    final Properties defaultProperties = templateManager.getDefaultProperties();

    for(final Map.Entry<Object, Object> entry : defaultProperties.entrySet()) {
      if(!properties.containsKey(entry.getKey())) {
        properties.put(entry.getKey(), entry.getValue());
      }
    }

    return templateManager.getInternalTemplate(template).getText(properties);
  }
}
