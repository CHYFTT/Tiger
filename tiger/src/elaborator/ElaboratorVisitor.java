package elaborator;

import java.util.LinkedList;

import ast.Ast;
import ast.Ast.Class;
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
import ast.Ast.MainClass;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec;
import ast.Ast.Exp;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type;
import ast.Ast.Type.ClassType;
import control.Control.ConAst;

public class ElaboratorVisitor implements ast.Visitor
{
  public ClassTable classTable; // symbol table for class
  public MethodTable methodTable; // symbol table for each method
  public String currentClass; // the class name being elaborated
  public Type.T type; // type of the expression being elaborated
  public String currentMethod;
  public int linenum;
  public ElaboratorVisitor()
  {
    this.classTable = new ClassTable();
    this.methodTable = new MethodTable();
    this.currentClass = null;
    this.type = null;
    
  }
  public enum Error
  {
	MISTYPE,
	UNDECL,
	RET;
  }
private void error()
{
	System.exit(1);
}
  private void error(Error c,int linenum)
  {
	  switch(c){
	  case MISTYPE:
		  System.err.println("error:type mismatch at line "+linenum);
		  System.err.println("need type:"+type.toString());
		  System.exit(1);
		  break;
	  case UNDECL:
		  System.err.println("error:un decl var at line "+linenum);
		  System.exit(1);
		  break;
	  case RET:
		  System.err.println("error:return val mis at line "+linenum);
		  System.err.println("return type must be "+type.toString());
		  System.exit(1);
	  }
	  
    
    
    
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
	  e.left.accept(this);
	  Type.T t=this.type;
	  e.right.accept(this);
	  if(!t.toString().equals(this.type.toString()))
		  error(Error.MISTYPE,e.linenum);								
	  if(!t.toString().equals("@int"))			
		  error(Error.MISTYPE,e.linenum);
	  return;
	  
  }

  @Override
  public void visit(And e)
  {
	  e.left.accept(this);
	  Type.T t=this.type;
	  e.right.accept(this);
	  if(!t.toString().equals(this.type.toString()))
		  error(Error.MISTYPE,e.linenum);
	  return;
  }

  @Override
  public void visit(ArraySelect e)
  {
	  
	  e.index.accept(this);
	  if(!this.type.toString().equals("@int"))
		  error(Error.MISTYPE,e.linenum);
	  e.array.accept(this);
	  //System.out.println(this.type.toString());
	  
	  return;
  }

  @Override
  public void visit(Call e)
  {
    Type.T leftty;
    Type.ClassType ty = null;

    e.exp.accept(this);
    leftty = this.type;
    if (leftty instanceof ClassType) {
      ty = (ClassType) leftty;
      e.type = ty.id;//将调用者的id记录
    } else
      error(Error.MISTYPE,e.linenum);
    MethodType mty = this.classTable.getm(ty.id, e.id);//在Tree里面找accept
    //收集call的所有参数的Type
    java.util.LinkedList<Type.T> argsty = new LinkedList<Type.T>();
    for (Exp.T a : e.args) {
      a.accept(this);
      argsty.addLast(this.type);
    }
    //验证方法的参数个数是否匹配
    if (mty.argsType.size() != argsty.size())
      error(Error.MISTYPE,e.linenum);
    //验证方法的参数类型是否匹配
    for (int i = 0; i < argsty.size(); i++) {
      Dec.DecSingle dec = (Dec.DecSingle) mty.argsType.get(i);
      if (dec.type.toString().equals(argsty.get(i).toString()))
    	  ;//如果相等
      else
      {//不相等时还可能可父类型匹配
    	  /*
    	   * 此时要比较的两个type必须是ClassType的实例。
    	   * 因为Classbinding对象里面记录的extenss，通过classTable查看是否有父类
    	   * 当确实存在父类时，直接用父类型替换Call对象的参数类型列表
    	   */
    	  if(dec.type instanceof ClassType&&
    			  argsty.get(i) instanceof ClassType)
    	  {
    		  String maybesub=argsty.get(i).toString();
    		  ClassBinding cbb=this.classTable.get(maybesub);
    		  if(dec.type.toString().equals(cbb.extendss))
    		  {
    			  Type.ClassType tc=new Type.ClassType(cbb.extendss);
    			  argsty.set(i, tc);
    		  }
    		  else
    		 	error(Error.MISTYPE,e.linenum);
    	  }
      }
        
    }
    this.type = mty.retType;
    e.at = argsty;
    e.rt = this.type;
    return;
  }

