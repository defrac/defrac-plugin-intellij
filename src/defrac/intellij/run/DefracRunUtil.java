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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.psi.util.PsiTreeUtil;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.DefracPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 */
public final class DefracRunUtil {

  private static final String ANDROID_ACTIVITY = "android.app.Activity";
  private static final String IOS_DELEGATE = "defrac.ios.uikit.UIApplicationDelegate";

  public static void checkState(@NotNull final DefracRunConfigurationBase config,
                                @NotNull final ExecutionEnvironment environment) throws ExecutionException {
    final Module module = config.getModule();

    if(module == null) {
      throw new ExecutionException(DefracBundle.message("config.run.error.noModule"));
    }

    if(isNullOrEmpty(config.getMain())) {
      throw new ExecutionException(DefracBundle.message("config.run.error.noMain"));
    }

    if(!isValidMainClass(module, config.getMain())) {
      throw new ExecutionException(DefracBundle.message("config.run.error.illegalMain"));
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.facetMissing", module.getName()));
    }

    if(!facet.getPlatform().isAvailableOnHostOS()) {
      throw new ExecutionException(DefracBundle.message("facet.error.unavailablePlatform", facet.getPlatform().displayName));
    }

    if(facet.getConfigOracle() == null) {
      throw new ExecutionException(DefracBundle.message("facet.error.noSettings"));
    }

    config.setDebug(isDebug(environment));
  }

  public static void checkConfiguration(@NotNull final DefracRunConfigurationBase config) throws RuntimeConfigurationException {
    final Module module = config.getModule();

    if(module == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("config.run.error.noModule"));
    }

    if(isNullOrEmpty(config.getMain())) {
      throw new RuntimeConfigurationError(DefracBundle.message("config.run.error.noMain"));
    }

    if(!isValidMainClass(module, config.getMain())) {
      throw new RuntimeConfigurationError(DefracBundle.message("config.run.error.illegalMain"));
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.facetMissing", module.getName()));
    }

