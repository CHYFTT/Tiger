package elaborator;

import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;

public class TestprintHash {
	
	public static void main(String[] args) {
		
		java.util.Hashtable<String, ClassBinding> table
		=new java.util.Hashtable<String, ClassBinding>();
		
		java.util.Hashtable<String, Type.T> fields=
				new java.util.Hashtable<String, Type.T>();
	      java.util.Hashtable<String, MethodType> methods=
	    		  new java.util.Hashtable<String, MethodType>();
		
		
		ClassBinding cb=new ClassBinding("extends",fields,methods);
		
		fields.put("1", new Type.Boolean());
		
		LinkedList<Dec.T> args123=new LinkedList<Dec.T>() ;
		Type.T t=new Type.Int();
		MethodType mp=new MethodType(t,
		  args123);
		methods.put("sum", mp);

		table.put("1", cb);
		table.put("2",cb);
		
		System.out.println(table);
	}

}
