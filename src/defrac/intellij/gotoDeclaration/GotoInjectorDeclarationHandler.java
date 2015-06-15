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

package defrac.intellij.gotoDeclaration;

import com.intellij.psi.PsiReference;
import defrac.intellij.psi.InjectorClassReference;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 *
 */
public final class GotoInjectorDeclarationHandler extends GotoDeclarationHandlerBase {
  public GotoInjectorDeclarationHandler() {
    super(Collections.singleton(Names.defrac_annotation_Injector), false);
  }

  @Override
  protected boolean isDefracReference(@NotNull final PsiReference reference) {
    return reference instanceof InjectorClassReference;
  }
}
