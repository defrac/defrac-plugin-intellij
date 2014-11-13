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

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IconUtil;
import com.intellij.util.Query;
import defrac.intellij.module.DefracModuleUtil;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public final class DefracPsiReference extends PsiReferenceBase<PsiLiteralExpression> implements PsiPolyVariantReference {
  @NotNull
  private static final Object[] NO_VARIANTS = new Object[0];

  @NotNull
  public static final Comparator<PsiClassType> TYPE_COMPARATOR = new Comparator<PsiClassType>() {
    @Override
    public int compare(final PsiClassType a, final PsiClassType b) {
      //TODO(joa): is there something like rank or better way to sort types?
      return a.getClassName().compareTo(b.getClassName());
    }
  };

  @NotNull
  private final DefracPsiReferenceType referenceType;

  public DefracPsiReference(@NotNull final String value,
                            @NotNull final PsiLiteralExpression element,
                            @NotNull final DefracPsiReferenceType referenceType) {
    super(element, new TextRange(1, 1 + value.length()), false);
    this.referenceType = referenceType;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    switch(referenceType) {
      case MACRO:
        return getMacroVariants();

      case DELEGATE:
        return getDelegateVariants();

      default:
        return NO_VARIANTS;
    }
  }

  @NotNull
  private Object[] getMacroVariants() {
    // all classes of current platform extending defrac.macro.Macro are eligible
    final Project project = getElement().getProject();
    final Module module = DefracModuleUtil.findDefracModule(getElement());

    if(module == null) {
      return NO_VARIANTS;
    }

    final GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);

    final PsiClass macro =
        JavaPsiFacade.getInstance(project).findClass(Names.defrac_macro_Macro, scope);

    if(macro == null) {
      return NO_VARIANTS;
    }

    final Query<PsiClass> query =
        ClassInheritorsSearch.search(macro, scope, true, true);

    return variantsViaQuery(query);
  }

  @NotNull
  private Object[] getDelegateVariants() {
    // all classes of current platform with same type closure are eligible
    final Project project = getElement().getProject();
    final Module module = DefracModuleUtil.findDefracModule(getElement());

    if(module == null) {
      return NO_VARIANTS;
    }

    final GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);
    final PsiJavaFile file = (PsiJavaFile)getElement().getContainingFile();

    if(file == null) {
      return NO_VARIANTS;
    }

    final PsiClass enclosingClass =
        PsiTreeUtil.getParentOfType(getElement(), PsiClass.class, false);

    if(enclosingClass == null) {
      return NO_VARIANTS;
    }

    final PsiClassType[] extendsTypes = enclosingClass.getExtendsListTypes();
    final PsiClassType[] implementsTypes = enclosingClass.getImplementsListTypes();
    final PsiClassType[] baseTypes = ArrayUtil.mergeArrays(extendsTypes, implementsTypes);

    final Query<PsiClass> query;

    if(baseTypes.length == 0) {
      // no base, no interfaces -- give up -- here comes everything!
      query = AllClassesSearch.search(scope, project);
    } else {
      PsiClass base = null;

      for(final PsiClassType baseType : baseTypes) {
        final PsiClass resolved = baseType.resolve();
        if(resolved != null) {
          base = resolved;
          break;
        }
      }

      if(base == null) {
        return NO_VARIANTS;
      }

      query = ClassInheritorsSearch.search(base, scope, true, true);
    }

    return variantsViaQuery(query, enclosingClass,
        extendsTypes.length == 0 ? null : extendsTypes,
        implementsTypes.length == 0 ? null : implementsTypes);
  }

  @NotNull
  private Object[] variantsViaQuery(@NotNull final Query<PsiClass> query) {
    return variantsViaQuery(query, null, null, null);
  }

  @NotNull
  private Object[] variantsViaQuery(@NotNull final Query<PsiClass> query,
                                    @Nullable final PsiClass exclude,
                                    @Nullable final PsiClassType[] typesToExtend,
                                    @Nullable final PsiClassType[] typesToImplement) {
    final List<LookupElement> variants = new LinkedList<LookupElement>();
    final Project project = getElement().getProject();
    final String qnameOfExclude = exclude == null ? null : exclude.getQualifiedName();

    if(typesToExtend != null) {
      sort(typesToExtend);
    }

    if(typesToImplement != null) {
      sort(typesToImplement);
    }

    for(final PsiClass klass : query.findAll()) {
      if(klass == null || (qnameOfExclude != null && qnameOfExclude.equals(klass.getQualifiedName()))) {
        continue;
      }

      if(typesToExtend != null && !typesEqual(typesToExtend, klass.getExtendsListTypes())) {
        continue;
      }

      if(typesToImplement != null && !typesEqual(typesToImplement, klass.getImplementsListTypes())) {
        continue;
      }

      try {
        variants.add(
            LookupElementBuilder.create(klass).
                withIcon(IconUtil.getIcon(klass.getContainingFile().getVirtualFile(), 0, project)).
                withTypeText(klass.getContainingFile().getName())
        );
      } catch(final PsiInvalidElementAccessException invalidElementAccess) {
        invalidElementAccess.printStackTrace();
      }
    }

    return variants.toArray(new Object[variants.size()]);
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(final boolean incompleteCode) {
    final Project project = getElement().getProject();
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    final PsiElement[] results = JavaPsiFacade.getInstance(project).findClasses(getValue(), scope);
    final ResolveResult[] resolveResult = new ResolveResult[results.length];

    for(int i = 0; i < results.length; i++) {
      final PsiElement result = results[i];
      resolveResult[i] = new PsiElementResolveResult(result);
    }

    return resolveResult;
  }

  private boolean typesEqual(@NotNull final PsiClassType[] thisType,
                             @NotNull final PsiClassType[] thatType) {
    if(thisType.length != thatType.length) {
      return false;
    }

    sort(thatType);

    for(int i = 0; i < thisType.length; ++i) {
      if(!PsiTypesUtil.compareTypes(thisType[i], thatType[i], true)) {
        return false;
      }
    }

    return true;
  }

  private void sort(@NotNull final PsiClassType[] types) {
    Arrays.sort(types, TYPE_COMPARATOR);
  }
}
