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

package defrac.intellij.ui;

import com.google.common.collect.Sets;
import defrac.intellij.DefracPlatform;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Set;

/**
 *
 */
public final class DefracPlatformChooser {
  private JPanel componentPanel;
  private JCheckBox androidCheckBox;
  private JCheckBox iosCheckBox;
  private JCheckBox jvmCheckBox;
  private JCheckBox webCheckBox;

  public DefracPlatformChooser() {
    // note: don't change signature of <init> since it'll break intellij form stuff
  }

  public void init(@NotNull final Iterable<DefracPlatform> enabledPlatforms) {
    disableAndDeselect(androidCheckBox);
    disableAndDeselect(iosCheckBox);
    disableAndDeselect(jvmCheckBox);
    disableAndDeselect(webCheckBox);

    for(final DefracPlatform platform : enabledPlatforms) {
      switch(platform) {
        case ANDROID: enableAndSelect(androidCheckBox); break;
        case IOS: enableAndSelect(iosCheckBox); break;
        case JVM: enableAndSelect(jvmCheckBox); break;
        case WEB: enableAndSelect(webCheckBox); break;
      }
    }
  }

  @NotNull
  public JPanel getComponentPanel() {
    return componentPanel;
  }

  public void setAll(final boolean android,
                     final boolean ios,
                     final boolean jvm,
                     final boolean web) {
    setAndroid(android);
    setIOS(ios);
    setJVM(jvm);
    setWeb(web);
  }

  public boolean getAndroid() {
    return androidCheckBox.isSelected();
  }

  public void setAndroid(final boolean selected) {
    androidCheckBox.setSelected(selected);
  }

  public boolean getIOS() {
    return iosCheckBox.isSelected();
  }

  public void setIOS(final boolean selected) {
    iosCheckBox.setSelected(selected);
  }

  public boolean getJVM() {
    return jvmCheckBox.isSelected();
  }

  public void setJVM(final boolean selected) {
    jvmCheckBox.setSelected(selected);
  }

  public boolean getWeb() {
    return webCheckBox.isSelected();
  }

  public void setWeb(final boolean selected) {
    webCheckBox.setSelected(selected);
  }

  private static void enableAndSelect(@NotNull final JCheckBox checkBox) {
    checkBox.setEnabled(true);
    checkBox.setSelected(true);
  }

  private void disableAndDeselect(@NotNull final JCheckBox checkBox) {
    checkBox.setEnabled(false);
    checkBox.setSelected(false);
  }

  public Set<DefracPlatform> getPlatforms() {
    final Set<DefracPlatform> set = Sets.newHashSetWithExpectedSize(4);
    if(getAndroid()) set.add(DefracPlatform.ANDROID);
    if(getIOS()) set.add(DefracPlatform.IOS);
    if(getJVM()) set.add(DefracPlatform.JVM);
    if(getWeb()) set.add(DefracPlatform.WEB);
    return set;
  }
}
