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

package defrac.intellij.util;

import com.google.common.collect.Lists;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.SystemInfo;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.sdk.DefracVersion;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracCommandLineBuilder {
  public static DefracCommandLineBuilder forFacet(@NotNull final DefracFacet facet) {
    return new DefracCommandLineBuilder(facet);
  }

  @NotNull private final DefracFacet facet;
  @NotNull private String home;
  @NotNull private String project;
  @NotNull private String command = "";
  @NotNull private DefracPlatform platform;
  private boolean debug;
  private boolean skipJavac;

  @NotNull
  public DefracCommandLineBuilder debug(final boolean value) {
    debug = value;
    return this;
  }

  @NotNull
  public DefracCommandLineBuilder skipJavac(final boolean value) {
    skipJavac = value;
    return this;
  }

  @NotNull
  public DefracCommandLineBuilder home(@NotNull final String value) {
    home = value;
    return this;
  }

  @NotNull
  public DefracCommandLineBuilder project(@NotNull final String value) {
    project = value;
    return this;
  }

  @NotNull
  public DefracCommandLineBuilder command(@NotNull final String value) {
    command = value;
    return this;
  }

  public GeneralCommandLine build() {
    final Sdk sdk = facet.getDefracSdk();

    if(sdk == null) {
      throw new IllegalStateException(DefracBundle.message("facet.error.noSDK"));
    }

    final DefracVersion version = facet.getDefracVersion();

    if(version == null) {
      throw new IllegalStateException(DefracBundle.message("facet.error.noVersion"));
    }

    final ArrayList<String> cmd = Lists.newArrayList();
    final String executable = sdk.getHomePath()+File.separatorChar+"defrac"+(SystemInfo.isWindows ? ".bat" : "");

    cmd.add(executable);

    if(debug) {
      cmd.add("-debug");
    }

    if(skipJavac) {
      cmd.add("-skip-javac");
    }

    if(!isNullOrEmpty(home)) {
      cmd.add("-home");
      cmd.add(home);
    }

    if(!isNullOrEmpty(project)) {
      cmd.add("-project");
      cmd.add(project);
    }

    if(!isNullOrEmpty(command)) {
      cmd.add((platform.isGeneric() ? "" : platform.name+":")+command);
    }

    return new GeneralCommandLine(cmd).
        withWorkDirectory(facet.getSettingsFile().getParentFile());
  }

  private DefracCommandLineBuilder(@NotNull final DefracFacet facet) {
    final Sdk sdk = facet.getDefracSdk();

    if(sdk == null) {
      throw new IllegalStateException(DefracBundle.message("facet.error.noSDK"));
    }

    final DefracVersion version = facet.getDefracVersion();

    if(version == null) {
      throw new IllegalStateException(DefracBundle.message("facet.error.noVersion"));
    }

    this.facet = facet;
    this.platform = facet.getPlatform();
    this.home = checkNotNull(sdk.getHomePath());
    this.project = facet.getSettingsFile().getParentFile().getAbsolutePath();
    this.skipJavac = false;
    this.debug = false;

    //TODO(joa): configure android sdk if available!
    //TODO(joa): configure settings when console supports it

    //TODO(joa): whats wrong with intellij?
    //skipJavac = facet.skipJavac();
  }
}
