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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.ide.projectView.impl.nodes.AbstractProjectNode;
import com.intellij.ide.projectView.impl.nodes.ProjectViewDirectoryHelper;
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleGroupNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
final class DefracViewProjectNode extends AbstractProjectNode {
  DefracViewProjectNode(@NotNull final Project project,
                        @NotNull final ViewSettings settings) {
    super(project, project, settings);
  }

  @NotNull
  @Override
  protected AbstractTreeNode createModuleGroup(@NotNull final Module module) {
    return DefracProjectViewUtil.createModuleNode(
        checkNotNull(getProject()),
        module,
        getSettings()
    );
  }

  @NotNull
  @Override
  protected AbstractTreeNode createModuleGroupNode(@NotNull final ModuleGroup moduleGroup) {
    return new ProjectViewModuleGroupNode(getProject(), moduleGroup, getSettings());
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    // we want to collect all modules and sort them based on
    // (1) the fact that they have a defrac facet
    // (1.1) the defrac project they belong to
    // (2) arbitrary modules
    final Project project = getProject();

    if(project == null) {
      return Collections.emptyList();
    }

    final Set<Module> modules = getModules(project);
    final Map<String, DefracProject> defracModules = Maps.newLinkedHashMap();
    final ArrayList<Module> normalModules = Lists.newArrayListWithCapacity(0);

    for(final Module module : modules) {
      final DefracFacet facet = DefracFacet.getInstance(module);

      if(facet == null) {
        normalModules.add(module);
      } else {
        final File settingsFile = facet.getSettingsFile();
        final String key = settingsFile.getAbsolutePath();

        DefracProject defracProject = defracModules.get(key);

        if(defracProject == null) {
          defracProject = DefracProject.forSettings(project, checkNotNull(VfsUtil.findFileByIoFile(settingsFile, true)));
          defracModules.put(key, defracProject);
        }

        defracProject.addModule(module);
      }
    }

    final Collection<AbstractTreeNode> defracNodes = defracModules(project, defracModules.values());
    final Collection<AbstractTreeNode> normalNodes = normalModules(normalModules);
    final int totalNodes = defracNodes.size() + normalNodes.size();
    final ArrayList<AbstractTreeNode> result = Lists.newArrayListWithExpectedSize(totalNodes);

    result.addAll(defracNodes);
    result.addAll(normalNodes);

    return result;
  }

  @NotNull
  private Collection<AbstractTreeNode> defracModules(@NotNull final Project project,
                                                     @NotNull final Collection<DefracProject> defracProjects) {
    final ArrayList<AbstractTreeNode> result =
        Lists.newArrayListWithExpectedSize(defracProjects.size());

    for(final DefracProject defracProject : defracProjects) {
      result.add(new DefracViewDefracNode(project, defracProject, getSettings()));
    }

    return result;
  }

  @NotNull
  private Collection<AbstractTreeNode> normalModules(@NotNull final Collection<Module> modules) {
    final ArrayList<AbstractTreeNode> result =
        Lists.newArrayListWithExpectedSize(modules.size());

    for(final Module module: modules) {
      result.add(createModuleGroup(module));
    }

    return result;
  }

  @NotNull
  private static Set<Module> getModules(@NotNull final Project project) {
    final List<VirtualFile> topLevelContentRoots =
        ProjectViewDirectoryHelper.getInstance(project).getTopLevelRoots();

    final Set<Module> modules =
        Sets.newLinkedHashSetWithExpectedSize(topLevelContentRoots.size());

    for(VirtualFile root : topLevelContentRoots) {
      final Module module = ModuleUtil.findModuleForFile(root, project);

      if(module == null) {
        continue;
      }

      modules.add(module);
    }

    return modules;
  }
}
