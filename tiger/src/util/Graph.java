package util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

public class Graph<X>
{

  // graph node
  public class Node
  {
    public X data;
    public LinkedList<Edge> edges;
    public HashSet<X> fr;//直接指向这个节点的node.data
    public HashSet<X> pre;//这个节点的所有祖先

    public Node()
    {
      this.data = null;
      this.edges = null;
      this.fr=null;
      this.pre=null;
    }

    public Node(X data)
    {
      this.data = data;//放block
      this.edges = new LinkedList<Edge>();
      this.fr=new HashSet<X>();//指向这个节点的node.data
      this.pre=new HashSet<X>();
      ;
    }

    @Override
    public String toString()
    {
      return data.toString();
    }
  }

  // graph edge
  public class Edge
  {
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
  
  public void addto(X to,X from)
  {
	  //TODO
	  Node too=this.lookupNode(to);
	  too.fr.add(from);
	  
  }
  
//  public void addPre(X to,X from)
//  {//加入祖先的信息
//	  Node too=this.lookupNode(to);//目的
//	  Node fromm=this.lookupNode(from);
//	  too.pre.add(fromm.data);
//	  too.pre.addAll(fromm.pre);
//  }

  private void addEdge(Node from, Node to)
  {
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

    //用Tree保存访问过的节点。
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
}
