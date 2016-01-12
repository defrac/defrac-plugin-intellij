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

package defrac.intellij.run.ui;

import com.intellij.execution.ui.ClassBrowser;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.ide.util.ClassFilter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import defrac.intellij.run.DefracRunUtil;
import org.jetbrains.annotations.NotNull;

/**
 */
public final class DefracMainClassBrowser extends ClassBrowser {
  @NotNull
  private final ConfigurationModuleSelector moduleSelector;
  @NotNull
  private final ClassFilter.ClassFilterWithScope classFilter;

  public DefracMainClassBrowser(@NotNull final Project project,
                                @NotNull final ConfigurationModuleSelector moduleSelector) {
    super(project, "Choose main class");

    this.moduleSelector = moduleSelector;
    this.classFilter = new ClassFilter.ClassFilterWithScope() {
      @Override
      public GlobalSearchScope getScope() {
        return GlobalSearchScope.moduleScope(moduleSelector.getModule());
      }

      @Override
      public boolean isAccepted(final PsiClass psiClass) {
        return DefracRunUtil.isValidMainClass(moduleSelector.getModule(), psiClass);
      }
    };
  }

  @Override
  protected ClassFilter.ClassFilterWithScope getFilter() throws NoFilterException {
    return classFilter;
  }

  @Override
  protected PsiClass findClass(final String s) {
    return moduleSelector.findClass(s);
  }
}
