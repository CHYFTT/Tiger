package codegen.C;

import java.util.HashSet;

import codegen.C.Ast.Class.ClassSingle;
import codegen.C.Ast.Dec;
import codegen.C.Ast.Dec.DecSingle;
import codegen.C.Ast.Exp;
import codegen.C.Ast.Exp.Add;
import codegen.C.Ast.Exp.And;
import codegen.C.Ast.Exp.ArraySelect;
import codegen.C.Ast.Exp.Call;
import codegen.C.Ast.Exp.Id;
import codegen.C.Ast.Exp.Length;
import codegen.C.Ast.Exp.Lt;
import codegen.C.Ast.Exp.NewIntArray;
import codegen.C.Ast.Exp.NewObject;
import codegen.C.Ast.Exp.Not;
import codegen.C.Ast.Exp.Num;
import codegen.C.Ast.Exp.Sub;
import codegen.C.Ast.Exp.This;
import codegen.C.Ast.Exp.Times;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.MainMethod.T;
import codegen.C.Ast.Method;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm;
import codegen.C.Ast.Stm.Assign;
import codegen.C.Ast.Stm.AssignArray;
import codegen.C.Ast.Stm.Block;
import codegen.C.Ast.Stm.If;
import codegen.C.Ast.Stm.Print;
import codegen.C.Ast.Stm.While;
import codegen.C.Ast.Type;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Type.Int;
import codegen.C.Ast.Type.IntArray;
import codegen.C.Ast.Vtable;
import codegen.C.Ast.Vtable.VtableSingle;
import control.Control;

public class PrettyPrintVisitor implements Visitor
{
  private int indentLevel;
  private java.io.BufferedWriter writer;
  private HashSet<String> redec=new HashSet<String>();
  

  public PrettyPrintVisitor()
  {
    this.indentLevel = 2;
  }

  private void indent()
  {
    this.indentLevel += 2;
  }

  private void unIndent()
  {
    this.indentLevel -= 2;
  }

  private void printSpaces()
  {
    int i = this.indentLevel;
    while (i-- != 0)
      this.say(" ");
  }

