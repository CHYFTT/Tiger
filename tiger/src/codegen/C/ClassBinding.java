package codegen.C;

import java.util.ArrayList;
import java.util.LinkedList;

import codegen.C.Ast.Dec;
import codegen.C.Ast.Type;

public class ClassBinding
{
  public String extendss; // null for non-existing extends
  public boolean visited; // whether or not this class has been visited
  public LinkedList<Tuple> fields; // all fields
  public ArrayList<Ftuple> methods; // all methods

  public ClassBinding(String extendss)
  {
    this.extendss = extendss;
    this.visited = false;
    this.fields = new LinkedList<Tuple>();//参数
    this.methods = new ArrayList<Ftuple>();//方法
  }

  // put a single field
  public void put(String c, Type.T type, String var)
  {
    this.fields.add(new Tuple(c, type, var));//
    /*
     * Tuple tu=new Tuple(c,type,car);
     * this.fields.put(tu);
     */
  }

  public void put(Tuple t)
  {
    this.fields.add(t);//上面不改这里就有点多余
  }

  public void update(java.util.LinkedList<Tuple> fs)
  {
    this.fields = fs;
  }

  public void update(java.util.ArrayList<Ftuple> ms)
  {
    this.methods = ms;
  }

  public void putm(String c, Type.T ret,
      java.util.LinkedList<Dec.T> args, String mthd)
  {
    Ftuple t = new Ftuple(c, ret, args, mthd);
    this.methods.add(t);
    return;
  }

  @Override
  public String toString()
  {
    System.out.print("extends: ");
    if (this.extendss != null)
      System.out.println(this.extendss);
    else
      System.out.println("<>");
    System.out.println("\nfields:\n  ");
    System.out.println(fields.toString());
    System.out.println("\nmethods:\n  ");
    System.out.println(methods.toString());

    return "";
  }

}
