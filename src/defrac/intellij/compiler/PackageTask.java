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
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.ipc.DefracIpc;
import defrac.intellij.run.DefracRunConfiguration;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

/**
 *
 */
public final class PackageTask extends BooleanBasedCompilerTask {
  @NotNull @NonNls private static final String NAME = "package";

  @NotNull
  public static final PackageTask INSTANCE = new PackageTask();

  private PackageTask() {
  }

  @Override
  protected boolean shouldRunForFacet(@NotNull final DefracFacet facet) {
    return facet.getPlatform().isJVM();
  }

  @NotNull
  @Override
  protected String getDefracCommandName() {
    return NAME;
  }


  @Override
  protected Callable<Boolean> createCallable(@NotNull final CompileContext context,
                                             @NotNull final DefracRunConfiguration configuration,
                                             @NotNull final DefracFacet facet,
                                             @NotNull final DefracIpc ipc) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return ipc.pack(context, facet.getPlatform());
      }
    };
  }
}
