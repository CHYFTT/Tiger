/*------------------------------------------------------------------*/
/* Copyright (C) SSE-USTC, 2014-2015                                */
/*                                                                  */
/*  FILE NAME             :  MethodTable.java                       */
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

import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;
import util.Todo;

public class MethodTable
{
  private java.util.Hashtable<String, Type.T> table;

  public MethodTable()
  {
    this.table = new java.util.Hashtable<String, Type.T>();
  }

  // Duplication is not allowed
  public void put(LinkedList<Dec.T> formals,
      LinkedList<Dec.T> locals)
  {
    for (Dec.T dec : formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("duplicated parameter: " + decc.id+"at line:"+"");
        System.exit(1);
      }
      this.table.put(decc.id, decc.type);
    }

    for (Dec.T dec : locals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("duplicated variable: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, decc.type);
    }

  }

  // return null for non-existing keys
  public Type.T get(String id)
  {
    return this.table.get(id);
  }

  public void dump()
  {
	  System.out.println("<id----Type>");
	  System.out.println(this.table);
  }

  @Override
  public String toString()
  {
    return this.table.toString();
  }
}
