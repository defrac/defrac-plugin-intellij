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

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import defrac.intellij.DefracPlatform;
import defrac.intellij.config.DefracConfig;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 */
public final class DefracAnnotatorUtil {
  public static void reportMoreGenericAnnotation(@NotNull final AnnotationHolder holder,
                                                 @NotNull final PsiAnnotation thisAnnotation,
                                                 @NotNull final PsiModifierListOwner annotatedElement) {
    final PsiAnnotation[] thatAnnotations = checkNotNull(annotatedElement.getModifierList()).getAnnotations();

    for(final PsiAnnotation thatAnnotation : thatAnnotations) {
      if(!Names.defrac_annotation_Macro.equals(thatAnnotation.getQualifiedName())) {
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
        holder.createWarningAnnotation(thisAnnotation, "More generic annotation with same implementation exists");
      }

      return;
    }
  }

  public static void reportMissingImplementations(@NotNull final PsiElement element,
                                                  @NotNull final AnnotationHolder holder,
                                                  @NotNull final DefracFacet facet,
                                                  @NotNull final PsiModifierListOwner annotatedElement,
                                                  @NotNull final Set<DefracPlatform> implementations,
                                                  @NotNull final Map<String, DefracPlatform> nameToPlatform) {
    // don't report that some class is missing for a platform if
    // the more specific annotation is present
    final PsiAnnotation[] otherAnnotations = checkNotNull(annotatedElement.getModifierList()).getAnnotations();
    for(final PsiAnnotation otherAnnotation : otherAnnotations) {
      final DefracPlatform moreSpecificPlatform =
          nameToPlatform.get(otherAnnotation.getQualifiedName());

      if(moreSpecificPlatform != null) {
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
              holder.createErrorAnnotation(element, "Missing implementation for " + platform.displayName);
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
