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

import com.google.common.collect.Maps;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import defrac.intellij.DefracPlatform;
import defrac.intellij.util.WeakReference2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
final class DefracProjectPlatform {
  @NotNull
  public static DefracProjectPlatform create(@NotNull final Project project,
                                             @NotNull final DefracProject defracProject,
                                             @NotNull final DefracPlatform platform) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    final PsiFile psiFile = checkNotNull(psiManager.findFile(defracProject.getVirtualFile()));

    @SuppressWarnings("UnnecessaryLocalVariable")
    final ConcurrentMap<DefracPlatform, DefracProjectPlatform> result = CachedValuesManager.
        getCachedValue(psiFile, new CachedValueProvider<ConcurrentMap<DefracPlatform, DefracProjectPlatform>>() {
          @NotNull
          public Result<ConcurrentMap<DefracPlatform, DefracProjectPlatform>> compute() {
            return Result.create(
                Maps.<DefracPlatform, DefracProjectPlatform>newConcurrentMap(),
                psiFile, PsiModificationTracker.MODIFICATION_COUNT
            );
          }
        });

    final DefracProjectPlatform newValue = new DefracProjectPlatform(defracProject, platform);
    final DefracProjectPlatform oldValue = result.putIfAbsent(platform, newValue);
    return oldValue == null ? newValue : oldValue;
  }

  @NotNull
  private final WeakReference2<DefracProject> project;

  @NotNull
  private final DefracPlatform platform;

  private DefracProjectPlatform(@NotNull final DefracProject project,
                                @NotNull final DefracPlatform platform) {
    this.project = WeakReference2.create(project);
    this.platform = platform;
  }

  @Nullable
  public DefracProject getProject() {
    return project.get();
  }

  @NotNull
  public DefracPlatform getPlatform() {
    return platform;
  }

  @NotNull
  public List<Module> getModules() {
    final DefracProject project = getProject();

    if(project == null) {
      return Collections.emptyList();
    }

    return project.getModules(platform);
  }

  public boolean isDisposed() {
    final DefracProject project = getProject();
    return project == null || project.isDisposed();
  }

  @Override
  public boolean equals(@Nullable final Object object) {
    if(this == object) {
      return true;
    }

    if(object == null || getClass() != object.getClass()) {
      return false;
    }

    final DefracProjectPlatform that = (DefracProjectPlatform)object;

    return Comparing.equal(this.platform, that.platform)
        && Comparing.equal(this.project, that.project);
  }

  @Override
  public int hashCode() {
    int result = project.hashCode();
    result = 31 * result + platform.hashCode();
    return result;
  }
}
