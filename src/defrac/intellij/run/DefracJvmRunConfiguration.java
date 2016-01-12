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
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public final class DefracJvmRunConfiguration extends DefracApplicationConfiguration {
  public DefracJvmRunConfiguration(@NotNull final Project project,
                                   @NotNull final ConfigurationFactory factory) {
    super("defrac.jvm.runConfig", project, factory);
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor,
                                  @NotNull final ExecutionEnvironment env) throws ExecutionException {
    DefracRunUtil.checkState(this, env);
    return new DefracJvmRunningState(env, this, checkNotNull(DefracFacet.getInstance(getModule())));
  }

  @NotNull
  @Override
  public DefracPlatform getPlatform() {
    return DefracPlatform.JVM;
  }
}
