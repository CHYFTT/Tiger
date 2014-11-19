package ast.optimizations;

import java.util.HashSet;
import java.util.LinkedList;

import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.Type;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.False;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Length;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewIntArray;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Not;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.Exp.True;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;

// Dead class elimination optimizations on an AST.

public class DeadClass implements ast.Visitor
{
  private HashSet<String> set;//存放要保留的class,负责收集有用的class
  private LinkedList<String> worklist;//被牵扯进来的类,且不重复。
  private ast.Ast.Class.T newClass;
  public Program.T program;
  

  public DeadClass()
  {
    this.set = new java.util.HashSet<String>();
    this.worklist = new java.util.LinkedList<String>();
    this.newClass = null;
    this.program = null;
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(And e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(ArraySelect e)
  {
    e.array.accept(this);
    e.index.accept(this);
    return;
  }

  @Override
  public void visit(Call e)
  {
    e.exp.accept(this);
    for (Exp.T arg : e.args) {//call的参数列表元素可以是Exp，而Exp里面可能包含class
      arg.accept(this);
    }
    return;
  }

  @Override
  public void visit(False e)
  {
    return;
  }

  @Override
  public void visit(Id e)
  {
    return;
  }

  @Override
  public void visit(Length e)
  {
    e.array.accept(this);
    return;
  }

  @Override
  public void visit(Lt e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(NewIntArray e)
  {
    e.exp.accept(this);
    return;
  }

  @Override
  public void visit(NewObject e)
  {
    if (this.set.contains(e.id))
      return;
    this.worklist.add(e.id);
    this.set.add(e.id);//NewObject的出现标志着这个类已经通过检查了。
    return;
  }

  @Override
  public void visit(Not e)
  {
    e.exp.accept(this);
    return;
  }

  @Override
  public void visit(Num e)
  {
    return;
  }

  @Override
  public void visit(Sub e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(This e)
  {
    return;
  }

  @Override
  public void visit(Times e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(True e)
  {
    return;
  }

  // statements
  @Override
  public void visit(Assign s)
  {
    s.exp.accept(this);
    return;
  }

  @Override
  public void visit(AssignArray s)
  {
    s.index.accept(this);
    s.exp.accept(this);
    return;
  }

  @Override
  public void visit(Block s)
  {
    for (Stm.T x : s.stms)
      x.accept(this);
    return;
  }

  @Override
  public void visit(If s)
  {
    s.condition.accept(this);
    s.thenn.accept(this);
    s.elsee.accept(this);
    return;
  }

  @Override
  public void visit(Print s)
  {
    s.exp.accept(this);
    return;
  }

  @Override
  public void visit(While s)
  {
    s.condition.accept(this);
    s.body.accept(this);
    return;
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
	  /*
	   * 之所以要对formals，locals，ret都进行accept，只因为这里面可能含有
	   * Exp.NewObject
	   */
	  //参数列表
	  for(ast.Ast.Dec.T decf:m.formals)
	  {
		  ast.Ast.Dec.DecSingle decff=(ast.Ast.Dec.DecSingle)decf;
		  decff.accept(this);
	  }
	  //声明
	  for(ast.Ast.Dec.T dec:m.locals)
	  {
		  ast.Ast.Dec.DecSingle decc=(ast.Ast.Dec.DecSingle)dec;
		  decc.accept(this);
	  }
	  //Stm
    for (Stm.T s : m.stms)
      s.accept(this);
    //返回值
    m.retExp.accept(this);
    return;
  }

  // class
  @Override
  public void visit(ClassSingle c)
  {
	  //有父类情况
	  if(c.extendss!=null)
	  {
		  if(this.worklist.contains(c.extendss))
			  ;
		  else
		  {
			  this.worklist.add(c.extendss);
			  this.set.add(c.extendss);
		  }
	  }
	  //声明
	  /*
	   *  对声明中出现的class不需要保留。
	   */
	  //方法
	  for(ast.Ast.Method.T m:c.methods)
	  {
		  ast.Ast.Method.MethodSingle mm=(ast.Ast.Method.MethodSingle)m;
		  mm.accept(this);
	  }
	  
  }

  // main class
  @Override
  public void visit(MainClassSingle c)
  {
    c.stm.accept(this);
    return;
  }

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    // we push the class name for mainClass onto the worklist
    MainClassSingle mainclass = (MainClassSingle) p.mainClass;
    this.set.add(mainclass.id);//mainClass属于免检产品……
    

    p.mainClass.accept(this);//在mainClass里面很可能会添加worklist的元素

    while (!this.worklist.isEmpty()) {
      String cid = this.worklist.removeFirst();

      for (ast.Ast.Class.T c : p.classes) {
    	  //遍历p的classes
        ClassSingle current = (ClassSingle) c;

        if (current.id.equals(cid)) {
          c.accept(this);
          break;//从p的所有class里面找一个处理,直到worklist为空
          //所以worklist里面不能重复。不然会做重复的工作。
        }
//        else
//        {//既然已经通过了Elab，这种情况实际不会出现。
//        	System.err.println(current.id);
//        	System.err.println(cid);
//        	System.err.println(
//        			"This case shouldn't be occur.Please fix your Elab");
//        	System.exit(0);
//        }
      }
    }

    LinkedList<ast.Ast.Class.T> newClasses = new LinkedList<ast.Ast.Class.T>();
    for (ast.Ast.Class.T classes : p.classes) {
      ClassSingle c = (ClassSingle) classes;
      if (this.set.contains(c.id))//当set中有这个class时，就保留。set中没有的丢弃。
        newClasses.add(c);
    }

    
    this.program =
    new ProgramSingle(p.mainClass, newClasses);
    
    if (control.Control.trace.equals("ast.DeadClass")){
      System.out.println("before optimization:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      p.accept(pp);
      System.out.println("after optimization:");
      this.program.accept(pp);
    }
      
    return;
  }
}
