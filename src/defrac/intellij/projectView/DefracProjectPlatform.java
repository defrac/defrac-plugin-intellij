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

import com.intellij.openapi.module.Module;
import defrac.intellij.DefracPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

/**
 *
 */
final class DefracProjectPlatform {
  @NotNull
  private final WeakReference<DefracProject> project;

  @NotNull
  private final DefracPlatform platform;

  public DefracProjectPlatform(@NotNull final DefracProject project,
                               @NotNull final DefracPlatform platform) {
    this.project = new WeakReference<DefracProject>(project);
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
}
