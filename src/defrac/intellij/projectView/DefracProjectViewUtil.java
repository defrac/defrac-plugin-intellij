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
import com.google.common.collect.Sets;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import defrac.intellij.DefracBundle;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 */
final class DefracProjectViewUtil {
  @NotNull public static final String MACROS = DefracBundle.message("projectView.macros");
  @NotNull public static final String SOURCE = DefracBundle.message("projectView.source");

  @NotNull
  public static String macroOrSource(@NotNull final DefracFacet facet) {
    return facet.isMacroLibrary() ? MACROS : SOURCE;
  }

  @NotNull
  public static List<Module> extractLiveModules(@NotNull final Collection<WeakReference<Module>> modules) {
    return getModules(modules, null);
  }

  @NotNull
  public static List<Module> getModules(@NotNull final Collection<WeakReference<Module>> modules,
                                        @Nullable final Condition<Module> filter) {
    final Iterator<WeakReference<Module>> iterator = modules.iterator();
    final List<Module> result =
        Lists.newArrayListWithExpectedSize(modules.size());

    while(iterator.hasNext()) {
      final WeakReference<Module> moduleRef = iterator.next();
      final Module module = moduleRef.get();

      if(module == null || module.isDisposed()) {
        iterator.remove();
        continue;
      }

      if(filter == null || filter.value(module)) {
        result.add(module);
      }
    }

    return result;
  }

  @NotNull
  public static AbstractTreeNode createModuleNode(@NotNull final Project project,
                                                  @NotNull final Module module,
                                                  @NotNull final ViewSettings settings) {
    final VirtualFile[] contentRoots =
        ModuleRootManager.getInstance(module).getContentRoots();

    if(contentRoots.length == 1) {
      final VirtualFile contentRoot = contentRoots[0];
      final PsiDirectory directory = PsiManager.getInstance(project).findDirectory(contentRoot);

      if(directory != null) {
        return new DefracViewDirectoryNode(project, directory, settings);
      }
    }

    return new ProjectViewModuleNode(project, module, settings);
  }

  @NotNull
  @SuppressWarnings("unchecked")
  public static Collection<VirtualFile> getRoots(final Collection<AbstractTreeNode> children) {
    final Set<VirtualFile> result = Sets.newHashSet();

    for(final AbstractTreeNode child : children) {
      if(child instanceof ProjectViewNode) {
        result.addAll(((ProjectViewNode)child).getRoots());
      }
    }

    return result;
  }

  private DefracProjectViewUtil() {}
}
