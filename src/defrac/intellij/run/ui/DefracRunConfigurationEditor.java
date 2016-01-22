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

import com.android.sdklib.repository.descriptors.IdDisplay;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.ListCellRendererWrapper;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.run.DefracRunConfiguration;
import org.jetbrains.android.run.AvdComboBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static defrac.intellij.run.DefracRunUtil.getCompileTimeQualifiedName;
import static defrac.intellij.run.DefracRunUtil.getRuntimeQualifiedName;

/**
 */
public final class DefracRunConfigurationEditor extends SettingsEditor<DefracRunConfiguration> {
  @NotNull
  private final Project project;

  private JPanel panel;
  private DefracModuleComboBox moduleComboBox;
  private DefracMainClassTextFieldWithBrowseButton mainClassTextField;
  private JRadioButton launchInEmulatorRadioButton;
  private JRadioButton launchOnDeviceRadioButton;
  private AvdComboBox emulatorComboBox;
  private JCheckBox strictModeCheckBox;
  private JCheckBox optimizeCheckBox;

  private String incorrectPreferredAvd;

  public DefracRunConfigurationEditor(@NotNull final Project project) {
    this.project = project;

    moduleComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        validateState();
      }
    });

    launchOnDeviceRadioButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        final boolean selected = launchOnDeviceRadioButton.isSelected();
        launchInEmulatorRadioButton.setSelected(!selected);
        emulatorComboBox.setEnabled(!selected && launchInEmulatorRadioButton.isEnabled());
      }
    });

    launchInEmulatorRadioButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        final boolean selected = launchInEmulatorRadioButton.isSelected();
        launchOnDeviceRadioButton.setSelected(!selected);
        emulatorComboBox.setEnabled(selected && launchInEmulatorRadioButton.isEnabled());
      }
    });

    final DefracMainClassBrowser classBrowser = new DefracMainClassBrowser(project, moduleComboBox.moduleSelector);
    classBrowser.setField(mainClassTextField);
  }

  private void validateState() {
    final Module selectedModule = moduleComboBox.getSelectedModule();

    mainClassTextField.setEnabled(selectedModule != null);

    final DefracFacet facet = DefracFacet.getInstance(selectedModule);

    if(facet != null) {
      final boolean supportLaunchMode = facet.getPlatform().isAndroid();

      launchOnDeviceRadioButton.setEnabled(supportLaunchMode);
      launchInEmulatorRadioButton.setEnabled(supportLaunchMode);
      emulatorComboBox.setEnabled(supportLaunchMode);
    }

    emulatorComboBox.startUpdatingAvds(ModalityState.current());
  }

  private void $$$setupUI$$$() {
  }

  private void createUIComponents() {
    moduleComboBox = new DefracModuleComboBox(project);
    mainClassTextField = new DefracMainClassTextFieldWithBrowseButton(project, moduleComboBox.moduleSelector);
    emulatorComboBox = new AvdComboBox(project, true, false) {
      @Nullable
      @Override
      public Module getModule() {
        return moduleComboBox.moduleSelector.getModule();
      }
    };
    emulatorComboBox.getComboBox().setRenderer(new ListCellRendererWrapper() {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if(value instanceof IdDisplay) {
          setText(((IdDisplay) value).getDisplay());
        }
      }
    });
  }

  @Override
  protected void resetEditorFrom(final DefracRunConfiguration configuration) {
    moduleComboBox.moduleSelector.reset(configuration);
    mainClassTextField.setText(getCompileTimeQualifiedName(configuration.getRunClass()));

    strictModeCheckBox.setSelected(configuration.isStrict());
    optimizeCheckBox.setSelected(configuration.getOptimize());

    launchOnDeviceRadioButton.setSelected(configuration.launchOnDevice());
    launchInEmulatorRadioButton.setSelected(configuration.launchInEmulator());

    final JComboBox combo = emulatorComboBox.getComboBox();
    final String avdName = configuration.getEmulator();

    if(avdName != null) {
      final Object avdDisplay = findAvdWithName(combo, avdName);

      combo.setSelectedItem(avdDisplay);

      if(avdDisplay == null) {
        incorrectPreferredAvd = avdName;
      }
    }

    validateState();
  }

  @Override
  protected void applyEditorTo(final DefracRunConfiguration configuration) throws ConfigurationException {
    final PsiClass mainClass = moduleComboBox.moduleSelector.findClass(mainClassTextField.getText());

    moduleComboBox.moduleSelector.applyTo(configuration);
    configuration.setMainClassName(getRuntimeQualifiedName(mainClass));

    configuration.setStrict(strictModeCheckBox.isSelected());
    configuration.setOptimize(optimizeCheckBox.isSelected());

    if(launchInEmulatorRadioButton.isSelected()) {
      configuration.setLaunchInEmulator();
    } else if(launchOnDeviceRadioButton.isSelected()) {
      configuration.setLaunchOnDevice();
    }

    final IdDisplay preferredAvd = (IdDisplay) emulatorComboBox.getComboBox().getSelectedItem();

    if(preferredAvd == null) {
      configuration.setEmulator(incorrectPreferredAvd != null ? incorrectPreferredAvd : "");
    } else {
      configuration.setEmulator(preferredAvd.getId());
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return panel;
  }

  @Nullable
  private static Object findAvdWithName(@NotNull final JComboBox avdCombo,
                                        @NotNull final String avdName) {
    for(int i = 0, n = avdCombo.getItemCount(); i < n; ++i) {
      final Object item = avdCombo.getItemAt(i);

      if(item instanceof IdDisplay && avdName.equals(((IdDisplay) item).getId())) {
        return item;
      }
    }

    return null;
  }
}
