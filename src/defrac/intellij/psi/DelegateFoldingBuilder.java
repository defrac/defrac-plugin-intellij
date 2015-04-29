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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public final class DelegateFoldingBuilder extends FoldingBuilderEx {
  private static final FoldingGroup GROUP = FoldingGroup.newGroup("delegate");

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull final PsiElement root,
                                              @NotNull final Document document,
                                              final boolean quick) {
    if(quick || !(root instanceof PsiJavaFile)) {
      // Delegate resolution isn't quick ಠ_ಠ
      return FoldingDescriptor.EMPTY;
    }

    final PsiJavaFile javaFile = (PsiJavaFile)root;
    ArrayList<FoldingDescriptor> result = null;

    for(final PsiClass klass : javaFile.getClasses()) {
      final PsiClass delegatedClass = DefracPsiUtil.getDelegatedClass(klass);

      if(delegatedClass == null) {
        continue;
      }

      PsiReferenceList list = klass.getExtendsList();
      if(list == null || list.getTextRange().isEmpty()) list = klass.getImplementsList();
      if(list != null && !list.getTextRange().isEmpty()) {
        if(result == null) {
          result = new ArrayList<FoldingDescriptor>(1);
        }

        final FoldingDescriptor descriptor =
            new FoldingDescriptor(list.getNode(), list.getTextRange(), GROUP, Collections.<Object>singleton(delegatedClass), true);

        result.add(descriptor);
      }

      final Map<String, PsiField> fields =
          Maps.newHashMapWithExpectedSize(klass.getFields().length);

      for(final PsiField field : klass.getFields()) {
        fields.put(field.getName(), field);
      }

      for(final PsiField field : delegatedClass.getAllFields()) {
        final PsiField delegatedField = fields.get(field.getName());

        if(delegatedField == null) {
          continue;
        }

        if(result == null) {
          result = new ArrayList<FoldingDescriptor>(1);
        }

        final FoldingDescriptor descriptor =
            new FoldingDescriptor(
                delegatedField.getNode(),
                delegatedField.getTextRange(),
                GROUP,
                ImmutableSet.<Object>of(delegatedClass, delegatedField), true);

        result.add(descriptor);
      }
    }

    return result == null ? FoldingDescriptor.EMPTY : result.toArray(new FoldingDescriptor[result.size()]);
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull final ASTNode node) {
    final PsiElement element = node.getPsi();

    if(element instanceof PsiField) {
      return "inherits "+((PsiField)element).getName();
    }

    final PsiClass klass = PsiTreeUtil.getParentOfType(element, PsiClass.class);

    if(klass != null) {
      final PsiClass delegatedClass = DefracPsiUtil.getDelegatedClass(klass);

      if(delegatedClass != null) {
        return "delegates "+delegatedClass.getName();
      }
    }

    return null;
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull final ASTNode node) {
    return true;
  }
}
