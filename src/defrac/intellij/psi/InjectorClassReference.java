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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.util.IconUtil;
import com.intellij.util.Query;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static defrac.intellij.psi.DefracPsiUtil.findReference;
import static defrac.intellij.psi.DefracPsiUtil.isInjectorAnnotation;

/**
 *
 */
public final class InjectorClassReference extends ClassReferenceBase {
  @NotNull
  private static final Logger LOG = Logger.getInstance(InjectorClassReference.class.getName());

  @Nullable
  @Contract("null -> null")
  public static InjectorClassReference getInstance(@Nullable final PsiAnnotation annotation) {
    if(!isInjectorAnnotation(annotation)) {
      return null;
    }

    return findReference(annotation, InjectorClassReference.class);
  }

  public InjectorClassReference(@NotNull final String value,
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

    final GlobalSearchScope scope = facet.getMultiPlatformClassSearchScope(platform);
    final PsiJavaFile file = (PsiJavaFile)getElement().getContainingFile();

    if(file == null) {
      return NO_VARIANTS;
    }

    final PsiClass enclosingClass =
        getParentOfType(getElement(), PsiClass.class, false);

    if(enclosingClass == null) {
      return NO_VARIANTS;
    }

    final Query<PsiClass> query = AllClassesSearch.search(scope, project);
    return variantsViaQuery(query, enclosingClass);
  }

  @NotNull
  private Object[] variantsViaQuery(@NotNull final Query<PsiClass> query,
                                    @Nullable final PsiClass exclude) {
    final List<LookupElement> variants = new LinkedList<LookupElement>();
    final Project project = getElement().getProject();
    final String qnameOfExclude = exclude == null ? null : exclude.getQualifiedName();

    for(final PsiClass klass : query.findAll()) {
      if(klass == null || (qnameOfExclude != null && qnameOfExclude.equals(klass.getQualifiedName()))) {
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
        LOG.error(invalidElementAccess);
      }
    }

    return variants.toArray(new Object[variants.size()]);
  }

  @NotNull
  @Override
  protected GlobalSearchScope getSearchScope(@NotNull final Project project) {
    final DefracFacet facet = DefracFacet.getInstance(getElement());
    return facet == null
        ? GlobalSearchScope.allScope(project)
        : facet.getMultiPlatformClassSearchScope(platform);
  }
}
