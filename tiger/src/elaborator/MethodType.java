/*------------------------------------------------------------------*/
/* Copyright (C) SSE-USTC, 2014-2015                                */
/*                                                                  */
/*  FILE NAME             :  MethodType.java                        */
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

public class MethodType
{
  public Type.T retType;
  public LinkedList<Dec.T> argsType;

  public MethodType(Type.T retType, LinkedList<Dec.T> decs)
  {
    this.retType = retType;
    this.argsType = decs;
  }

  @Override
  public String toString()
  {
    String s = "";
    for (Dec.T dec : this.argsType) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      s = decc.type.toString() + "*" + s;
    }
    s = s + " -> " + this.retType.toString();
    return s;
  }
}