  private void sayln(String s)
  {
    say(s);
    try {
      this.writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void say(String s)
  {
    try {
      this.writer.write(s);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
	  e.left.accept(this);
	  this.say(" + ");
	  e.right.accept(this);
  }

  @Override
  public void visit(And e)
  {
	  e.left.accept(this);
	  this.say("&&");
	  e.right.accept(this);
  }

  @Override
  public void visit(ArraySelect e)
  {
	  e.array.accept(this);
	  this.say("[");
	  e.index.accept(this);
	  this.say("]");
  }

  @Override
  public void visit(Call e)
  {
	  /*
	   * 在这里面与jasmin不同的是，C语言的调用不需要输出调用函数的参数
	   */
	  if(!this.redec.contains(e.assign))
		  this.say("(" + e.assign + "=");
	  else
		  this.say("(frame." + e.assign + "=");
    e.exp.accept(this);//n=this->r
    this.say(", ");
    if(!this.redec.contains(e.assign))
    	this.say(e.assign + "->vptr->" + e.id + "(" + e.assign);
    else
    	this.say("frame."+e.assign + "->vptr->" + e.id + "(frame." + e.assign);
    int size = e.args.size();
    if (size == 0) {
      this.say("))");
      return;
    }
    for (Exp.T x : e.args) {
      this.say(", ");
      x.accept(this);
    }
    this.say("))");
    return;
  }

  @Override
  public void visit(Id e)
  {
	  if(e.isField==false)
	  {
		  if(this.redec.contains(e.id))
			  this.say("frame."+e.id);
		  else
			  this.say(e.id);
	  }
	  else
	  {
		  this.say("this->"+e.id);
	  }
	 
  }

  @Override
  public void visit(Length e)
  {
	  e.array.accept(this);
	  this.say("[-1]");
  }

  @Override
  public void visit(Lt e)
  {
    e.left.accept(this);
    this.say(" < ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(NewIntArray e)
  {
	  //重点
	  //new int[exp]------>(int)malloc((exp)*sizeof(int))
//	  this.say("(int)malloc((");
//	  e.exp.accept(this);
//	  this.say(")*sizeof(int))");
	  this.say("(int*)Tiger_new_array(");
	  e.exp.accept(this);
	  this.say(")");
  }

  @Override

  public void visit(NewObject e)
  {
	  //重点！！
	  //new Object()----->(struct e.id *)malloc(sizeof(struct e.id))
    this.say("((struct " + e.id + "*)(Tiger_new (&" + e.id
        + "_vtable_, sizeof(struct " + e.id + "))))");
    return;
  }

  @Override
  public void visit(Not e)
  {
	  this.say("!(");
	  e.exp.accept(this);
	  this.say(")");
  }

  @Override
  public void visit(Num e)
  {
    this.say(Integer.toString(e.num));
    return;
  }

  @Override
  public void visit(Sub e)
  {
    e.left.accept(this);
    this.say(" - ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(This e)
  {
    this.say("this");
  }

  @Override
  public void visit(Times e)
  {
    e.left.accept(this);
    this.say(" * ");
    e.right.accept(this);
    return;
  }

  // statements
  @Override
  public void visit(Assign s)
  {

	   this.printSpaces();
		if(s.isField==false)
		{
			if(this.redec.contains(s.id))
				this.say("frame."+s.id+" = ");
			else
			this.say(s.id + " = ");
		}
		else 
		{
			this.say("this->"+s.id + " = ");
		}
		s.exp.accept(this);
		this.sayln(";");
		return;
  }

  @Override
  public void visit(AssignArray s)
  {
	  this.printSpaces();
	  if(s.isField==false)
	  {
		  if(this.redec.contains(s.id))
			  this.say("frame."+s.id+"[");
		  else
		  this.say(s.id+"[");
	  }
	  else
	  {
		  this.say("this->"+s.id+"[");
	  }
	  s.index.accept(this);
	  this.say("]");
	  this.say(" = ");
	  s.exp.accept(this);
	  this.sayln(";");
	  
  }

  @Override
  public void visit(Block s)
  {
	  this.sayln("");
	  this.printSpaces();
	  this.sayln("{");
	  this.indent();
	  for(Stm.T b:s.stms)
		  b.accept(this);
	  this.unIndent();
	  this.printSpaces();
	  this.sayln("}");
  }

  @Override
  public void visit(If s)
  {
    this.printSpaces();
    this.say("if (");
    s.condition.accept(this);
    this.sayln(")");
    this.indent();
    s.thenn.accept(this);
    this.unIndent();
    this.sayln("");
    this.printSpaces();
    this.sayln("else");
    this.indent();
    s.elsee.accept(this);
    this.sayln("");
    this.unIndent();
    return;
  }

  @Override
  public void visit(Print s)   
  {
    this.printSpaces();
    this.say("System_out_println (");
    s.exp.accept(this);
    this.sayln(");");
    return;
  }

  @Override
  public void visit(While s)
  {
	  this.printSpaces();
	  this.say("while (");
	  s.condition.accept(this);
	  this.say(")");
	  this.indent();
	  s.body.accept(this);
	  this.unIndent();
	  this.printSpaces();
  }

  // type
  @Override
  public void visit(ClassType t)
  {
    this.say("struct " + t.id + " *");
  }

  @Override
  public void visit(Int t)
  {
    this.say("int");
  }

  @Override
  public void visit(IntArray t)
  {
	  this.say("int* ");//貌似没用
  }

  // dec
  @Override
  public void visit(DecSingle d)
  {
	  d.type.accept(this);
	  this.say("");
  }

  // method
  @Override
  public void visit(MethodSingle m)
  {
	 this.redec.clear();
    m.retType.accept(this);//处理返回值
    this.say(" " + m.classId + "_" + m.id + "(");//Fac_ComputeFac
    int size = m.formals.size();
    for (Dec.T d : m.formals) {//参数列表
      DecSingle dec = (DecSingle) d;
      size--;
      dec.type.accept(this);//声明的类型， int num_aux;
      this.say(" " + dec.id);//声明的ID
      if (size > 0)
        this.say(", ");
    }
    this.sayln(")");
    
    
    //TODO 并不是所有的局部变量声明都需要打印
    this.sayln("{");//局部变量声明
    
  //Lab4
		this.printSpaces();
		this.sayln("struct " + m.classId + "_" + m.id + "_gc_frame frame;");
    
    for (Dec.T d : m.locals)
    {
		DecSingle dec = (DecSingle) d;
		if(!(dec.type instanceof Type.ClassType || 
				dec.type instanceof Type.IntArray))
		{
		this.say("  ");
		dec.type.accept(this);// 类型
		this.say(" " + dec.id + ";\n");//id
		}
		else
			this.redec.add(dec.id);
    }
    this.sayln("");
    for (Stm.T s : m.stms)
      s.accept(this);
    this.say("  return ");
    m.retExp.accept(this);
    this.sayln(";");
    this.sayln("}");
    return;
  }

  @Override
  public void visit(MainMethodSingle m)
  {
	  this.redec.clear();
    this.sayln("int Tiger_main ()");
    this.sayln("{");
    
    //Lab4:

    
    this.indent();
    this.printSpaces();
    this.sayln("struct Tiger_main_gc_frame frame;");
    this.unIndent();
    
    for (Dec.T dec : m.locals) 
    {
			this.say("  ");
			DecSingle d = (DecSingle) dec;
			if (!(d.type instanceof Type.ClassType || 
					d.type instanceof Type.IntArray)) 
			{//当不是Class也不是IntArray时才打印
				d.type.accept(this);
				this.say(" ");
				this.sayln(d.id + ";");
			}
			else
				this.redec.add(d.id);
      
    }
    
    m.stm.accept(this);
    
    this.sayln("}\n");
    return;
  }

  // vtables
  @Override
  public void visit(VtableSingle v)
  {
    this.sayln("struct " + v.id + "_vtable");
    this.sayln("{");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      t.ret.accept(this);//方法的返回值
      this.say(" (*" + t.id + ")(");//方法名+参数
      int size=t.args.size();
      
      for(Dec.T d:t.args)
      {
    	  DecSingle dd=(DecSingle)d;
    	  dd.type.accept(this);
    	  this.say(" " + dd.id);
    	  size--;
    	  if(size>0)
    		  this.say(",");
      }
      
      this.sayln(");");
    }
    
    this.sayln("};\n");
    return;
  }

  private void outputVtable(VtableSingle v)
  {
    this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
    this.sayln("{");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      this.sayln(t.classs + "_" + t.id + ",");
    }
    this.sayln("};\n");
    return;
  }
  //TODO outputGCstack
  private void outputGCstack(codegen.C.Ast.MainMethod.MainMethodSingle mainMethod)
  {
	  	this.sayln("struct Tiger_main_gc_frame");
		this.sayln("{");
		this.indent();
		this.printSpaces();
		this.sayln("void *prev_;");
		this.printSpaces();
		this.sayln("char *arguments_gc_map;");
		this.printSpaces();
		this.sayln("int *arguments_base_address;");
		this.printSpaces();
		this.sayln("int locals_gc_map;");
		
		for(codegen.C.Ast.Dec.T d:mainMethod.locals)
		{
			DecSingle dd=(DecSingle)d;
			if(dd.type instanceof ClassType||dd.type instanceof IntArray)
			{
				this.printSpaces();
				dd.accept(this);
				//在set中加入这个声明
				
				this.say(" ");
			    this.sayln(dd.id + ";");
			}
		}
		
		this.unIndent();
		this.sayln("};\n");
	  
  }
  
  private void outputGcstack(codegen.C.Ast.Method.MethodSingle m)
  {
	  this.sayln("struct "+m.classId+"_"+m.id+"_gc_frame");
		this.sayln("{");
		this.indent();
		this.printSpaces();
		this.sayln("void *prev_;");
		this.printSpaces();
		this.sayln("char *arguments_gc_map;");
		this.printSpaces();
		this.sayln("int *arguments_base_address;");
		this.printSpaces();
		this.sayln("int locals_gc_map;");
		
		for(codegen.C.Ast.Dec.T d:m.locals)
		{
			DecSingle dd=(DecSingle)d;
			if(dd.type instanceof ClassType||dd.type instanceof IntArray)
			{
				this.printSpaces();
				dd.accept(this);
				this.say(" ");
			    this.sayln(dd.id + ";");
			}
		}
		
		this.unIndent();
		this.sayln("};\n");
  }

  // class
  @Override
  public void visit(ClassSingle c)
  {
    this.sayln("struct " + c.id);
    this.sayln("{");
    this.sayln("  struct " + c.id + "_vtable *vptr;");
    for (codegen.C.Tuple t : c.decs) {//处理类里面的声明
      this.say("  ");
      t.type.accept(this);
      this.say(" ");
      this.sayln(t.id + ";");
    }
    this.sayln("};");
    return;
  }

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    // we'd like to output to a file, rather than the "stdout".
    try {
      String outputName = null;
      if (Control.ConCodeGen.outputName != null)
        outputName = Control.ConCodeGen.outputName;
      else if (Control.ConCodeGen.fileName != null)
        outputName = Control.ConCodeGen.fileName + ".c";
      else
        outputName = "a.c";

      this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(outputName)));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    this.sayln("// This is automatically generated by the Tiger compiler.");
    this.sayln("// Do NOT modify!\n");
    
    this.sayln("extern void *previous=0;");
  

    //TODO
    this.sayln("// structures");
    for (codegen.C.Ast.Class.T c : p.classes) {//处理类的声明
      c.accept(this);
    }

    //TODO
    this.sayln("// vtables structures");
    for (Vtable.T v : p.vtables) {//虚函数表，里面放有函数指针.注意！！！函数指针需要带参数。
      v.accept(this);				//这里为了可以打印函数指针的参数，对classTable的初始化
    }								//进行了修改，在Ftuple
    this.sayln("");
    
    
    this.sayln("//methods decl");//方法声明
    for(Method.T mm:p.methods)
    {
    	MethodSingle m=(MethodSingle)mm;
    	m.retType.accept(this);//处理返回值
        this.say(" " + m.classId + "_" + m.id + "(");//Fac_ComputeFac
        int size = m.formals.size();
        for (Dec.T d : m.formals) {//参数列表
          DecSingle dec = (DecSingle) d;
          size--;
          dec.type.accept(this);//声明的类型， int num_aux;
          this.say(" " + dec.id);//声明的ID
          if (size > 0)
            this.say(", ");
        }
        this.sayln(");");
    }
    
    //TODO
    this.sayln("// vtables");//虛函数表初始化-----在初始化之前必须先声明方法
    for (Vtable.T v : p.vtables) {
      outputVtable((VtableSingle) v);
    }
    this.sayln("");
    
    //TODO GC stack frames
    this.sayln("//GC stack frames");
    //先打印main
    outputGCstack((codegen.C.Ast.MainMethod.MainMethodSingle)p.mainMethod);
    for(codegen.C.Ast.Method.T m:p.methods)
    {
    	outputGcstack((MethodSingle)m);
    }
    
    
    //TODO // memory GC maps
    this.sayln("// memory GC maps");

    
    //TODO
    this.sayln("// methods");
    for (Method.T m : p.methods) {//方法的定义------在方法定义以前，就应该初始化虚函数表
    							//但是，虚函数表的初始化又需要方法名，所以在方法定义之前，
    							//应该先声明方法
      m.accept(this);
    }
    this.sayln("");
    

   
    
    

    this.sayln("// main method");//处理main函数
    p.mainMethod.accept(this);
    this.sayln("");

    this.say("\n\n");

    try {
      this.writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

}
