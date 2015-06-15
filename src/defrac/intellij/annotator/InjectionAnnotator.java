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
import com.intellij.codeInsight.intention.AddAnnotationFix;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassKind;
import com.intellij.util.ArrayUtil;
import defrac.intellij.DefracBundle;
import defrac.intellij.DefracPlatform;
import defrac.intellij.annotator.quickfix.RemoveInjectorQuickFix;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.psi.DefracPsiUtil;
import defrac.intellij.psi.InjectorClassReference;
import defrac.intellij.psi.validation.MultiPlatformClassValidator;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.intellij.psi.util.PsiUtil.mapElements;
import static defrac.intellij.psi.DefracPsiUtil.isInjectorAnnotation;

/**
 *
 */
public final class InjectionAnnotator implements Annotator {
  public InjectionAnnotator() {}

  @Override
  public void annotate(@NotNull final PsiElement element,
                       @NotNull final AnnotationHolder holder) {
    if(element instanceof PsiClass) {
      annotateInjectClass((PsiClass) element, holder);
      return;
    }

    if(!(element instanceof PsiLiteralExpression)) {
      return;
    }

    final DefracFacet facet = DefracFacet.getInstance(element);

    if(facet == null || facet.isMacroLibrary()) {
      return;
    }

    final PsiAnnotation annotation =
        getParentOfType(element, PsiAnnotation.class, /*strict=*/false);

    if(annotation == null || !isInjectorAnnotation(annotation)) {
      return;
    }

    final PsiClass klass = getParentOfType(element, PsiClass.class, /*strict=*/false);

    if(klass == null || klass.getNameIdentifier() == null) {
      return;
    }

    final PsiReference[] references =
        element.getReferences();

    String target;

    for(final PsiReference reference : references) {
      if(!(reference instanceof InjectorClassReference)) {
        continue;
      }

      final InjectorClassReference defracRef = (InjectorClassReference)reference;

      if(isNullOrEmpty(defracRef.getValue())) {
        holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.qualifiedName"));
        return;
      }

      target = defracRef.getValue();

      final ResolveResult[] resolveResults = defracRef.multiResolve();

      if(resolveResults.length == 0) {
        final String value = defracRef.getValue();

        if(isNullOrEmpty(value)) {
          holder.createErrorAnnotation(element, DefracBundle.message("annotator.expect.qualifiedName"));
          return;
        } else {
          final Annotation errorAnnotation = holder.
              createErrorAnnotation(element, DefracBundle.message("annotator.unresolved", target));
          final PsiClass superClass = klass.getSuperClass();

          final CreateClassOrPackageFix fix = DefracAnnotatorUtil.createCreateClassOrPackageFix(
              target,
              checkNotNull(DefracFacet.getInstance(klass)).
                  getMultiPlatformClassSearchScope(DefracPlatform.GENERIC),
              element,
              ClassKind.CLASS,
              superClass == null ? null : checkNotNull(superClass.getQualifiedName()),
              null);

          if(fix != null) {
            errorAnnotation.registerFix(fix);
          }
          return;
        }
      } else {
        final PsiElement[] psiElements = mapElements(resolveResults);

        if(psiElements.length == 1) {
          MultiPlatformClassValidator.annotate(klass.getNameIdentifier(), holder, psiElements[0], klass);
        } else {
          holder.createErrorAnnotation(element, DefracBundle.message("annotator.ambiguousQname", defracRef.getValue()));
        }
      }
    }
  }

