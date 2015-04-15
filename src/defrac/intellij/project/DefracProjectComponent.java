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

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
abstract class DefracProjectComponent extends AbstractProjectComponent {
  DefracProjectComponent(final Project project) {
    super(checkNotNull(project));
  }

  @Override
  public final void initComponent() {
    if(!DefracProjectUtil.isDefracProject(getProject())) {
      return;
    }

    doInitComponent(getProject());
  }

  protected void doInitComponent(@NotNull final Project project) {}

  @Override
  public final void disposeComponent() {
    if(!DefracProjectUtil.isDefracProject(getProject())) {
      return;
    }

    doDisposeComponent(getProject());
  }

  protected void doDisposeComponent(@NotNull final Project project) {}

  @Override
  public final void projectOpened() {
    if(!DefracProjectUtil.isDefracProject(getProject())) {
      return;
    }

    doProjectOpened(getProject());
  }

  protected void doProjectOpened(@NotNull final Project project) {}

  @Override
  public final void projectClosed() {
    if(!DefracProjectUtil.isDefracProject(getProject())) {
      return;
    }

    doProjectClosed(getProject());
  }

  protected void doProjectClosed(@NotNull final Project project) {}

  @NotNull
  protected final Project getProject() {
    return myProject;
  }
}
