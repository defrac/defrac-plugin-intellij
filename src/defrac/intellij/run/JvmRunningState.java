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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.BaseJavaApplicationCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathsList;
import defrac.intellij.DefracBundle;
import defrac.intellij.config.DefracConfigOracle;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.sdk.DefracVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public final class JvmRunningState extends BaseJavaApplicationCommandLineState<DefracRunConfiguration> {
  @NotNull
  private final DefracFacet facet;

  public JvmRunningState(@NotNull final ExecutionEnvironment environment,
                         @NotNull final DefracRunConfiguration configuration,
                         @NotNull final DefracFacet facet) {
    super(environment, configuration);
    this.facet = facet;
  }

  @Override
  protected JavaParameters createJavaParameters() throws ExecutionException {
    final DefracVersion defrac = facet.getDefracVersion();

    if(defrac == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noVersion"));
    }

    final JavaParameters params = new JavaParameters();
    final JavaRunConfigurationModule module = myConfiguration.getConfigurationModule();
    final int classPathType = JavaParametersUtil.getClasspathType(module, myConfiguration.MAIN_CLASS_NAME, false);
    final String jreHome = myConfiguration.ALTERNATIVE_JRE_PATH_ENABLED ? myConfiguration.ALTERNATIVE_JRE_PATH : null;
    final String projectBasePath = FileUtil.toSystemDependentName(checkNotNull(module.getProject().getBaseDir().getCanonicalPath()));
    final String defracHomePath = FileUtil.toSystemDependentName(checkNotNull(checkNotNull(facet.getDefracSdk()).getHomePath()));
    final VirtualFile settingsFile = facet.getVirtualSettingsFile();
    final VirtualFile nat1ve = defrac.getNative(), nativeJvmDir, nativeJvmPlatformDir;
    final VirtualFile target, targetJvmDir;
    final DefracConfigOracle config = facet.getConfigOracle();

    if(config == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noSettings"));
    }

    final boolean isDebug =
           DefaultDebugExecutor.EXECUTOR_ID.equals(getEnvironment().getExecutor().getId())
        || config.isDebug();

    final String nativeLibs;

    if(settingsFile == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noSettings"));
    }

    target = settingsFile.getParent().findChild("target");

    if(target == null) {
      throw new ExecutionException("Couldn't find target directory");
    }

    targetJvmDir = target.findChild("jvm");

    if(targetJvmDir == null) {
      throw new ExecutionException("Couldn't find JVM directory");
    }

    if(nat1ve == null) {
      throw new ExecutionException("Couldn't find native libraries");
    }

    nativeJvmDir = nat1ve.findChild("jvm");

    if(nativeJvmDir == null) {
      throw new ExecutionException("Couldn't find native JVM libraries");
    }

    if(SystemInfo.isLinux) {
      nativeLibs = "linux";
    } else if(SystemInfo.isWindows) {
      nativeLibs = "win";
    } else if(SystemInfo.isMac) {
      nativeLibs = "mac";
    } else {
      throw new ExecutionException("Unsupported OS");
    }

    nativeJvmPlatformDir = nativeJvmDir.findChild(nativeLibs);

    if(nativeJvmPlatformDir == null) {
      throw new ExecutionException("Couldn't find native platform libraries");
    }

    // let intellij do its stuff
    JavaParametersUtil.configureModule(module, params, classPathType, jreHome);
    params.setMainClass(myConfiguration.MAIN_CLASS_NAME);
    setupJavaParameters(params);

    // now get rid of all the stuff we don't want from intellij
    // - this includes any classpath inside the project
    // - or any path in the defrac sdk
    final PathsList pathList = params.getClassPath();
    for(final String path : pathList.getPathList()) {
      if(path.startsWith(projectBasePath) || path.startsWith(defracHomePath)) {
        pathList.remove(path);
      }
    }

    // add the classpath of everything we just compiled
    params.getClassPath().add(targetJvmDir);

    // now add the actual runtime dependencies
    for(final VirtualFile runtimeLibrary: defrac.getRuntimeJars()) {
      params.getClassPath().add(runtimeLibrary);
    }

    final ParametersList vmParametersList = params.getVMParametersList();

    if(isDebug) {
      // enable assertions if the user wants to debug the app
      // because it is the same behaviour of jvm:run
      vmParametersList.add("-ea");
    }

    vmParametersList.add("-Ddefrac.hotswap=false");
    vmParametersList.add("-Djava.library.path="+nativeJvmPlatformDir.getCanonicalPath());
    vmParametersList.add("-Xms512M");
    vmParametersList.add("-XX:+TieredCompilation");

    final Iterator<String> resourcePaths = Iterators.transform(
        config.getResources(facet).iterator(),
        new Function<VirtualFile, String>() {
          @Override
          public String apply(final VirtualFile virtualFile) {
            return virtualFile.getCanonicalPath();
          }
        }
    );

    vmParametersList.add("-Ddefrac.resources="+Joiner.on('$').join(resourcePaths));

    return params;
  }
}
