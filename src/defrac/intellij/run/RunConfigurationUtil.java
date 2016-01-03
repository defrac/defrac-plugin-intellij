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
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.psi.util.PsiTreeUtil;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Tim Richter
 */
public final class RunConfigurationUtil {

  private static final String ANDROID_ACTIVITY = "android.app.Activity";
  private static final String IOS_DELEGATE = "defrac.ios.uikit.UIApplicationDelegate";

  public static boolean isValidMainClass(@NotNull final Module module, @NotNull final PsiElement element) {
    final DefracFacet facet = DefracFacet.getInstance(module);

    if(facet == null || facet.getModule() != module) {
      return false;
    }

    if(facet.getPlatform().isAndroid()) {
      return findActivityClass(module, element) != null;
    }

    return findMainClass(element) != null;
  }

  public static boolean isValidMainClass(@NotNull final Module module, @NotNull final String className) {
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
  public static PsiClass findEntryPoint(@NotNull final Location location, @NotNull final Module module) {

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
      return findActivityClass(module, element);
    }

    return findMainClass(element);
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
  public static PsiMethod findMain(@Nullable PsiElement element) {
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
