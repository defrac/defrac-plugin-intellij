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

package defrac.intellij.action.create.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.Producer;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.action.create.MultiPlatformCreateAction;
import defrac.intellij.ui.DefracPlatformChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public final class MultiPlatformCreateDialog<T extends PsiElement> extends DialogWrapper {
  public static <T extends PsiElement> MultiPlatformCreateDialog<T> create(@NotNull final Project project,
                                                                           @NotNull final Set<DefracPlatform> enabledPlatforms,
                                                                           @NotNull final MultiPlatformCreateAction.Creator<T> generic,
                                                                           @NotNull final MultiPlatformCreateAction.Creator<T> android,
                                                                           @NotNull final MultiPlatformCreateAction.Creator<T> ios,
                                                                           @NotNull final MultiPlatformCreateAction.Creator<T> jvm,
                                                                           @NotNull final MultiPlatformCreateAction.Creator<T> web) {
    return new MultiPlatformCreateDialog<T>(project, enabledPlatforms, generic, android, ios, jvm, web);
  }

  private JPanel componentPanel;
  private JLabel nameLabel;
  private JTextField className;
  private DefracPlatformChooser platformChooser;

  @NotNull private final MultiPlatformCreateAction.Creator<T> generic;
  @NotNull private final MultiPlatformCreateAction.Creator<T> android;
  @NotNull private final MultiPlatformCreateAction.Creator<T> ios;
  @NotNull private final MultiPlatformCreateAction.Creator<T> jvm;
  @NotNull private final MultiPlatformCreateAction.Creator<T> web;

  @Nullable
  private Producer<Boolean> okAction = null;

  public MultiPlatformCreateDialog(@NotNull final Project project,
                                   @NotNull final Set<DefracPlatform> enabledPlatforms,
                                   @NotNull final MultiPlatformCreateAction.Creator<T> generic,
                                   @NotNull final MultiPlatformCreateAction.Creator<T> android,
                                   @NotNull final MultiPlatformCreateAction.Creator<T> ios,
                                   @NotNull final MultiPlatformCreateAction.Creator<T> jvm,
                                   @NotNull final MultiPlatformCreateAction.Creator<T> web) {
    super(project);

    this.generic = generic;
    this.android = android;
    this.ios = ios;
    this.jvm = jvm;
    this.web = web;

    setTitle(DefracBundle.message("dialog.new.delegate.title"));
    init();

    platformChooser.init(enabledPlatforms);
    nameLabel.setLabelFor(className);
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return className;
  }

  @Override
  protected void doOKAction() {
    if(null != okAction) {
      if(checkNotNull(okAction.produce())) {
        super.doOKAction();
      }

      return;
    }

    super.doOKAction();
  }

  @Nullable
  public Result<T> getResult(@NotNull final Project project,
                             @NotNull final PsiDirectory dir) {
    final Ref<Result<T>> created = Ref.create(null);

    okAction = new Producer<Boolean>() {
      @Nullable
      @Override
      public Boolean produce() {
        final String name = className.getText().trim();

        if(name.isEmpty()) {
          return Boolean.FALSE;
        }

        final Set<DefracPlatform> enabledPlatforms = platformChooser.getPlatforms();
        final T genericElement = createElement(generic, project, dir, name, DefracPlatform.GENERIC, enabledPlatforms);

        if(genericElement == null) {
          return Boolean.FALSE;
        }

        final T androidElement = createElement(android, project, dir, name, DefracPlatform.ANDROID, enabledPlatforms);
        final T iosElement     = createElement(ios    , project, dir, name, DefracPlatform.IOS    , enabledPlatforms);
        final T jvmElement     = createElement(jvm    , project, dir, name, DefracPlatform.JVM    , enabledPlatforms);
        final T webElement     = createElement(web    , project, dir, name, DefracPlatform.WEB    , enabledPlatforms);

        created.set(new Result<T>(genericElement, androidElement, iosElement, jvmElement, webElement));
        return Boolean.TRUE;
      }
    };

    show();

    if(getExitCode() == OK_EXIT_CODE) {
      return created.get();
    }

    return null;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return componentPanel;
  }

  private static <T extends PsiElement> T createElement(@NotNull final MultiPlatformCreateAction.Creator<T> creator,
                                                        @NotNull final Project project,
                                                        @NotNull final PsiDirectory dir,
                                                        @NotNull final String name,
                                                        @NotNull final DefracPlatform platform,
                                                        @NotNull final Set<DefracPlatform> enabledPlatforms) {
    return creator.createElement(name, project, dir, platform, enabledPlatforms);
  }

  public static class Result<T extends PsiElement> {
    @Nullable
    public final T generic, android, ios, jvm, web;

    private Result(@Nullable T generic,
                   @Nullable T android,
                   @Nullable T ios,
                   @Nullable T jvm,
                   @Nullable T web) {
      this.generic = generic;
      this.android = android;
      this.ios = ios;
      this.jvm = jvm;
      this.web = web;
    }
  }
}
