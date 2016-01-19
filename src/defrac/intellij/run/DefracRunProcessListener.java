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

package defrac.intellij.run;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import defrac.intellij.DefracPlatform;
import defrac.intellij.ipc.DefracIpc;
import org.jetbrains.annotations.NotNull;

/**
 */
public class DefracRunProcessListener extends ProcessAdapter {
  @NotNull
  protected final DefracIpc ipc;
  @NotNull
  protected final DefracIpc.Executor executor;
  @NotNull
  private final DefracPlatform platform;

  public DefracRunProcessListener(@NotNull final DefracIpc ipc,
                                  @NotNull final DefracIpc.Executor executor,
                                  @NotNull final DefracPlatform platform) {
    this.ipc = ipc;
    this.executor = executor;
    this.platform = platform;
  }

  @Override
  public void processTerminated(final ProcessEvent event) {
    if(executor.listening()) {
      // case when the process is destroyed from within the IDE
      executor.dispose();

      final DefracIpc.Executor abort = ipc.close(platform);

      abort.addListener(new DefracIpc.ExecutorAdapter() {
        @Override
        public void onCancel() {
          abort.dispose();
        }

        @Override
        public void onComplete(final int exitCode) {
          abort.dispose();
        }

        @Override
        public void onError(@NotNull final Exception exception) {
          abort.dispose();
        }
      });

      ipc.submit(abort);
    }
  }
}
