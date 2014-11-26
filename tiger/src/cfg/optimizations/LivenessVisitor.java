package cfg.optimizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import util.Graph;
import util.Graph.Edge;
import util.Graph.Node;
import cfg.Cfg.Block;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Block.T;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Var;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm;
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

public class LivenessVisitor implements cfg.Visitor
{
  // gen, kill for one statement
  private HashSet<String> oneStmGen;
  private HashSet<String> oneStmKill;

  // gen, kill for one transfer
  private HashSet<String> oneTransferGen;
  private HashSet<String> oneTransferKill;

  // gen, kill for statements
  private HashMap<Stm.T, HashSet<String>> stmGen;
  private HashMap<Stm.T, HashSet<String>> stmKill;

  // gen, kill for transfers
  private HashMap<Transfer.T, HashSet<String>> transferGen;
  private HashMap<Transfer.T, HashSet<String>> transferKill;

  // gen, kill for blocks
  private HashMap<Block.T, HashSet<String>> blockGen;
  private HashMap<Block.T, HashSet<String>> blockKill;

  // liveIn, liveOut for blocks
  private HashMap<Block.T, HashSet<String>> blockLiveIn;
  private HashMap<Block.T, HashSet<String>> blockLiveOut;

  // liveIn, liveOut for statements
  public HashMap<Stm.T, HashSet<String>> stmLiveIn;
  public HashMap<Stm.T, HashSet<String>> stmLiveOut;

  // liveIn, liveOut for transfer
  public HashMap<Transfer.T, HashSet<String>> transferLiveIn;
  public HashMap<Transfer.T, HashSet<String>> transferLiveOut;

  // As you will walk the tree for many times, so
  // it will be useful to recored which is which:
  enum Liveness_Kind_t
  {
    None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
  }

  private Liveness_Kind_t kind = Liveness_Kind_t.None;

  public LivenessVisitor()
  {
    this.oneStmGen = new HashSet<>();
    this.oneStmKill = new java.util.HashSet<>();

    this.oneTransferGen = new java.util.HashSet<>();
    this.oneTransferKill = new java.util.HashSet<>();

    this.stmGen = new java.util.HashMap<>();
    this.stmKill = new java.util.HashMap<>();

    this.transferGen = new java.util.HashMap<>();
    this.transferKill = new java.util.HashMap<>();

    this.blockGen = new java.util.HashMap<>();
    this.blockKill = new java.util.HashMap<>();

    this.blockLiveIn = new java.util.HashMap<>();
    this.blockLiveOut = new java.util.HashMap<>();

    this.stmLiveIn = new java.util.HashMap<>();
    this.stmLiveOut = new java.util.HashMap<>();

    this.transferLiveIn = new java.util.HashMap<>();
    this.transferLiveOut = new java.util.HashMap<>();

    this.kind = Liveness_Kind_t.None;
  }

  // /////////////////////////////////////////////////////
  // utilities

