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

package defrac.intellij.psi;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.*;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import defrac.intellij.DefracPlatform;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.intellij.psi.util.PsiTypesUtil.compareTypes;
import static com.intellij.psi.util.PsiUtil.mapElements;
import static com.intellij.psi.util.PsiUtil.setModifierProperty;

/**
 *
 */
public final class DefracPsiUtil {
  @NotNull
  private static final String[] VISIBILITY_MODIFIERS = {
      PsiModifier.PRIVATE,
      PsiModifier.PACKAGE_LOCAL,
      PsiModifier.PROTECTED,
      PsiModifier.PUBLIC,
  };

  @Nullable
  public static PsiClass enclosingClass(@Nullable final PsiElement element) {
    if(element == null) {
      return null;
    }

    if(element instanceof PsiClass) {
      return (PsiClass)element;
    }

    return enclosingClass(element.getParent());
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Contract("null -> false")
  public static boolean isMacro(@Nullable final PsiElement element) {
    if(!(element instanceof PsiClass)) {
      return false;
    }

    return isMacro((PsiClass)element);
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Contract("null -> false")
  public static boolean isMacro(@Nullable final PsiClass klass) {
    if(klass == null) {
      return false;
    }

    final PsiClass macro =
        JavaPsiFacade.
            getInstance(klass.getProject()).
            findClass(Names.defrac_compiler_macro_Macro, GlobalSearchScope.allScope(klass.getProject()));


    if(macro == null) {
      return false;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    final Boolean cachedValue = CachedValuesManager.getCachedValue(klass, new CachedValueProvider<Boolean>() {
      @Nullable
      public Result<Boolean> compute() {
        return Result.create(
            klass.isInheritor(macro, true),
            klass, PsiModificationTracker.JAVA_STRUCTURE_MODIFICATION_COUNT
        );
      }
    });

    return cachedValue;
  }

  private DefracPsiUtil() {}

  @Contract("null -> false")
  public static boolean isInjectorAnnotation(@Nullable final PsiElement element) {
    return element instanceof PsiAnnotation && isInjectorAnnotation((PsiAnnotation) element);
  }

  @Contract("null -> false")
  public static boolean isInjectorAnnotation(@Nullable final PsiAnnotation annotation) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName = annotation.getQualifiedName();
    return qualifiedName != null
        && isInjectorAnnotation(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isInjectorAnnotation(@Nullable final String qualifiedName) {
    return Names.defrac_annotation_Injector.equals(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isInjectAnnotation(@Nullable final PsiElement element) {
    return element instanceof PsiAnnotation && isInjectAnnotation((PsiAnnotation) element);
  }

  @Contract("null -> false")
  public static boolean isInjectAnnotation(@Nullable final PsiAnnotation annotation) {
    return isInjectAnnotation(annotation, null);
  }

  @Contract("null, _ -> false")
  public static boolean isInjectAnnotation(@Nullable final PsiAnnotation annotation,
                                           @Nullable final DefracPlatform platform) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName = annotation.getQualifiedName();
    return qualifiedName != null
        && isInjectAnnotation(qualifiedName, DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform));
  }

  @Contract("null, _ -> false")
  public static boolean isInjectAnnotation(@Nullable final String qualifiedName,
                                           @Nullable final String nameOfAnnotation) {
    return nameOfAnnotation == null
        ? Names.ALL_INJECTS.contains(qualifiedName)
        : nameOfAnnotation.equals(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isMacroAnnotation(@Nullable final PsiElement element) {
    return element instanceof PsiAnnotation && isMacroAnnotation((PsiAnnotation)element);
  }

  @Contract("null -> false")
  public static boolean isMacroAnnotation(@Nullable final PsiAnnotation annotation) {
    return isMacroAnnotation(annotation, null);
  }

  @Contract("null, _ -> false")
  public static boolean isMacroAnnotation(@Nullable final PsiAnnotation annotation,
                                          @Nullable final DefracPlatform platform) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName = annotation.getQualifiedName();
    return qualifiedName != null
        && isMacroAnnotation(qualifiedName, DefracPlatform.PLATFORM_TO_MACRO_ANNOTATION.get(platform));
  }

  @Contract("null, _ -> false")
  public static boolean isMacroAnnotation(@Nullable final String qualifiedName,
                                          @Nullable final String nameOfAnnotation) {
    return nameOfAnnotation == null
        ? Names.ALL_MACROS.contains(qualifiedName)
        : nameOfAnnotation.equals(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isUnsupportedAnnotation(@Nullable final PsiElement element) {
    return element instanceof PsiAnnotation && isUnsupportedAnnotation((PsiAnnotation)element);
  }

  @Contract("null -> false")
  public static boolean isUnsupportedAnnotation(@Nullable final PsiAnnotation annotation) {
    return isUnsupportedAnnotation(annotation, null);
  }

  @Contract("null, _ -> false")
  public static boolean isUnsupportedAnnotation(@Nullable final PsiAnnotation annotation,
                                                @Nullable final DefracPlatform platform) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName = annotation.getQualifiedName();
    return qualifiedName != null
        && isUnsupportedAnnotation(qualifiedName, DefracPlatform.PLATFORM_TO_UNSUPPORTED_ANNOTATION.get(platform));
  }

  @Contract("null, _ -> false")
  public static boolean isUnsupportedAnnotation(@Nullable final String qualifiedName,
                                                @Nullable final String nameOfAnnotation) {
    return nameOfAnnotation == null
        ? Names.ALL_UNSUPPORTED.contains(qualifiedName)
        : nameOfAnnotation.equals(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isIntrinsicAnnotation(@Nullable final PsiAnnotation annotation) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName = annotation.getQualifiedName();
    return qualifiedName != null
        && isIntrinsicAnnotation(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isIntrinsicAnnotation(@Nullable final String qualifiedName) {
    return Names.defrac_dni_Intrinsic.equals(qualifiedName);
  }

  public static boolean isSignatureEqual(@NotNull final PsiMethod thisMethod,
                                         @NotNull final PsiMethod thatMethod) {
    // there is no check for type parameters here due to erasure
    // and all casts are at call site

    final MethodSignature thisSignature = thisMethod.getSignature(PsiSubstitutor.EMPTY);
    final MethodSignature thatSignature = thatMethod.getSignature(PsiSubstitutor.EMPTY);

    if(thisSignature.isConstructor() != thatSignature.isConstructor()) {
      return false;
    }

    final PsiType[] thisTypes = thisSignature.getParameterTypes();
    final PsiType[] thatTypes = thatSignature.getParameterTypes();

    if(thisTypes.length != thatTypes.length) {
      return false;
    }

    for(int i = 0; i < thisTypes.length; ++i) {
      final PsiType thisType = thisTypes[i];
      final PsiType thatType = thatTypes[i];

      if(!compareBytecodeTypes(thisType, thatType)) {
        return false;
      }
    }

    return true;
  }

  public static boolean compareBytecodeTypes(@Nullable PsiType[] a,
                                             @Nullable PsiType[] b) {
    if(a == null) {
      return b == null;
    } else if(b == null) {
      return false;
    } else if(a.length != b.length) {
      return false;
    }

    for(int i = 0; i < a.length; ++i) {
      if(!compareBytecodeTypes(a[i], b[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares two types for equality in Java bytecode
   *
   * Two types are equal in bytecode if they have the same erasure and
   * point to a class with equal qualified name.
   *
   * That is {@code java.lang.Iterator&lt;A&gt;} equals {@code java.lang.Iterator&lt;B&gt;}
   * since both types represent only {@code java.lang.Iterator} when compiled to Java bytecode.
   *
   * @param a The first type to compare
   * @param b The second type to compare
   * @return {@literal true} if both types are equal in bytecode; {@literal false} otherwise
   */
  public static boolean compareBytecodeTypes(@Nullable PsiType a,
                                             @Nullable PsiType b) {
    if(a == null) {
      return b == null;
    } else if(b == null) {
      return false;
    }

    if(a instanceof PsiEllipsisType) {
      a = ((PsiEllipsisType)a).toArrayType();
    }

    if(b instanceof PsiEllipsisType) {
      b = ((PsiEllipsisType)b).toArrayType();
    }

    if(isTypeParameter(a)) {
      if(isTypeParameter(b)) {
        // both are type parameters, always equal
        return true;
      }

      // if b itself is not a type parameter, we have to check if it
      // matches j.l.Object
      return isJavaLangObject(b);
    } else if(isTypeParameter(b)) {
      // if a is not a type parameter but b is, we have to check if
      // a itself matches j.l.Object
      return isJavaLangObject(a);
    }

    // we know both a and b are not type parameters, but they might
    // contain type parameters. in that case we do not care about them
    // and only want to know if the referenced class is equal
    if(a instanceof PsiClassReferenceType && b instanceof PsiClassReferenceType) {
      final PsiClass classA = ((PsiClassReferenceType)a).resolve();
      final PsiClass classB = ((PsiClassReferenceType)b).resolve();

      if(classA == null) {
        return classB == null;
      } else if(classB == null) {
        return false;
      }

      return isQualifiedNameEqual(classA, classB);
    } else if(a instanceof PsiArrayType && b instanceof PsiArrayType) {
      final PsiArrayType arrayA = (PsiArrayType)a;
      final PsiArrayType arrayB = (PsiArrayType)b;

      return compareBytecodeTypes(arrayA.getComponentType(), arrayB.getComponentType());
    }

    // primitive types, go with default behaviour of IntelliJ
    return compareTypes(a, b, /*ignoreEllipsis=*/false);
  }

  public static boolean isJavaLangObject(final PsiType b) {
    final GlobalSearchScope scope = b.getResolveScope();

    if(scope == null) {
      return false;
    }

    final Project project = scope.getProject();

    if(project == null) {
      return false;
    }

    final PsiManager manager = PsiManager.getInstance(project);
    return b.equals(PsiType.getJavaLangObject(manager, scope));
  }

  @Contract("null -> false")
  public static boolean isTypeParameter(@Nullable final PsiType type) {
    return type instanceof PsiClassReferenceType
        && ((PsiClassReferenceType)type).resolveGenerics().getElement() instanceof PsiTypeParameter;
  }

  public static boolean isEqualVisibility(@NotNull final PsiModifierListOwner thisOwner,
                                          @NotNull final PsiModifierListOwner thatOwner) {
    final PsiModifierList thisList = thisOwner.getModifierList();
    final PsiModifierList thatList = thatOwner.getModifierList();

    if(thisList == null) {
      return thatList == null;
    }

    if(thatList == null) {
      return false;
    }

    for(final String modifier : VISIBILITY_MODIFIERS) {
      if(thisList.hasModifierProperty(modifier) != thatList.hasModifierProperty(modifier)) {
        return false;
      }
    }

    return true;
  }

  public static String getVisibility(@NotNull final PsiModifierListOwner owner) {
    final PsiModifierList list = owner.getModifierList();

    if(list == null) {
      return PsiModifier.PACKAGE_LOCAL;
    }

    for(final String modifier : VISIBILITY_MODIFIERS) {
      if(list.hasModifierProperty(modifier)) {
        return modifier;
      }
    }

    throw new IllegalStateException("No visibility modifier for "+owner);
  }

  public static void setVisibility(@NotNull final PsiMember member, @NotNull final String newVisibility) {
    for(final String modifier : VISIBILITY_MODIFIERS) {
      if(PsiModifier.PACKAGE_LOCAL.equals(modifier)) {
        continue;
      }

      setModifierProperty(member, modifier, false);
    }

    setModifierProperty(member, newVisibility, true);
  }

  public static boolean isQualifiedNameEqual(@NotNull final PsiClass thisClass,
                                             @NotNull final PsiClass thatClass) {
    if(thisClass == thatClass) {
      // We take a shortcut in case the PsiClass is from
      // the same SDK
      return true;
    }

    final String thisQualifiedName = thisClass.getQualifiedName();
    final String thatQualifiedName = thatClass.getQualifiedName();

    return thisQualifiedName != null && thisQualifiedName.equals(thatQualifiedName);
  }

  @NotNull
  public static String[] mapQualifiedName(@NotNull final PsiClass[] classes) {
    final ArrayList<String> result = Lists.newArrayListWithExpectedSize(classes.length);

    for(final PsiClass klass : classes) {
      final String qualifiedName = klass.getQualifiedName();

      if(qualifiedName == null) {
        continue;
      }

      result.add(qualifiedName);
    }

    return result.toArray(new String[result.size()]);
  }

  public static boolean isReadOnly(@NotNull final PsiModifierListOwner element) {
    final PsiModifierList list = element.getModifierList();
    return list != null && list.findAnnotation(Names.defrac_dni_ReadOnly) != null;
  }

  public static boolean isWriteOnly(@NotNull final PsiModifierListOwner element) {
    final PsiModifierList list = element.getModifierList();
    return list != null && list.findAnnotation(Names.defrac_dni_WriteOnly) != null;
  }

  public static boolean isUnsupported(@NotNull final PsiModifierListOwner element, @NotNull DefracPlatform platform) {
    if(platform.isGeneric()) {
      return false;
    }

    final PsiModifierList list = element.getModifierList();
    return list != null && list.findAnnotation(DefracPlatform.PLATFORM_TO_UNSUPPORTED_ANNOTATION.get(platform)) != null;
  }

  public static boolean hasUnsupported(@NotNull final PsiModifierListOwner element) {
    final PsiModifierList list = element.getModifierList();
    if(list == null) {
      return false;
    }

    for(final String name : Names.ALL_UNSUPPORTED) {
      if(list.findAnnotation(name) != null) {
        return true;
      }
    }

    return false;
  }

  public static boolean hasMacro(@NotNull final PsiModifierListOwner element,
                                 @NotNull final DefracPlatform platform) {
    final PsiModifierList list = element.getModifierList();
    return list != null && list.findAnnotation(DefracPlatform.PLATFORM_TO_MACRO_ANNOTATION.get(platform)) != null;
  }

  public static boolean hasInjection(@NotNull final PsiModifierListOwner element,
                                     @NotNull final DefracPlatform platform) {
    final PsiModifierList list = element.getModifierList();
    return list != null && list.findAnnotation(DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform)) != null;
  }

  public static boolean hasInjector(@NotNull final PsiModifierListOwner element) {
    final PsiModifierList list = element.getModifierList();
    return list != null && list.findAnnotation(Names.defrac_annotation_Injector) != null;
  }

  /**
   * Find the injector via the @Injector annotation
   * @param element The current class; the injection
   * @return The injector if found via annotation; null if not found or no annotation is present
   */
  @Nullable
  public static PsiClass getInjector(@NotNull final PsiModifierListOwner element) {
    final PsiModifierList list = element.getModifierList();

    return list == null
        ? null
        : findClassInDefracAnnotation(
            list.findAnnotation(Names.defrac_annotation_Injector),
            InjectorClassReference.class);
  }

  /**
   * Find the injection via the @Inject annotation
   *
   * <p>This method looks for the more specific annotation first
   *
   * @param element The current class; the injector
   * @param platform The platform of the injection
   * @return The injection if found via annotation; null if not found or no annotation is present
   */
  @Nullable
  public static PsiClass getInjection(@NotNull final PsiModifierListOwner element,
                                      @NotNull final DefracPlatform platform) {
    final PsiModifierList list = element.getModifierList();

    if(list == null) {
      return null;
    }

    PsiAnnotation annotation =
        list.findAnnotation(DefracPlatform.PLATFORM_TO_INJECT_ANNOTATION.get(platform));

    if(annotation == null && !platform.isGeneric()) {
      annotation =
          list.findAnnotation(Names.defrac_annotation_Inject);
    }

    return findClassInDefracAnnotation(annotation, InjectionClassReference.class);
  }

  @Contract("null, _ -> null")
  @Nullable
  private static PsiClass findClassInDefracAnnotation(@Nullable final PsiAnnotation annotation,
                                                      @NotNull final Class<? extends ClassReferenceBase> referenceType) {
    if(annotation == null) {
      return null;

    }
    final PsiAnnotationParameterList parameterList = annotation.getParameterList();
    final PsiNameValuePair[] attributes = parameterList.getAttributes();

    if(attributes.length == 0) {
      return null;
    }

    final PsiAnnotationMemberValue value = attributes[0].getValue();

    if(value == null) {
      return null;
    }

    final PsiReference[] references = value.getReferences();

    for(final PsiReference reference : references) {
      if(!referenceType.isInstance(reference)) {
        continue;
      }

      final ClassReferenceBase defracRef = (ClassReferenceBase)reference;

      if(isNullOrEmpty(defracRef.getValue())) {
        continue;
      }

      final ResolveResult[] resolveResults = defracRef.multiResolve();

      if(resolveResults.length != 0) {
        for(final PsiElement result : mapElements(resolveResults)) {
          if(result instanceof PsiClass) {
            return (PsiClass)result;
          }
        }
      }
    }

    return null;
  }

  @Nullable
  public static <T extends DefracReference> T findReference(
      @NotNull final PsiAnnotation annotation,
      @NotNull final Class<T> referenceClass) {
    final PsiAnnotationParameterList parameterList = annotation.getParameterList();
    final PsiNameValuePair[] values = parameterList.getAttributes();

    if(values.length < 1) {
      return null;
    }

    final PsiAnnotationMemberValue value = values[0].getValue();

    if(!(value instanceof PsiLiteralExpression)) {
      return null;
    }

    final PsiLiteralExpression literal = (PsiLiteralExpression)value;
    final PsiReference[] references = literal.getReferences();

    for(final PsiReference reference : references) {
      if(referenceClass.isInstance(reference)) {
        return referenceClass.cast(reference);
      }
    }

    return null;
  }

  @Contract("null -> false")
  public static boolean isWriteOnlyAnnotation(@Nullable final PsiAnnotation annotation) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName = annotation.getQualifiedName();
    return qualifiedName != null && isWriteOnlyAnnotation(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isWriteOnlyAnnotation(@Nullable final String qualifiedName) {
    return Names.defrac_dni_WriteOnly.equals(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isReadOnlyAnnotation(@Nullable final PsiAnnotation annotation) {
    if(annotation == null) {
      return false;
    }

    final String qualifiedName = annotation.getQualifiedName();
    return qualifiedName != null && isReadOnlyAnnotation(qualifiedName);
  }

  @Contract("null -> false")
  public static boolean isReadOnlyAnnotation(@Nullable final String qualifiedName) {
    return Names.defrac_dni_ReadOnly.equals(qualifiedName);
  }

  @NotNull
  public static Set<PsiClass> mapToContainingClasses(@NotNull final List<PsiMethod> methods) {
    final Set<PsiClass> set = Sets.newLinkedHashSet();

    for(final PsiMethod method : methods) {
      final PsiClass klass = method.getContainingClass();

      if(klass == null) {
        continue;
      }

      set.add(klass);
    }

    return set;
  }

  @Nullable
  public static String getValue(@NotNull final PsiLiteralExpression literalExp) {
    final Object value = literalExp.getValue();
    return value instanceof String ? (String)value : null;
  }

  @Nullable
  @Contract("null -> null")
  public static String intrinsicNameOf(@Nullable final PsiModifierListOwner element) {
    if(element == null) {
      return null;
    }

    final PsiModifierList modifierList = element.getModifierList();
    if(modifierList == null) {
      return null;
    }

    final PsiAnnotation annotation = modifierList.findAnnotation(Names.defrac_dni_Intrinsic);
    if(annotation == null) {
      return null;
    }

    final PsiAnnotationParameterList parameterList =
        annotation.getParameterList();
    final PsiNameValuePair[] attributes = parameterList.getAttributes();

    if(attributes.length < 1) {
      return null;
    }

    final PsiElement value = attributes[0].getValue();
    if(value instanceof PsiLiteralExpression) {
      return getValue((PsiLiteralExpression)value);
    }

    return null;
  }


  /**
   * Finds the injector class for a given injection
   *
   * <p>The injector class contains the &auml;Inject annotation to the given class.
   *
   * @param klass The injected class
   * @return The injector; {@literal null} if the given class isn't an injection
   */
  @Contract("null -> null")
  @Nullable
  public static PsiClass findInjector(@Nullable final PsiClass klass) {
    if(klass == null) {
      return null;
    }

    return CachedValuesManager.getCachedValue(klass, new CachedValueProvider<PsiClass>() {
      @Nullable
      @Override
      public Result<PsiClass> compute() {
        final Query<PsiReference> query = ReferencesSearch.search(klass);
        final Ref<PsiClass> ref = Ref.create();

        query.forEach(new Processor<PsiReference>() {
          @Override
          public boolean process(final PsiReference reference) {
            if(reference instanceof InjectionClassReference) {
              ref.set(PsiTreeUtil.getParentOfType(reference.getElement(), PsiClass.class));
              return false;
            }

            return true;
          }
        });

        final PsiClass result = ref.get();

        if(result == null) {
          return Result.create(null, PsiModificationTracker.MODIFICATION_COUNT);
        } else {
          return Result.create(result, checkNotNull(result.getModifierList()));
        }
      }
    });
  }

  @Contract("null -> false")
  public static boolean hasUsage(@Nullable final PsiElement element) {
    if(element == null) {
      return false;
    }

    final Query<PsiReference> query =
        ReferencesSearch.search(element, element.getUseScope());

    return query.findFirst() != null;
  }
}
