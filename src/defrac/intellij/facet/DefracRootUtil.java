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

package defrac.intellij.facet;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracRootUtil {
  @Nullable
  public static VirtualFile getFileByRelativeProjectPath(@NotNull final Project project,
                                                         @Nullable final String relativePath) {
    if(isNullOrEmpty(relativePath)) {
      return null;
    }

    final String projectDirPath = project.getBasePath();

    if(projectDirPath != null) {
      final String absPath = FileUtil.toSystemIndependentName(projectDirPath+File.separatorChar+relativePath);
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(absPath);

      if(file != null) {
        return file;
      }
    }

    return null;
  }

  public static String getBasePath(@NotNull final FacetEditorContext context) {
    return getBasePath(context.getModule());
  }

  public static String getBasePath(@NotNull final Module module) {
    return module.getProject().getBasePath();
  }

  private DefracRootUtil() {
  }

  public static VirtualFile getBaseDir(@NotNull final DefracFacet facet) {
    return facet.getModule().getProject().getBaseDir();
  }
}
