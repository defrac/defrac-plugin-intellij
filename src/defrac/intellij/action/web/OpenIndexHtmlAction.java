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

package defrac.intellij.action.web;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VirtualFile;
import defrac.intellij.action.DefracAction;
import defrac.intellij.config.DefracConfigOracle;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class OpenIndexHtmlAction extends DefracAction implements DumbAware {
  public OpenIndexHtmlAction() {
    super(Conditions.<AnActionEvent>alwaysTrue());
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent event) {
    final Project project = event.getProject();
    if(project == null) {
      return;
    }

    final VirtualFile indexHtml = getIndexHtml(event);
    if(indexHtml == null) {
      return;
    }

    FileEditorManager.getInstance(event.getProject()).openFile(indexHtml, true);
  }

  @Override
  protected boolean isActionEnabled(@NotNull final AnActionEvent event) {
    return getIndexHtml(event) != null;
  }

  @Nullable
  private VirtualFile getIndexHtml(@NotNull final AnActionEvent event) {
    final DefracFacet facet = getFacet(event);
    final DefracConfigOracle config = facet.getConfigOracle();

    if(config == null) {
      return null;
    }

    return config.getIndexHtml(facet);
  }
}
