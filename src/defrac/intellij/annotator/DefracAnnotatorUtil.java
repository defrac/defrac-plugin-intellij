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

package defrac.intellij.annotator;

import com.intellij.codeInsight.daemon.quickFix.CreateClassOrPackageFix;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.ClassKind;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.annotator.quickfix.RemoveDelegateQuickFix;
import defrac.intellij.annotator.quickfix.RemoveMacroQuickFix;
import defrac.intellij.config.DefracConfig;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.MacroMethodReference;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracAnnotatorUtil {
  @Nullable
  public static CreateClassOrPackageFix createCreateClassOrPackageFix(
      @NotNull final String qualifiedName,
      @NotNull final GlobalSearchScope scope,
      @NotNull final PsiElement context,
      @NotNull final ClassKind classKind,
      @Nullable final String superClass,
      @Nullable final String template) {
    final PsiPackage package$;


    final int lastIndexOfDot = qualifiedName.lastIndexOf('.');

    if(lastIndexOfDot == -1) {
      package$ = null;
    } else if(lastIndexOfDot == (qualifiedName.length() - 1)) {
      return null;
    } else {
      package$ = JavaPsiFacade.
          getInstance(context.getProject()).
          findPackage(qualifiedName.substring(0, lastIndexOfDot));
    }

    return CreateClassOrPackageFix.
        createFix(
            qualifiedName,
            scope,
            context,
            package$,
            classKind,
            superClass,
            template);
  }
  public static void reportMoreGenericAnnotation(@NotNull final AnnotationHolder holder,
                                                 @NotNull final PsiAnnotation thisAnnotation,
                                                 @NotNull final PsiModifierListOwner annotatedElement,
                                                 @NotNull final String nameOfGenericAnnotation,
                                                 @NotNull final DefracPlatform platform,
                                                 final boolean isDelegate) {
    final PsiAnnotation[] thatAnnotations = checkNotNull(annotatedElement.getModifierList()).getAnnotations();

    for(final PsiAnnotation thatAnnotation : thatAnnotations) {
      if(!nameOfGenericAnnotation.equals(thatAnnotation.getQualifiedName())) {
        continue;
      }

      final PsiAnnotationParameterList thisList = thisAnnotation.getParameterList();
      final PsiNameValuePair[] thisAttributes = thisList.getAttributes();

      if(thisAttributes.length < 1) {
        return;
      }

      final PsiNameValuePair thisPair = thisAttributes[0];

      final PsiAnnotationParameterList thatList = thatAnnotation.getParameterList();
      final PsiNameValuePair[] thatAttributes = thatList.getAttributes();

      if(thatAttributes.length < 1) {
        return;
      }

      final PsiNameValuePair thatPair = thatAttributes[0];

      final String thisLiteral = thisPair.getText();
      final String thatLiteral = thatPair.getText();

      if(isNullOrEmpty(thisLiteral) || isNullOrEmpty(thatLiteral)) {
        return;
      }

      if(thisLiteral.equals(thatLiteral)) {
        holder.
            createWarningAnnotation(thisAnnotation, DefracBundle.message("annotator.platform.redundant")).
            registerFix(
                isDelegate
                    ? new RemoveDelegateQuickFix((PsiClass)annotatedElement, platform)
                    : new RemoveMacroQuickFix((PsiMethod)annotatedElement, platform));
      }

      return;
    }
  }

  public static void reportMissingImplementations(@NotNull final PsiElement element,
                                                  @NotNull final AnnotationHolder holder,
                                                  @NotNull final DefracFacet facet,
                                                  @NotNull final PsiModifierListOwner annotatedElement,
                                                  @NotNull final Set<DefracPlatform> implementations,
                                                  @NotNull final Map<String, DefracPlatform> nameToPlatform,
                                                  @Nullable final String target,
                                                  final boolean isDelegate) {
    // don't report that a platform is missing if the project is not
    // generic since it's not required!
    if(!facet.getPlatform().isGeneric()) {
      return;
    }

    // don't report that some class is missing for a platform if
    // the more specific annotation is present
    final PsiAnnotation[] otherAnnotations = checkNotNull(annotatedElement.getModifierList()).getAnnotations();
    for(final PsiAnnotation otherAnnotation : otherAnnotations) {
      final DefracPlatform moreSpecificPlatform =
          nameToPlatform.get(otherAnnotation.getQualifiedName());

      if(moreSpecificPlatform != null && !moreSpecificPlatform.isGeneric()) {
        implementations.add(moreSpecificPlatform);
      }
    }

    // now report all missing implementations for configured targets
    try {
      final VirtualFile settingsFile = VfsUtil.findFileByIoFile(facet.getSettingsFile(), false);
      final PsiManager psiManager = PsiManager.getInstance(element.getProject());

      if(settingsFile != null) {
        final PsiFile file = checkNotNull(psiManager.findFile(settingsFile));

        if(file != null) {
          final DefracConfig config = DefracConfig.fromJson(file);

          for(final DefracPlatform platform : config.getTargets()) {
            if(platform.isGeneric()) {
              continue;
            }

            if(!implementations.contains(platform)) {
              final Annotation annotation =
                  holder.
                      createErrorAnnotation(element,
                          DefracBundle.message("annotator.platform.missing", platform.displayName));

              if(isDelegate) {
                if(target != null) {
                  final PsiClass superClass = ((PsiClass)annotatedElement).getSuperClass();
                  final CreateClassOrPackageFix fix = DefracAnnotatorUtil.createCreateClassOrPackageFix(
                      target,
                      checkNotNull(DefracFacet.getInstance(annotatedElement)).getDelegateSearchScope(platform),
                      element,
                      ClassKind.CLASS,
                      superClass == null ? null : checkNotNull(superClass.getQualifiedName()),
                      null);

                  if(fix != null) {
                    annotation.registerFix(fix);
                  }
                }
              } else {
                final String qualifiedClassName = MacroMethodReference.getQualifiedClassName(target);
                final CreateClassOrPackageFix fix = DefracAnnotatorUtil.createCreateClassOrPackageFix(
                    qualifiedClassName == null ? "" : qualifiedClassName,
                    checkNotNull(DefracFacet.getInstance(annotatedElement)).getMacroSearchScope(platform),
                    element,
                    ClassKind.CLASS,
                    Names.defrac_compiler_macro_Macro,
                    null);

                if(fix != null) {
                  annotation.registerFix(fix);
                }
              }
            }
          }
        }
      }
    } catch(final IOException ioException) {
      // ignored
    }
  }

  private DefracAnnotatorUtil() {}
}
