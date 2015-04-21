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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameHelper;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.action.create.ui.MultiPlatformCreateDialog;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.fileTemplate.DefracFileTemplateProvider;
import defrac.intellij.project.DefracProjectUtil;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public class NewDelegateAction extends MultiPlatformCreateAction<PsiFile> {
  @NotNull
  private static final PlatformSpecificCreator.ModuleFilter MODULE_FILTER =
      new PlatformSpecificCreator.ModuleFilter() {
        @Override
        public Module[] getModules(@NotNull final Project project, @NotNull final DefracPlatform platform) {
          return DefracProjectUtil.findModulesForPlatform(project, platform, new Condition<Module>() {
            @Override
            public boolean value(final Module module) {
              return !checkNotNull(DefracFacet.getInstance(module)).isMacroLibrary();
            }
          });
        }
      };

  public NewDelegateAction() {
    super(Conditions.and(IS_GENERIC, IS_IN_SOURCE));
  }

  @Override
  protected void updateDialog(@NotNull final Project project,
                              @NotNull final DefracFacet facet,
                              @NotNull final AnActionEvent event,
                              @NotNull final MultiPlatformCreateDialog<PsiFile> dialog) {
    dialog.setTitle(DefracBundle.message("dialog.new.delegate.title"));
    dialog.setValidator(new InputValidatorEx() {
      public String getErrorText(String inputString) {
        return inputString.length() > 0 && !PsiNameHelper.getInstance(project).isQualifiedName(inputString)
            ? "This is not a valid Java class name"
            : null;
      }

      public boolean checkInput(String inputString) {
        return true;
      }

      public boolean canClose(String inputString) {
        return !StringUtil.isEmptyOrSpaces(inputString) && this.getErrorText(inputString) == null;
      }
    });
  }

  @NotNull
  @Override
  protected Creator<PsiFile> createGeneric() {
    return new TemplateBasedCreator(DefracFileTemplateProvider.DELEGATE);
  }

  @NotNull
  @Override
  protected Creator<PsiFile> creatorForPlatform(@NotNull final DefracPlatform platform) {
    return new PlatformSpecificCreator<PsiFile>(
        new TemplateBasedCreator(DefracFileTemplateProvider.DELEGATE_IMPLEMENTATION), MODULE_FILTER);
  }
}
