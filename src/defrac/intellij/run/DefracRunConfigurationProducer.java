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

package defrac.intellij.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public final class DefracRunConfigurationProducer extends JavaRunConfigurationProducerBase<DefracRunConfiguration> {

  public DefracRunConfigurationProducer(@NotNull final ConfigurationFactory factory) {
    super(factory);
  }

  @Override
  protected boolean setupConfigurationFromContext(final DefracRunConfiguration config,
                                                  final ConfigurationContext context,
                                                  final Ref<PsiElement> sourceElement) {
    final PsiElement entryPoint = DefracRunUtil.findEntryPoint(context);

    if(entryPoint == null) {
      return false;
    }

    sourceElement.set(entryPoint);

    config.setMainClass(checkNotNull(DefracPsiUtil.enclosingClass(entryPoint)));
    config.setGeneratedName();

    setupConfigurationModule(context, config);

    return true;
  }

  @Override
  public boolean isConfigurationFromContext(final DefracRunConfiguration config,
                                            final ConfigurationContext context) {
    return Comparing.equal(DefracRunUtil.getRuntimeQualifiedName(DefracRunUtil.findMainClass(context)), config.getRunClass())
        && Comparing.equal(context.getModule(), config.getModule());
  }
}
