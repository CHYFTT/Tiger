/*------------------------------------------------------------------*/
/* Copyright (C) SSE-USTC, 2014-2015                                */
/*                                                                  */
/*  FILE NAME             :  DeadCode.java                          */
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

package ast.optimizations;

import java.util.LinkedList;

import ast.Ast.Class;
import ast.Ast.Method;
import ast.Ast.Stm;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec.DecSingle;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;
import ast.Ast.Exp.*;
import ast.Ast.Stm.*;
import ast.Ast.Type.*;

// Dead code elimination optimizations on an AST.

public class DeadCode implements ast.Visitor
{
  private ast.Ast.Class.T newClass;
  private ast.Ast.MainClass.T mainClass;
  public ast.Ast.Program.T program;
  public  ast.Ast.Stm.T  stm;
  public LinkedList<Class.T> classes;
  public LinkedList<ast.Ast.Method.T> methods;
  public LinkedList<Stm.T> stms;
  public ast.Ast.Method.T method;
  
  public boolean isTypeBoolean=false;
  public boolean isNot=false;
  public boolean isTrue=false;
  
  public DeadCode()
  {
    this.newClass = null;
    this.mainClass = null;
    this.program = null;
  }

  // //////////////////////////////////////////////////////
  // 
  public String genId()
  {
    return util.Temp.next();
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
	  this.isTypeBoolean=false;
  }

  @Override
  public void visit(And e)
  {
	  e.left.accept(this);
	  boolean left=this.isTypeBoolean;
	  boolean leftTrue=this.isTrue;
	  e.right.accept(this);
	  boolean right=this.isTypeBoolean;
	  boolean rightTrue=this.isTrue;
	  if(left&&right)
	  {
		  this.isTypeBoolean=true;//为了支持递归
		  if(leftTrue&&rightTrue)
			  this.isTrue=true;
		  else
			  this.isTrue=false;
	  }
	  return;
  }

  @Override
  public void visit(ArraySelect e)
  {
	  this.isTypeBoolean=false;
  }

  @Override
  public void visit(Call e)
  {
	  this.isTypeBoolean=false;
    return;
  }

  @Override
  public void visit(False e)
  {
	  this.isTypeBoolean=true;
	  this.isTrue=false;
	  return;
  }

  @Override
  public void visit(Id e)
  {
	  this.isTypeBoolean=false;
    return;
  }

  @Override
  public void visit(Length e)
  {
	  this.isTypeBoolean=false;
  }

  @Override
  public void visit(Lt e)
  {
	  this.isTypeBoolean=false;
    return;
  }

  @Override
  public void visit(NewIntArray e)
  {
	  this.isTypeBoolean=false;
	  return;
  }

  @Override
  public void visit(NewObject e)
  {
	  this.isTypeBoolean=false;
    return;
  }

  @Override
  public void visit(Not e)
  {
	  //可以用来简化类似！！！！！！！！！Exp的代码
	  this.isTypeBoolean=true;
	  e.exp.accept(this);
		if (this.isTypeBoolean)
			this.isTrue = !this.isTrue;
		else
			return;
  }

  @Override
  public void visit(Num e)
  {
	  this.isTypeBoolean=false;
    return;
  }

  @Override
  public void visit(Sub e)
  {
	  this.isTypeBoolean=false;
    return;
  }

  @Override
  public void visit(This e)
  {
	  this.isTypeBoolean=false;
    return;
  }

  @Override
  public void visit(Times e)
  {
    this.isTypeBoolean=false;
    return;
  }

  @Override
  public void visit(True e)
  {
	  this.isTypeBoolean=true;
	  this.isTrue=true;
	  return;
  }

  // statements
  @Override
  public void visit(Assign s)
  {
    this.stm=s;
    return;
  }

  @Override
  public void visit(AssignArray s)
  {
	  this.stm=s;
  }

  @Override
  public void visit(Block s)
  {
	  for(ast.Ast.Stm.T ss:s.stms)
	  {
		  ss.accept(this);
	  }
  }

  @Override
  public void visit(If s)
  {//重点！！！！！！！！！！！！！！！!
	  s.condition.accept(this);
	  if(this.isTypeBoolean)
	  {
		  if(this.isTrue)
			  this.stm=s.thenn;
		  else
			  this.stm=s.elsee;
	  }
	  else
		  this.stm=s;
    return;
  }

  @Override
  public void visit(Print s)
  {
	  this.stm=s;
    return;
  }

  @Override
  public void visit(While s)
  {
	//重点！！！！！！！！！！！！！！！
	  s.condition.accept(this);
	  if(this.isTypeBoolean&&this.isTrue)
		 return;//消除true死循环，并不是所有死循环。
	  else
	  {
		  this.stm=s;
  
	  }
  }

  // type
  @Override
  public void visit(Boolean t)
  {
	  return;
  }

  @Override
  public void visit(ClassType t)
  {
	  return;
  }

  @Override
  public void visit(Int t)
  {
	  return;
  }

  @Override
  public void visit(IntArray t)
  {
	  return;
  }

  // dec
  @Override
  public void visit(DecSingle d)
  {
    return;
  }

  // method
  @Override
  public void visit(MethodSingle m)
  {
	  stms=new LinkedList<Stm.T>();
	  for(ast.Ast.Stm.T s:m.stms)
	  {
		  s.accept(this);
		  if(this.stm!=null)//While可能返回空。
			  stms.add(this.stm);
	  }
	  this.method=new ast.Ast.Method.MethodSingle(
			  m.retType, m.id, m.formals, m.locals, this.stms, m.retExp);
    return;
  }

  // class
  @Override
  public void visit(ClassSingle c)
  {
	  methods=new LinkedList<Method.T>();
	  for(ast.Ast.Method.T m:c.methods)
	  {
		  ast.Ast.Method.MethodSingle mm=(ast.Ast.Method.MethodSingle)m;
		  mm.accept(this);
		  methods.add(this.method);
	  }
    this.newClass=new ast.Ast.Class.ClassSingle(c.id, c.extendss, c.decs,methods);
    return;
  }

  // main class
  @Override
  public void visit(MainClassSingle c)
  {
    c.stm.accept(this);
    this.mainClass=new ast.Ast.MainClass.MainClassSingle(c.id, c.arg, this.stm);
    
    return;
  }

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    
 // You should comment out this line of code:
	  p.mainClass.accept(this);
	  
	  classes=new LinkedList<Class.T>();
	  for(ast.Ast.Class.T c:p.classes)
	  {
		  ast.Ast.Class.ClassSingle cc=(ast.Ast.Class.ClassSingle)c;
		  cc.accept(this);
		  classes.add(newClass);
	  }
    this.program = new ast.Ast.Program.ProgramSingle(mainClass, classes);
	 // this.program=p;

    if (control.Control.trace.equals("ast.DeadCode")){
      System.out.println("before optimization:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      p.accept(pp);
      System.out.println("after optimization:");
      this.program.accept(pp);
    }
    return;
  }
}
