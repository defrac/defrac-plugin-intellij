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

package defrac.intellij.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.JavaContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class DefracModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
  public DefracModuleConfigurationEditorProvider() {}

  @Override
  public ModuleConfigurationEditor[] createEditors(@NotNull final ModuleConfigurationState state) {
    final Module module = state.getRootModel().getModule();

    if(!DefracModuleUtil.isDefracModule(module)) {
      return ModuleConfigurationEditor.EMPTY;
    }

    return new ModuleConfigurationEditor[] {
        new JavaContentEntriesEditor(module.getName(), state),
        new ClasspathEditor(state),
    };
  }
}
