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

package defrac.intellij.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class DefracProjectUtil {
  @Contract("null -> false")
  public static boolean isDefracProject(@Nullable final Project project) {
    if(project == null) {
      return false;
    }

    final VirtualFile settingsFile =
        project.getBaseDir().findChild(Names.default_settings);

    return settingsFile != null && settingsFile.exists();
  }

  @Nullable
  @Contract("null -> null")
  public static Sdk getProjectSdk(@Nullable final Project project) {
    return project == null
        ? null
        : ProjectRootManager.getInstance(project).getProjectSdk();
  }

  private DefracProjectUtil() {}
}
