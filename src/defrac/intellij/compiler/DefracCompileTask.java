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

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.ipc.DefracIpc;
import defrac.intellij.project.DefracProcess;
import defrac.intellij.run.DefracRunConfiguration;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.PooledThreadExecutor;

import java.util.concurrent.*;

/**
 *
 */
public final class DefracCompileTask extends DefracCompilerTask {
  @NotNull @NonNls private static final String NAME = "compile";

  @NotNull
  public static final DefracCompileTask INSTANCE = new DefracCompileTask();

  private DefracCompileTask() {
  }

  @NotNull
  @Override
  protected String getPresentableName() {
    return NAME;
  }

  @Override
  protected boolean doCompile(@NotNull final CompileContext context,
                              @NotNull final DefracFacet facet,
                              @NotNull final DefracRunConfiguration configuration) {
    if(facet.getPlatform() != DefracPlatform.JVM) {
      return true;
    }

    final DefracIpc ipc =
        DefracProcess.getInstance(context.getProject()).getIpc();

    if(ipc == null) {
      context.addMessage(CompilerMessageCategory.ERROR, "Couldn't find defrac facet", null, -1, -1);
      return false;
    }

    final Future<Boolean> future = PooledThreadExecutor.INSTANCE.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return ipc.compile(context, facet.getPlatform());
      }
    });

    for(;;) {
      if(context.getProgressIndicator().isCanceled()) {
        future.cancel(/*mayInterruptIfRunning=*/true);
        return false;
      } else {
        try {
          return future.get(100, TimeUnit.MILLISECONDS);
        } catch(final InterruptedException interrupt) {
          Thread.currentThread().interrupt();
          return false;
        } catch(final ExecutionException exception) {
          return false;
        } catch(final TimeoutException timeout) {
          /* retry */
        }
      }
    }
  }
}
