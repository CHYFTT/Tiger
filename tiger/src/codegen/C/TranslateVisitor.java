/*------------------------------------------------------------------*/
/* Copyright (C) SSE-USTC, 2014-2015                                */
/*                                                                  */
/*  FILE NAME             :  TranslateVisitor.java                  */
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
package codegen.C;

import java.util.LinkedList;

import codegen.C.Ast.Class;
import codegen.C.Ast.Class.ClassSingle;
import codegen.C.Ast.Dec;
import codegen.C.Ast.Exp;
import codegen.C.Ast.Exp.Id;
import codegen.C.Ast.Exp.Lt;
import codegen.C.Ast.Exp.NewObject;
import codegen.C.Ast.Exp.Num;
import codegen.C.Ast.Exp.Sub;
import codegen.C.Ast.Exp.Add;
import codegen.C.Ast.Exp.And;
import codegen.C.Ast.Exp.This;
import codegen.C.Ast.Exp.Times;
import codegen.C.Ast.MainMethod;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.Method;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm;
import codegen.C.Ast.Stm.Assign;
import codegen.C.Ast.Stm.If;
import codegen.C.Ast.Stm.Print;
import codegen.C.Ast.Type;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Vtable;
import codegen.C.Ast.Vtable.VtableSingle;

// Given a Java AST, translate it into a C AST and outputs it.

public class TranslateVisitor implements ast.Visitor
{
  private ClassTable table;//整个的trans过程用ClassTable做辅助
  private String classId;
  private Type.T type; // type after translation
  private Dec.T dec;
  private Stm.T stm;
  private Exp.T exp;//每当XXX.XXX.accept(this)执行后，以上几个变量会改变(this.exp=new XX)
  private Method.T method;//临时存放生成的Method obj
  private LinkedList<Dec.T> tmpVars;//!!!在Call中会用到。
  									//如果调用了方法，就要把对应的类先声明
  
  
  private LinkedList<Class.T> classes;//只存放类的信息。
  private LinkedList<Vtable.T> vtables;//虚函数表，存放所有的方法
  private LinkedList<Method.T> methods;
  /*
   * 在C的Ast中，method不在class里面，用虚方法表来表示哪个方法属于那个类
   * 方法表只存放方法体。虛方法表存放类和类的方法的信息
   */
  private MainMethod.T mainMethod;//main对象
  
  
  public Program.T program;

