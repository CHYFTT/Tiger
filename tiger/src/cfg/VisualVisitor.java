/*------------------------------------------------------------------*/
/* Copyright (C) SSE-USTC, 2014-2015                                */
/*                                                                  */
/*  FILE NAME             :  VisualVisitor.java                     */
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
package cfg;

import java.util.HashMap;

import cfg.Cfg.Block;
import cfg.Cfg.Stm;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Var;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm.Add;
import cfg.Cfg.Stm.And;
import cfg.Cfg.Stm.ArraySelect;
import cfg.Cfg.Stm.AssignArray;
import cfg.Cfg.Stm.InvokeVirtual;
import cfg.Cfg.Stm.Length;
import cfg.Cfg.Stm.Lt;
import cfg.Cfg.Stm.Move;
import cfg.Cfg.Stm.NewIntArray;
import cfg.Cfg.Stm.NewObject;
import cfg.Cfg.Stm.Not;
import cfg.Cfg.Stm.Print;
import cfg.Cfg.Stm.Sub;
import cfg.Cfg.Stm.Times;
import cfg.Cfg.Transfer;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;
import cfg.Cfg.Type.ClassType;
import cfg.Cfg.Type.IntArrayType;
import cfg.Cfg.Type.IntType;
import cfg.Cfg.Vtable.VtableSingle;

public class VisualVisitor implements Visitor
{
  public StringBuffer strb;

  public VisualVisitor()
  {
    this.strb = new StringBuffer();
  }

  // ///////////////////////////////////////////////////
  private void emit(String s)
  {
    strb.append(s);
    return;
  }

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(Int operand)
  {
    emit(new Integer(operand.i).toString());
  }

  @Override
  public void visit(Var operand)
  {
    emit(operand.id);
  }

