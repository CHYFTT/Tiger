package cfg.optimizations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import util.Graph;
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
	//top序列
	static ArrayList<Block.T> top=new ArrayList<Block.T>();
	//reverse top
	static ArrayList<Block.T> retop=new ArrayList<Block.T>();
	//原始图
	static util.Graph<Block.T> graph;
	//删除环之后的图
	static util.Graph<Block.T> graphtemp;
  // gen, kill for one statement
  private HashSet<String> oneStmGen;
  private HashSet<String> oneStmKill;

  // gen, kill for one transfer
  private HashSet<String> oneTransferGen;
  private HashSet<String> oneTransferKill;
  
  private HashSet<String> oneStmIn;
  private HashSet<String> oneStmOut;
  
  private HashSet<String> oneTransferIn;
  private HashSet<String> oneTransferOut;

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
  
  public ArrayList<Block.T> cy;
  public HashMap<Block.T,ArrayList<Block.T>> cycle;
  public HashMap<Block.T,ArrayList<Block.T>> recycle;

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
    
    this.oneStmIn=new HashSet<String>();
    this.oneStmOut=new HashSet<String>();
    
    this.oneTransferIn=new HashSet<String>();
    this.oneTransferOut=new HashSet<String>();

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
    
    this.cy=new ArrayList<Block.T>();
    this.cycle=new HashMap<Block.T,ArrayList<Block.T>>();
    
    this.recycle=new HashMap<Block.T,ArrayList<Block.T>>();

    this.kind = Liveness_Kind_t.None;
  }

  // /////////////////////////////////////////////////////
  // utilities