    if(facet.isMacroLibrary()) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.isMacroLibrary"));
    }

    if(facet.getDefracVersion() == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.noVersion"));
    }

    if(!facet.getSettingsFile().canRead()) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.fileDoesNotExist", facet.getSettingsFile()));
    }

    if(facet.getPlatform().isGeneric()) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.genericPlatform", module.getName()));
    }

    if(facet.getConfigOracle() == null) {
      throw new RuntimeConfigurationError(DefracBundle.message("facet.error.noSettings"));
    }
  }

  public static boolean canRun(@NotNull final String executorId,
                               @NotNull final RunProfile profile,
                               @NotNull final DefracPlatform platform,
                               final boolean debug) {
    if(profile instanceof DefracRunConfigurationBase) {
      final DefracRunConfigurationBase configuration = (DefracRunConfigurationBase) profile;
      final Module module = configuration.getModule();

      if(module == null) {
        return false;
      }

      final DefracFacet facet = DefracFacet.getInstance(module);
      final boolean isDebug = DefaultDebugExecutor.EXECUTOR_ID.equals(executorId);

      return facet != null
          && isDebug == debug
          && facet.getPlatform() == platform;

    }

    return false;
  }

  public static boolean isValidMainClass(@Nullable final Module module, @Nullable final PsiElement element) {
    if(module == null || element == null) {
      return false;
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null || facet.getModule() != module) {
      return false;
    }

    if(facet.getPlatform().isGeneric() || facet.isMacroLibrary()) {
      return false;
    }

    if(facet.getPlatform().isAndroid()) {
      return findActivityClass(module, element) != null;
    }

    return findMainClass(element) != null;
  }

  public static boolean isValidMainClass(@Nullable final Module module, @Nullable final String className) {
    if(module == null || className == null) {
      return false;
    }

    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null) {
      return false;
    }

    if(facet.getPlatform().isAndroid()) {
      final PsiClass psiClass = JavaPsiFacade.getInstance(module.getProject()).findClass(className, module.getModuleScope());

      return psiClass != null && findActivityClass(module, psiClass) != null;
    }

    return JavaExecutionUtil.findMainClass(module.getProject(), className, module.getModuleScope()) != null;
  }

  @Nullable
  public static PsiElement findEntryPoint(@Nullable final Location location, @Nullable final Module module) {
    if(location == null || module == null) {
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

    if(facet == null || !facet.getPlatform().isAvailableOnHostOS() || facet.getPlatform().isGeneric()) {
      return null;
    }

    if(facet.getPlatform().isAndroid()) {
      return findActivityClass(module, element);
    }

    final PsiElement method = findMainMethod(element);

    if(method != null) {
      return method;
    }

    return ApplicationConfigurationType.getMainClass(element);
  }

  @Nullable
  public static PsiClass findUIDelegate(@NotNull final Module module,
                                        @NotNull final PsiElement element) {
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(element.getProject());
    final GlobalSearchScope scope = module.getModuleWithDependenciesAndLibrariesScope(true);
    final PsiClass delegateClass = facade.findClass(IOS_DELEGATE, scope);

    if(delegateClass == null) {
      return null;
    }

    return inheritor(delegateClass, element);
  }

  @Nullable
  public static PsiClass findActivityClass(@NotNull final Module module,
                                           @NotNull final PsiElement element) {
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(element.getProject());
    final GlobalSearchScope scope = module.getModuleWithDependenciesAndLibrariesScope(true);
    final PsiClass activityClass = facade.findClass(ANDROID_ACTIVITY, scope);

    if(activityClass == null) {
      return null;
    }

    return inheritor(activityClass, element);
  }

  @Nullable
  public static PsiClass inheritor(@NotNull final PsiClass inheritedClass,
                                   @NotNull final PsiElement inheritor) {
    PsiClass elementClass = PsiTreeUtil.getParentOfType(inheritor, PsiClass.class, false);

    while(elementClass != null) {
      if(elementClass.isInheritor(inheritedClass, true)) {
        return elementClass;
      }

      elementClass = PsiTreeUtil.getParentOfType(elementClass, PsiClass.class);
    }

    return null;
  }

  @Nullable
  public static PsiClass findMainClass(@NotNull final PsiElement element) {
    PsiMethod method = findMainMethod(element);

    while(method != null) {
      final PsiClass cls = method.getContainingClass();

      if(ConfigurationUtil.MAIN_CLASS.value(cls)) {
        return cls;
      }

      method = findMainMethod(method.getParent());
    }

    return ApplicationConfigurationType.getMainClass(element);
  }

  @Nullable
  public static PsiMethod findMainMethod(@Nullable PsiElement element) {
    PsiMethod method;
    while((method = PsiTreeUtil.getParentOfType(element, PsiMethod.class)) != null) {
      if(PsiMethodUtil.isMainMethod(method)) {
        return method;
      }

      element = method.getParent();
    }

    return null;
  }

  @Nullable
  public static PsiClass findMainClass(@NotNull final ConfigurationContext context) {
    return DefracPsiUtil.enclosingClass(findEntryPoint(context.getLocation(), context.getModule()));
  }

  @Nullable
  public static PsiElement findEntryPoint(@NotNull final ConfigurationContext context) {
    return findEntryPoint(context.getLocation(), context.getModule());
  }

  @Nullable
  public static String getRuntimeQualifiedName(@Nullable final PsiClass aClass) {
    return aClass != null ? JavaExecutionUtil.getRuntimeQualifiedName(aClass) : null;
  }

  @Nullable
  public static String getCompileTimeQualifiedName(@Nullable final String className) {
    return className != null ? className.replaceAll("\\$", "\\.") : null;
  }

  @NotNull
  public static String suggestedName(@NotNull final DefracRunConfigurationBase config) {
    return checkNotNull(JavaExecutionUtil.getShortClassName(getCompileTimeQualifiedName(config.getMain()))) +
        '(' + config.getPlatform().displayName + ')';
  }

  public static boolean isDebug(@NotNull final ExecutionEnvironment environment) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(environment.getExecutor().getId());
  }
}
