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

package defrac.intellij.facet;

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.patterns.ElementPattern;
import com.intellij.util.indexing.FileContent;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfig;
import defrac.intellij.fileType.DefracSettingsFileType;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class DefracFrameworkDetector extends FacetBasedFrameworkDetector<DefracFacet, DefracFacetConfiguration> {
  @NotNull @NonNls public static final String DETECTOR_ID = "defrac";

  public DefracFrameworkDetector() {
    super(DETECTOR_ID);
  }

  @Override
  public void setupFacet(@NotNull final DefracFacet facet, final ModifiableRootModel model) {
    final Module module = facet.getModule();
    final Project project = module.getProject();

    //TODO(joa): import dependencies if missing

    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      @Override
      public void run() {
        importSdkAndFacetConfiguration(facet, model);
        ApplicationManager.getApplication().saveAll();
      }
    });
  }

  private static void importSdkAndFacetConfiguration(@NotNull final DefracFacet facet, @Nullable final ModifiableRootModel model) {
    final Module module = facet.getModule();

    //TODO(joa): setup sdk if missing

    if(model != null && !model.isDisposed() && model.isWritable()) {
      model.setSdk(ModuleRootManager.getInstance(module).getSdk());
    }

    final DefracConfig config = facet.getConfig();

    if(config != null) {
      final DefracPlatform[] targets = config.getTargets();

      if(targets.length == 1) {
        facet.getProperties().PLATFORM = targets[0].name;
      }

      /*final String main = config.getMain();
      if(!isNullOrEmpty(main)) {
        //TODO(joa): create run config for main
      }*/
    }
  }

  @Override
  public FacetType<DefracFacet, DefracFacetConfiguration> getFacetType() {
    return DefracFacet.getFacetType();
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return DefracSettingsFileType.getInstance();
  }

  @NotNull
  @Override
  public ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().withName(Names.default_settings);
  }
}
