<<<<<<< HEAD
=======
/*------------------------------------------------------------------*/
/* Copyright (C) SSE-USTC, 2014-2015                                */
/*                                                                  */
/*  FILE NAME             :  Graph.java                             */
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
>>>>>>> origin/Lab4
package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import cfg.Cfg.Block;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Block.T;

public class Graph<X> implements Serializable
{
	private static final long serialVersionUID = 1L;

// graph node
  public class Node implements Serializable
  {
	private static final long serialVersionUID = 1L;
	public X data;
    public LinkedList<Edge> edges;
    public Integer indegree;
	public Integer outdegree;
   // public HashSet<X> fr;//直接指向这个节点的node.data
    //public HashSet<X> pre;//这个节点的所有祖先

    public Node()
    {
      this.data = null;
      this.edges = null;
      this.indegree=0;
      this.outdegree=0;
      //this.fr=null;
     // this.pre=null;
    }

    public Node(X data)
    {
      this.data = data;//放block
      this.edges = new LinkedList<Edge>();
      this.indegree=0;
      this.outdegree=0;
     // this.fr=new HashSet<X>();//指向这个节点的node.data
     // this.pre=new HashSet<X>();
      ;
    }

    @Override
    public String toString()
    {
      return data.toString();
    }
    
    public HashSet<Node> getNeighbors()
	{
		HashSet<Node> nei=new HashSet<Node>();
		for(Edge e:this.edges)
		{
			nei.add(e.to);
		}
		return nei;
	}
  }

  // graph edge
  public class Edge implements Serializable
  {
	private static final long serialVersionUID = 1L;
	public Node from;
    public Node to;

    public Edge(Node from, Node to)
    {
      this.from = from;
      this.to = to;
    }

    @Override
    public boolean equals(Object o)
    {
      if (o == null)
        return false;
      if (!(o instanceof Graph.Edge))
        return false;

      return (this == o);
    }

    @Override
    public String toString()
    {
      return (this.from.toString() + "->" + this.to.toString());
    }
  }

  // the graph
  public LinkedList<Node> graph;//block的链表
  public String gname;

  public Graph(String name)
  {//构造方法
    this.gname = name;
    this.graph = new LinkedList<Node>();
  }

  public void delNode(X data)
  {
	  Node node=this.lookupNode(data);
	  if(node==null)
		 new util.Bug();
	  
	  this.delNode(node);
	  
	  
  }
  
  private void delNode(Node node)
  {
	  //先处理边
	  for(Edge edge:node.edges)
	  {
			if (edge.to != null)
				edge.to.indegree--;
			if (edge.from != null)
				edge.from.outdegree--;
	  }
	  this.graph.remove(node);
  }
  
  private void addNode(Node node)
  {//在最后加入node，主要是给下面的方法调用
    this.graph.addLast(node);
  }

  public void addNode(X data)
  {
    for (Node n : this.graph)
      if (n.data.equals(data))
        new util.Bug();

    Node node = new Node(data);
    this.addNode(node);
  }

  public Node lookupNode(X data)
  {//查找node
    for (Node node : this.graph) {
      if (node.data.equals(data))
        return node;
    }
    return null;
  }
  
//  public void addto(X to,X from)
//  {
//	  Node too=this.lookupNode(to);
//	  too.fr.add(from);
//	  
//  }
  
//  public void addPre(X to,X from)
//  {//加入祖先的信息
//	  Node too=this.lookupNode(to);//目的
//	  Node fromm=this.lookupNode(from);
//	  too.pre.add(fromm.data);
//	  too.pre.addAll(fromm.pre);
//  }

  private void addEdge(Node from, Node to)
  {
	  from.outdegree++;//from的出度++
		to.indegree++;//to的入度++
    from.edges.addLast(new Edge(from, to));
  }

  public void addEdge(X from, X to)
  {//加入边
    Node f = this.lookupNode(from);
    Node t = this.lookupNode(to);

    if (f == null || t == null)
      new util.Bug();

    this.addEdge(f, t);
  }

  public HashSet<Node> dfsDoit(Node start,HashSet<Node> visited)
  {
    //visited.add(start);

    for (Edge edge : start.edges)
    {
    	//这个节点的edge指向的节点没有在visited里面，就从指向的那个节点开始,继续DFS
    	//如果已经在，那就换一条边继续
      if (!visited.contains(edge.to))
      {
    	  visited.add(edge.to);
        visited=dfsDoit(edge.to, visited);
      }
    }
    return visited;
  }

