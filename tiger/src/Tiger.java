import static control.Control.ConAst.dumpAst;
import static control.Control.ConAst.testFac;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;

import ast.Ast.Program;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import control.CommandLine;
import control.Control;

public class Tiger
{
  public static void main(String[] args)
  {
    InputStream fstream;
    Parser parser;
    PushbackInputStream f;

    // ///////////////////////////////////////////////////////
    // handle command line arguments
    CommandLine cmd = new CommandLine();
    String fname = cmd.scan(args);

    // /////////////////////////////////////////////////////
    // to test the pretty printer on the "test/Fac.java" program
    if (testFac) {
      System.out.println("Testing the Tiger compiler on Fac.java starting:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      ast.Fac.prog.accept(pp);

      // elaborate the given program, this step is necessary
      // for that it will annotate the AST with some
      // informations used by later phase.
      elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
      ast.Fac.prog.accept(elab);

      // Compile this program to C.
      System.out.println("code generation starting");
      // code generation
      switch (control.Control.ConCodeGen.codegen) {
      case Bytecode:
        System.out.println("bytecode codegen");
        codegen.bytecode.TranslateVisitor trans = 
        		new codegen.bytecode.TranslateVisitor();
        ast.Fac.prog.accept(trans);
        codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;
        codegen.bytecode.PrettyPrintVisitor ppbc =
        		new codegen.bytecode.PrettyPrintVisitor();
        bytecodeAst.accept(ppbc);
        break;
      case C:
        System.out.println("C codegen");
        codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
        ast.Fac.prog.accept(transC);
        codegen.C.Ast.Program.T cAst = transC.program;
        codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
        cAst.accept(ppc);
        break;
      case Dalvik:
        // similar
        break;
      case X86:
        // similar
        break;
      default:
        break;
      }
      System.out.println("Testing the Tiger compiler on Fac.java finished.");
      System.exit(1);
    }

    if (fname == null) {
      cmd.usage();
      return;
    }
    Control.ConCodeGen.fileName = fname;

    // /////////////////////////////////////////////////////
    // it would be helpful to be able to test the lexer
    // independently.
    if (Control.ConLexer.test) {
      System.out.println("Testing the lexer. All tokens:");
      try {
        fstream = new BufferedInputStream(new FileInputStream(fname));
        f=new PushbackInputStream(fstream);
        Lexer lexer = new Lexer(fname, f);
        Token token = lexer.nextToken();

        while (token.kind != Token.Kind.TOKEN_EOF) {
          System.out.println(token.toString());
          token = lexer.nextToken();
        }
        fstream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.exit(1);
    }

    // /////////////////////////////////////////////////////////
    // normal compilation phases.
    Program.T theAst = null;

    // parsing the file, get an AST.
    try {
      fstream = new BufferedInputStream(new FileInputStream(fname));
      f=new PushbackInputStream(fstream);
      parser = new Parser(fname, f);

      theAst = parser.parse();

      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    // pretty printing the AST, if necessary
    if (dumpAst) {
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      theAst.accept(pp);
    }

    // elaborate the AST, report all possible errors.
    elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
    theAst.accept(elab);
    System.out.println("Lab2 is finished....to be continue...");

    // code generation
    switch (control.Control.ConCodeGen.codegen) {
    case Bytecode:
      System.out.println("Start generate Bytecode....");
      codegen.bytecode.TranslateVisitor trans =
    		  new codegen.bytecode.TranslateVisitor();
      theAst.accept(trans);
      codegen.bytecode.Ast.Program.ProgramSingle bytecodeAst =
    		  (codegen.bytecode.Ast.Program.ProgramSingle) trans.program;
      codegen.bytecode.PrettyPrintVisitor ppbc = 
    		  new codegen.bytecode.PrettyPrintVisitor();
      bytecodeAst.accept(ppbc);
      System.out.println("Bytecode finished....");
      
      System.out.println("Jasmin start...");
      codegen.bytecode.Ast.MainClass.MainClassSingle mainClass = 
    		  (codegen.bytecode.Ast.MainClass.MainClassSingle)bytecodeAst.mainClass;
      String command2="java -jar jasmin.jar test\\"+mainClass.id+".j";
      String err2=null;
		try {
			Process pro2=Runtime.getRuntime().exec(command2);
			BufferedReader br=new BufferedReader(
					new InputStreamReader(pro2.getErrorStream()));
			while((err2=br.readLine())!=null)
			{
				System.out.println(err2);
			}
			
			for(codegen.bytecode.Ast.Class.T c:bytecodeAst.classes)
		      {
				codegen.bytecode.Ast.Class.ClassSingle cs=
						(codegen.bytecode.Ast.Class.ClassSingle)c;
		    	command2="java -jar jasmin.jar test\\"+cs.id+".j";
		    	pro2=Runtime.getRuntime().exec(command2);
		    	br=new BufferedReader(new InputStreamReader(pro2.getErrorStream()));
		    	while((err2=br.readLine())!=null)
		    	{
		    		System.out.println(err2);
		    	}
		      
		      }
			System.out.println("Jasmin finished...");
			command2="java "+mainClass.id;
			System.out.println("Run "+mainClass.id+".class");
			pro2=Runtime.getRuntime().exec(command2);
			br=new BufferedReader(new InputStreamReader(pro2.getInputStream()));
			while((err2=br.readLine())!=null)
			{
				System.out.println(err2);
			}
			br=new BufferedReader(new InputStreamReader(pro2.getErrorStream()));
			while((err2=br.readLine())!=null)
			{
				System.err.println(err2);
			}
			System.out.println("Execute finished...");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      
      break;
    case C:
      System.out.println("Start generate C....");
      codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
      theAst.accept(transC);
      codegen.C.Ast.Program.T cAst = transC.program;
      codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
      cAst.accept(ppc);
      System.out.println("Finished generate C...");
      System.out.println("GCC in windows...");
      String command="gcc "+fname+
    		  ".c  runtime\\runtime.c "+"-o "+fname+".exe";
      BufferedReader br=null;
      String err=null;
      
		try {
			System.out.println("Run "+command);
			Process proC=Runtime.getRuntime().exec(command);
			br=new BufferedReader(new InputStreamReader(proC.getErrorStream()));
			while((err=br.readLine())!=null)
			{
				System.out.println(err);
			}
			System.out.println("GCC complie finished...");
			command=fname+".exe";
			System.out.println("Run "+command);
			proC=Runtime.getRuntime().exec(command);
			br=new BufferedReader(new InputStreamReader(proC.getInputStream()));
			while((err=br.readLine())!=null)
			{
				System.out.println(err);
			}
			System.out.println("Execute finished...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      break;
    case Dalvik:
      // similar
      break;
    case X86:
      // similar
      break;
    default:
      break;
    }

    // Lab3, exercise 6: add some glue code to
    // call gcc to compile the generated C or x86
    // file, or call java to run the bytecode file,
    // or dalvik to run the dalvik bytecode.
    // Your code here:

    return;
  }
}
