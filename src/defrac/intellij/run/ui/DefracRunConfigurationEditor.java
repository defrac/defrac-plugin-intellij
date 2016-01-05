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

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ui.ClassBrowser;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.ide.util.ClassFilter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.run.DefracRunConfiguration;
import defrac.intellij.run.RunConfigurationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 *
 */
public final class DefracRunConfigurationEditor extends SettingsEditor<DefracRunConfiguration> {
  @NotNull
  private final Project project;
  @NotNull
  private final ConfigurationModuleSelector moduleSelector;

  private JPanel componentPanel;
  private ModulesComboBox moduleComboBox;
  private EditorTextFieldWithBrowseButton editorTextFieldWithBrowseButton;
  private JLabel moduleLabel;

  @SuppressWarnings("unchecked")
  public DefracRunConfigurationEditor(@NotNull final Project project) {
    this.project = project;

    this.moduleSelector = new ConfigurationModuleSelector(project, moduleComboBox) {
      @Override
      public boolean isModuleAccepted(final Module module) {
        if(module == null || !super.isModuleAccepted(module)) {
          return false;
        }

        final DefracFacet facet = DefracFacet.getInstance(module);

        return facet != null
            && !facet.getPlatform().isGeneric()
            && !facet.isMacroLibrary();
      }
    };

    moduleLabel.setLabelFor(moduleComboBox);

    moduleComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        // we need a module to select the main class
        editorTextFieldWithBrowseButton.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });

    // disabled per default. Will be enabled when module is selected
    editorTextFieldWithBrowseButton.setEnabled(false);

    final ClassBrowser classBrowser = new ClassBrowser(project, "Select main class") {
      final ClassFilter.ClassFilterWithScope classFilterWithScope = new ClassFilter.ClassFilterWithScope() {
        @Override
        public GlobalSearchScope getScope() {
          return GlobalSearchScope.moduleScope(moduleSelector.getModule());
        }

        @Override
        public boolean isAccepted(final PsiClass psiClass) {
          return isValidMainClass(psiClass);
        }
      };

      @Override
      protected ClassFilter.ClassFilterWithScope getFilter() throws NoFilterException {
        return classFilterWithScope;
      }

      @Override
      protected PsiClass findClass(final String s) {
        return moduleSelector.findClass(s);
      }
    };

    classBrowser.setField(editorTextFieldWithBrowseButton);
  }

  private boolean isValidMainClass(@Nullable final PsiClass cls) {
    return RunConfigurationUtil.isValidMainClass(moduleSelector.getModule(), cls);

  }

  private void $$$setupUI$$$() {
    createUIComponents();
  }

  private void createUIComponents() {
    editorTextFieldWithBrowseButton = new EditorTextFieldWithBrowseButton(project, true, new JavaCodeFragment.VisibilityChecker() {
      public Visibility isDeclarationVisible(PsiElement declaration, PsiElement place) {
        if(declaration instanceof PsiClass) {
          final PsiClass aClass = (PsiClass) declaration;

          if(isValidMainClass(aClass) || place.getParent() != null && isValidMainClass(moduleSelector.findClass(aClass.getQualifiedName()))) {
            return Visibility.VISIBLE;
          }
        }

        return Visibility.NOT_VISIBLE;
      }
    });
  }

  @Override
  protected void resetEditorFrom(final DefracRunConfiguration configuration) {
    moduleSelector.reset(configuration);
    editorTextFieldWithBrowseButton.setText(configuration.getCompileTimeQualifiedRunClass());
  }

  @Override
  protected void applyEditorTo(final DefracRunConfiguration configuration) throws ConfigurationException {
    moduleSelector.applyTo(configuration);
    configuration.setRunClass(moduleSelector.findClass(editorTextFieldWithBrowseButton.getText()));
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return componentPanel;
  }
}