  private java.util.HashSet<String> getOneStmGenAndClear()
  {
    java.util.HashSet<String> temp = this.oneStmGen;
    this.oneStmGen = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneStmKillAndClear()
  {
    java.util.HashSet<String> temp = this.oneStmKill;
    this.oneStmKill = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferGenAndClear()
  {
    java.util.HashSet<String> temp = this.oneTransferGen;
    this.oneTransferGen = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferKillAndClear()
  {
    java.util.HashSet<String> temp = this.oneTransferKill;
    this.oneTransferKill = new java.util.HashSet<>();
    return temp;
  }

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(Int operand)
  {
	  //int也不做操作
    return;
  }

  @Override
  public void visit(Var operand)
  {
    this.oneStmGen.add(operand.id);
    return;
  }

  // statements
  @Override
  public void visit(Add s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(InvokeVirtual s)
  {
    this.oneStmKill.add(s.dst);
    this.oneStmGen.add(s.obj);
    for (Operand.T arg : s.args) {
      arg.accept(this);
    }
    return;
  }

  @Override
  public void visit(Lt s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(Move s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.src.accept(this);
    return;
  }

  @Override
  public void visit(NewObject s)
  {
    this.oneStmKill.add(s.dst);
    return;
  }

  @Override
  public void visit(Print s)
  {
    s.arg.accept(this);
    return;
  }

  @Override
  public void visit(Sub s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(Times s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  // transfer
  @Override
  public void visit(If s)
  {
    // Invariant: accept() of operand modifies "gen"
    s.operand.accept(this);
    return;
  }

  @Override
  public void visit(Goto s)
  {
	  //不需要做处理。因为Goto不涉及operand
    return;
  }

  @Override
  public void visit(Return s)
  {
    // Invariant: accept() of operand modifies "gen"
    s.operand.accept(this);
    return;
  }

  // type
  @Override
  public void visit(ClassType t)
  {
  }

  @Override
  public void visit(IntType t)
  {
  }

  @Override
  public void visit(IntArrayType t)
  {
  }

  // dec
  @Override
  public void visit(DecSingle d)
  {
  }

  // utility functions:
  private void calculateStmTransferGenKill(BlockSingle b)
  {
    for (Stm.T s : b.stms) {
    	//oneStm每一条语句刷新一次
      this.oneStmGen = new java.util.HashSet<>();
      this.oneStmKill = new java.util.HashSet<>();      
      s.accept(this);
      /*
       * s执行完accept后会怎么样
       * oneStmKill会加入id
       * oneStmGen会加入id
       * 一条Stm可能会加入很多的Kill或Gen
       */
      //stmGen和stmKill只有一个，里面都放着每个stm的kill和gen
      this.stmGen.put(s, this.oneStmGen);
      this.stmKill.put(s, this.oneStmKill);
      if (control.Control.isTracing("liveness.step1")) {
        System.out.print("\ngen, kill for statement:");
       System.out.println(s.toString());
        System.out.print("gen is:");
        for (String str : this.oneStmGen) {
          System.out.print(str + ", ");
        }
        System.out.print("\nkill is:");
        for (String str : this.oneStmKill) {
          System.out.print(str + ", ");
        }
        System.out.println("");
      }
    }
    //oneTrans每次都需要刷新
    this.oneTransferGen = new java.util.HashSet<>();
    this.oneTransferKill = new java.util.HashSet<>();
    b.transfer.accept(this);
    /*
     * 执行完后会改变oneStmGen
     * 
     * this.oneTransfer里面是空的？？？
     */
    this.transferGen.put(b.transfer, this.oneTransferGen);
    this.transferKill.put(b.transfer, this.oneTransferGen);
    if (control.Control.isTracing("liveness.step1")) {
      System.out.print("\ngen, kill for transfer:");
      System.out.println(b.transfer.toString());
      System.out.print("gen is:");
      for (String str : this.oneTransferGen) {
        System.out.print(str + ", ");
      }
      System.out.println("\nkill is:");
      for (String str : this.oneTransferKill) {
        System.out.print(str + ", ");
      }
      System.out.println("");
    }
    return;
    /*
     * 之行完这个方法后，this.stmGen里面放着这个block的所有语句，以及与这个语句对应的gen和kill的set
     */
  }
  
  private void calculateBlockTransferGenKill(BlockSingle b)
  {
	  // 计算block的Gen和Kill
	  java.util.HashSet<String> oneBlockGen=new java.util.HashSet<String>();
	  java.util.HashSet<String> oneBlockKill=new java.util.HashSet<String>();
	  
	  oneBlockGen.addAll(this.transferGen.get(b.transfer));
	  oneBlockKill.addAll(this.transferKill.get(b.transfer));
	  
	  
	  //revers！！！！
	  for(int i=b.stms.size()-1;i>=0;i--)
	  {
		  cfg.Cfg.Stm.T s=b.stms.get(i);
		  
		  oneBlockGen.removeAll(this.stmKill.get(s));
		  oneBlockGen.addAll(this.stmGen.get(s));
		  
		  oneBlockKill.addAll(this.stmKill.get(s));
	  }
	  
	  if (control.Control.isTracing("liveness.step2")) {
			System.out.print("    block  "+b.label.toString()+" "+" gen is: {");
			for (String s : oneBlockGen)
				System.out.print(s + ", ");
			System.out.println("}");

			System.out.print("    block  "+b.label.toString()+" "+"kill is: {");
			for (String s : oneBlockKill)
				System.out.print(s + ", ");
			System.out.println("}");
		}
	  
	  this.blockGen.put(b, oneBlockGen);
	  this.blockKill.put(b, oneBlockKill);
	  
  }
  
  private void calculateBlockInOut(BlockSingle b)
  {
	  //TODO
  }

  // block
  @Override
  public void visit(BlockSingle b)
  {
    switch (this.kind) {
    case StmGenKill:
      calculateStmTransferGenKill(b);
      break;
    case BlockGenKill:
    	calculateBlockTransferGenKill(b);
    	break;
    case BlockInOut:
    	calculateBlockInOut(b);
    	break;
    	
    	
    default:
      // Your code here:
      return;
    }
  }

  // method
  @Override
  public void visit(MethodSingle m)
  {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }

    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block in a reverse order.
    // Your code here:
    this.kind=Liveness_Kind_t.BlockGenKill;
    for(Block.T block :m.blocks)
    {
    	block.accept(this);
    }
    

    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    // Note that to speed up the calculation, you should first
    // calculate a reverse topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:
    
    //初始化一个hashmap，方便以后根据lable查找block
    java.util.HashMap<util.Label, Block.T> map=
    		new java.util.HashMap<util.Label, Block.T>();
    for(Block.T b:m.blocks)
    {
    	BlockSingle bb=(BlockSingle)b;
    	map.put(bb.label, bb);
    }
    
    util.Graph<Block.T> graph=new util.Graph<Block.T>(m.classId);
    
    //把所有block作为节点放入graph
    for(Block.T b:m.blocks)
    {
    	BlockSingle bb=(BlockSingle)b;
    	graph.addNode(bb);
    }
    
    //绘图
    for(Block.T b:m.blocks)
    {
    	BlockSingle bb=(BlockSingle)b;
    	Transfer.T transfer=bb.transfer;
    	if(transfer instanceof Transfer.Goto)
    	{
    		//画边
    		Block.T to=map.get(((Transfer.Goto) transfer).label);
    		graph.addEdge(bb, to);
    		//在目的节点加入入节点
    		graph.addto(to,bb);
    	}
    	else if(transfer instanceof Transfer.If)
    	{
    		Block.T to1=map.get(((Transfer.If) transfer).falsee);
    		graph.addEdge(bb, to1);
    		graph.addto(to1,bb);
    		Block.T to2=map.get(((Transfer.If) transfer).truee);
    		graph.addEdge(bb, to2);
    		graph.addto(to2,bb);
    	}
    	else//当为return时，不需要加edge
    		;
    }
    
    graph.visualize();
    
    //图的拓扑排序
    LinkedList<Block.T> top=new LinkedList<Block.T>();
    boolean isFirst=true;
    boolean topChanged=false;
    int locate=0;
    int size=graph.graph.size();
		while (top.size() != size)
		{
			for (int i = 0; i <= graph.graph.size() - 1; i++) 
			{
				Graph<Block.T>.Node b = graph.graph.get(i);
				if (isFirst) 
				{
					if (b.fr.isEmpty() && (!top.contains(b.data))) 
					{//b没有入边，且不在top中
						top.add(b.data);
						locate=i;//记录位置，graph中哪一个节点进的top
						topChanged=true;
						BlockSingle bbb = (BlockSingle)b.data;
						System.out.println(bbb.label.toString());
						
					}
					isFirst = false;
				} 
				else
				{
					if (b.fr.isEmpty() && (!top.contains(b.data)))
					{
						top.add(b.data);
						locate=i;//记录位置，graph中哪一个节点进的top
						topChanged=true;
						BlockSingle bbb = (BlockSingle)b.data;
						System.out.println(bbb.label.toString());
					} else if ((!top.contains(b.data))) 
					{//可以有入边，但入边的节点已经在top中才行
						boolean ok = true;
						for (Block.T block : b.fr)
						{
							if (!top.contains(block)) 
							{
								ok = false;
								break;
							}
						}
						if (ok) 
						{
							top.add(b.data);
							locate=i;//记录位置，graph中哪一个节点进的top
							topChanged=true;
							BlockSingle bbb = (BlockSingle)b.data;
							System.out.println(bbb.label.toString());
						}
					} 
					else
						topChanged=false;
				}
			}
			//for循环结束
			/*
			 * 每一次for循环必须要有一个节点进入top。否则就会出现死循环，即找不到没有入度的节点。
			 * 所以一旦出现这种情况，要特殊处理。
			 * 选上一次进top的节点所指向的节点进top
			 */
			if (!topChanged) 
			{
				Graph<Block.T>.Node temp = graph.graph.get(locate);
				Edge edge = temp.edges.getFirst();
				Graph<Block.T>.Node to = edge.to;
				int k = graph.graph.indexOf(to);
				top.add(to.data);
				locate = k;
				BlockSingle bbb = (BlockSingle) to.data;
				System.out.println(bbb.label);
			}
			
			
		}
		//reverse
		System.out.println("reverse top sort");
		for (int j = 0, k = top.size()-1; j<k;j++,k--) 
		{
			BlockSingle temp=(BlockSingle)top.get(j);
			top.set(j, top.get(k));
			top.set(k, temp);
		}
		for(Block.T b:top)
		{
			BlockSingle bb=(BlockSingle)b;
			System.out.println(bb.label);
		}
		
		//do real work
		
		this.kind=Liveness_Kind_t.BlockInOut;
		for(Block.T b:top)
		{//按照 逆拓扑的顺序执行
			b.accept(this);
		}
		
    
    
    
    //用DFS得到伪逆拓扑序
   // LinkedList<Block.T> top=new LinkedList<Block.T>();
    
    

    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    // Your code here:

  }

  @Override
  public void visit(MainMethodSingle m)
  {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }

    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block in a reverse order.
    // Your code here:

    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    // Note that to speed up the calculation, you should first
    // calculate a reverse topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:

    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    // Your code here:
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
    p.mainMethod.accept(this);
    for (Method.T mth : p.methods) {
      mth.accept(this);
    }
    return;
  }

@Override
public void visit(And m) {
	this.oneStmKill.add(m.dst);
	m.left.accept(this);
	m.right.accept(this);
}

@Override
public void visit(ArraySelect m) {
	this.oneStmKill.add(m.id);
	m.array.accept(this);
	m.index.accept(this);
}

@Override
public void visit(Length m) {
	this.oneStmKill.add(m.dst);
	m.array.accept(this);
	
}

@Override
public void visit(NewIntArray m) {
	this.oneStmKill.add(m.dst);
	m.exp.accept(this);
	
}

@Override
public void visit(Not m) {
	this.oneStmKill.add(m.dst);
	m.exp.accept(this);
	
}

@Override
public void visit(AssignArray m) {
	this.oneStmKill.add(m.dst);
	m.exp.accept(this);
	m.index.accept(this);
}

}
