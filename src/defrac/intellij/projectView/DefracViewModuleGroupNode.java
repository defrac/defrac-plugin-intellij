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
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static defrac.intellij.projectView.DefracProjectViewUtil.createModuleNode;

/**
 *
 */
final class DefracViewModuleGroupNode extends ProjectViewNode<DefracModuleGroup> {
  public DefracViewModuleGroupNode(final Project project, final DefracModuleGroup value, final ViewSettings viewSettings) {
    super(project, value, viewSettings);
  }

  @Override
  @NotNull
  public Collection<AbstractTreeNode> getChildren() {
    final Project project = getProject();

    if(project == null) {
      return Lists.newArrayListWithCapacity(0);
    }

    final List<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
    final Collection<Module> modules = getValue().getModules();

    for(final Module module : modules) {
      result.add(createModuleNode(project, module, getSettings()));
    }

    return result;
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

  @Override
  public void update(PresentationData presentation) {
    presentation.setPresentableText(getValue().getName());
    presentation.setIcon(PlatformIcons.CLOSED_MODULE_GROUP_ICON);
  }

  @Override
  public int getWeight() {
    return 0;
  }

  @Override
  public int getTypeSortWeight(final boolean sortByType) {
    return 1;
  }
}
