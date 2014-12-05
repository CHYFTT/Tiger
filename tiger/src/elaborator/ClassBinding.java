/*------------------------------------------------------------------*/
/* Copyright (C) SSE-USTC, 2014-2015                                */
/*                                                                  */
/*  FILE NAME             :  ClassBinding.java                      */
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
package elaborator;

import java.util.Hashtable;

import ast.Ast.Type;

public class ClassBinding
{
  public String extendss; // null for non-existing extends
  public java.util.Hashtable<String, Type.T> fields;//存放类里面声明的变量
  public java.util.Hashtable<String, MethodType> methods;//存放类里面定义的方法

  public ClassBinding(String extendss)
  {
    this.extendss = extendss;
    this.fields = new Hashtable<String, Type.T>();
    this.methods = new Hashtable<String, MethodType>();
  }

  public ClassBinding(String extendss,
      java.util.Hashtable<String, Type.T> fields,
      java.util.Hashtable<String, MethodType> methods)
  {
    this.extendss = extendss;
    this.fields = fields;
    this.methods = methods;
  }

  public void put(String xid, Type.T type)
  {
    if (this.fields.get(xid) != null) {
      System.err.println("duplicated class field: " + xid+" at line:");
      System.exit(1);
    }
    this.fields.put(xid, type);
  }

  public void put(String mid, MethodType mt)
  {
    if (this.methods.get(mid) != null) {//当没有重名方法时，将对应的方法放入
										//ClassBindling的methods表
      System.err.println("duplicated class method: " + mid);
      System.exit(1);
    }
    this.methods.put(mid, mt);
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
