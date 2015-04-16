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

package defrac.intellij.toolWindow;

import com.intellij.execution.filters.BrowserHyperlinkInfo;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import defrac.intellij.project.DefracConsoleView;
import defrac.intellij.project.DefracProcess;
import icons.DefracIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracToolWindowFactory implements ToolWindowFactory {
  @NotNull @NonNls public static final String TOOLWINDOW_ID = "defrac.toolWindow";

  @Override
  public void createToolWindowContent(@NotNull final Project project,
                                      @NotNull final ToolWindow toolWindow) {
    final ConsoleView console = DefracConsoleView.getInstance(project);

    if(console == null) {
      toolWindow.setAvailable(false, null);
      return;
    }

    final Content content =
        toolWindow.getContentManager().getFactory().createContent(console.getComponent(), "", true);

    toolWindow.getContentManager().addContent(content);
    toolWindow.setIcon(DefracIcons.ToolWindow);

    console.print("Initializing ", ConsoleViewContentType.SYSTEM_OUTPUT);
    console.printHyperlink("defrac", new BrowserHyperlinkInfo("https://www.defrac.com/"));
    console.print(" ...\n", ConsoleViewContentType.SYSTEM_OUTPUT);

    console.attachToProcess(DefracProcess.getInstance(project).getProcessHandler());
  }
}
