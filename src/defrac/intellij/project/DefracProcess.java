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

import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.BrowserHyperlinkInfo;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import defrac.intellij.ipc.DefracIpc;
import defrac.intellij.sdk.DefracSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracProcess extends DefracProjectComponent {
  @NotNull
  private static AtomicInteger WEB_SERVER_PORT = new AtomicInteger(0x8080);

  @Nullable
  private ProcessHandler processHandler;

  @Nullable
  private DefracIpc ipc;

  private int webServerPort;

  @NotNull
  public static DefracProcess getInstance(@NotNull final Project project) {
    return project.getComponent(DefracProcess.class);
  }

  public DefracProcess(@NotNull final Project project) {
    super(project);
  }

  @Override
  protected void doInitComponent(@NotNull final Project project) {
    webServerPort = WEB_SERVER_PORT.getAndIncrement();
    killProcessHandler();
  }

  private void tryInitProcess() {
    if(processHandler != null || ipc != null) {
      if(processHandler != null && (processHandler.isProcessTerminating() || processHandler.isProcessTerminated())) {
        processHandler = null;
        ipc = null;

        final CyclicBarrier barrier = new CyclicBarrier(2);

        UIUtil.invokeLaterIfNeeded(new Runnable() {
          @Override
          public void run() {
            final ConsoleView console =
                DefracConsoleView.getInstance(getProject());

            if(console != null) {
              console.clear();
              console.print("Restarting ", ConsoleViewContentType.SYSTEM_OUTPUT);
              console.printHyperlink("defrac", new BrowserHyperlinkInfo("https://www.defrac.com/"));
              console.print(" ...\n", ConsoleViewContentType.SYSTEM_OUTPUT);
            }


            tryInitProcess();

            if(console != null && processHandler != null) {
              console.attachToProcess(processHandler);
            }

            try {
              barrier.await();
            } catch(final InterruptedException interrupt) {
              Thread.currentThread().interrupt();
            } catch(final BrokenBarrierException brokenBarrier) {
              throw new IllegalStateException(brokenBarrier);
            }
          }
        });

        try {
          barrier.await();
        } catch(final InterruptedException interrupt) {
          Thread.currentThread().interrupt();
        } catch(final BrokenBarrierException brokenBarrier) {
          throw new IllegalStateException(brokenBarrier);
        }
      }

      return;
    }

    if(!DefracProjectUtil.isDefracProject(getProject())) {
      return;
    }

    final List<String> cmd = Lists.newArrayList();
    final String pathToExecutable;
    final Sdk sdk = DefracProjectUtil.getProjectSdk(getProject());

    if(DefracSdkUtil.isDefracSdk(sdk)) {
      final VirtualFile sdkDir = sdk.getHomeDirectory();
      final VirtualFile executable = sdkDir == null ? null : sdkDir.findChild(getDefracExecutableName());

      if(executable == null) {
        //TODO(joa): display toast or smth similar for user
        return;
      }

      final String path = executable.getCanonicalPath();

      if(isNullOrEmpty(path)) {
        return; //TODO(joa): see above
      }

      pathToExecutable = FileUtil.toSystemDependentName(path);
    } else {
      pathToExecutable = getDefracExecutableName();
    }

    // the defrac executable is either the one from the sdk if configured
    // or we fallback to the OS and the user has to have DEFRAC_HOME set
    cmd.add(pathToExecutable);

    // switch into plugin-mode to disable ansi and javac invocation
    cmd.add("--plugin-mode");

    // redefine the port of the configuration so multiple open
    // projects don't create a mess for the user
    cmd.add("--Cport");
    cmd.add(String.valueOf(webServerPort));

    // let defrac know the current path to the project
    cmd.add("--project");
    cmd.add(FileUtil.toSystemDependentName(getProject().getBasePath()));

    // if we have a valid sdk, let defrac know the actual path to it as well
    // and don't rely on DEFRAC_HOME being set
    if(DefracSdkUtil.isDefracSdk(sdk)) {
      final VirtualFile sdkDir = sdk.getHomeDirectory();
      final String pathToHome = sdkDir == null ? null : sdkDir.getCanonicalPath();

      if(!isNullOrEmpty(pathToHome)) {
        cmd.add("--home");
        cmd.add(FileUtil.toSystemDependentName(pathToHome));
      }
    }

    final GeneralCommandLine cmdLine =
        new GeneralCommandLine(cmd).
            withWorkDirectory(getProject().getBasePath());

    try {
      processHandler = KillableColoredProcessHandler.create(cmdLine);
      ipc = DefracIpc.getInstance(processHandler);
      processHandler.startNotify();
    } catch(final ExecutionException executionException) {
      processHandler = null;
      ipc = null;
    }
  }

  private String getDefracExecutableName() {
    return "defrac"+(SystemInfo.isWindows ? ".bat" : "");
  }

  @Nullable
  public DefracIpc getIpc() {
    tryInitProcess();
    return ipc;
  }

  @Nullable
  public ProcessHandler getProcessHandler() {
    tryInitProcess();
    return processHandler;
  }


  public int getWebServerPort() {
    return webServerPort;
  }

  @Override
  protected void doDisposeComponent(@NotNull final Project project) {
    killProcessHandler();
  }

  private void killProcessHandler() {
    if(processHandler != null) {
      processHandler.destroyProcess();
      processHandler = null;
      ipc = null;
    }
  }
}
