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

package defrac.intellij.action.ios;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import defrac.intellij.DefracPlatform;
import defrac.intellij.action.DefracAction;
import defrac.intellij.config.DefracConfigOracle;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class SwitchToXCodeAction extends DefracAction {
  public SwitchToXCodeAction() {
    super(Conditions.<AnActionEvent>alwaysTrue());
  }

  @Override
  protected boolean isActionEnabled(@NotNull final AnActionEvent event) {
    return DefracPlatform.IOS.isAvailableOnHostOS()
        && getFacet(event).getPlatform() == DefracPlatform.IOS
        && getXCodeProject(event) != null;
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent event) {
    final VirtualFile project = getXCodeProject(event);

    if(project == null) {
      return;
    }

    final GeneralCommandLine commandLine =
        new GeneralCommandLine("open", FileUtil.toSystemDependentName(project.getPath()));

    try {
      final ProcessHandler processHandler =
          new OSProcessHandler(commandLine);
      processHandler.startNotify();
      processHandler.waitFor();
    } catch(ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Nullable
  private VirtualFile getXCodeProject(@NotNull final AnActionEvent event) {
    final DefracFacet facet = getFacet(event);
    final DefracConfigOracle config = facet.getConfigOracle();

    if(config == null) {
      return null;
    }

    return config.getXCodeProject(facet);
  }
}
