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
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.facet.ProjectFacetManager;
import com.intellij.openapi.project.Project;
import defrac.intellij.facet.DefracFacet;
import icons.DefracIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 */
public final class DefracConfigurationFactory extends ConfigurationFactory {
  public DefracConfigurationFactory(@NotNull final ConfigurationType configurationType) {
    super(configurationType);
  }

  @Override
  public RunConfiguration createTemplateConfiguration(final Project project) {
    return new DefracRunConfiguration(project, this);
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
}
