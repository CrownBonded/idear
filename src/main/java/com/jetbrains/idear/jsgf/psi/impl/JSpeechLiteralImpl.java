// This is a generated file. Not intended for manual editing.
package com.jetbrains.idear.jsgf.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.idear.jsgf.psi.JSpeechTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.jetbrains.idear.jsgf.psi.*;
import com.jetbrains.idear.jsgf.JSpeechPsiImplUtil;

public class JSpeechLiteralImpl extends ASTWrapperPsiElement implements JSpeechLiteral {

  public JSpeechLiteralImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JSpeechVisitor) ((JSpeechVisitor)visitor).visitLiteral(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getString() {
    return findNotNullChildByType(STRING);
  }

}
