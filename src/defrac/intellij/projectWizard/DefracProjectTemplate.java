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

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.platform.templates.BuilderBasedTemplate;
import icons.DefracIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 */
public final class DefracProjectTemplate extends BuilderBasedTemplate {
  @NotNull
  private final String name;

  @NotNull
  private final String description;

  public DefracProjectTemplate(@NotNull final String name, @NotNull final String description, final ModuleBuilder builder) {
    super(builder);
    this.name = name;
    this.description = description;
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @NotNull
  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Icon getIcon() {
    return DefracIcons.Defrac16x16;
  }
}
