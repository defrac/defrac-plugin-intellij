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

package defrac.intellij.action.create;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.util.DirectoryChooser;
import com.intellij.ide.util.PackageUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import defrac.intellij.DefracPlatform;
import defrac.intellij.action.DefracAction;
import defrac.intellij.action.create.ui.MultiPlatformCreateDialog;
import defrac.intellij.config.DefracConfig;
import defrac.intellij.facet.DefracFacet;
import org.apache.velocity.runtime.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static defrac.intellij.fileTemplate.DefracFileTemplateUtil.PLATFORM_ALL;
import static defrac.intellij.fileTemplate.DefracFileTemplateUtil.getPlatformKey;

/**
 *
 */
public abstract class MultiPlatformCreateAction<T extends PsiElement> extends DefracAction {
  @NotNull
  private static final Logger LOG = Logger.getInstance(MultiPlatformCreateAction.class.getName());

  @SuppressWarnings("unchecked")
  protected static <T extends PsiElement> Creator<T> nullCreator() {
    return (Creator<T>)NullCreator.INSTANCE;
  }

  @Nullable
  protected static PsiFile createFileFromTemplate(@Nullable String name,
                                                @NotNull FileTemplate template,
                                                @NotNull PsiDirectory dir,
                                                @Nullable String defaultTemplateProperty,
                                                @NotNull final DefracPlatform platform,
                                                @NotNull final Set<DefracPlatform> enabledPlatforms) {
    if(name != null) {
      final CreateFileAction.MkDirs mkdirs = new CreateFileAction.MkDirs(name, dir);
      name = mkdirs.newName;
      dir = mkdirs.directory;
    }

    PsiElement element;
    Project project = dir.getProject();

    try {
      final Properties properties = FileTemplateManager.getInstance().getDefaultProperties(project);

      //minus one for generic
      final boolean isAllPlatforms = enabledPlatforms.size() == (DefracPlatform.values().length - 1);

      for(final DefracPlatform enabledPlatform : enabledPlatforms) {
        properties.setProperty(getPlatformKey(enabledPlatform), Boolean.TRUE.toString());
      }

      if(isAllPlatforms) {
        properties.setProperty(PLATFORM_ALL, Boolean.TRUE.toString());
      }

      element =
          FileTemplateUtil.createFromTemplate(template, name, properties, dir);

      final PsiFile psiFile = element.getContainingFile();
      final VirtualFile virtualFile = psiFile.getVirtualFile();

      if(virtualFile != null) {
        if(platform.isGeneric()) {
          FileEditorManager.getInstance(project).openFile(virtualFile, true);
        }

        if(defaultTemplateProperty != null) {
          PropertiesComponent.getInstance(project).setValue(defaultTemplateProperty, template.getName());
        }

        return psiFile;
      }
    } catch(final ParseException parseException) {
      Messages.showErrorDialog(project, "Error parsing Velocity template: " + parseException.getMessage(), "Create File from Template");
      return null;
    } catch(final IncorrectOperationException incorrectOperation) {
      throw incorrectOperation;
    } catch(final Exception exception) {
      LOG.error(exception);
    }

    return null;
  }

  public MultiPlatformCreateAction(Condition<AnActionEvent> condition) {
    super(condition);
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent event) {
    final DataContext dataContext = event.getDataContext();

    final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
    if(view == null) {
      return;
    }

    final Project project = event.getProject();
    if(project == null) {
      return;
    }

    final DefracFacet facet = getFacet(event);
    final DefracConfig config = facet.getConfig();
    if(config == null) {
      return;
    }

    final PsiDirectory dir = view.getOrChooseDirectory();
    if(dir == null) {
      return;
    }

    final MultiPlatformCreateDialog<T> dialog =
        MultiPlatformCreateDialog.create(
            event.getProject(),
            ImmutableSet.copyOf(config.getTargets()),
            createGeneric(),
            createAndroid(), createIOS(),
            createJVM(), createWeb());

    final MultiPlatformCreateDialog.Result<T> result = dialog.getResult(project, dir);

    if(result != null && result.generic != null) {
      view.selectElement(result.generic);
    }
  }

  @NotNull
  protected abstract Creator<T> createGeneric();

  @NotNull
  protected Creator<T> createAndroid() {
    return creatorForPlatform(DefracPlatform.ANDROID);
  }

  @NotNull
  protected Creator<T> createIOS() {
    return creatorForPlatform(DefracPlatform.IOS);
  }

