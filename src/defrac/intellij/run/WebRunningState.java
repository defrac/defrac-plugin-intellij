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
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import defrac.intellij.DefracBundle;
import defrac.intellij.config.DefracConfigOracle;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.project.DefracProcess;
import defrac.intellij.run.web.DefracBrowserUtil;
import defrac.intellij.sdk.DefracVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;

/**
 *
 */
public final class WebRunningState implements RunProfileState {
  @NotNull
  private final ExecutionEnvironment environment;

  @NotNull
  private final DefracRunConfiguration configuration;


  public WebRunningState(@NotNull final ExecutionEnvironment environment,
                         @NotNull final DefracRunConfiguration configuration) {
    this.environment = environment;
    this.configuration = configuration;
  }

  @Nullable
  @Override
  public ExecutionResult execute(final Executor executor,
                                 @NotNull final ProgramRunner runner) throws ExecutionException {
    final Module module = configuration.getConfigurationModule().getModule();
    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.facetMissing", module));
    }

    final Sdk defracSdk = facet.getDefracSdk();

    if(defracSdk == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noSDK"));
    }

    final DefracVersion defracVersion = facet.getDefracVersion();

    if(defracVersion == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noVersion"));
    }

    final DefracConfigOracle config = facet.getConfigOracle();

    if(config == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noSettings"));
    }

    final File browser = DefracBrowserUtil.getBrowser(config);
    final int port = DefracProcess.getInstance(environment.getProject()).getWebServerPort();
    final URI uri = URI.create("http://127.0.0.1:"+port+'/');

    if(!DefracBrowserUtil.isChromium(browser)) {
      BrowserUtil.browse(uri);
    } else {
      //TODO(joa): intellij remote debug for chrome...
      BrowserUtil.browse(uri);
    }

    return null;
  }
}
