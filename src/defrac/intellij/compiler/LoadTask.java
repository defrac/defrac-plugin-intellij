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
import defrac.intellij.config.DefracConfig;
import defrac.intellij.config.DefracConfigBase;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.ipc.DefracCommands;
import defrac.intellij.ipc.DefracIpc;
import defrac.intellij.run.DefracRunConfiguration;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public final class LoadTask extends BooleanBasedCompilerTask {
  @NotNull
  public static final LoadTask INSTANCE = new LoadTask();

  private LoadTask() {
  }

  @NotNull
  @Override
  protected String getDefracCommandName() {
    return DefracCommands.LOAD;
  }

  protected boolean shouldRunForFacet(@NotNull final DefracFacet facet) {
    return !facet.getPlatform().isGeneric();
  }

  @Override
  protected DefracIpc.Executor doCompile(@NotNull final CompileContext context,
                                         @NotNull final DefracRunConfiguration configuration,
                                         @NotNull final DefracFacet facet,
                                         @NotNull final DefracIpc ipc) {

    final DefracConfig config = facet.getConfig();

    if(config == null) {
      reportError(context, "Can't load defrac settings");
      return null;
    }

    // configure settings
    final DefracConfigBase settings = checkNotNull(config.getOrCreatePlatform(facet.getPlatform())).copy();
    settings.setMain(checkNotNull(configuration.getRunClass()));
    settings.setStrict(configuration.isStrict());

    if(configuration.launchInEmulator()) {
      settings.setDeployOnEmulator();
    } else if(configuration.launchOnDevice()) {
      settings.setDeployOnDevice();
    }

    return ipc.load(facet.getPlatform(), settings);
  }
}
