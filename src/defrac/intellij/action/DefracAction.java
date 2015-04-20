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

package defrac.intellij.action;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDirectory;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.project.DefracProjectUtil;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public abstract class DefracAction extends AnAction {
  @NotNull
  protected static final Condition<AnActionEvent> IS_GENERIC = new Condition<AnActionEvent>() {
    @Override
    public boolean value(final AnActionEvent actionEvent) {
      return getFacet(actionEvent).getPlatform().isGeneric();
    }
  };

  @NotNull
  protected static final Condition<AnActionEvent> IS_IN_SOURCE = new Condition<AnActionEvent>() {
    @Override
    public boolean value(final AnActionEvent actionEvent) {
      return isInSourceContent(actionEvent.getDataContext());
    }
  };

  @NotNull
  protected static DefracFacet getFacet(@NotNull final AnActionEvent event) {
    return checkNotNull(DefracFacet.getInstance(event));
  }

  @NotNull
  private final Condition<AnActionEvent> condition;

  protected DefracAction(@NotNull final Condition<AnActionEvent> condition) {
    this.condition = condition;
  }

  @Override
  public final void update(@NotNull final AnActionEvent event) {
    final Presentation presentation = event.getPresentation();

    if(DefracProjectUtil.isDefracProject(event.getProject()) && DefracFacet.getInstance(event) != null) {
      presentation.setVisible(true);

      final boolean enabled =
          condition.value(event) && isActionEnabled(event);

      presentation.setEnabled(enabled);

      if(enabled) {
        doUpdate(event);
      }
    } else {
      presentation.setEnabledAndVisible(false);
    }
  }

  protected boolean isActionEnabled(@NotNull final AnActionEvent event) {
    return true;
  }

  protected static boolean isInSourceContent(@NotNull final DataContext dataContext) {
    final IdeView ideView = checkNotNull(LangDataKeys.IDE_VIEW.getData(dataContext));
    final Project project = checkNotNull(PlatformDataKeys.PROJECT.getData(dataContext));
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();

    for (PsiDirectory dir : ideView.getDirectories()) {
      if (projectFileIndex.isInSourceContent(dir.getVirtualFile())) {
        return true;
      }
    }

    return false;
  }

  protected void doUpdate(@NotNull final AnActionEvent event) {}
}
