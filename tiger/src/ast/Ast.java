package ast;

import java.util.LinkedList;

public class Ast
{

  // ///////////////////////////////////////////////////////////
  // type
  public static class Type
  {
    public static abstract class T implements ast.Acceptable
    {
      // boolean: -1
      // int: 0
      // int[]: 1
      // class: 2
      // Such that one can easily tell who is who
      public abstract int getNum();//抽象方法，只能定义在抽象类当中！！
    }

    // boolean
    public static class Boolean extends T
    {
      public Boolean()
      {
      }

      @Override
      public String toString()
      {
        return "@boolean";
      }

      @Override
      public int getNum()//子类必须实现父类的抽象方法
      {
        return -1;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    // class
    public static class ClassType extends T
    {
      public String id;

      public ClassType(String id)
      {
        this.id = id;
      }

      @Override
      public String toString()
      {
        return this.id;
      }

      @Override
      public int getNum()
      {
        return 2;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    // int
    public static class Int extends T
    {
      public Int()
      {
      }

      @Override
      public String toString()
      {
        return "@int";
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }

      @Override
      public int getNum()
      {
        return 0;
      }
    }

    // int[]
    public static class IntArray extends T
    {
    	//String id;
      public IntArray()
      {
    	  //this.id=id;
      }

      @Override
      public String toString()
      {
        return "@int[]";
      }

      @Override
      public int getNum()
      {
        return 1;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }

  // ///////////////////////////////////////////////////
  // dec
  public static class Dec
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    public static class DecSingle extends T
    {
      public Type.T type;
      public String id;
      public boolean isField;

      public DecSingle(Type.T type, String id,boolean isField)
      {
        this.type = type;
        this.id = id;
        this.isField=isField;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
  }

  // /////////////////////////////////////////////////////////
  // expression
  public static class Exp
  {
    public static abstract class T implements ast.Acceptable
    {
    	  public int linenum;

    }

    // +
    public static class Add extends T
    {
      public T left;
      public T right;

      public Add(T left, T right,int linenum)
      {
        this.left = left;
        this.right = right;
        this.linenum=linenum;
      }
      public Add(T left, T right)
      {
        this.left = left;
        this.right = right;
        this.linenum=0;
       
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // and
    public static class And extends T
    {
      public T left;
      public T right;

      public And(T left, T right,int linenum)
      {
        this.left = left;
        this.right = right;
        this.linenum=linenum;
      }
      public And(T left, T right)
      {
        this.left = left;
        this.right = right;
        this.linenum=0;
        
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // ArraySelect
    public static class ArraySelect extends T
    {
      public T array;
      public T index;

      public ArraySelect(T array, T index,int linenum)
      {
        this.array = array;
        this.index = index;
        this.linenum=linenum;
      }
      public ArraySelect(T array, T index)
      {
        this.array = array;
        this.index = index;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // Call
    public static class Call extends T
    {
      public T exp;
      public String id;
      public java.util.LinkedList<T> args;
      public String type; // type of first field "exp"
      public java.util.LinkedList<Type.T> at; // arg's type
      public Type.T rt;

      public Call(T exp, String id, java.util.LinkedList<T> args,int linenum)
      {
        this.exp = exp;
        this.id = id;
        this.args = args;
        this.type = null;
        this.linenum=linenum;
      }
      public Call(T exp, String id, java.util.LinkedList<T> args)
      {
        this.exp = exp;
        this.id = id;
        this.args = args;
        this.type = null;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // False
    public static class False extends T
    {
    	
      public False(int linenum)
      {
    	 this.linenum=linenum;
      }
      public False()
      {
    	  this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // Id
    public static class Id extends T
    {
      public String id; // name of the id
      public Type.T type; // type of the id,type与isField在elab时会给赋值！！
      public boolean isField; // whether or not this is a class field

      public Id(String id,int linenum)
      {
        this.id = id;
        this.type = null;
        this.isField = false;
        this.linenum=linenum;
      }
      public Id(String id)
      {
        this.id = id;
        this.type = null;
        this.isField = false;
        this.linenum=0;
      }

      public Id(String id, Type.T type, boolean isField,int linenum)
      {
        this.id = id;
        this.type = type;
        this.isField = isField;
        this.linenum=linenum;
      }
      public Id(String id, Type.T type, boolean isField)
      {
        this.id = id;
        this.type = type;
        this.isField = isField;
        this.linenum=0;
      }
//      public Id(String id,boolean isField)
//      {
//    	  this.id=id;
//    	  this.isField=isField;
//    	  this.linenum=0;
//    	  
//      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // length
    public static class Length extends T
    {
      public T array;

      public Length(T array,int linenum)
      {
        this.array = array;
        this.linenum=linenum;
      }
      public Length(T array)
      {
        this.array = array;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // <
    public static class Lt extends T
    {
      public T left;
      public T right;

      public Lt(T left, T right,int linenum)
      {
        this.left = left;
        this.right = right;
        this.linenum=linenum;
      }
      public Lt(T left, T right)
      {
        this.left = left;
        this.right = right;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // new int [e]
    public static class NewIntArray extends T
    {
      public T exp;

      public NewIntArray(T exp,int linenum)
      {
        this.exp = exp;
        this.linenum=linenum;
      }
      public NewIntArray(T exp)
      {
        this.exp = exp;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // new A();
    public static class NewObject extends T
    {
      public String id;

      public NewObject(String id,int linenum)
      {
        this.id = id;
        this.linenum=linenum;
      }
      public NewObject(String id)
      {
        this.id = id;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // !
    public static class Not extends T
    {
      public T exp;

      public Not(T exp,int linenum)
      {
        this.exp = exp;
        this.linenum=linenum;
      }
      public Not(T exp)
      {
        this.exp = exp;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // number
    public static class Num extends T
    {
      public int num;

      public Num(int num,int linenum)
      {
        this.num = num;
        this.linenum=linenum;
      }
      public Num(int num)
      {
        this.num = num;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // -
    public static class Sub extends T
    {
      public T left;
      public T right;

      public Sub(T left, T right,int linenum)
      {
        this.left = left;
        this.right = right;
        this.linenum=linenum;
      }
      public Sub(T left, T right)
      {
        this.left = left;
        this.right = right;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // this
    public static class This extends T
    {
      public This(int linenum)
      {
    	  this.linenum=linenum;
      }
      public This()
      {
    	  this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // *
    public static class Times extends T
    {
      public T left;
      public T right;

      public Times(T left, T right,int linenum)
      {
    	  this.left = left;
    	  this.right = right;
    	  this.linenum=linenum;
      }
      public Times(T left, T right)
      {
    	  this.left = left;
    	  this.right = right;
    	  this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // True
    public static class True extends T
    {
      public True(int linenum)
      {
    	  this.linenum=linenum;
      }
      public True()
      {
    	  this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

  }// end of expression

  // /////////////////////////////////////////////////////////
  // statement
  public static class Stm
  {
    public static abstract class T implements ast.Acceptable
    {

		public int linenum;
    }

    // assign
    public static class Assign extends T
    {
      public String id;
      public Exp.T exp;
      public Type.T type; // type of the id,在elab时会赋值
      public boolean isField;//在elab时会赋值
    
      public Assign(String id, Exp.T exp,int linenum)
      {
        this.id = id;
        this.exp = exp;
        this.type = null;
        isField=false;
        this.linenum=linenum;
      }
      public Assign(String id, Exp.T exp)
      {
        this.id = id;
        this.exp = exp;
        this.type = null;
        isField=false;
        this.linenum=0;
      }
//      public Assign(Type.T type)
//      {
//    	  this.type=type;
//      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // assign-array   number[10]=exp;
    public static class AssignArray extends T
    {
      public String id;
      public Exp.T index;
      public Exp.T exp;
      public Type.T tyep;//type of the id
      public boolean isField;
      

      public AssignArray(String id, Exp.T index, Exp.T exp,int linenum)
      {
        this.id = id;
        this.index = index;
        this.exp = exp;
        this.isField=false;
        this.linenum=linenum;
      }
      public AssignArray(String id, Exp.T index, Exp.T exp)
      {
        this.id = id;
        this.index = index;
        this.exp = exp;
        this.isField=false;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // block
    public static class Block extends T
    {
      public java.util.LinkedList<T> stms;
      

      public Block(java.util.LinkedList<T> stms,int linenum)
      {
        this.stms = stms;
        this.linenum=linenum;
      }
      public Block(java.util.LinkedList<T> stms)
      {
        this.stms = stms;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // if
    public static class If extends T
    {
      public Exp.T condition;
      public T thenn;
      public T elsee;
     

      public If(Exp.T condition, T thenn, T elsee,int linenum)
      {
        this.condition = condition;
        this.thenn = thenn;
        this.elsee = elsee;
        this.linenum=linenum;
      }
      public If(Exp.T condition, T thenn, T elsee)
      {
        this.condition = condition;
        this.thenn = thenn;
        this.elsee = elsee;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // Print
    public static class Print extends T
    {
      public Exp.T exp;
      

      public Print(Exp.T exp,int linenum)
      {
        this.exp = exp;
        this.linenum=linenum;
      }
      public Print(Exp.T exp)
      {
        this.exp = exp;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // while
    public static class While extends T
    {
      public Exp.T condition;
      public T body;
    
      public While(Exp.T condition, T body,int linenum)
      {
        this.condition = condition;
        this.body = body;
        this.linenum=linenum;
      }
      public While(Exp.T condition, T body)
      {
        this.condition = condition;
        this.body = body;
        this.linenum=0;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

  }// end of statement

  // /////////////////////////////////////////////////////////
  // method
  public static class Method
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    public static class MethodSingle extends T
    {
      public Type.T retType;
      public String id;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Stm.T> stms;
      public Exp.T retExp;

      public MethodSingle(Type.T retType, String id,
          LinkedList<Dec.T> formals, LinkedList<Dec.T> locals,
          LinkedList<Stm.T> stms, Exp.T retExp)
      {
        this.retType = retType;
        this.id = id;
        this.formals = formals;
        this.locals = locals;
        this.stms = stms;
        this.retExp = retExp;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
  }

  // class
  public static class Class
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    public static class ClassSingle extends T
    {
      public String id;
      public String extendss; // null for non-existing "extends"
      public java.util.LinkedList<Dec.T> decs;
      public java.util.LinkedList<ast.Ast.Method.T> methods;

      public ClassSingle(String id, String extendss,
          java.util.LinkedList<Dec.T> decs,
          java.util.LinkedList<ast.Ast.Method.T> methods)
      {
        this.id = id;
        this.extendss = extendss;//何时添加？？在Parse中已经添加了
        this.decs = decs;
        this.methods = methods;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
  }

  // main class
  public static class MainClass
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    public static class MainClassSingle extends T
    {
      public String id;//Sum
      public String arg;//a
      public  Stm.T  stm;//system.out.println

      public MainClassSingle(String id, String arg,  Stm.T  stm)
      {
        this.id = id;
        this.arg = arg;
        this.stm = stm;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
        return;
      }
    }

  }

  // whole program
  public static class Program
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    public static class ProgramSingle extends T
    {
      public MainClass.T mainClass;
      public LinkedList<Class.T> classes;

      public ProgramSingle(MainClass.T mainClass, LinkedList<Class.T> classes)
      {
        this.mainClass = mainClass;
        this.classes = classes;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
        return;
      }
    }

  }
}