  @Override
  public void visit(False e)
  {
	  this.type=new Type.Boolean();
  }

  @Override
  public void visit(Id e)
  {
    // first look up the id in method table
    Type.T type = this.methodTable.get(e.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass, e.id);
      // mark this id as a field id, this fact will be
      // useful in later phase.
      e.isField = true;
    }
    if (type == null)
      error(Error.UNDECL,e.linenum);
    this.type = type;
    // record this type on this node for future use.
    e.type = type;//给这个id加上类型。
    return;
  }

  @Override
  public void visit(Length e)
  {
	  e.array.accept(this);
	  
	  this.type= new Type.Int();
	  return;
  }

  @Override
  public void visit(Lt e)
  {
    e.left.accept(this);
    Type.T ty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(ty.toString()))
      error(Error.MISTYPE,e.linenum);
    this.type = new Type.Boolean();
    return;
  }

  @Override
  public void visit(NewIntArray e)
  {
	  e.exp.accept(this);
	  if(!this.type.toString().equals("@int"))
		  error(Error.MISTYPE,e.linenum);
	  this.type=new Type.IntArray();
  }

  @Override
  public void visit(NewObject e)
  {
    this.type = new Type.ClassType(e.id);
    return;
  }

  @Override
  public void visit(Not e)
  {
	  e.exp.accept(this);
	  this.type=new Type.Boolean();
  }

  @Override
  public void visit(Num e)
  {
    this.type = new Type.Int();
    return;
  }

  @Override
  public void visit(Sub e)
  {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(leftty.toString()))
      error(Error.MISTYPE,e.linenum);
    this.type = new Type.Int();
    return;
  }

  @Override
  public void visit(This e)
  {
    this.type = new Type.ClassType(this.currentClass);
    return;
  }

  @Override
  public void visit(Times e)
  {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(leftty.toString()))
    	error(Error.MISTYPE,e.linenum);
    this.type = new Type.Int();
    return;
  }

  @Override
  public void visit(True e)
  {
	 this.type=new Type.Boolean(); 
  }

  // statements
  @Override
  public void visit(Assign s)
  {
    // first look up the id in method table
    Type.T type = this.methodTable.get(s.id);
    // if search failed, then s.id must
    if (type == null)
    {
      type = this.classTable.get(this.currentClass, s.id);
      s.isField=true;
    }
    if (type == null)
    	error(Error.UNDECL,s.linenum);
    //s.isField=true;
    s.type=type;//为了适应bytecode的需要！！！！！在此时需要给Assign的type赋值！！！！
    s.exp.accept(this);//type是存放=左边的id的类型，this.type是存放=右边exp的类型，
    					//因此，执行完s.exp.accept(this)后，this.type一定要改变。
    if(!s.exp.getClass().getName().equals("ast.Ast$Exp$ArraySelect"))
    {
    	//type代表左边，this.type代表右边
    	if(!this.type.toString().equals(type.toString()))
    		error(Error.MISTYPE,s.linenum);
    }
    else//如果=右边是ArraySelect类型，那左边只能是int型。
    {
    	if(!type.toString().equals("@int"))
    		error(Error.MISTYPE,s.linenum);
    }
    return;
  }

  @Override
  public void visit(AssignArray s)
  {
	  Type.T type=this.methodTable.get(s.id);
	 
	  if(type==null)
	  {
		  type=this.classTable.get(this.currentClass, s.id);
		  s.isField=true;
	  }
	  if(type==null)
		  error(Error.UNDECL,s.linenum);
	  s.tyep=type;
	  //判断索引号
	 // System.out.println(type.toString());// ---------------------------------------
	  s.index.accept(this);
	  if(!this.type.toString().equals("@int"))
		  error(Error.UNDECL,s.linenum);
	  //System.out.println("index finished.................");
	  //判断id类型
	  s.exp.accept(this);
	 // System.out.println(s.exp.getClass().getName());
	  if(!s.exp.getClass().getName().equals("ast.Ast$Exp$ArraySelect"))
	  {
		  if(!this.type.toString().equals("@int"))
			  error(Error.MISTYPE,s.linenum);
	  }
	  else
	  {
		  if(!type.toString().equals("@int[]"))
			  error(Error.MISTYPE,s.linenum);
		  
	  }
	  
  }

  @Override
  public void visit(Block s)
  {
	  for(Stm.T t:s.stms)
		  t.accept(this);
  }

  @Override
  public void visit(If s)
  {
    s.condition.accept(this);
    if (!this.type.toString().equals("@boolean"))
    	error(Error.MISTYPE,s.linenum);
    s.thenn.accept(this);
    s.elsee.accept(this);
    return;
  }

  @Override
  public void visit(Print s)
  {
    s.exp.accept(this);
    if(!s.exp.getClass().getName().equals("ast.Ast$Exp$ArraySelect"))
    {
    	if (!this.type.toString().equals("@int"))
    		error(Error.MISTYPE,s.linenum);
    }
    else
    {
    	if (!this.type.toString().equals("@int[]"))
    		error(Error.MISTYPE,s.linenum);
    }
    return;
  }

  @Override
  public void visit(While s)
  {
	  s.condition.accept(this);
	  if(!this.type.toString().equals("@boolean"))
		  error(Error.MISTYPE,s.linenum);
	  s.body.accept(this);
	  return;
  }

  // type
  @Override
  public void visit(Type.Boolean t)
  {
	  System.out.println("The Ast is wrong!");
	  error();
  }

  @Override
  public void visit(Type.ClassType t)
  {
	  System.out.println("The Ast is wrong!");
	  error();
  }

  @Override
  public void visit(Type.Int t)
  {
	  System.out.println("The Ast is wrong!");
	  error();
  }

  @Override
  public void visit(Type.IntArray t)
  {
	  System.out.println("The Ast is wrong!");
	  error();
  }

  // dec
  @Override
  public void visit(Dec.DecSingle d)
  {
	  System.out.println("The Ast is wrong!");
	  error();
  }

  // method
  @Override
  public void visit(Method.MethodSingle m)
  {
    // construct the method table
	this.methodTable = new  MethodTable();
    this.methodTable.put(m.formals, m.locals);

    if (ConAst.elabMethodTable)
      this.methodTable.dump();

    for (Stm.T s : m.stms)
    {
    	System.out.println("This is the Stm:"+s.linenum );
    	s.accept(this);
    	linenum=s.linenum;
    }
    ClassBinding cb=this.classTable.get(currentClass);
    MethodType methodtype=cb.methods.get(m.id);
    
     m.retExp.accept(this);
     if(!methodtype.retType.toString().equals(this.type.toString()))//Why??
    	 //methodtype.retType==this.type
     {
    	 
    	 error(Error.RET,linenum);
     }
    return;
  }

  // class
  @Override
  public void visit(Class.ClassSingle c)
  {
    this.currentClass = c.id;

    for (Method.T m : c.methods) {
    	MethodSingle mm = (MethodSingle) m;
    	System.out.println("This is the method:  "+ mm.id);
      m.accept(this);
    }
    return;
  }

  // main class
  @Override
  public void visit(MainClass.MainClassSingle c)
  {
    this.currentClass = c.id;
    // "main" has an argument "arg" of type "String[]", but
    // one has no chance to use it. So it's safe to skip it...
    
    c.stm.accept(this);
    
    return;
  }

  // ////////////////////////////////////////////////////////
  // step 1: build class table
  // class table for Main class
  private void buildMainClass(MainClass.MainClassSingle main)
  {
    this.classTable.put(main.id, new ClassBinding(null));
  }

  // class table for normal classes
  private void buildClass(ClassSingle c)
  {
    this.classTable.put(c.id, new ClassBinding(c.extendss));
    //VarDecls
    for (Dec.T dec : c.decs) {
      Dec.DecSingle d = (Dec.DecSingle) dec;
      this.classTable.put(c.id, d.id, d.type);
    }
    //Method
    for (Method.T method : c.methods) {
      MethodSingle m = (MethodSingle) method;
      this.classTable.put(c.id, m.id, new MethodType(m.retType, m.formals));
    }
  }

  // step 1: end
  // ///////////////////////////////////////////////////

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    // ////////////////////////////////////////////////
    // step 1: build a symbol table for class (the class table)
    // a class table is a mapping from class names to class bindings
    // classTable: className -> ClassBinding{extends, fields, methods}
    buildMainClass((MainClass.MainClassSingle) p.mainClass);
    for (Class.T c : p.classes) {
      buildClass((ClassSingle) c);
    }

    // we can double check that the class table is OK!
    if (control.Control.ConAst.elabClassTable) {
      this.classTable.dump();
    }

    // ////////////////////////////////////////////////
    // step 2: elaborate each class in turn, under the class table
    // built above.
    System.out.println("mainClass....................");
    p.mainClass.accept(this);
    for (Class.T c : p.classes) {
    	System.out.println("normalClass....................");
      c.accept(this);
    }

  }
}
