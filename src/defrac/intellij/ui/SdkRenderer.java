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

import com.intellij.ide.util.projectWizard.ProjectJdkListRenderer;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.ui.SimpleTextAttributes;
import defrac.intellij.sdk.DefracSdkAdditionalData;
import defrac.intellij.sdk.DefracSdkUtil;
import icons.DefracIcons;

import javax.swing.*;

/**
 *
 */
public final class SdkRenderer extends ProjectJdkListRenderer {
  @Override
  public void doCustomize(JList list, Object value, int index, boolean selected, boolean hasFocus)  {
    if(value instanceof Sdk) {
      final Sdk sdk = (Sdk)value;

      append(sdk.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

      if(DefracSdkUtil.isDefracSdk(sdk)) {
        final DefracSdkAdditionalData data = (DefracSdkAdditionalData)sdk.getSdkAdditionalData();

        setIcon(DefracIcons.Defrac16x16);

        if(data != null && data.getJavaSdk() != null) {
          append(" (" + data.getJavaSdk().getVersionString() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
      }
    }
  }
}