//  private java.util.HashSet<String> getOneStmGenAndClear()
//  {
//    java.util.HashSet<String> temp = this.oneStmGen;
//    this.oneStmGen = new java.util.HashSet<>();
//    return temp;
//  }
//
//  private java.util.HashSet<String> getOneStmKillAndClear()
//  {
//    java.util.HashSet<String> temp = this.oneStmKill;
//    this.oneStmKill = new java.util.HashSet<>();
//    return temp;
//  }
//
//  private java.util.HashSet<String> getOneTransferGenAndClear()
//  {
//    java.util.HashSet<String> temp = this.oneTransferGen;
//    this.oneTransferGen = new java.util.HashSet<>();
//    return temp;
//  }
//
//  private java.util.HashSet<String> getOneTransferKillAndClear()
//  {
//    java.util.HashSet<String> temp = this.oneTransferKill;
//    this.oneTransferKill = new java.util.HashSet<>();
//    return temp;
//  }
  
  private java.util.HashSet<String> getOneStmInAndClear()
  {
    java.util.HashSet<String> temp = this.oneStmIn;
    this.oneStmIn = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneStmOutAndClear()
  {
    java.util.HashSet<String> temp = this.oneStmOut;
    this.oneStmOut = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferInAndClear()
  {
    java.util.HashSet<String> temp = this.oneTransferIn;
    this.oneTransferIn = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferOutAndClear()
  {
    java.util.HashSet<String> temp = this.oneTransferOut;
    this.oneTransferOut = new java.util.HashSet<>();
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
    this.oneTransferKill.add(s.operand.toString());
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
    this.oneTransferGen.add(s.operand.toString());
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
      if (control.Control.isTracing("liveness.step1")) 
      {
        System.out.print("\ngen, kill for statement:"+s+"\n");
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
     * 执行完后会改变oneTransfer
     */
    this.transferGen.put(b.transfer, this.oneTransferGen);
    this.transferKill.put(b.transfer, this.oneTransferKill);
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
      
      
//      System.out.println("Stm&&Transfer gen and kill___________________finished");
    return;
    /*
     * 之行完这个方法后，this.stmGen里面放着这个block的所有语句，以及与这个语句对应的gen和kill的set
     */
  }
  
  private void calculateBlockGenKill(BlockSingle b)
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
	  HashSet<String> oneBlockGen=this.blockGen.get(b);
	  HashSet<String> oneBlockKill=this.blockKill.get(b);
	  HashSet<String> oneBlockIn=new HashSet<String>();
	  HashSet<String> oneBlockOut=new HashSet<String>();
	  HashSet<String> oneBlockOuttemp=new HashSet<String>();
	  
	  //用top
	  
	  for(Block.T bb:LivenessVisitor.retop)
	  {
		  BlockSingle bs=(BlockSingle)bb;
		  if(!bs.label.equals(b.label))
		  {		//从top拿出来的与b不相等
			  oneBlockOut.addAll(this.blockLiveIn.get(bs));
		  }
		  else//相等时退出循环
			  break;
	  }
	  //用DFS
//	  HashSet<Graph<Block.T>.Node> dfs=new HashSet<Graph<Block.T>.Node>();
//	  
//	  dfs=LivenessVisitor.graph.dfs(b);
//	  Iterator it=dfs.iterator();
//	  while(it.hasNext())
//	  {
//		  Graph<Block.T>.Node n=(Graph<Block.T>.Node)it.next();
//		 
//			  System.out.println(n);
//		  
//		  oneBlockOut.addAll(this.blockLiveIn.get(n.data));
//	  }
	  //确定了out
	  this.blockLiveOut.put(b, oneBlockOut);
	  //
	  oneBlockOuttemp.addAll(oneBlockOut);
	  oneBlockOuttemp.removeAll(oneBlockKill);
	  oneBlockGen.addAll(oneBlockOuttemp);
	  oneBlockIn.addAll(oneBlockGen);
	  
	  this.blockLiveIn.put(b, oneBlockIn);
	  
	  
	  if (control.Control.isTracing("liveness.step3")) 
		 {
		  BlockSingle bs=(BlockSingle)b;
			 oneBlockOut=this.blockLiveOut.get(b);
			 oneBlockIn=this.blockLiveIn.get(b);
				System.out.print("    block  "+bs.label.toString()+" "+" out is: {");
				for (String s : oneBlockOut)
					System.out.print(s + ", ");
				System.out.println("}");

				System.out.print("    block  "+bs.label.toString()+" "+"in is: {");
				for (String s : oneBlockIn)
					System.out.print(s + ", ");
				System.out.println("}");
			}
  }
  
  private void calculateStmInOut(BlockSingle b)
  {
	  System.out.println("StmInOut start----------------------");
	  //TODO
	  //得到这个block里面每个语句的Gen和Kill。还有Transfer的Gen和Kill
	  this.calculateStmTransferGenKill(b);
	  //执行后StmGen和TransferGen都有了值，即得到了这个block每条语句的Gen和Kill
	  /*对Stm逆序处理
	   * 将block的out作为Transfer的out
	   */
	  //先处理Transfer
	 this.oneTransferIn=new HashSet<String>();
	 this.oneTransferOut=new HashSet<String>();
	 //计算TransferOut
	 oneTransferOut=this.blockLiveOut.get(b);
	 this.transferLiveOut.put(b.transfer, oneTransferOut);
	  //计算TransferIn
	 HashSet<String> temp=new HashSet<String>();
	 temp.addAll(oneTransferOut);
	 temp.removeAll(this.oneTransferKill);
	 this.oneTransferGen.addAll(temp);
	 oneTransferIn=this.oneTransferGen;
	 this.transferLiveIn.put(b.transfer, oneTransferIn);
	 
	 
     if (control.Control.isTracing("liveness.step4")) 
     {
       System.out.print("\nIn, Out for Transfer:"+b.transfer.toString()+"\n");
       System.out.print("In is:");
       for (String str : oneTransferIn) {
         System.out.print(str + ", ");
       }
       System.out.print("\nOut is:");
       for (String str : oneTransferOut) {
         System.out.print(str + ", ");
       }
       System.out.println("");
     }
	  
	  //逆序处理Stm
	  for(int i=b.stms.size()-1;i>=0;i--)
	  {
		  //这条Stm的kill&&gen
		  oneStmKill=this.stmKill.get(b.stms.get(i));
		  oneStmGen=this.stmGen.get(b.stms.get(i));
		  
		//计算Stm的Out
		  for(int j=i;j<b.stms.size()-1;j++)
		  {
			  HashSet<String> tempIn=new HashSet<String>();
			  tempIn=this.stmLiveIn.get(b.stms.get(j+1));
			  oneStmOut.addAll(tempIn);
		  }
		  oneStmOut.addAll(oneTransferIn);//StmOut计算完成
		  this.stmLiveOut.put(b.stms.get(i), oneStmOut);
		  
		  //计算Stm的In
		  temp.addAll(oneStmOut);
		  temp.removeAll(oneStmKill);//out-kill
		  oneStmGen.addAll(temp);//gen∪(out-kill)
		  oneStmIn.addAll(oneStmGen);
		  this.stmLiveIn.put(b.stms.get(i), oneStmIn);
		  
	//打印测试	  
	      if (control.Control.isTracing("liveness.step4")) 
	      {
	        System.out.print("\nIn, Out for statement:"+b.stms.get(i)+"\n");
	        System.out.print("In is:");
	        for (String str : this.oneStmIn) {
	          System.out.print(str + ", ");
	        }
	        System.out.print("\nOut is:");
	        for (String str : this.oneStmOut) {
	          System.out.print(str + ", ");
	        }
	        System.out.println("");
	      }
		  
		  
		  
	  }
	  System.out.println("StmInOut finished~~~~~~~~~~~~~~~~~~~~~");
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
    	calculateBlockGenKill(b);
    	break;
    case BlockInOut:
    	calculateBlockInOut(b);
    	break;
    case StmInOut:
    	calculateStmInOut(b);
    	
    	
    default:
      // Your code here:
      return;
    }
  }

  // method
  @SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
@Override
  public void visit(MethodSingle m)
  {
	  LivenessVisitor.top.clear();
	  LivenessVisitor.retop.clear();
	  
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
    
    this.kind=Liveness_Kind_t.BlockInOut;
    //初始化一个hashmap，方便以后根据lable查找block
    java.util.HashMap<util.Label, Block.T> map=
    		new java.util.HashMap<util.Label, Block.T>();
    for(Block.T b:m.blocks)
    {
    	BlockSingle bb=(BlockSingle)b;
    	map.put(bb.label, bb);
    }
    
    util.Graph<Block.T> graph=new util.Graph<Block.T>(m.id+"@@@@@");
    
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
    	}
    	else if(transfer instanceof Transfer.If)
    	{
    		Block.T to1=map.get(((Transfer.If) transfer).falsee);
    		graph.addEdge(bb, to1);
    		Block.T to2=map.get(((Transfer.If) transfer).truee);
    		graph.addEdge(bb, to2);
    	}
    	else//当为return时，不需要加edge
    		;
    }
    //画出删除环之后的图，会存在一点绘图时的误差，没必要再写一个visual
    graph.visualize();
    LivenessVisitor.graph=graph;
    //克隆图
    util.Graph<Block.T> graphtemp=(Graph<Block.T>) util.Clone.clone(graph);
    LivenessVisitor.graphtemp=graphtemp;
    
    Node start=LivenessVisitor.graphtemp.graph.getFirst();
    cycle=LivenessVisitor.graphtemp.delCycle(start);
  //环测试  ：会输出一个节点上最大的环
   System.out.println("test cycle");

    Set enn=cycle.entrySet();
    Iterator itt=enn.iterator();
    while(itt.hasNext())
    {
    	System.out.print("\n@");
    	 Map.Entry   entry   =   (Map.Entry)itt.next(); 
    	 BlockSingle b1=(BlockSingle) entry.getKey();
    	 System.out.print(b1.label.toString()+": ");
    	 ArrayList<Block.T> b2=(ArrayList<Block.T>) entry.getValue();
    	 for(int j=0;j<b2.size();j++)
    	 {
    		 BlockSingle b3=(BlockSingle)b2.get(j);
    		 System.out.print(b3.label.toString()+",");
    	 }
    	 System.out.println("");
    }
    
    
    //删除节点
    Set en=cycle.entrySet();
    Iterator it=en.iterator();
    while(it.hasNext())
    {
    	Map.Entry entry=(Map.Entry)it.next();
    	ArrayList<Block.T> b1=(ArrayList<Block.T>)entry.getValue();
    	for(int j=0;j<b1.size();j++)
    	{
    		LivenessVisitor.graphtemp.delNode(b1.get(j));
    	}
    }
    
    LivenessVisitor.graphtemp.visualize();
   //删除测试 
    System.out.print("number of node:");
    System.out.println(LivenessVisitor.graphtemp.graph.size());
    for(Node n1:LivenessVisitor.graphtemp.graph)
    {
    	BlockSingle b3=(BlockSingle)n1.data;
    	System.out.println(b3.label+"入度为:"+n1.indegree);
    }
    
    //top排序
    LivenessVisitor.top=LivenessVisitor.graphtemp.topSort();
    //top测试
    System.out.println("test top");
    for(int i=0;i<LivenessVisitor.top.size();i++)
    {
    	BlockSingle s=(BlockSingle)LivenessVisitor.top.get(i);
    	System.out.println(s.label);
    }
    
    //reverse top
    
    for (int j =LivenessVisitor.top.size()-1 ;j>-1; j--) 
	{
		BlockSingle temp=(BlockSingle)top.get(j);
		for(Node e:this.graph.graph)
		{
			//找到原始图中的Block
			BlockSingle temp2=(BlockSingle)e.data;
			if(temp.label.i==temp2.label.i)
				temp=(BlockSingle) e.data;
		}
		LivenessVisitor.retop.add(temp);
		
	}
  //测试retop
	System.out.println("this.top--------------------------");
	for(Block.T b:LivenessVisitor.retop)
	{
		BlockSingle bb=(BlockSingle)b;
		System.out.println(bb.label);
	}
	//先重造retop,为了能使用原来的map，必须用原图的block替换retop的。现在retop的block是深度克隆的。
    Set<Entry<T, ArrayList<T>>> en2=cycle.entrySet();
    Iterator<Entry<T, ArrayList<T>>> it2=en2.iterator();
    while(it2.hasNext())
    {
    	Map.Entry entry=(Map.Entry)it2.next();
    	//将List里面的Block用原图替换
    	ArrayList<Block.T> b1=(ArrayList<Block.T>)entry.getValue();
    	for(int i=0;i<b1.size();i++)
    	{
    		BlockSingle bs2=(BlockSingle)b1.get(i);
    		for(Node n2:LivenessVisitor.graph.graph)
    		{
    			BlockSingle b3=(BlockSingle)n2.data;
	    		if(b3.label.i==bs2.label.i)
	    		{
	    			//用原图的block替换cycle里面的block
	    			b1.set(i, b3);
	    		} 
    		}
    	}
    	
    	//将key用原图的替换
    	BlockSingle b2=(BlockSingle)entry.getKey();
    	for(Node n2:LivenessVisitor.graph.graph)
    	{
    		BlockSingle b3=(BlockSingle)n2.data;
    		if(b3.label.i==b2.label.i)
    		{
    			recycle.put(b3, b1);
    		}
    	}
    	//找到
    }
    
    
		
	
		
		//do real work 
		this.kind=Liveness_Kind_t.BlockInOut;
//		this.blockLiveIn=new HashMap<Block.T, HashSet<String>>();
//		this.blockLiveOut=new HashMap<Block.T, HashSet<String>>();
		for(Block.T b:LivenessVisitor.retop)
		{//按照 逆拓扑的顺序执行 
			if(LivenessVisitor.retop.indexOf(b)==0)
			{
				 HashSet<String> oneBlockIn=new HashSet<String>();
				 HashSet<String> oneBlockOut=new HashSet<String>();
				 //final的out为empty
				 this.blockLiveOut.put(b, oneBlockOut);
				 
				 HashSet<String> oneBlockGen=this.blockGen.get(b);
				 HashSet<String> oneBolckKill=this.blockKill.get(b);
				 
				 oneBlockOut.removeAll(oneBolckKill);
				 oneBlockGen.addAll(oneBlockOut);
				 oneBlockIn.addAll(oneBlockGen);
				 
				 this.blockLiveIn.put(b, oneBlockIn);
				 
				 BlockSingle bb=(BlockSingle)b;
				 if (control.Control.isTracing("liveness.step3")) 
				 {
					 oneBlockOut=this.blockLiveOut.get(b);
					 oneBlockIn=this.blockLiveIn.get(b);
						System.out.print("    block  "+bb.label.toString()+" "
					 +" out is: {");
						for (String s : oneBlockOut)
							System.out.print(s + ", ");
						System.out.println("}");

						System.out.print("    block  "+bb.label.toString()+" "
						+"in is: {");
						for (String s : oneBlockIn)
							System.out.print(s + ", ");
						System.out.println("}");
					}
			}
			else
				b.accept(this);
		}
		
		//补上删除的那些节点的BlockInOut
		
		
		    //recycle里面都是原图的block
		    Set en3=recycle.entrySet();
		    Iterator it3=en3.iterator();
		    HashMap<Block.T,Block.T> temp=new HashMap<Block.T,Block.T>();
		    while(it3.hasNext())
		    {
		    	//得到环的起点
		    	Map.Entry entry=(Map.Entry)it3.next();
		    	BlockSingle bs3=(BlockSingle) entry.getKey();
		    	//对涉及到的环上的节点补充In&Out
		    	ArrayList<Block.T> array=(ArrayList<Block.T>)entry.getValue();
		    	 HashSet<String> oneBlockIn=new HashSet<String>();
		   	  	HashSet<String> oneBlockOut=new HashSet<String>();
		   	  	
		   	  	oneBlockIn=this.blockLiveIn.get(bs3);
		   	  	oneBlockOut=this.blockLiveOut.get(bs3);
		   	  	for(Block.T bs4:array)
		   	  	{
		   	  		BlockSingle bs4_1=(BlockSingle)bs4;
		   	  		if(oneBlockIn==null)
		   	  		{
		   	  			if(bs3.label.i==32)
		   	  			{
		   	  				System.out.println("你妹");
		   	  			}
		   	  			temp.put(bs4_1, bs3);
		   	  			System.out.println(bs4_1.label+" need "+bs3.label);
		   	  			continue;
		   	  		}
		   	  		this.blockLiveIn.put(bs4_1, oneBlockIn);
		   	  		this.blockLiveOut.put(bs4_1, oneBlockOut);
			}

		}
    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    // Your code here:
		    this.kind=Liveness_Kind_t.StmInOut;
		    for (Block.T block : m.blocks) {
		        block.accept(this);
		      }

  
  
  
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
  
  



}
