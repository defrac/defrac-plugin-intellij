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
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public final class DelegateClassReference extends ClassReferenceBase {
  @NotNull
  public static final Comparator<PsiClassType> TYPE_COMPARATOR = new Comparator<PsiClassType>() {
    @Override
    public int compare(final PsiClassType a, final PsiClassType b) {
      //TODO(joa): is there something like rank or better way to sort types?
      return a.getClassName().compareTo(b.getClassName());
    }
  };

  @Nullable
  @Contract("null -> null")
  public static DelegateClassReference getInstance(@Nullable final PsiAnnotation annotation) {
    if(!DefracPsiUtil.isDelegateAnnotation(annotation)) {
      return null;
    }

    return DefracPsiUtil.findReference(annotation, DelegateClassReference.class);
  }

  public DelegateClassReference(@NotNull final String value,
                                @NotNull final PsiLiteralExpression element,
                                @NotNull final DefracPlatform platform) {
    super(element, new TextRange(1, 1 + value.length()), platform);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    // all classes of current platform with same type closure are eligible
    final Project project = getElement().getProject();
    final DefracFacet facet = DefracFacet.getInstance(getElement());

    if(facet == null) {
      return NO_VARIANTS;
    }

    final GlobalSearchScope scope = facet.getDelegateSearchScope(platform);
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
      // There is no filter (no bases or interfaces the delegate must implement)
      // so we offer the user everything we got.
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
        extendsTypes.length    == 0 ? null : extendsTypes,
        implementsTypes.length == 0 ? null : implementsTypes);
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
                withInsertHandler(QualifiedClassNameInsertHandler.INSTANCE).
                withIcon(IconUtil.getIcon(klass.getContainingFile().getVirtualFile(), 0, project)).
                withTypeText(klass.getContainingFile().getName())
        );
      } catch(final PsiInvalidElementAccessException invalidElementAccess) {
        invalidElementAccess.printStackTrace();
      }
    }

    return variants.toArray(new Object[variants.size()]);
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

  @NotNull
  @Override
  protected GlobalSearchScope getSearchScope(@NotNull final Project project) {
    final DefracFacet facet = DefracFacet.getInstance(getElement());
    return facet == null
        ? GlobalSearchScope.allScope(project)
        : facet.getDelegateSearchScope(platform);
  }
}
