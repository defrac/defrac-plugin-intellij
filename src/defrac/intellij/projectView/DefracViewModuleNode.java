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

package defrac.intellij.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import defrac.intellij.facet.DefracFacet;

/**
 *
 */
final class DefracViewModuleNode extends ProjectViewModuleNode {
  public DefracViewModuleNode(final Project project, final Module value, final ViewSettings viewSettings) {
    super(project, value, viewSettings);
  }

  @Override
  public void update(final PresentationData data) {
    super.update(data);

    final Module module = getValue();

    if(module == null || module.isDisposed()) {
      return;
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      return;
    }

    final String text = DefracProjectViewUtil.macroOrSource(facet);

    data.clearText();
    data.setPresentableText(text);
    data.addText(text, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
  }
}
