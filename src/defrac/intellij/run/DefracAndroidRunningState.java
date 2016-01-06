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

import com.intellij.execution.runners.ExecutionEnvironment;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.run.AndroidApplicationLauncher;
import org.jetbrains.android.run.AndroidRunConfigurationBase;
import org.jetbrains.android.run.AndroidRunningState;
import org.jetbrains.android.run.TargetChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 */
public final class DefracAndroidRunningState extends AndroidRunningState {
  public DefracAndroidRunningState(@NotNull final ExecutionEnvironment environment,
                                   @NotNull final AndroidFacet facet,
                                   @Nullable final TargetChooser targetChooser,
                                   @NotNull final String commandLine,
                                   final AndroidApplicationLauncher applicationLauncher,
                                   final boolean supportMultipleDevices,
                                   final boolean clearLogcatBeforeStart,
                                   @NotNull final AndroidRunConfigurationBase configuration,
                                   final boolean nonDebuggableOnDevice) {
    super(environment, facet, targetChooser, commandLine, applicationLauncher, supportMultipleDevices, clearLogcatBeforeStart, configuration, nonDebuggableOnDevice);
  }
}
