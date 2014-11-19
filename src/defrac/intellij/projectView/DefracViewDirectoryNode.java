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
 */ense.
 */

package defrac.intellij.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.SimpleTextAttributes;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
final class DefracViewDirectoryNode extends PsiDirectoryNode {
  public DefracViewDirectoryNode(@NotNull final Project project,
                                 @NotNull final PsiDirectory value,
                                 @NotNull final ViewSettings viewSettings) {
    super(project, value, viewSettings);
  }

  public DefracViewDirectoryNode(@NotNull final Project project,
                                 @NotNull final Object value,
                                 @NotNull final ViewSettings viewSettings) {
    super(project, (PsiDirectory)value, viewSettings);
  }

  @Override
  protected void updateImpl(final PresentationData data) {
    super.updateImpl(data);

    final Project project = getProject();

    if(project == null) {
      return;
    }

    final PsiDirectory psiDirectory = getValue();
    final VirtualFile directoryFile = psiDirectory.getVirtualFile();

    if(ProjectRootsUtil.isModuleContentRoot(directoryFile, project)) {
      final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
      final Module module = fileIndex.getModuleForFile(directoryFile);
      final DefracFacet facet = DefracFacet.getInstance(module);

      if(facet == null) {
        return;
      }

      //we don't want "folder.name [module.name] (source root)"
      data.clearText();

      //we want "Source (location)"
      final String location = FileUtil.getLocationRelativeToUserHome(directoryFile.getPresentableUrl());
      final String text = DefracProjectViewUtil.macroOrSource(facet);
      data.setPresentableText(text);
      data.addText(text, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
      data.addText(" ("+location+')', SimpleTextAttributes.GRAYED_ATTRIBUTES);
    }
  }
}
