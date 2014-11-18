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

import com.google.common.collect.Lists;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 */
public final class DefracProcessText {
  private static final Key<DefracProcessText> KEY = new Key<DefracProcessText>("DEFRAC_PROCESS_TEXT");

  public static void attach(@NotNull final ProcessHandler processHandler) {
    new DefracProcessText(processHandler);
  }

  @Nullable
  public static DefracProcessText get(@NotNull final ProcessHandler processHandler) {
    return processHandler.getUserData(KEY);
  }

  @NotNull
  private final List<Pair<String, Key>> fragments = Lists.newLinkedList();

  private DefracProcessText(@NotNull ProcessHandler processHandler) {
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        synchronized(fragments) {
          fragments.add(Pair.create(event.getText(), outputType));
        }
      }
    });
    processHandler.putUserData(KEY, this);
  }

  public void printTo(@NotNull final ProcessHandler processHandler) {
    synchronized(fragments) {
      for(final Pair<String, Key> fragment : fragments) {
        processHandler.notifyTextAvailable(fragment.getFirst(), fragment.getSecond());
      }
    }
  }
}
