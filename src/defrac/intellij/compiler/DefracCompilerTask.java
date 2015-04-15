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

package defrac.intellij.compiler;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.project.DefracConsoleView;
import defrac.intellij.run.DefracRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public abstract class DefracCompilerTask implements CompileTask {
  @Override
  public boolean execute(@NotNull final CompileContext context) {
    if(isErroneous(context)) {
      return false;
    }

    final Project project = context.getProject();

    final RunConfiguration runConfiguration =
        context.getCompileScope().getUserData(CompileStepBeforeRun.RUN_CONFIGURATION);

    if(!isDefracRunConfiguration(runConfiguration)) {
      return true;
    }

    final DefracRunConfiguration defracRunConfiguration =
        (DefracRunConfiguration)runConfiguration;

    final Module[] modules = defracRunConfiguration.getModules();

    if(modules.length != 1) {
      context.addMessage(CompilerMessageCategory.ERROR, "No module found", null, -1, -1);
      return false;
    }

    final DefracFacet facet = DefracFacet.getInstance(modules[0]);

    if(facet == null) {
      context.addMessage(CompilerMessageCategory.ERROR, "Couldn't find defrac facet", null, -1, -1);
      return false;
    }

    try {
      context.getProgressIndicator().pushState();
      setProgressIndicatorText(context, facet);

      final CyclicBarrier barrier = new CyclicBarrier(2);

      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          final ConsoleView console = DefracConsoleView.getInstance(project);

          if(console != null) {
            console.clear();
          }

          try {
            barrier.await();
          } catch(final InterruptedException interrupt) {
            Thread.currentThread().interrupt();
          } catch(final BrokenBarrierException brokenBarrier) {
            // ignore broken barrier state due to timeout
          }
        }
      });

      try {
        barrier.await(256, TimeUnit.MILLISECONDS);
      } catch(final InterruptedException interrupt) {
        Thread.currentThread().interrupt();
      } catch(final BrokenBarrierException brokenBarrier) {
        throw new IllegalStateException(brokenBarrier);
      } catch(final TimeoutException timeout) {
        // simply continue with compilation if ui-thread wasn't
        // able to show and clear console view in time
      }

      return doCompile(context, facet, defracRunConfiguration);
    } finally {
      context.getProgressIndicator().popState();
    }
  }

  private void setProgressIndicatorText(@NotNull final CompileContext context,
                                        @NotNull final DefracFacet facet) {
    context.
        getProgressIndicator().
        setText("defrac " + (facet.getPlatform().isGeneric() ? "" : facet.getPlatform().name + ":") + getPresentableName());
  }

  @NotNull
  protected abstract String getPresentableName();

  protected abstract boolean doCompile(@NotNull final CompileContext context,
                                       @NotNull final DefracFacet facet,
                                       @NotNull final DefracRunConfiguration configuration);

  private boolean isDefracRunConfiguration(@Nullable final RunConfiguration runConfiguration) {
    return runConfiguration instanceof DefracRunConfiguration;
  }

  private boolean isErroneous(@NotNull final CompileContext context) {
    return context.getMessageCount(CompilerMessageCategory.ERROR) > 0;
  }
}
