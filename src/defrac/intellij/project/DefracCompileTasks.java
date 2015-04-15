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

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import defrac.intellij.compiler.DefracCompileTask;
import defrac.intellij.compiler.DefracCompilerTask;
import defrac.intellij.compiler.DefracPackageTask;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracCompileTasks extends DefracProjectComponent {
  protected DefracCompileTasks(final Project project) {
    super(project);
  }

  @Override
  protected void doProjectOpened(@NotNull final Project project) {
    final CompilerManager manager =
        CompilerManager.getInstance(project);

    installTask(manager, DefracCompileTask.INSTANCE);
    installTask(manager, DefracPackageTask.INSTANCE);
  }

  private void installTask(@NotNull final CompilerManager manager,
                           @NotNull final DefracCompilerTask task) {
    if(!ArrayUtil.contains(task, (Object) manager.getAfterTasks())) {
      manager.addAfterTask(task);
    }
  }
}
