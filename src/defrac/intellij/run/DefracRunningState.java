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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.projectRoots.Sdk;
import defrac.intellij.DefracBundle;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.sdk.DefracVersion;
import defrac.intellij.util.DefracCommandLineBuilder;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracRunningState extends CommandLineState {
  @NotNull
  private final DefracFacet facet;

  private final boolean isDebug;

  public DefracRunningState(@NotNull final ExecutionEnvironment environment,
                            @NotNull final DefracFacet facet,
                            final boolean isDebug) {
    super(environment);

    this.facet = facet;
    this.isDebug = isDebug;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    final Sdk defracSdk = facet.getDefracSdk();

    if(defracSdk == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noSDK"));
    }

    final DefracVersion defracVersion = facet.getDefracVersion();

    if(defracVersion == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noVersion"));
    }

    final GeneralCommandLine cmdLine =
        DefracCommandLineBuilder.forFacet(facet).
            debug(isDebug).
            command("run").
            build();

    return new KillableColoredProcessHandler(cmdLine);
  }
}
