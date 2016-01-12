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
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 */
public abstract class DefracRunConfigurationProducer extends JavaRunConfigurationProducerBase<ModuleBasedConfiguration> {
  @NotNull
  private final DefracPlatform platform;

  public DefracRunConfigurationProducer(@NotNull final ConfigurationFactory factory,
                                        @NotNull final DefracPlatform platform) {
    super(factory);
    this.platform = platform;
  }

  @Override
  protected boolean setupConfigurationFromContext(final ModuleBasedConfiguration configuration,
                                                  final ConfigurationContext context,
                                                  final Ref<PsiElement> sourceElement) {
    if(configuration instanceof DefracRunConfigurationBase) {
      final DefracRunConfigurationBase config = (DefracRunConfigurationBase) configuration;
      final DefracFacet facet = DefracFacet.getInstance(context.getModule());

      if(facet == null || facet.getPlatform() != platform) {
        return false;
      }

      final PsiElement entryPoint = DefracRunUtil.findEntryPoint(context);

      if(entryPoint == null) {
        return false;
      }

      final RunConfiguration runConfig = context.getOriginalConfiguration(null);

      if(runConfig != null) {
        System.out.println(runConfig);
      }
      sourceElement.set(entryPoint);

      config.setModule(context.getModule());
      config.setMain(DefracRunUtil.getRuntimeQualifiedName(DefracPsiUtil.enclosingClass(entryPoint)));
      config.setGeneratedName();

      setupConfigurationModule(context, configuration);
      return true;
    }

    return false;
  }

  @Override
  public boolean isConfigurationFromContext(final ModuleBasedConfiguration configuration,
                                            final ConfigurationContext context) {
    if(configuration instanceof DefracRunConfigurationBase) {
      final DefracRunConfigurationBase config = (DefracRunConfigurationBase) configuration;

      return Comparing.equal(DefracRunUtil.getRuntimeQualifiedName(DefracRunUtil.findMainClass(context)), config.getMain())
          && Comparing.equal(context.getModule(), config.getModule());
    }

    return false;
  }
}
