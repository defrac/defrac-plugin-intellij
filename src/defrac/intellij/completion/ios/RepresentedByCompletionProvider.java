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

package defrac.intellij.completion.ios;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.siyeh.ig.psiutils.ImportUtils;
import defrac.intellij.facet.DefracFacet;
import defrac.intellij.util.Names;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static defrac.intellij.psi.DefracPsiUtil.getValue;
import static defrac.intellij.psi.DefracPsiUtil.intrinsicNameOf;

/**
 *
 */
public final class RepresentedByCompletionProvider extends CompletionProvider<CompletionParameters> {
  public RepresentedByCompletionProvider() {
  }

  @Override
  protected void addCompletions(@NotNull final CompletionParameters args,
                                final ProcessingContext context,
                                @NotNull final CompletionResultSet resultSet) {
    final PsiElement position = args.getPosition();
    final PsiMethodCallExpression methodCall =
        PsiTreeUtil.getParentOfType(position, PsiMethodCallExpression.class);

    if(methodCall == null) {
      return;
    }

    final PsiMethod method = methodCall.resolveMethod();

    if(method != null) {
      contribute(args, resultSet, methodCall, method);
    } else {
      final JavaResolveResult results[] = methodCall.getMethodExpression().multiResolve(true);
      for(final JavaResolveResult result : results) {
        if(result.getElement() instanceof PsiMethod) {
          contribute(args, resultSet, methodCall, (PsiMethod)result.getElement());
        }
      }
    }
  }

  private void contribute(@NotNull final CompletionParameters args,
                          @NotNull final CompletionResultSet resultSet,
                          @NotNull final PsiMethodCallExpression methodCall,
                          @NotNull final PsiMethod method) {

    final DefracFacet facet = DefracFacet.getInstance(methodCall);

    if(facet == null || !facet.getPlatform().isIOS()) {
      return;
    }


    // find argument index
    final int argumentIndex = indexOfArgument(args, methodCall);
    if(argumentIndex < 0) {
      return;
    }

    // find annotation for argument index
    final PsiAnnotation representedBy = findAnnotationAt(method, argumentIndex);
    if(representedBy == null) {
      return;
    }

    final InsertHandler<LookupElement> insertHandler =
        new AutoImportInsertHandler(args.getOriginalFile());

    for(final PsiNameValuePair nameValuePair : representedBy.getParameterList().getAttributes()) {
      final PsiAnnotationMemberValue value = nameValuePair.getValue();

      if(value == null) {
        continue;
      }

      if(value instanceof PsiLiteralExpression) {
        final String className = getValue((PsiLiteralExpression) value);

        if(className == null || !PsiNameHelper.getInstance(value.getProject()).isQualifiedName(className)) {
          continue;
        }

        final JavaPsiFacade jpf = JavaPsiFacade.getInstance(value.getProject());
        final PsiClass klass = jpf.findClass(className, GlobalSearchScope.allScope(value.getProject()));

        if(klass == null) {
          continue;
        }

        addField(resultSet, insertHandler, className, klass);
      }
    }
  }

  private void addField(@NotNull final CompletionResultSet resultSet,
                           @NotNull final InsertHandler<LookupElement> insertHandler,
                           @NotNull final String className,
                           @NotNull final PsiClass klass) {
    for(final PsiField field : klass.getAllFields()) {
      final String intrinsicName = intrinsicNameOf(field);

      if(isNullOrEmpty(intrinsicName)) {
        continue;
      }

      final LookupElementBuilder element =
          JavaLookupElementBuilder.
              forField(field, klass.getName()+'.'+field.getName(), klass).
              withCaseSensitivity(false).
              withInsertHandler(insertHandler).
              withTailText(" aka " + intrinsicName, true);

      resultSet.addElement(element);
      resultSet.addLookupAdvertisement("Represented by "+className);
    }
  }

  private int indexOfArgument(final @NotNull CompletionParameters args, final @NotNull PsiMethodCallExpression methodCall) {
    int argumentIndex = -1;

    final PsiExpressionList argumentList = methodCall.getArgumentList();
    final PsiExpression[] arguments = argumentList.getExpressions();


    for(int i = 0; i < arguments.length; i++) {
      final PsiExpression argument = arguments[i];

      if(argument.getTextRange().contains(args.getOffset())) {
        argumentIndex = i;
        break;
      }
    }
    return argumentIndex;
  }

  @Nullable
  private PsiAnnotation findAnnotationAt(@NotNull final PsiMethod method,
                                         final int argumentIndex) {
    final PsiParameterList parameterList = method.getParameterList();
    final PsiParameter[] parameters = parameterList.getParameters();

    if(argumentIndex >= parameters.length) {
      return null;
    }

    final PsiParameter parameter = parameters[argumentIndex];
    final PsiModifierList parameterModifiers = parameter.getModifierList();

    if(parameterModifiers == null) {
      return null;
    }

    return parameterModifiers.findAnnotation(Names.defrac_dni_RepresentedBy);
  }

  private static class AutoImportInsertHandler implements InsertHandler<LookupElement> {
    @NotNull
    private final PsiElement targetContext;

    public AutoImportInsertHandler(@NotNull final PsiElement targetContext) {
      this.targetContext = targetContext;
    }

    @Override
    public void handleInsert(final InsertionContext ctx, final LookupElement item) {
      final PsiElement element = item.getPsiElement();
      if(element instanceof PsiMember) {
        final PsiMember member = (PsiMember)element;
        final PsiClass owner = member.getContainingClass();

        if(owner != null) {
          ImportUtils.addImportIfNeeded(owner, targetContext);
        }
      }
    }
  }
}
