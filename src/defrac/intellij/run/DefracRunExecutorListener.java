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

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.util.Key;
import defrac.intellij.ipc.DefracCommandLineParser;
import defrac.intellij.ipc.DefracIpc;
import org.jetbrains.annotations.NotNull;

/**
 */
public class DefracRunExecutorListener implements DefracIpc.ExecutorListener {
  @NotNull
  protected final ProcessHandler process;

  public DefracRunExecutorListener(@NotNull final ProcessHandler process) {
    this.process = process;
  }

  @Override
  public void onMessage(@NotNull final DefracCommandLineParser.Message message) {
    final Key outputType = message.isError() ? ProcessOutputTypes.STDERR : ProcessOutputTypes.STDOUT;

    process.notifyTextAvailable(message.text + "\n", outputType);
  }

  @Override
  public void onError(@NotNull final Exception exception) {
    process.notifyTextAvailable(exception.getMessage() + "\n", ProcessOutputTypes.STDERR);
    process.notifyTextAvailable("\nProcess finished with exit code 1\n", ProcessOutputTypes.SYSTEM);
    process.destroyProcess();
  }

  @Override
  public void onComplete(final int exitCode) {
    // we do not cancel the executor since we want to fetch incoming messages
  }

  @Override
  public void onCancel() {
    process.notifyTextAvailable("\nProcess finished with exit code 0\n", ProcessOutputTypes.SYSTEM);
    process.destroyProcess();
  }
}
