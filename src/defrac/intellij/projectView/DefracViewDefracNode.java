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

import com.google.common.collect.Lists;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.NavigatableWithText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.PlatformIcons;
import defrac.intellij.DefracPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
final class DefracViewDefracNode extends ProjectViewNode<DefracProject> implements NavigatableWithText {
  public DefracViewDefracNode(@NotNull final Project project,
                              @NotNull final DefracProject value,
                              @NotNull final ViewSettings viewSettings) {
    super(project, value, viewSettings);
  }

  @SuppressWarnings("UnusedDeclaration")
  public DefracViewDefracNode(@NotNull final Project project,
                              @NotNull final Object value,
                              @NotNull final ViewSettings viewSettings) {
    this(project, (DefracProject) value, viewSettings);
  }

  @NotNull
  @Override
  @SuppressWarnings("unchecked")
  public Collection<VirtualFile> getRoots() {
    return DefracProjectViewUtil.getRoots(getChildren());
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    return someChildContainsFile(file, false);
  }

  @Nullable
  @Override
  public String getNavigateActionText(final boolean focusEditor) {
    return "Open defrac Settings";
  }

  @NotNull
  @Override
  public Collection<AbstractTreeNode> getChildren() {
    final Project project = getProject();

    if(project == null) {
      return Lists.newArrayListWithCapacity(0);
    }

    final DefracProject defracProject = getValue();
    final ArrayList<AbstractTreeNode> children =
        Lists.newArrayListWithExpectedSize(DefracPlatform.values().length);

    if(defracProject == null || defracProject.isDisposed()) {
      setValue(null);
      return children;
    }

    for(final DefracPlatform platform : DefracPlatform.values()) {
      final List<Module> modules = defracProject.getModules(platform);

      if(modules.isEmpty()) {
        continue;
      }

      // note: we can use getParentValue apparently and don't need DefracProjectPlatform
      children.add(new DefracViewPlatformNode(getProject(),
          new DefracProjectPlatform(defracProject, platform), getSettings()));
    }

    return children;
  }

  @Override
  protected void update(final PresentationData presentation) {
    final DefracProject project = getValue();

    if(project == null || project.isDisposed()) {
      setValue(null);
      return;
    }

    presentation.setPresentableText(project.getName());
    presentation.addText(project.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
    presentation.setIcon(PlatformIcons.CLOSED_MODULE_GROUP_ICON);
  }
}
