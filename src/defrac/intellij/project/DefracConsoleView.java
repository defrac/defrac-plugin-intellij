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

import com.intellij.execution.filters.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import defrac.intellij.ipc.DefracIpc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class DefracConsoleView extends DefracProjectComponent {
  @NotNull
  private static final String ERROR_PREFIX = "["+DefracIpc.LEVEL_ERROR+"] ";

  @NotNull
  private static final String WARN_PREFIX = "["+DefracIpc.LEVEL_WARN+"] ";

  @Nullable
  public static ConsoleView getInstance(@NotNull final Project project) {
    return project.getComponent(DefracConsoleView.class).console;
  }

  @Nullable
  private ConsoleView console;

  protected DefracConsoleView(final Project project) {
    super(project);
  }

  @Override
  protected void doInitComponent(@NotNull final Project project) {
    final TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);

    consoleBuilder.addFilter(new ExceptionFilter(GlobalSearchScope.projectScope(project)));


    consoleBuilder.addFilter(new Filter() {
      @Nullable
      @Override
      public Result applyFilter(final String line, final int entireLength) {
        boolean isErrorPrefix;

        if((isErrorPrefix = line.startsWith(ERROR_PREFIX)) || line.startsWith(WARN_PREFIX)) {
          final String prefix = isErrorPrefix ? ERROR_PREFIX : WARN_PREFIX;
          final int indexOfColon = line.indexOf(':');

          if(-1 == indexOfColon) {
            return null;
          }

          final String nameOfClass;

          if(indexOfColon > 1 && line.charAt(indexOfColon - 1) == ')') {
            final int indexOfLastDot = line.lastIndexOf('.', indexOfColon);

            if(indexOfLastDot == -1) {
              return null;
            }

            nameOfClass = line.substring(prefix.length(), indexOfLastDot);
          } else {
            nameOfClass = line.substring(prefix.length(), indexOfColon);
          }

          final PsiClass klass =
              JavaPsiFacade.getInstance(project).
                  findClass(nameOfClass, GlobalSearchScope.allScope(project));

          if(klass == null) {
            return null;
          }

          // wow... i hope this isn't the way you're supposed
          // to use this api
          final int youCannotBeSerious =
              entireLength - line.length() + prefix.length();

          return new Result(
              youCannotBeSerious,
              youCannotBeSerious + nameOfClass.length(),
              new OpenFileHyperlinkInfo(project, klass.getContainingFile().getVirtualFile(), 0));
        }

        return null;
      }
    });
    console = consoleBuilder.getConsole();

    final ProcessHandler processHandler = DefracProcess.getInstance(project).getProcessHandler();

    if(processHandler != null) {
      console.attachToProcess(processHandler);
    }
  }
}
