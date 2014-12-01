package util;

public class Bug
{
  public Bug()
  {
    throw new java.lang.Error("Compiler bug");
  }
  
  public enum Error
  {
	MISTYPE,
	UNDECL,
	RET,
	COOKBLOCK;
	
  }


public static void error(Error c)
{
	System.out.println("cookboloc error");
	System.exit(1);
}

public static void error(Error c,ast.Ast.Type.T type,int linenum)
{
	  switch(c){
	  case MISTYPE:
		  System.err.println("error:type mismatch at line "+linenum);
		  System.err.println("need type:"+type.toString());
		  System.exit(1);
		  break;
	  case UNDECL:
		  System.err.println("error:un decl var at line "+linenum);
		  System.exit(1);
		  break;
	  case RET:
		  System.err.println("error:return val mis at line "+linenum);
		  System.err.println("return type must be "+type.toString());
		  System.exit(1);
	  }
  
}
}