  private void annotateInjectClass(@NotNull final PsiClass injectClass,
                                   @NotNull final AnnotationHolder holder) {
    final DefracFacet facet = DefracFacet.getInstance(injectClass);

    if(facet == null || facet.isMacroLibrary()) {
      return;
    }

    final Project project = injectClass.getProject();
    final DefracPlatform platform = facet.getPlatform();
    final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();

    // Try to find the Injector via the annotation
    PsiClass injectorClass = DefracPsiUtil.getInjector(injectClass);

    if(injectorClass != null) {
      final PsiAnnotation ourInjectorAnnotation =
          checkNotNull(injectClass.getModifierList()).
              findAnnotation(Names.defrac_annotation_Injector);

      if(ourInjectorAnnotation == null) {
        throw new IllegalStateException();
      }

      // If the Injector we reference doesn't inject us, we
      // can either add an annotation to the injector or
      // remove our annotation

      final PsiClass injectorsInjectClass =
          DefracPsiUtil.getInjection(injectorClass, platform);

      if(injectorsInjectClass == null) {
        // The Injector doesn't inject to us, we can either:
        // (1) Remove our @Injector annotation
        // (2) Add the @Inject annotation to the injector
        //     Note: The Injector may have a broken ref in
        //           its Inject annotation!

        final Annotation annotation =
            holder.createErrorAnnotation(ourInjectorAnnotation,
                DefracBundle.message("annotator.multiPlatformClass.injectorDoesNotInject",
                    injectorClass.getName(),
                    injectClass.getName()));

        // (1)
        annotation.registerFix(new RemoveInjectorQuickFix(injectClass));

        // (2)
        final String annotationText = "@"+DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform) + "(\"" + injectClass.getQualifiedName() + "\")";
        final PsiAnnotation newAnnotation = elementFactory.createAnnotationFromText(annotationText, injectorClass);

        final String[] annotationsToRemove =
            DefracPsiUtil.hasInjection(injectorClass, platform)
                ? new String[] { DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform) }
                : ArrayUtil.EMPTY_STRING_ARRAY;

        //TODO(joa): needs individual quickfix impl for existing annotation
        annotation.registerFix(
            new AddAnnotationFix(
                DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform),
                injectorClass, newAnnotation.getParameterList().getAttributes(),
                annotationsToRemove));
      } else if(injectorsInjectClass != injectClass) {
        // The Injector injects to a different class, we can either:
        // (1) Remove our @Injector annotation
        // (2) Change the @Inject annotation of the injector
        final Annotation annotation =
            holder.createErrorAnnotation(ourInjectorAnnotation,
                DefracBundle.message("annotator.multiPlatformClass.injectorInjectsDifferent",
                    injectorClass.getName(),
                    injectorsInjectClass.getName()));

        // (1)
        annotation.registerFix(new RemoveInjectorQuickFix(injectClass));

        // (2)
        final String annotationText = "@"+DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform)+"(\""+injectClass.getQualifiedName()+"\")";
        final PsiAnnotation newAnnotation = elementFactory.createAnnotationFromText(annotationText, injectorClass);

        //TODO(joa): needs individual quickfix impl for existing annotation
        annotation.registerFix(
            new AddAnnotationFix(
                DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform),
                injectorClass, newAnnotation.getParameterList().getAttributes(),
                DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform)));
      }

      // In this case the Injector injects our class and
      // we are good to go.
      return;
    }

    final PsiElement nameElement = injectClass.getNameIdentifier();

    if(nameElement == null) {
      return;
    }

    // There is no @Injector annotation present or the qualified classname
    // of the Injector is broken, this is handled by the annotator above
    //
    // We need to search for a possible Injector referencing us. Ouch :/
    injectorClass = DefracPsiUtil.findInjector(injectClass);

    if(injectorClass == null) {
      // At this point, we can be sure that this class isn't an
      // injection
      return;
    }

    // At this point we know that we're missing an @Injector
    // annotation for the current class since we are referenced
    // via @Inject somewhere
    final Annotation annotation =
        holder.createErrorAnnotation(nameElement, DefracBundle.message("annotator.multiPlatformClass.missingInjector"));

    // There is no API to create a PsiNameValuePair in the PsiElementFactory
    // so we have to let IntelliJ IDEA parse a String for the annotation we
    // want to create, extract the PsiNameValuePair[] array and pass that
    // to the fix ...
    final String annotationText = "@"+Names.defrac_annotation_Injector+"(\""+injectorClass.getQualifiedName()+"\")";
    final PsiAnnotation newAnnotation = elementFactory.createAnnotationFromText(annotationText, injectClass);

    annotation.registerFix(
        new AddAnnotationFix(
            Names.defrac_annotation_Injector,
            injectClass, newAnnotation.getParameterList().getAttributes()));
  }
}