  public TranslateVisitor()
  {
    this.table = new ClassTable();
    this.classId = null;
    this.type = null;
    this.dec = null;
    this.stm = null;
    this.exp = null;
    this.method = null;
    this.classes = new LinkedList<Class.T>();
    this.vtables = new LinkedList<Vtable.T>();
    this.methods = new LinkedList<Method.T>();
    this.mainMethod = null;
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
  public void visit(ast.Ast.Exp.Add e)
  {
	    e.left.accept(this);
	    Exp.T left = this.exp;
	    e.right.accept(this);
	    Exp.T right = this.exp;
	    this.exp = new Add(left, right);
	    return;
	  
  }

  @Override
  public void visit(ast.Ast.Exp.And e)
  {
	  e.left.accept(this);
	    Exp.T left = this.exp;
	    e.right.accept(this);
	    Exp.T right = this.exp;
	    this.exp = new And(left, right);
	    return;
	  
  }

  @Override
  public void visit(ast.Ast.Exp.ArraySelect e)
  {
	  e.array.accept(this);
	  Exp.T array=this.exp;
	  e.index.accept(this);
	  Exp.T index=this.exp;
	  this.exp=new codegen.C.Ast.Exp.ArraySelect(array,index);
	  return;
  }

  @Override
  public void visit(ast.Ast.Exp.Call e)
  {
    e.exp.accept(this);
    String newid = this.genId();//生成x_0等在mini java AST中不存在的id
    this.tmpVars.add(new Dec.DecSingle(new Type.ClassType(e.type), newid));
    Exp.T exp = this.exp;
    LinkedList<Exp.T> args = new LinkedList<Exp.T>();
    for (ast.Ast.Exp.T x : e.args) {
      x.accept(this);
      args.add(this.exp);
    }
    e.rt.accept(this);
    
  //  this.exp = new Call(newid, exp, e.id, args,e.rt);
    this.exp = new codegen.C.Ast.Exp.Call(newid, exp, e.id, args,this.type);
    return;
  }

  @Override
  public void visit(ast.Ast.Exp.False e)
  {
	  this.exp=new Num(0);
  }

  @Override
  public void visit(ast.Ast.Exp.Id e)
  {
	  
	  boolean isField=e.isField;
	  this.exp = new Id(e.id,isField);
    return;
  }

  @Override
  public void visit(ast.Ast.Exp.Length e)
  {
	  this.type=new Type.IntArray();
	  e.array.accept(this);
	  Exp.T array=this.exp;
	  this.exp=new codegen.C.Ast.Exp.Length(array);
  }

  @Override
  public void visit(ast.Ast.Exp.Lt e)
  {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Lt(left, right);
    return;
  }

  @Override
  public void visit(ast.Ast.Exp.NewIntArray e)
  {
	  e.exp.accept(this);
	  Exp.T t=this.exp;
	  this.exp=new codegen.C.Ast.Exp.NewIntArray(t);
  }

  @Override
  public void visit(ast.Ast.Exp.NewObject e)
  {
    this.exp = new NewObject(e.id);
    return;
  }

  @Override
  public void visit(ast.Ast.Exp.Not e)
  {
	  e.exp.accept(this);
	  Exp.T t=this.exp;
	  this.exp=new codegen.C.Ast.Exp.Not(t);
  }

  @Override
  public void visit(ast.Ast.Exp.Num e)
  {
    this.exp = new Num(e.num);
    return;
  }

  @Override
  public void visit(ast.Ast.Exp.Sub e)
  {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Sub(left, right);
    return;
  }

  @Override
  public void visit(ast.Ast.Exp.This e)
  {
    this.exp = new This();
    return;
  }

  @Override
  public void visit(ast.Ast.Exp.Times e)
  {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Times(left, right);
    return;
  }

  @Override
  public void visit(ast.Ast.Exp.True e)
  {
	  this.exp=new Num(1);
	  
  }

  // //////////////////////////////////////////////
  // statements
  @Override
  public void visit(ast.Ast.Stm.Assign s)
  {
	  boolean isField=s.isField;
    s.exp.accept(this);
    this.stm = new Assign(s.id, this.exp,isField);
    return;
  }

  @Override
  public void visit(ast.Ast.Stm.AssignArray s)
  {
	  boolean isField=s.isField;
	  s.index.accept(this);
	  Exp.T index=this.exp;
	  s.exp.accept(this);
	  
	  this.stm=new codegen.C.Ast.Stm.AssignArray(s.id, index, exp,isField);
  }

  @Override
  public void visit(ast.Ast.Stm.Block s)
  {
	  LinkedList<codegen.C.Ast.Stm.T> tempstms=
			  new java.util.LinkedList<Ast.Stm.T>();
	  for(ast.Ast.Stm.T t:s.stms)
	  {
		  t.accept(this);
		  tempstms.add(this.stm);
	  }
	  this.stm=new codegen.C.Ast.Stm.Block(tempstms);
  }

  @Override
  public void visit(ast.Ast.Stm.If s)
  {
    s.condition.accept(this);
    Exp.T condition = this.exp;
    s.thenn.accept(this);
    Stm.T thenn = this.stm;
    s.elsee.accept(this);
    Stm.T elsee = this.stm;
    this.stm = new If(condition, thenn, elsee);
    return;
  }

  @Override
  public void visit(ast.Ast.Stm.Print s)
  {
    s.exp.accept(this);
    this.stm = new Print(this.exp);
    return;
  }

  @Override
  public void visit(ast.Ast.Stm.While s)
  {
	  s.condition.accept(this);
	  Exp.T condition = this.exp;
	  s.body.accept(this);
	  Stm.T body = this.stm;
	  this.stm=new codegen.C.Ast.Stm.While(condition,body);
	  return;
  }

  // ///////////////////////////////////////////
  // type
  @Override
  public void visit(ast.Ast.Type.Boolean t)
  {
	  this.type=new Type.Int();
  }

  @Override
  public void visit(ast.Ast.Type.ClassType t)
  {
	  this.type=new Type.ClassType(t.id);
  }

  @Override
  public void visit(ast.Ast.Type.Int t)
  {
    this.type = new Type.Int();
  }

  @Override
  public void visit(ast.Ast.Type.IntArray t)
  {
	  this.type=new Type.IntArray();
  }

  // ////////////////////////////////////////////////
  // dec
  @Override
  public void visit(ast.Ast.Dec.DecSingle d)
  {
    d.type.accept(this);
    this.dec = new codegen.C.Ast.Dec.DecSingle(this.type, d.id);
    return;
  }

  // method
  @Override
  public void visit(ast.Ast.Method.MethodSingle m)
  {
    this.tmpVars = new LinkedList<Dec.T>();
    m.retType.accept(this);
    Type.T newRetType = this.type;//构造新的返回值对象
    LinkedList<Dec.T> newFormals = new LinkedList<Dec.T>();//构造新的参数列表声明
    
    newFormals.add(new Dec.DecSingle(
        new ClassType(this.classId), "this")); //先在参数列表加入一个指向自己类的指针
    
    for (ast.Ast.Dec.T d : m.formals) {//遍历java的ast的这个方法的参数列表，
    									//将翻译过后的对象添加到新的参数列表对象里
      d.accept(this);
      newFormals.add(this.dec);
    }
    LinkedList<Dec.T> locals = new LinkedList<Dec.T>();//构造新的局部变量声明列表
    for (ast.Ast.Dec.T d : m.locals) {
      d.accept(this);
      locals.add(this.dec);
    }
    LinkedList<Stm.T> newStm = new LinkedList<Stm.T>();
    
    for (ast.Ast.Stm.T s : m.stms) {
      s.accept(this);				//重点！
      newStm.add(this.stm);
    }
    m.retExp.accept(this);
    Exp.T retExp = this.exp;
    for (Dec.T dec : this.tmpVars) {//在声明的最后，补上需要的声明，比如call调用产生的类名。    								
      locals.add(dec);
    }
    this.method = new MethodSingle(newRetType, this.classId, m.id,
        newFormals, locals, newStm, retExp);
    return;
  }

  // class
  @Override
  public void visit(ast.Ast.Class.ClassSingle c)
  {
    ClassBinding cb = this.table.get(c.id);//根据class表查询classbinding对象
    //向class表中加入类的信息
    this.classes.add(new ClassSingle(c.id, cb.fields));
    //向虚方法表中加入类名及对应的方法信息
    this.vtables.add(new VtableSingle(c.id, cb.methods));
    this.classId = c.id;
    for (ast.Ast.Method.T m : c.methods) {
      m.accept(this);
      //将类的方法放入方法表
      this.methods.add(this.method);
    }
    return;
  }

  // main class
  @Override
  public void visit(ast.Ast.MainClass.MainClassSingle c)
  {
    ClassBinding cb = this.table.get(c.id);
    Class.T newc = new ClassSingle(c.id, cb.fields);
    this.classes.add(newc);
    this.vtables.add(new VtableSingle(c.id, cb.methods));

    this.tmpVars = new LinkedList<Dec.T>();
   

    c.stm.accept(this);//在这里面可能会往tmpVars里面加Dec对象
    MainMethod.T mthd = new MainMethodSingle(this.tmpVars, this.stm);
    this.mainMethod = mthd;
    return;
  }

  // /////////////////////////////////////////////////////
  // the first pass
  public void scanMain(ast.Ast.MainClass.T m)
  {
    this.table.init(((ast.Ast.MainClass.MainClassSingle) m).id, null);
    // this is a special hacking in that we don't want to
    // enter "main" into the table.
    return;
  }

  public void scanClasses(java.util.LinkedList<ast.Ast.Class.T> cs)
  {
    // put empty chuncks into the table-----
	  //现初始化classTable，只填入extends信息。
    for (ast.Ast.Class.T c : cs) {
      ast.Ast.Class.ClassSingle cc = (ast.Ast.Class.ClassSingle) c;
      this.table.init(cc.id, cc.extendss);
    }

    // put class fields and methods into the table-----
    //再次遍历java的class表
    for (ast.Ast.Class.T c : cs) {
      ast.Ast.Class.ClassSingle cc = (ast.Ast.Class.ClassSingle) c;
      LinkedList<Dec.T> newDecs = new LinkedList<Dec.T>();//声明一个新的声明链表
      
      for (ast.Ast.Dec.T dec : cc.decs) {
        dec.accept(this);
        newDecs.add(this.dec);//在dec.accept(this)执行后，this.dec变为了一个C类型的Dec
      }
      this.table.initDecs(cc.id, newDecs);//将新的声明放入ClassBinding对象中

      // all methods
      java.util.LinkedList<ast.Ast.Method.T> methods = cc.methods;
      for (ast.Ast.Method.T mthd : methods) {
        ast.Ast.Method.MethodSingle m = (ast.Ast.Method.MethodSingle) mthd;
        LinkedList<Dec.T> newArgs = new LinkedList<Dec.T>();//声明一个新的参数列表
        
        newArgs.add(new Dec.DecSingle(//第一个参数是this
                new ClassType(cc.id), "this"));
        /*
         * 重点！！也是放了一个自己所在类的类型,这样classTable中对应ClassBinding对象的
         * Ftuple的参数列表也会多一项。且在第一个位置
         */
        //后面的参数
        for (ast.Ast.Dec.T arg : m.formals) {
          arg.accept(this);
          newArgs.add(this.dec);//同上
        }
        m.retType.accept(this);
        Type.T newRet = this.type;
        this.table.initMethod(cc.id, newRet, newArgs, m.id);//将新的方法放入
      }														//ClassBinding对象中
      //通过上面的代码，整个的C的Class已经构造完成。ClassBinding对象里面的信息都已经填充。
    }

    // calculate all inheritance information
    for (ast.Ast.Class.T c : cs) {
      ast.Ast.Class.ClassSingle cc = (ast.Ast.Class.ClassSingle) c;
      this.table.inherit(cc.id);
    }
  }

  public void scanProgram(ast.Ast.Program.T p)
  {
    ast.Ast.Program.ProgramSingle pp = (ast.Ast.Program.ProgramSingle) p;
    scanMain(pp.mainClass);
    scanClasses(pp.classes);
    return;
  }

  // end of the first pass
  // ////////////////////////////////////////////////////

  // program
  @Override
  public void visit(ast.Ast.Program.ProgramSingle p)
  {
    // The first pass is to scan the whole program "p", and
    // to collect all information of inheritance.
    scanProgram(p);//在scan的过程中，C的classTable已经建好了
    System.out.println("scan finished...");

    // do translations
    p.mainClass.accept(this);
    System.out.println("new mainClass...");
    for (ast.Ast.Class.T classs : p.classes) {
      classs.accept(this);
    }
    System.out.println("new Classes");
    this.program = new ProgramSingle(this.classes, this.vtables,
        this.methods, this.mainMethod);
    System.out.println("new program");
    return ;
  }
}
