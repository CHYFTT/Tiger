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
  static Tiger tiger;
  static CommandLine cmd;
  static InputStream fstream;
  PushbackInputStream f;
  public ast.Ast.Program.T theAst;
  codegen.bytecode.Ast.Program.ProgramSingle bytecodeAst;

  // lex and parse
public void lexAndParse(String fname)
  {
    Parser parser;

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
    return;
  }

  public void compile(String fname)
  {

    // /////////////////////////////////////////////////////
    // to test the pretty printer on the "test/Fac.java" program
   /* if (testFac) {
      System.out.println("Testing the Tiger compiler on Fac.java starting:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      control.CompilerPass ppPass = new control.CompilerPass(
          "Pretty printing AST", ast.Fac.prog, pp);
       ppPass.doit();
       System.out.println("PrettyPrintVisitor finished!!!!!!!!!!!!!!!!!!!!!!!!!\n");

      // elaborate the given program, this step is necessary
      // for that it will annotate the AST with some
      // informations used by later phase.
      elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
      control.CompilerPass elabPass = new control.CompilerPass(
          "Elaborating the AST", ast.Fac.prog, elab);
      elabPass.doit();
      System.out.println("Elab finished!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");

      // optimize the AST
      ast.optimizations.Main optAstPasses = new ast.optimizations.Main();
      control.CompilerPass optAstPass = new control.CompilerPass(
          "Optimizing AST", optAstPasses, ast.Fac.prog);
      optAstPass.doit();
      System.out.println("Optimizing finished!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
      ast.Fac.prog = (ast.Ast.Program.T) optAstPasses.program;

      // Compile this program to C.
      codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
      control.CompilerPass genCCodePass = new control.CompilerPass(
          "Translation to C code", ast.Fac.prog, transC);
      genCCodePass.doit();
      codegen.C.Ast.Program.T cAst = transC.program;

      if (control.Control.ConAst.dumpC) {
        codegen.C.PrettyPrintVisitor ppC = new codegen.C.PrettyPrintVisitor();
        control.CompilerPass ppCCodePass = new control.CompilerPass(
            "C code printing", cAst, ppC);
        ppCCodePass.doit();
        System.out.println("C code finished!!!!!!!!!!!!!!!!!!!!");
      }

      // translation to control-flow graph
      cfg.TranslateVisitor transCfg = new cfg.TranslateVisitor();
      control.CompilerPass genCfgCodePass = new control.CompilerPass(
          "Control-flow graph generation", cAst, transCfg);
      genCfgCodePass.doit();
      cfg.Cfg.Program.T cfgAst = transCfg.program;

      // visualize the control-flow graph, if necessary
      if (control.Control.visualize != Control.Visualize_Kind_t.None) {
        cfg.VisualVisitor toDot = new cfg.VisualVisitor();
        control.CompilerPass genDotPass = new control.CompilerPass(
            "Draw control-flow graph", cfgAst, toDot);
        genDotPass.doit();
      }

      // optimizations on the control-flow graph
      cfg.optimizations.Main cfgOpts = new cfg.optimizations.Main();
      control.CompilerPass cfgOptPass = new control.CompilerPass(
          "Control-flow graph optimizations", cfgOpts, cfgAst);
      cfgOptPass.doit();

      // code generation
      switch (control.Control.ConCodeGen.codegen) {
      case Bytecode:
        codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
        control.CompilerPass genBytecodePass = new control.CompilerPass(
            "Bytecode generation", ast.Fac.prog, trans);
        genBytecodePass.doit();
        codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;

        codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
        control.CompilerPass ppBytecodePass = new control.CompilerPass(
            "Bytecode printing", bytecodeAst, ppbc);
        ppBytecodePass.doit();
        break;
      case C:

        cfg.PrettyPrintVisitor ppCfg = new cfg.PrettyPrintVisitor();
        control.CompilerPass ppCfgCodePass = new control.CompilerPass(
            "C code printing", cfgAst, ppCfg);
        ppCfgCodePass.doit();
        break;
      case Dalvik:
        codegen.dalvik.TranslateVisitor transDalvik = new codegen.dalvik.TranslateVisitor();
        control.CompilerPass genDalvikCodePass = new control.CompilerPass(
            "Dalvik code generation", ast.Fac.prog, transDalvik);
        genDalvikCodePass.doit();
        codegen.dalvik.Ast.Program.T dalvikAst = transDalvik.program;

        codegen.dalvik.PrettyPrintVisitor ppDalvik = new codegen.dalvik.PrettyPrintVisitor();
        control.CompilerPass ppDalvikCodePass = new control.CompilerPass(
            "Dalvik code printing", dalvikAst, ppDalvik);
        ppDalvikCodePass.doit();
        break;
      case X86:
        // similar
        break;
      default:
        break;
      }
      return;
    }
*/
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
    //Program.T theAst = null;

    control.CompilerPass lexAndParsePass = new control.CompilerPass(
        "Lex and parse", tiger, fname);
    System.out.println("do lexAndParse");
    lexAndParsePass.doitName("lexAndParse");
    System.out.println("lexAndParse well down\n");
    
    // pretty printing the AST, if necessary
    if (dumpAst) {
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      control.CompilerPass ppAstPass = new control.CompilerPass(
          "Pretty printing the AST", theAst, pp);
      ppAstPass.doit();
    }

    // elaborate the AST, report all possible errors.
    elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
    control.CompilerPass elabAstPass = new control.CompilerPass(
        "Elaborating the AST", theAst, elab);
    elabAstPass.doit();
    System.out.println("Elab well down\n");

    // optimize the AST
    ast.optimizations.Main optAstPasses = new ast.optimizations.Main();
    control.CompilerPass optAstPass = new control.CompilerPass(
        "Optimizing the AST", optAstPasses, theAst);
   // optAstPass.doitName("doit");
    optAstPass.doit();
    theAst = optAstPasses.program;
    

    // code generation
    switch (control.Control.ConCodeGen.codegen) {
    case Bytecode:
      codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
      control.CompilerPass genBytecodePass = new control.CompilerPass(
          "Bytecode generation", theAst, trans);
      genBytecodePass.doit();
      codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;
      codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
      control.CompilerPass ppBytecodePass = new control.CompilerPass(
          "Bytecode printing", bytecodeAst, ppbc);
      ppBytecodePass.doit();//打印
      this.bytecodeAst=(codegen.bytecode.Ast.Program.ProgramSingle)bytecodeAst;//为了在Runtime里面使用
      break;
    case C:
      codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
      control.CompilerPass genCCodePass = new control.CompilerPass(
          "C code generation", theAst, transC);
      genCCodePass.doit();
      //theAst.accept(transC);
      codegen.C.Ast.Program.T cAst = transC.program;
      codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
      control.CompilerPass ppCCodePass = new control.CompilerPass(
          "C code printing", cAst, ppc);
      ppCCodePass.doit();
      //cAst.accept(ppc);
      break;
    case Dalvik:
      codegen.dalvik.TranslateVisitor transDalvik = new codegen.dalvik.TranslateVisitor();
      control.CompilerPass genDalvikCodePass = new control.CompilerPass(
          "Dalvik code generation", theAst, transDalvik);
      genDalvikCodePass.doit();
      codegen.dalvik.Ast.Program.T dalvikAst = transDalvik.program;

      codegen.dalvik.PrettyPrintVisitor ppDalvik = new codegen.dalvik.PrettyPrintVisitor();
      control.CompilerPass ppDalvikCodePass = new control.CompilerPass(
          "Dalvik code printing", dalvikAst, ppDalvik);
      ppDalvikCodePass.doit();
      break;
    case X86:
      // similar
      break;
    default:
      break;
    }
    System.out.println("Compile finished...\n");
    return;
  }

  public void assemble(String str)
  {
    // Your code here: methods[i].invoke(this.obj, this.x);
	// Your code here:
		  //str是fname
		  switch(control.Control.ConCodeGen.codegen)
		  {
		  case C:
		  String command="gcc -c "+str+
	    		  ".c -o "+str+".o";
		  BufferedReader br=null;
	      String err=null;
		  try 
		  {
			Process proC=Runtime.getRuntime().exec(command);
			
			br=new BufferedReader(new InputStreamReader(proC.getErrorStream()));
			while((err=br.readLine())!=null)
				{
					System.out.println(err);
				}
			
			command="gcc -c runtime\\runtime.c "+"-o runtime.o";
			
			proC=Runtime.getRuntime().exec(command);
			br=new BufferedReader(new InputStreamReader(proC.getErrorStream()));
			while((err=br.readLine())!=null)
			{
				System.out.println(err);
			}
			System.out.println("GCC assemble finished...\n");
			
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		  break;
		  case Bytecode:
			  codegen.bytecode.Ast.MainClass.MainClassSingle mainClass = 
			  (codegen.bytecode.Ast.MainClass.MainClassSingle)bytecodeAst.mainClass;
			  
			  String command2="java -jar jasmin.jar test\\"+mainClass.id+".j";
		      String err2=null;
				try {
					Process pro2=Runtime.getRuntime().exec(command2);
					BufferedReader br2=new BufferedReader(
							new InputStreamReader(pro2.getErrorStream()));
					while((err2=br2.readLine())!=null)
					{
						System.out.println(err2);
					}
					
					for(codegen.bytecode.Ast.Class.T c:bytecodeAst.classes)
				      {//用jasmin汇编每一个J文件
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
					System.out.println("Jasmin finished...\n");
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		      
		      break;
			  
		default:
			break;
		  }
  }

  public void link(String str)
  {
    // Your code here:
	  //str是fname
	  switch(control.Control.ConCodeGen.codegen)
	  {
	  case C:
	  String command="gcc "+str+
    		  ".o   "+"runtime.o -o "+str+".exe";
	  BufferedReader br=null;
      String err=null;
	  try 
	  {
		Process proC=Runtime.getRuntime().exec(command);
		
		br=new BufferedReader(new InputStreamReader(proC.getErrorStream()));
		while((err=br.readLine())!=null)
			{
				System.out.println(err);
			}
		System.out.println("GCC link finished...\n");
		command=str+".exe";
		System.out.println("Run "+command);
		proC=Runtime.getRuntime().exec(command);
		br=new BufferedReader(new InputStreamReader(proC.getInputStream()));
		while((err=br.readLine())!=null)
			{
				System.out.println(err);
			}
		System.out.println("Execute finished...\n");
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	  break;
	  case Bytecode:
		  codegen.bytecode.Ast.MainClass.MainClassSingle mainClass = 
		  (codegen.bytecode.Ast.MainClass.MainClassSingle)bytecodeAst.mainClass;
		  
		  String command2="java "+mainClass.id;
	      String err2=null;
			try {
				System.out.println("Run "+mainClass.id+".class");
				Process pro2=Runtime.getRuntime().exec(command2);
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
		  
	default:
		break;
	  }
	  
  }

  public void compileAndLink(String fname)
  {
    // compile生成Ast
    control.CompilerPass compilePass = new control.CompilerPass("Compile",
        tiger, fname);
    compilePass.doitName("compile");

    // assembling生成.o   .j
    control.CompilerPass assemblePass = new control.CompilerPass("Assembling",
        tiger, fname);
    assemblePass.doitName("assemble");

    // linking生成.c     .class
    control.CompilerPass linkPass = new control.CompilerPass("Linking", tiger,
        fname);
    linkPass.doitName("link");

    return;
  }

  public static void main(String[] args)
  {
    // ///////////////////////////////////////////////////////
    // handle command line arguments
    tiger = new Tiger();
    cmd = new CommandLine();
    String fname = "";
    fname = cmd.scan(args);

    control.CompilerPass tigerAll = new control.CompilerPass("Tiger", tiger,
        fname);
    
    tigerAll.doitName("compileAndLink");
    return;
  }
}
