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

import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 */
public final class DefracRunConfigurationProducer extends JavaRunConfigurationProducerBase<DefracRunConfiguration> implements Cloneable {

  public DefracRunConfigurationProducer() {
    super(DefracConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(final DefracRunConfiguration config,
                                                  final ConfigurationContext context,
                                                  final Ref<PsiElement> sourceElement) {
    final PsiElement entryPoint = findEntryPoint(context);

    if(entryPoint == null) {
      return false;
    }

    sourceElement.set(entryPoint);
    setupConfiguration(config, DefracPsiUtil.enclosingClass(entryPoint), context);

    return true;
  }

  private void setupConfiguration(final DefracRunConfiguration configuration,
                                  final PsiClass mainClass,
                                  final ConfigurationContext context) {
    configuration.setRunClass(mainClass);
    configuration.setModule(context.getModule());
    configuration.setGeneratedName();
    setupConfigurationModule(context, configuration);
  }

  @Override
  public boolean isConfigurationFromContext(final DefracRunConfiguration config,
                                            final ConfigurationContext context) {
    final PsiClass mainClass = findMainClass(context);

    return mainClass != null
        && Comparing.equal(JavaExecutionUtil.getRuntimeQualifiedName(mainClass), config.MAIN_CLASS_NAME)
        && Comparing.equal(context.getModule(), config.getConfigurationModule().getModule());
  }

  @Nullable
  private static PsiClass findMainClass(@NotNull final ConfigurationContext context) {
    return DefracPsiUtil.enclosingClass(RunConfigurationUtil.findEntryPoint(context.getLocation(), context.getModule()));
  }

  @Nullable
  private static PsiElement findEntryPoint(@NotNull final ConfigurationContext context) {
    return RunConfigurationUtil.findEntryPoint(context.getLocation(), context.getModule());
  }
}
