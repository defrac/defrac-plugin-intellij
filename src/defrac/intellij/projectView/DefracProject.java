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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfig;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.util.WeakReference2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
final class DefracProject {
  @NotNull
  public static DefracProject forSettings(@NotNull final Project project,
                                          @NotNull final VirtualFile settings) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    final PsiFile file = checkNotNull(psiManager.findFile(settings));

    @SuppressWarnings("UnnecessaryLocalVariable")
    final DefracProject result = CachedValuesManager.getCachedValue(file, new CachedValueProvider<DefracProject>() {
      @NotNull
      public Result<DefracProject> compute() {
        return Result.create(
            new DefracProject(settings),
            file, PsiModificationTracker.MODIFICATION_COUNT
        );
      }
    });

    // NOTE: We need to clear the modules here since the DefracViewProjectNode has
    //       no clue about the fact that we cache DefracProject instances and happily
    //       adds modules -- we keep the cached name though since it shall be invalidated
    //       by IntelliJ's internal mechanisms
    result.modules.clear();

    return result;
  }

  @NotNull
  private final VirtualFile settings;

  @NotNull
  private final List<WeakReference2<Module>> modules = Lists.newLinkedList();

  @Nullable
  private String nameCached;

  private DefracProject(@NotNull final VirtualFile settings) {
    this.settings = settings;
  }

  public void addModule(@NotNull final Module module) {
    assert DefracFacet.getInstance(module) != null;
    modules.add(WeakReference2.create(module));
  }

  @NotNull
  public List<Module> getModules() {
    return DefracProjectViewUtil.extractLiveModules(modules);
  }

  @NotNull
  public List<Module> getModules(@NotNull final DefracPlatform platform) {
    return DefracProjectViewUtil.getModules(modules, new Condition<Module>() {
      @Override
      public boolean value(@NotNull final Module module) {
        return getFacet(module).getPlatform() == platform;
      }
    });
  }

  @NotNull
  private DefracFacet getFacet(@NotNull final Module module) {
    final DefracFacet facet = DefracFacet.getInstance(module);
    return checkNotNull(facet, "Illegal module %s", module);
  }

  public boolean isDisposed() {
    if(modules.isEmpty()) {
      return true;
    }

    final Iterator<WeakReference2<Module>> iterator = modules.iterator();

    while(iterator.hasNext()) {
      final WeakReference2<Module> moduleRef = iterator.next();
      final Module module = moduleRef.get();

      if(module == null || module.isDisposed()) {
        iterator.remove();
      }
    }

    return modules.isEmpty();
  }

  @NotNull
  public VirtualFile getVirtualFile() {
    return settings;
  }

  @NotNull
  public String getName() {
    if(nameCached == null) {
      try {
        nameCached =
            DefracConfig.fromJson(getVirtualFile()).getName();
      } catch(final IOException ioException) {
        return "<error>";
      }
    }

    return nameCached;
  }

  @Override
  public boolean equals(@Nullable final Object object) {
    if(this == object) {
      return true;
    }

    if(object == null || getClass() != object.getClass()) {
      return false;
    }

    final DefracProject that = (DefracProject)object;

    return Comparing.haveEqualElements(this.modules, that.modules)
        && Comparing.equal(this.settings, that.settings);

  }

  @Override
  public int hashCode() {
    int result = settings.hashCode();

    for(final WeakReference2<Module> module : modules) {
      result = 31 * result + module.hashCode();
    }

    return result;
  }
}
