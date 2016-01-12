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

package defrac.intellij.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.facet.ProjectFacetManager;
import com.intellij.openapi.project.Project;
import defrac.intellij.DefracBundle;
import defrac.intellij.facet.DefracFacet;
import icons.DefracIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 */
public final class DefracConfigurationType extends ConfigurationTypeBase {
  @NotNull
  @NonNls
  public static final String ID = "DEFRAC";
  @NotNull
  public static final String DISPLAY_NAME = DefracBundle.message("config.run.name");
  @NotNull
  public static final String DESCRIPTION = DefracBundle.message("config.run.description");

  public static DefracConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(DefracConfigurationType.class);
  }

  @NotNull
  public final ConfigurationFactory jvmFactory = new DefracConfigurationFactory("jvm") {
    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
      return new DefracJvmRunConfiguration(project, this);
    }
  };

  @NotNull
  public final ConfigurationFactory webFactory = new DefracConfigurationFactory("web") {
    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
      return new DefracWebRunConfiguration(project, this);
    }
  };

  @NotNull
  public final ConfigurationFactory androidFactory = new DefracConfigurationFactory("android") {
    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
      return new DefracAndroidRunConfiguration(project, this);
    }
  };

  @NotNull
  public final ConfigurationFactory iosFactory = new DefracConfigurationFactory("ios") {
    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
      return new DefracIOSRunConfiguration(project, this);
    }
  };

  public DefracConfigurationType() {
    super(ID, DISPLAY_NAME, DESCRIPTION, DefracIcons.Defrac16x16);
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{jvmFactory, webFactory, androidFactory, iosFactory};
  }

  @Override
  public Icon getIcon() {
    return DefracIcons.Defrac16x16;
  }

  abstract class DefracConfigurationFactory extends ConfigurationFactory {
    @NotNull
    final String name;

    public DefracConfigurationFactory(@NotNull final String name) {
      super(DefracConfigurationType.this);
      this.name = name;
    }

    @Override
    public Icon getIcon() {
      return DefracIcons.Defrac16x16;
    }

    @Override
    public boolean isConfigurationSingletonByDefault() {
      return true;
    }

    @Override
    public boolean canConfigurationBeSingleton() {
      return true;
    }

    @Override
    public boolean isApplicable(@NotNull final Project project) {
      return ProjectFacetManager.getInstance(project).hasFacets(DefracFacet.ID);
    }

    @Override
    public String getName() {
      return name;
    }
  }
}