  @NotNull
  protected Creator<T> createJVM() {
    return creatorForPlatform(DefracPlatform.JVM);
  }

  @NotNull
  protected Creator<T> createWeb() {
    return creatorForPlatform(DefracPlatform.WEB);
  }

  @NotNull
  protected Creator<T> creatorForPlatform(@NotNull final DefracPlatform platform) {
    return nullCreator();

  }

  public static interface Creator<T extends PsiElement> {
    @Nullable
    public T createElement(@NotNull final String name,
                           @NotNull final Project project,
                           @NotNull final PsiDirectory dir,
                           @NotNull final DefracPlatform platform,
                           @NotNull final Set<DefracPlatform> enabledPlatforms);
  }

  private static enum NullCreator implements Creator<PsiElement> {
    INSTANCE;

    @Nullable
    @Override
    public PsiElement createElement(@NotNull final String name,
                                    @NotNull final Project project,
                                    @NotNull final PsiDirectory dir,
                                    @NotNull final DefracPlatform platform,
                                    @NotNull final Set<DefracPlatform> enabledPlatforms) {
      return null;
    }
  }

  protected static final class TemplateBasedCreator implements Creator<PsiFile> {
    @NotNull
    private final String templateName;

    public TemplateBasedCreator(@NotNull final String templateName) {
      this.templateName = templateName;
    }

    @Nullable
    @Override
    public PsiFile createElement(@NotNull final String name,
                                 @NotNull final Project project,
                                 @NotNull final PsiDirectory dir,
                                 @NotNull final DefracPlatform platform,
                                 @NotNull final Set<DefracPlatform> enabledPlatforms) {
      return createFileFromTemplate(
          name,
          FileTemplateManager.getInstance().getInternalTemplate(templateName),
          dir, null, platform, enabledPlatforms);
    }
  }

  protected static final class PlatformSpecificCreator<T extends PsiElement> implements Creator<T> {
    public static interface ModuleFilter {
      public Module[] getModules(@NotNull final Project project, @NotNull final DefracPlatform platform);
    }

    @NotNull private final Creator<T> creator;

    @NotNull
    private final ModuleFilter moduleFilter;

    public PlatformSpecificCreator(@NotNull final Creator<T> creator,
                                   @NotNull final ModuleFilter moduleFilter) {
      this.creator = creator;
      this.moduleFilter = moduleFilter;
    }

    @Nullable
    @Override
    public T createElement(@NotNull final String name,
                           @NotNull final Project project,
                           @NotNull final PsiDirectory dir,
                           @NotNull final DefracPlatform platform,
                           @NotNull final Set<DefracPlatform> enabledPlatforms) {
      final PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(dir);

      if(psiPackage == null) {
        return null;
      }

      final Module[] availablePlatformModules = moduleFilter.getModules(project, platform);

      final Module module;
      final PsiDirectory baseDir;

      if(availablePlatformModules.length == 0) {
        final Module[] modules = ModuleManager.getInstance(project).getModules();

        if(modules.length < 1) {
          return null;
        }

        module = modules[0];
        baseDir = null;
      } else {
        if(availablePlatformModules.length == 1) {
          module = availablePlatformModules[0];
          baseDir = null;
        } else {
          final PsiManager psiManager = PsiManager.getInstance(project);
          final DirectoryChooser chooser = new DirectoryChooser(project);
          final Map<PsiDirectory,Module> dirs = Maps.newHashMap();

          for(final Module platformModule : availablePlatformModules) {
            final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(platformModule);
            for(final VirtualFile sourceRoot : moduleRootManager.getSourceRoots()) {
              final PsiDirectory candidate = psiManager.findDirectory(sourceRoot);
              dirs.put(candidate, platformModule);
            }
          }

          final PsiDirectory[] array = dirs.keySet().toArray(new PsiDirectory[dirs.size()]);
          chooser.fillList(array, null, project, (Map<PsiDirectory, String>) null);
          chooser.setTitle("Choose "+platform.displayName+" target ...");
          chooser.show();

          if(chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
            return null;
          }

          final PsiDirectory result =
              chooser.getSelectedDirectory();

          module = dirs.get(result);
          baseDir = result;
        }
      }

      final PsiDirectory actualDir =
          PackageUtil.findOrCreateDirectoryForPackage(module, psiPackage.getQualifiedName(), baseDir, false, false);

      if(actualDir == null) {
        return null;
      }

      return creator.createElement(name, project, actualDir, platform, Collections.singleton(platform));
    }
  }
}
