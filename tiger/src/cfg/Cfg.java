package cfg;

import java.util.LinkedList;

public class Cfg
{
  // //////////////////////////////////////////////////
  // type
  public static class Type
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

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
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    public static class IntType extends T
    {
      public IntType()
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
    }

    public static class IntArrayType extends T
    {
      public IntArrayType()
      {
      }

      @Override
      public String toString()
      {
        return "@int[]";
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }// end of type

  // //////////////////////////////////////////////////
  // dec
  public static class Dec
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class DecSingle extends T
    {
      public Type.T type;
      public String id;

      public DecSingle(Type.T type, String id)
      {
        this.type = type;
        this.id = id;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }// end of dec

  // //////////////////////////////////////////////////
  // Operand
  public static class Operand
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class Int extends T
    {
      public int i;

      public Int(int i)
      {
        this.i = i;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    public static class Var extends T
    {
      public String id;
      public boolean isField;

      
      public Var(String id)
      {
    	  this.id=id;
    	  this.isField=false;
      }
      
      public Var(String id,boolean isField)
      {
    	  this.id=id;
    	  this.isField=isField;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }// end of operand

  // //////////////////////////////////////////////////
  // statement
  public static class Stm
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class Add extends T
    {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Add(String dst, Type.T ty, Operand.T left, Operand.T right)
      {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
    
    public static class And extends T
    {
    	public String dst;
    	public Operand.T left;
    	public Operand.T right;
    	
		public And(String dst, Operand.T left, Operand.T right) {
			this.dst = dst;
			this.left = left;
			this.right = right;
		}
		@Override
		public void accept(Visitor v) {
			v.visit(this);
			
		}
    }
    
    public static class ArraySelect extends T
    {
    	public String id;
    	public Operand.T array;
    	public Operand.T index;
    	
		public ArraySelect(String id, cfg.Cfg.Operand.T array,
				cfg.Cfg.Operand.T index) {
			this.id = id;
			this.array = array;
			this.index = index;
		}

		@Override
		public void accept(Visitor v) {
			v.visit(this);
			
		}
    	
    }
    
    public static class AssignArray extends T
    {
    	public String dst;
    	public Operand.T index;
    	public Operand.T exp;
    	public boolean isField;
    	/*
    	 * 这个isField字段需要自己添加。也是为了输出类似this->number[0]的形式
    	 */
    	
		public AssignArray(String dst, cfg.Cfg.Operand.T index,
				cfg.Cfg.Operand.T exp,boolean isField) {
			this.dst = dst;
			this.index = index;
			this.exp = exp;
			this.isField=isField;
		}

		@Override
		public void accept(Visitor v) {
			v.visit(this);
		}
		
		
    	
    	
    }

    public static class InvokeVirtual extends T
    {
      public String dst;
      public String obj;
      public String f;
      // type of the destination variable
      public java.util.LinkedList<Operand.T> args;

      public InvokeVirtual(String dst, String obj, String f,
          LinkedList<Operand.T> args)
      {
        this.dst = dst;
        this.obj = obj;
        this.f = f;
        this.args = args;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
    
    public static class Length extends T
    {
    	public String dst;
    	public Operand.T array;
    	
    	
		public Length(String dst, cfg.Cfg.Operand.T array) {
			super();
			this.dst = dst;
			this.array = array;
		}


		@Override
		public void accept(Visitor v) {
			v.visit(this);
		}
    }

    public static class Lt extends T
    {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Lt(String dst, Type.T ty, Operand.T left, Operand.T right)
      {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    public static class Move extends T
    {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T src;
      
      public boolean isField;

      public Move(String dst, Type.T ty, Operand.T src,boolean isField)
      {
        this.dst = dst;
        this.ty = ty;
        this.src = src;
        this.isField=isField;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
    
    public static class Not extends T
    {
    	public String dst;
    	public Operand.T exp;
    	
    	
		public Not(String dst, cfg.Cfg.Operand.T exp) {
			this.dst = dst;
			this.exp = exp;
		}


		@Override
		public void accept(Visitor v) {
			v.visit(this);
			
		}
    	
    	
    }
    
    public static class NewIntArray extends T
    {
    	public String dst;
    	public Operand.T exp;
    	
    	
		public NewIntArray(String dst, cfg.Cfg.Operand.T exp) {
			this.dst = dst;
			this.exp = exp;
		}


		@Override
		public void accept(Visitor v) {
			v.visit(this);
			
		}
    	
    	
    }

    public static class NewObject extends T
    {
      public String dst;
      // type of the destination variable
      public String c;

      public NewObject(String dst, String c)
      {
        this.dst = dst;
        this.c = c;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    public static class Print extends T
    {
      public Operand.T arg;

      public Print(Operand.T arg)
      {
        this.arg = arg;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    public static class Sub extends T
    {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Sub(String dst, Type.T ty, Operand.T left, Operand.T right)
      {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    public static class Times extends T
    {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Times(String dst, Type.T ty, Operand.T left, Operand.T right)
      {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }// end of statement

  // //////////////////////////////////////////////////
  // transfer
  public static class Transfer
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class Goto extends T
    {
      public util.Label label;

      public Goto(util.Label label)
      {
        this.label = label;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    public static class If extends T
    {
      public Operand.T operand;
      public util.Label truee;
      public util.Label falsee;

      public If(Operand.T operand, util.Label truee, util.Label falsee)
      {
        this.operand = operand;
        this.truee = truee;
        this.falsee = falsee;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    public static class Return extends T
    {
      public Operand.T operand;

      public Return(Operand.T operand)
      {
        this.operand = operand;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }// end of transfer

  // //////////////////////////////////////////////////
  // block
  public static class Block
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class BlockSingle extends T
    {
      public util.Label label;
      public LinkedList<Stm.T> stms;
      public Transfer.T transfer;

      public BlockSingle(util.Label label, LinkedList<Stm.T> stms,
          Transfer.T transfer)
      {
        this.label = label;
        this.stms = stms;
        this.transfer = transfer;
      }

      @Override
      public boolean equals(Object o)
      {
        if (o == null)
          return false;

        if (!(o instanceof BlockSingle))
          return false;

        BlockSingle ob = (BlockSingle) o;
        return this.label.equals(ob.label);
      }

      @Override
      public String toString()
      {
        StringBuffer strb = new StringBuffer();
        strb.append(this.label.toString() + ":\\n");
        // Lab5. Your code here:
        strb.append("Your code here:\\n");

        return strb.toString();
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
  }// end of block

  // //////////////////////////////////////////////////
  // method
  public static class Method
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class MethodSingle extends T
    {
      public Type.T retType;
      public String id;
      public String classId;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Block.T> blocks;
      public util.Label entry;//block的第一个label
      public util.Label exit;//?
      public Operand.T retValue;//?

      public MethodSingle(Type.T retType, String id, String classId,
          LinkedList<Dec.T> formals, LinkedList<Dec.T> locals,
          LinkedList<Block.T> blocks, util.Label entry, util.Label exit,
          Operand.T retValue)
      {
        this.retType = retType;
        this.id = id;
        this.classId = classId;
        this.formals = formals;
        this.locals = locals;
        this.blocks = blocks;
        this.entry = null;
        this.exit = null;
        this.retValue = null;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }// end of method

  // //////////////////////////////////////////////////
  // main method
  public static class MainMethod
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class MainMethodSingle extends T
    {
      public LinkedList<Dec.T> locals;
      public LinkedList<Block.T> blocks;

      public MainMethodSingle(LinkedList<Dec.T> locals,
          LinkedList<Block.T> blocks)
      {
        this.locals = locals;
        this.blocks = blocks;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
        return;
      }

    }

  }// end of main method

  // //////////////////////////////////////////////////
  // vtable
  public static class Vtable
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class VtableSingle extends T
    {
      public String id; // name of the class
      public LinkedList<cfg.Ftuple> ms; // all methods

      public VtableSingle(String id, LinkedList<cfg.Ftuple> ms)
      {
        this.id = id;
        this.ms = ms;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }

  // //////////////////////////////////////////////////
  // class
  public static class Class
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class ClassSingle extends T
    {
      public String id;
      public LinkedList<cfg.Tuple> decs;

      public ClassSingle(String id, LinkedList<cfg.Tuple> decs)
      {
        this.id = id;
        this.decs = decs;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }

    }

  }// enf of clazz

  // //////////////////////////////////////////////////
  // program
  public static class Program
  {
    public static abstract class T implements cfg.Acceptable
    {
    }

    public static class ProgramSingle extends T
    {
      public LinkedList<Class.T> classes;
      public LinkedList<Vtable.T> vtables;
      public LinkedList<Method.T> methods;
      public MainMethod.T mainMethod;

      public ProgramSingle(LinkedList<Class.T> classes,
          LinkedList<Vtable.T> vtables, LinkedList<Method.T> methods,
          MainMethod.T mainMethod)
      {
        this.classes = classes;
        this.vtables = vtables;
        this.methods = methods;
        this.mainMethod = mainMethod;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
        return;
      }
    }
  }// end of program
}
