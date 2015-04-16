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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.IconUtil;
import com.intellij.util.Query;
import defrac.intellij.DefracPlatform;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public final class MacroClassReference extends ClassReferenceBase {
  @NotNull
  private static final Logger LOG = Logger.getInstance(MacroClassReference.class.getName());

  public MacroClassReference(@NotNull final PsiLiteralExpression element,
                             final int offset,
                             final int length,
                             @NotNull final DefracPlatform platform) {
    super(element, new TextRange(offset, offset + length), platform);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    // all classes of current platform extending defrac.macro.Macro are eligible
    final Project project = getElement().getProject();
    final DefracFacet facet = DefracFacet.getInstance(getElement());

    if(facet == null) {
      return NO_VARIANTS;
    }

    final PsiClass macro =
        JavaPsiFacade.getInstance(project).findClass(
            Names.defrac_compiler_macro_Macro, GlobalSearchScope.allScope(project));

    if(macro == null) {
      return NO_VARIANTS;
    }

    final GlobalSearchScope scope = facet.getMacroSearchScope(platform);
    final Query<PsiClass> query =
        ClassInheritorsSearch.search(macro, scope, true, true);

    return variantsViaQuery(query);
  }

  @NotNull
  private Object[] variantsViaQuery(@NotNull final Query<PsiClass> query) {
    final List<LookupElement> variants = new LinkedList<LookupElement>();
    final Project project = getElement().getProject();

    for(final PsiClass klass : query.findAll()) {
      if(klass == null) {
        continue;
      }

      try {
        variants.add(
            LookupElementBuilder.create(klass).
                withInsertHandler(QualifiedClassNameInsertHandler.INSTANCE).
                withIcon(IconUtil.getIcon(klass.getContainingFile().getVirtualFile(), 0, project)).
                withTypeText(klass.getContainingFile().getName()));
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
        : facet.getMacroSearchScope(platform);
  }
}
