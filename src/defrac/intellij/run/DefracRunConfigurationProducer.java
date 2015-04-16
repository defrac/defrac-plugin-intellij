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
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.psi.util.PsiTreeUtil;
import defrac.intellij.config.DefracConfig;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public final class DefracRunConfigurationProducer extends JavaRunConfigurationProducerBase<DefracRunConfiguration> {
  @NotNull
  private static final Logger LOG = Logger.getInstance(DefracRunConfigurationProducer.class.getName());
  @NotNull
  private static final String ACTIVITY_BASE_CLASS_NAME = "android.app.Activity";

  public DefracRunConfigurationProducer() {
    super(DefracConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(final DefracRunConfiguration config,
                                                  final ConfigurationContext context,
                                                  final Ref<PsiElement> sourceElement) {
    final PsiClass cls = findEntryPoint(context);

    if(cls == null) {
      return false;
    }

    sourceElement.set(cls);

    updateConfig(config, context, cls);

    return true;
  }

  @Override
  public boolean isConfigurationFromContext(final DefracRunConfiguration config,
                                            final ConfigurationContext context) {
    final PsiClass cls = findEntryPoint(context);

    return cls != null
        && Comparing.equal(JavaExecutionUtil.getRuntimeQualifiedName(cls), config.MAIN_CLASS_NAME)
        && Comparing.equal(context.getModule(), config.getConfigurationModule().getModule());
  }

  private void updateConfig(@NotNull final DefracRunConfiguration config,
                            @NotNull final ConfigurationContext context,
                            @NotNull final PsiClass cls) {
    setupConfigurationModule(context, config);
    config.setGeneratedName();

    // always update main class in defrac config
    final DefracConfig defracConfig = checkNotNull(checkNotNull(DefracFacet.getInstance(cls)).getConfig());
    defracConfig.setMain(checkNotNull(JavaExecutionUtil.getRuntimeQualifiedName(cls)));

    try {
      defracConfig.commit(config.getProject());
    } catch(Exception e) {
      LOG.error(e);
    }
  }

  @Nullable
  private static PsiClass findEntryPoint(@NotNull final ConfigurationContext context) {
    final Location location = context.getLocation();

    if(location == null) {
      return null;
    }

    final Module module = context.getModule();

    if(module == null) {
      return null;
    }

    final Location classLocation = JavaExecutionUtil.stepIntoSingleClass(location);

    if(classLocation == null) {
      return null;
    }

    final PsiElement element = classLocation.getPsiElement();

    if(!element.isPhysical()) {
      return null;
    }

    final DefracFacet facet = DefracFacet.getInstance(element);

    if(facet == null) {
      return null;
    }

    if(facet.getPlatform().isGeneric()) {
      // ignore generic platform
      return null;
    }

    if(facet.getPlatform().isAndroid()) {
      return findActivityClass(context, element);
    }

    return findMainClass(element);
  }

  @Nullable
  private static PsiClass findActivityClass(@NotNull final ConfigurationContext context,
                                            @NotNull final PsiElement element) {
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(element.getProject());
    final GlobalSearchScope scope = context.getModule().getModuleWithDependenciesAndLibrariesScope(true);
    final PsiClass activityClass = facade.findClass(ACTIVITY_BASE_CLASS_NAME, scope);

    if(activityClass == null) {
      return null;
    }

    PsiClass elementClass = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);

    while(elementClass != null) {
      if(elementClass.isInheritor(activityClass, true)) {
        return elementClass;
      }

      elementClass = PsiTreeUtil.getParentOfType(elementClass, PsiClass.class);
    }

    return null;
  }

  @Nullable
  private static PsiClass findMainClass(@NotNull final PsiElement element) {
    PsiMethod method = findMain(element);

    while(method != null) {
      final PsiClass cls = method.getContainingClass();

      if(ConfigurationUtil.MAIN_CLASS.value(cls)) {
        return cls;
      }

      method = findMain(method.getParent());
    }

    return ApplicationConfigurationType.getMainClass(element);
  }

  @Nullable
  private static PsiMethod findMain(@Nullable PsiElement element) {
    PsiMethod method;
    while((method = PsiTreeUtil.getParentOfType(element, PsiMethod.class)) != null) {
      if(PsiMethodUtil.isMainMethod(method)) {
        return method;
      }

      element = method.getParent();
    }

    return null;
  }
}
