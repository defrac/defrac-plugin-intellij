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
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

/**
 *
 */
final class DefracModuleGroup {
  @NotNull
  private final List<WeakReference<Module>> modules = Lists.newLinkedList();

  @NotNull
  private final String name;

  public DefracModuleGroup(@NotNull final String name, @NotNull final Collection<Module> modules) {
    this.name = name;

    for(final Module module : modules) {
      this.modules.add(new WeakReference<Module>(module));
    }
  }

  @NotNull
  public String getName() {
    return name;
  }

  public List<Module> getModules() {
    return DefracProjectViewUtil.extractLiveModules(modules);
  }
}
