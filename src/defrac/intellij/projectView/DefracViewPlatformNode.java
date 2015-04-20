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
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.NavigatableWithText;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.PlatformIcons;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import icons.DefracIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
final class DefracViewPlatformNode extends ProjectViewNode<DefracProjectPlatform> implements NavigatableWithText {
  public DefracViewPlatformNode(@NotNull final Project project,
                                @NotNull final DefracProjectPlatform value,
                                @NotNull final ViewSettings viewSettings) {
    super(project, value, viewSettings);
  }

  @SuppressWarnings("UnusedDeclaration")
  public DefracViewPlatformNode(@NotNull final Project project,
                                @NotNull final Object value,
                                @NotNull final ViewSettings viewSettings) {
    this(project, (DefracProjectPlatform)value, viewSettings);
  }

  @Nullable
  @Override
  public VirtualFile getVirtualFile() {
    final DefracProjectPlatform projectWithPlatform = getValue();

    if(projectWithPlatform == null || projectWithPlatform.isDisposed()) {
      setValue(null);
      return null;
    }

    final DefracProject project = projectWithPlatform.getProject();

    if(project == null || project.isDisposed()) {
      return null;
    }

    return project.getVirtualFile();
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

    final DefracProjectPlatform projectWithPlatform = getValue();
    final ArrayList<AbstractTreeNode> children =
        Lists.newArrayListWithExpectedSize(2);

    if(projectWithPlatform == null || projectWithPlatform.isDisposed()) {
      setValue(null);
      return children;
    }

    final List<Module> sourceModules = Lists.newArrayList();
    final List<Module> macroModules = Lists.newArrayList();

    for(final Module module : projectWithPlatform.getModules()) {
      final DefracFacet facet = DefracFacet.getInstance(module);

      if(facet == null) {
        continue;
      }

      if(facet.isMacroLibrary()) {
        macroModules.add(module);
      } else {
        sourceModules.add(module);
      }
    }

    addModules(project, children, DefracProjectViewUtil.SOURCE, sourceModules);
    addModules(project, children, DefracProjectViewUtil.MACROS, macroModules);

    return children;
  }

  private void addModules(@NotNull final Project project,
                          @NotNull final ArrayList<AbstractTreeNode> children,
                          @NotNull final String groupName,
                          @NotNull final List<Module> modules) {
    if(modules.size() == 1) {
      final Module module = modules.get(0);
      addFlattenedSingleChild(project, children, module);
    } else if(!modules.isEmpty()) {
      final DefracViewModuleGroupNode groupNode =
          new DefracViewModuleGroupNode(project, new DefracModuleGroup(groupName, modules), getSettings());
      children.add(groupNode);
    }
  }

  private void addFlattenedSingleChild(@NotNull final Project project,
                                       @NotNull final ArrayList<AbstractTreeNode> children,
                                       @NotNull final Module module) {
    final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    final ModuleFileIndex moduleFileIndex = rootManager.getFileIndex();
    final PsiManager psiManager = PsiManager.getInstance(project);
    final VirtualFile[] contentRoots = rootManager.getContentRoots();

    if(contentRoots.length == 1) {
      final VirtualFile contentRoot = contentRoots[0];

      if(moduleFileIndex.isInContent(contentRoot)) {
        final AbstractTreeNode child;

        if(contentRoot.isDirectory()) {
          final PsiDirectory directory = psiManager.findDirectory(contentRoot);
          child = new DefracViewDirectoryNode(project, checkNotNull(directory), getSettings());
        } else {
          child = new DefracViewModuleNode(project, module, getSettings());
        }

        children.add(child);
      }
    } else {
      children.add(new DefracViewModuleNode(project, module, getSettings()));
    }
  }

  @Override
  protected void update(final PresentationData presentation) {
    final DefracProjectPlatform projectWithPlatform = getValue();

    if(projectWithPlatform == null || projectWithPlatform.isDisposed()) {
      setValue(null);
      return;
    }

    final DefracPlatform platform = projectWithPlatform.getPlatform();
    final String name = platform.displayName;

    presentation.setPresentableText(name);
    presentation.addText(name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
    presentation.setIcon(getPlatformIcon(platform));
  }

  @Nullable
  private Icon getPlatformIcon(@NotNull final DefracPlatform platform) {
    //TODO(joa): need icons for iOS
    switch(platform) {
      case ANDROID:
        return DefracIcons.AndroidModule;
      case WEB:
        return PlatformIcons.WEB_ICON;
      default:
        return JavaModuleType.getModuleType().getIcon();
    }
  }
}
