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

package defrac.intellij.resolve;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ResolveScopeProvider;
import com.intellij.psi.search.GlobalSearchScope;
import defrac.intellij.module.DefracModuleUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class DefracResolveScopeProvider extends ResolveScopeProvider {
  public DefracResolveScopeProvider() {}

  @Nullable
  @Override
  public GlobalSearchScope getResolveScope(@NotNull final VirtualFile file, final Project project) {
    if(file.getFileType() != JavaFileType.INSTANCE) {
      return null;
    }

    final Module module = DefracModuleUtil.findDefracModule(file, project);

    if(module == null) {
      return null;
    }

    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final VirtualFile contentRoot = projectFileIndex.getContentRootForFile(file);

    if(contentRoot == null) {
      return null;
    }

    //TODO(joa): build scope for platform

    //final DefracPlatform platform = DefracPlatform.platformOf(contentRoot.getName());
    boolean includeTests = projectFileIndex.isInTestSourceContent(file) || !projectFileIndex.isInSourceContent(file);
    //final VirtualFile[] sources = ProjectRootManager.getInstance(project).getContentSourceRoots();
    final GlobalSearchScope scope =
        GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, includeTests);

    return scope;
  }
}
