/*------------------------------------------------------------------*/
/* Copyright (C) SSE-USTC, 2014-2015                                */
/*                                                                  */
/*  FILE NAME             :  Visitor.java                           */
/*  PRINCIPAL AUTHOR      :  qcLiu                                  */
/*  LANGUAGE              :  Java                                   */
/*  TARGET ENVIRONMENT    :  ANY                                    */
/*  DATE OF FIRST RELEASE :  2014/10/05                             */
/*  DESCRIPTION           :  the tiger compiler                     */
/*------------------------------------------------------------------*/

/*
 * Revision log:
 *
 * 
 *
 */
package codegen.bytecode;

import codegen.bytecode.Ast.Class.ClassSingle;
import codegen.bytecode.Ast.Dec.DecSingle;
import codegen.bytecode.Ast.MainClass.MainClassSingle;
import codegen.bytecode.Ast.Method.MethodSingle;
import codegen.bytecode.Ast.Program.ProgramSingle;
import codegen.bytecode.Ast.Stm.*;
import codegen.bytecode.Ast.Type.*;

public interface Visitor
{
  // statements
  public void visit(Aload s);

  public void visit(Areturn s);
  
  public void visit(ArrayLength s);

  public void visit(Astore s);
  
  public void visit(Getfield s);
  
  public void visit(Goto s);
  
  public void visit(Iand s);
 
  public void visit(IAload s);
  
  public void visit(IAstore s);

  public void visit(Ificmplt s);

  public void visit(Ifne s);

  public void visit(Iload s);

  public void visit(Imul s);

  public void visit(Ireturn s);

  public void visit(Istore s);

  public void visit(Iadd s);

  public void visit(Isub s);

  public void visit(Invokevirtual s);

  public void visit(LabelJ s);

  public void visit(Ldc s);

  public void visit(Print s);
 
  public void visit(Putfield s);

  public void visit(New s);
  
  public void visit(NewIntArray s);

  // type
  public void visit(ClassType t);

  public void visit(Int t);

  public void visit(IntArray t);

  // dec
  public void visit(DecSingle d);

  // method
  public void visit(MethodSingle m);

  // class
  public void visit(ClassSingle c);

  // main class
  public void visit(MainClassSingle c);

  // program
  public void visit(ProgramSingle p);
}
