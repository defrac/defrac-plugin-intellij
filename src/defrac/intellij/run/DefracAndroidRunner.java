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

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.DefaultProgramRunner;
import defrac.intellij.DefracPlatform;
import org.jetbrains.android.run.AndroidDebugRunner;
import org.jetbrains.annotations.NotNull;

/**
 */
public final class DefracAndroidRunner extends AndroidDebugRunner {
  @NotNull
  @Override
  public String getRunnerId() {
    return "defrac.android.run";
  }

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefracRunUtil.canRun(executorId, profile, DefracPlatform.ANDROID, false);
  }
}
