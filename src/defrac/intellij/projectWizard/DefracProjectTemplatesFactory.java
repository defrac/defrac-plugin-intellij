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

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import defrac.intellij.DefracPlatform;
import icons.DefracIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 */
public final class DefracProjectTemplatesFactory extends ProjectTemplatesFactory {
  @NotNull @NonNls public static final String DEFRAC = "defrac";
  @NotNull @NonNls public static final String JAVA = "Java";

  @NotNull
  @Override
  public String[] getGroups() {
    return new String[] { DEFRAC };
  }

  @Override
  public Icon getGroupIcon(final String group) {
    return DefracIcons.Defrac16x16;
  }

  @Override
  public String getParentGroup(final String group) {
    return JAVA;
  }

  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(@Nullable final String group, final WizardContext wizardContext) {
    final List<ProjectTemplate> templates = new ArrayList<ProjectTemplate>();

    templates.add(new DefracProjectTemplate("Generic", "Creates a new Multi-Platform project", new DefracModuleBuilder.Generic()));

    if(DefracPlatform.IOS.isAvailableOnHostOS()) {
      templates.add(new DefracProjectTemplate("iOS", "Creates a new iOS project based on native UIKit components", new DefracModuleBuilder.IOS()));
    }

    templates.add(new DefracProjectTemplate("Empty", "Creates an empty Multi-Platform project", new DefracModuleBuilder.Empty()));

    return templates.toArray(new ProjectTemplate[templates.size()]);
  }
}
