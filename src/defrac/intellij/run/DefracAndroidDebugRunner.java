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
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.android.run.AndroidDebugRunner;
import org.jetbrains.annotations.NotNull;

/**
 */
public final class DefracAndroidDebugRunner extends AndroidDebugRunner {
  @NotNull
  @Override
  public String getRunnerId() {
    return "DefracAndroidDebugRunner";
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull final RunProfileState state, @NotNull final ExecutionEnvironment environment) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    return super.doExecute(state, environment);
  }

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    if(profile instanceof DefracRunConfiguration) {
      final DefracRunConfiguration configuration = (DefracRunConfiguration) profile;
      final DefracFacet facet = configuration.getFacet();

      return facet != null && facet.getPlatform().isAndroid();
    }
    return false;
  }
}
