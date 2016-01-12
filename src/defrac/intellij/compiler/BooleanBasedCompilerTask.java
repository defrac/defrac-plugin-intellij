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
import defrac.concurrent.Future;
import defrac.concurrent.Futures;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.ipc.DefracCommandLineParser;
import defrac.intellij.ipc.DefracIpc;
import defrac.intellij.project.DefracProcess;
import defrac.intellij.run.DefracRunConfigurationBase;
import defrac.lang.Attempt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.PooledThreadExecutor;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public abstract class BooleanBasedCompilerTask extends DefracCompilerTask {
  @Override
  protected boolean doCompile(@NotNull final CompileContext context,
                              @NotNull final DefracRunConfigurationBase configuration,
                              @NotNull final DefracFacet facet) {
    final DefracIpc ipc =
        DefracProcess.getInstance(context.getProject()).getIpc();

    if(ipc == null) {
      reportError(context, "Couldn't find defrac facet");
      return false;
    }

    final DefracIpc.Executor executor = doCompile(context, configuration, facet, ipc);

    if(executor == null) {
      // noop
      return true;
    }

    executor.addListener(new DefracIpc.ExecutorListener() {
      @Override
      public void onMessage(@NotNull final DefracCommandLineParser.Message message) {
        context.addMessage(message.category, message.text, null, -1, -1);
      }

      @Override
      public void onError(@NotNull final Exception exception) {
        reportError(context, exception.getMessage());

        executor.dispose();
      }

      @Override
      public void onComplete(final int exitCode) {
        executor.dispose();
      }

      @Override
      public void onCancel() {
        executor.dispose();
      }
    });

    final Future<Boolean> future = ipc.submit(executor);

    for(; ; ) {
      if(context.getProgressIndicator().isCanceled()) {
        if(executor.listening()) {
          executor.cancel();
        }
        return false;
      }

      try {
        final Attempt<Boolean> attempt =
            Futures.await(future, PooledThreadExecutor.INSTANCE, 100, TimeUnit.MILLISECONDS);

        if(attempt != null) {
          if(attempt.isSuccess()) {
            return attempt.get();
          }

          return false;
        }
      } catch(final InterruptedException interrupt) {
        Thread.currentThread().interrupt();
        return false;
      }
    }
  }

  @Nullable
  protected abstract DefracIpc.Executor doCompile(@NotNull final CompileContext context,
                                                  @NotNull final DefracRunConfigurationBase configuration,
                                                  @NotNull final DefracFacet facet,
                                                  @NotNull final DefracIpc ipc);
}
