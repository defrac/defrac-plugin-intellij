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

package defrac.intellij.projectView;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.ProjectTreeStructure;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.openapi.project.Project;
import defrac.intellij.DefracBundle;
import icons.DefracIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 */
public final class DefracProjectViewPane extends ProjectViewPane {
  @NotNull @NonNls public static final String ID = "DEFRAC";
  @NotNull public static final String TITLE = DefracBundle.message("projectView.title");
  public static final int WEIGHT = 50;

  public DefracProjectViewPane(@NotNull final Project project) {
    super(project);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }

  @NotNull
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public int getWeight() {
    return WEIGHT;
  }

  @Override
  public Icon getIcon() {
    return DefracIcons.Defrac16x16;
  }

  @Override
  protected DefracProjectTreeStructure createStructure() {
    return new DefracProjectTreeStructure(getProject());
  }

  private Project getProject() {
    // we don't use the "my" prefix, so this method exists
    // only to enforce consistent style
    return myProject;
  }

  private static class DefracProjectTreeStructure extends ProjectTreeStructure {
    public DefracProjectTreeStructure(final Project project) {
      super(project, ID);
    }

    @Override
    public boolean isShowModules() {
      return true;
    }

    @Override
    protected DefracViewProjectNode createRoot(final Project project, final ViewSettings settings) {
      return new DefracViewProjectNode(project, settings);
    }
  }
}