  public HashSet<Node> dfs(X start)
  {//深度优先
	  
	  //先查找node
    Node startNode = this.lookupNode(start);
    if (startNode == null)
      new util.Bug();

    //用Set保存访问过的节点。
    java.util.HashSet<Node> visited = new java.util.HashSet<Node>();
    //开始DFS
    visited=this.dfsDoit(startNode,visited);

    // For control-flow graph, we don't need this.
    
//      for (Node n : this.graph)
//      {
//    	  if (!visited.contains(n)) 
//    		  dfsDoit(n, visited);
//      }
//     
    return visited;
  }
  
  

  public void visualize()
  {
    Dot dot = new Dot();
    String fname;

    fname = this.gname;

    for (Node node : this.graph) {
				for (Edge edge : node.edges) 
				{
					dot.insert(edge.from.toString(), edge.to.toString());
				}
    }

    try {
      dot.visualize(fname);
    } catch (Exception e) {
      e.printStackTrace();
      new util.Bug();
    }
  }
  
  
  HashSet<Graph<T>.Node> visited = new HashSet<Graph<T>.Node>();
  LinkedList<Graph<T>.Node> trace=new LinkedList<Graph<T>.Node>();
  HashMap<Block.T,ArrayList<Block.T>> cycle=new HashMap<Block.T,ArrayList<Block.T>>();

  //对克隆图进行删除环路 
  public HashMap<Block.T,ArrayList<Block.T>> delCycle(Graph<T>.Node n)
  {
		if(visited.contains(n))
		{
			BlockSingle bs=(BlockSingle)n.data;
			int j;
            if((j=trace.indexOf(n))!=-1)
            {//只输出trace的一部分。j-i的部分
                //System.out.print("Cycle:");
                
                BlockSingle b=(BlockSingle) trace.get(j).data;
               // System.out.print(b.label.toString()+" ");
                j++;
                
                ArrayList<Block.T> cy=new ArrayList<Block.T>();
                while(j<trace.size())
                {
                	BlockSingle bb=(BlockSingle) trace.get(j).data;
                   // System.out.print(bb.label.toString()+" ");
                    j++;
                    //
                    cy.add(bb);
                }
                //cy1是之前的环
                ArrayList<Block.T> cy1=cycle.get(b);
                //将新环的节点加入
                /*
                 * 此处并不能求得所有的环了。只是消除了一个最大的环。
                 */
                if(cy1!=null)
                {
                for(Block.T bt:cy)
                {
                	BlockSingle btt=(BlockSingle)bt;
                	if(!cy1.contains(btt))
                		cy1.add(btt);
                }
                cycle.put(b, cy1);
                }
                else
                	cycle.put(b, cy);//b的值可能会重复，所以很多环会
                System.out.print("\n");
                return cycle;
            }
            return cycle;
			
		}
		else
		{
			visited.add(n);
			trace.add(n);
		}
		
		for(Graph<T>.Edge edge:n.edges)
		{
			delCycle(edge.to);
		}
		visited.remove(n);
		trace.remove(trace.size()-1);
		return cycle;
	
  }
  
  @SuppressWarnings("unchecked")
public ArrayList<X> topSort()
  {
	  ArrayList<X> top=new ArrayList<X>();
	  Queue<Node> q=new LinkedList<Node>();
	  HashSet<Node> neighbors=new HashSet<Node>();
	  util.Graph<T> g=null;
		try {
			g=(Graph<T>) Clone.clone(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	  for(Graph<T>.Node n:g.graph)
	  {
		  if(n.indegree==0)
		  {
			  q.add( (Graph<X>.Node) n);
			  top.add((X) n.data);
		  }
		  
	  }
		  while(!q.isEmpty())
		  {
			  Node qn=q.poll();
			  //System.out.println(qn);
			  
			  for(Edge edge:qn.edges)
			  {
				  if(g.graph.contains(edge.to))
					  neighbors.add(edge.to);
			  }
			  //这个getNeighbors方法有问题。返回的还是原图的nei
			  //neighbors=qn.getNeighbors();
			  g.graph.remove(qn);
			  for(Node nn:neighbors)
			  {
				  nn.indegree--;
				  if(nn.indegree==0)
				  {
					  	q.add(nn);
						top.add(nn.data);
						g.graph.remove(nn);
				  }
			  }
		  }
		  
		if (q.size() != g.graph.size()) 
		{
			throw new IllegalStateException("存在环");
		}
		return top;
}
	
	  
  }
  