  // statements
  @Override
  public void visit(Add s)
  {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" + ");
    s.right.accept(this);
    emit(";");
    return;
  }

  @Override
  public void visit(InvokeVirtual s)
  {
    emit(s.dst + " = " + s.obj);
    emit("->vptr->" + s.f + "(" + s.obj);
    for (Operand.T x : s.args) {
      emit(", ");
      x.accept(this);
    }
    emit(");");
    return;
  }

  @Override
  public void visit(Lt s)
  {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" < ");
    s.right.accept(this);
    emit(";");
    return;
  }

  @Override
  public void visit(Move s)
  {
    emit(s.dst + " = ");
    s.src.accept(this);
    emit(";");
    return;
  }

  @Override
  public void visit(NewObject s)
  {
    emit(s.dst + " = ((struct " + s.c + "*)(Tiger_new (&" + s.c
        + "_vtable_, sizeof(struct " + s.c + "))));");
    return;
  }

  @Override
  public void visit(Print s)
  {
    emit("System_out_println (");
    s.arg.accept(this);
    emit(");");
    return;
  }

  @Override
  public void visit(Sub s)
  {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" - ");
    s.right.accept(this);
    emit(";");
    return;
  }

  @Override
  public void visit(Times s)
  {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" * ");
    s.right.accept(this);
    emit(";");
    return;
  }

  // transfer
  @Override
  public void visit(If s)
  {
    emit("if (");
    s.operand.accept(this);
    emit(")\n");
    emit("  goto " + s.truee.toString() + ";\n");
    emit("else\n");
    emit("  goto " + s.falsee.toString() + ";\n");
    return;
  }

  @Override
  public void visit(Goto s)
  {
    emit("goto " + s.label.toString() + ";\n");
    return;
  }

  @Override
  public void visit(Return s)
  {
	  emit("return ");
	  s.operand.accept(this);
    return;
  }

  // type
  @Override
  public void visit(ClassType t)
  {
    emit("struct " + t.id + " *");
  }

  @Override
  public void visit(IntType t)
  {
	  return;
  }

  @Override
  public void visit(IntArrayType t)
  {
	  return;
  }

  // dec
  @Override
  public void visit(DecSingle d)
  {
    return;
  }

  // dec
  @Override
  public void visit(BlockSingle b)
  {
	  emit("hahahahaha");

    return;
  }

  // method
  @Override
  public void visit(MethodSingle m)
  {
    java.util.HashMap<util.Label, Block.T> map = new HashMap<util.Label, Block.T>();
    for (Block.T block : m.blocks) {
      BlockSingle b = (BlockSingle) block;
      util.Label label = b.label;
      map.put(label, b);
    }

    //初始化图
    util.Graph<Block.T> graph = new util.Graph<Block.T>(m.classId + "_"
        + m.id);

    //遍历block，把所有节点加入图
    for (Block.T block : m.blocks) {
      graph.addNode(block);
    }
    //再次遍历block
    for (Block.T block : m.blocks) {
      BlockSingle b = (BlockSingle) block;
      
      Transfer.T transfer = b.transfer;
      if (transfer instanceof Transfer.Goto) {
        Transfer.Goto gotoo = (Transfer.Goto) transfer;
        //因为map的key是label，所以用label获取目标节点
        Block.T to = map.get(gotoo.label);
        //加入边
        graph.addEdge(block, to);
      } else if (transfer instanceof Transfer.If) {
        Transfer.If iff = (If) transfer;
        //if的then和else都是label类型，都表示目的节点
        Block.T truee = map.get(iff.truee);
        graph.addEdge(block, truee);
        Block.T falsee = map.get(iff.falsee);
        graph.addEdge(block, falsee);
      }
    }
    graph.visualize();
    return;
  }

  @Override
  public void visit(MainMethodSingle m)
  {
    java.util.HashMap<util.Label, Block.T> map = new HashMap<util.Label, Block.T>();
    for (Block.T block : m.blocks) {
      Block.BlockSingle b = (Block.BlockSingle) block;
      util.Label label = b.label;
      map.put(label, b);
    }

    util.Graph<Block.T> graph = new util.Graph<>("Tiger_main");

    for (Block.T block : m.blocks) {
      graph.addNode(block);
    }
    for (Block.T block : m.blocks) {
      BlockSingle b = (BlockSingle) block;
      Transfer.T transfer = b.transfer;
      if (transfer instanceof Goto) {
        Transfer.Goto gotoo = (Transfer.Goto) transfer;
        Block.T to = map.get(gotoo.label);
        graph.addEdge(block, to);
      } else if (transfer instanceof Transfer.If) {
        Transfer.If iff = (Transfer.If) transfer;
        Block.T truee = map.get(iff.truee);
        graph.addEdge(block, truee);
        Block.T falsee = map.get(iff.falsee);
        graph.addEdge(block, falsee);
      }
    }
    graph.visualize();
    return;
  }

  // vtables
  @Override
  public void visit(VtableSingle v)
  {
    return;
  }

  // class
  @Override
  public void visit(ClassSingle c)
  {
    return;
  }

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    // we'd like to output to a file, rather than the "stdout".
    for (cfg.Cfg.Method.T m : p.methods) {
      m.accept(this);
    }
    p.mainMethod.accept(this);
  }

@Override
public void visit(And m) {
	emit(m.dst + " = ");
    m.left.accept(this);
    emit(" + ");
    m.right.accept(this);
	
}

@Override
public void visit(ArraySelect m) {
	emit(m.id+" = ");
	m.array.accept(this);
	emit("[");
	m.index.accept(this);
	emit("]");
	
}

@Override
public void visit(Length m) {
	emit(m.dst+" = ");
	m.array.accept(this);
	emit("[-1]");//这个地方特殊处理一下，上面一句只能输出this->number
	
}

@Override
public void visit(NewIntArray m) {
	emit(m.dst + " = (int*)Tiger_new_array(");
	m.exp.accept(this);//exp就是index的operand,提前已经打印出来了
	emit(")");
	
}

@Override
public void visit(Not m) {
	emit(m.dst+" = "+"!" );
	m.exp.accept(this);
	return;
	
}

@Override
public void visit(AssignArray m) {
	if(m.isField==false)
		emit(m.dst+"[");
	else
		emit("this->"+m.dst+"[");
	m.index.accept(this);
	
}

}
