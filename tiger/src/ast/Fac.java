package ast;

import ast.Ast.Dec;
import ast.Ast.Exp;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.MainClass;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.If;
import ast.Ast.Stm.While;
import ast.Ast.Stm.Print;
import ast.Ast.Type;

public class Fac
{
  // Lab2, exercise 2: read the following code and make
  // sure you understand how the sample program "test/Fac.java" is represented.

  // /////////////////////////////////////////////////////
  // To represent the "Fac.java" program in memory manually
  // this is for demonstration purpose only, and
  // no one would want to do this in reality (boring and error-prone).
  /*
   * class Factorial { public static void main(String[] a) {
   * System.out.println(new Fac().ComputeFac(10)); } } class Fac { public int
   * ComputeFac(int num) { int num_aux; if (num < 1) num_aux = 1; else num_aux =
   * num * (this.ComputeFac(num-1)); return num_aux; } }
   */

  // // main class: "Factorial"
  static MainClass.T factorial = new MainClassSingle(
      "Factorial", "a", new Print(new Call(
          new NewObject("Fac",1), "ComputeFac",
          new util.Flist<Exp.T>().list(new Num(10,1)),1),1));

  // // class "Fac"
  static ast.Ast.Class.T fac = new ast.Ast.Class.ClassSingle("Fac", null,
      new util.Flist<Dec.T>().list(),
      new util.Flist<Method.T>().list(new Method.MethodSingle(
          new Type.Int(), "ComputeFac", new util.Flist<Dec.T>()
              .list(new Dec.DecSingle(new Type.Int(), "num",false)),
          new util.Flist<Dec.T>().list(new Dec.DecSingle(
              new Type.Int(), "num_aux",false)), new util.Flist<Stm.T>()
              .list(new If(new Lt(new Id("num"),
                  new Num(1,1)), new Assign("num_aux",
                  new Num(1,1)), new Assign("num_aux",
                  new Times(new Id("num",1), new Call(
                      new This(), "ComputeFac",
                      new util.Flist<Exp.T>().list(new Sub(
                          new Id("num"), new Num(1)))))))),
          new Id("num_aux",1))));

  // program
  public static Program.T prog = new ProgramSingle(factorial,
      new util.Flist<ast.Ast.Class.T>().list(fac));

  // Lab2, exercise 2: you should write some code to
  // represent the program "test/Sum.java".
  // Your code here:
  
  
 
  static MainClass.T Sum = new MainClassSingle(
	      "Sum", "a",  new Print(new Call(
	          new NewObject("Doit",1), "doit",
	          new util.Flist<Exp.T>().list(new Num(101,1)),1),1));
  
  
  static ast.Ast.Class.T summ = new ast.Ast.Class.ClassSingle("Sum", null,
	      new util.Flist<Dec.T>().list(),
	      new util.Flist<Method.T>().list(new Method.MethodSingle(
	          new Type.Int(), "doit", new util.Flist<Dec.T>()
	              .list(new Dec.DecSingle(new Type.Int(), "n",false)),
	          new util.Flist<Dec.T>().list(new Dec.DecSingle(
	              new Type.Int(), "sum",false),new Dec.DecSingle(
	    	              new Type.Int(), "i",false)), 
	    	              new util.Flist<Stm.T>().list(new Assign("i",new Num(0,1),1), 
	    	            		  new Assign("sum",new Exp.False(1),1),
	    	            		  new Assign("sum",new Num(0,1),1),
	    	            		  new While(new Lt(new Id("i",1),new Id("n",1),1),
	    	            				  new Stm.Block(new util.Flist<Stm.T>().list( 
	    	            						  new Assign("sum",new Add(new Id("sum",1),new Id("i",1),1),1),
	    	            	            		  new Assign("i",new Add(new Id("i",1),new Num(1,1),1),1)),1),1)),
	          new Id("sum",1))));
  
  public static Program.T prog2 =new ProgramSingle(Sum,
		  new util.Flist<ast.Ast.Class.T>().list(summ));
  
  
}
