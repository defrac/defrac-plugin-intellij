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
import com.intellij.openapi.project.Project;
import defrac.intellij.ipc.DefracIpc;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tim Richter
 */
public final class DefracCompileContext implements DefracIpc.Context {
  @NotNull
  private final CompileContext context;

  public DefracCompileContext(@NotNull final CompileContext context) {
    this.context = context;
  }

  @Override
  public void addMessage(@NotNull final CompilerMessageCategory category, @NotNull final String message) {
    context.addMessage(category, message, null, -1, -1);
  }

  @NotNull
  @Override
  public Project getProject() {
    return context.getProject();
  }
}
