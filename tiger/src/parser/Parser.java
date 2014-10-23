package parser;

import java.util.LinkedList;

import ast.Ast;
import ast.Ast.Class;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.MainClass;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program;
import ast.Ast.Type;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.T;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory.Default;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser {
	Lexer lexer;
	Token current;
	Token currentNext;//in order to deal with the margin between VarDecls and Statements
	boolean isSpecial=false;//when current.kind=Kind.TOKEN_ID,it may special
	int linenum;
	

	public Parser(String fname, java.io.PushbackInputStream f) {
		lexer = new Lexer(fname, f);
		current = lexer.nextToken();
		
	}

	// /////////////////////////////////////////////
	// utility methods to connect the lexer
	// and the parser.

	private void advance() // advance() can get the nextToken
	{
		System.out.println(current.kind.toString() + "  "+current.lexeme+"   " + linenum);
		linenum=current.lineNum;
		current = lexer.nextToken();
	}

	private void eatToken(Kind kind) {
		if (kind == current.kind) {
			advance();
		} else {
			System.out.println("Expects: " + kind.toString());
			System.out.println("But got: " + current.kind.toString());
			System.exit(1);
		}
	}

	private void error() {
		System.out.println("Syntax error: compilation aborting...at line:\n"+linenum);
		System.exit(1);
		return;
	}

	// ////////////////////////////////////////////////////////////
	// below are method for parsing.

	// A bunch of parsing methods to parse expressions. The messy
	// parts are to deal with precedence and associativity.

	// ExpList -> Exp ExpRest*
	// ->
	// ExpRest -> , Exp
	private LinkedList<T> parseExpList() {
		LinkedList<T> args=new LinkedList<T>();
		if (current.kind == Kind.TOKEN_RPAREN)
			return args;
			
		args.add(parseExp());
		while (current.kind == Kind.TOKEN_COMMER) {
			advance();
			args.add(parseExp());
		}
		return args;
	}

	// AtomExp -> (exp)
	// -> INTEGER_LITERAL
	// -> true
	// -> false
	// -> this
	// -> id
	// -> new int [exp]
	// -> new id ()
	private Exp.T parseAtomExp() {
		Exp.T exp = null;
		String s;
		int i;
		
		switch (current.kind) {
		case TOKEN_LPAREN:
			advance();
			exp=parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			return exp;
		case TOKEN_NUM:
			i=Integer.parseInt(current.lexeme);
			advance();
			return new Exp.Num(i,linenum);
		case TOKEN_TRUE:
			advance();
			return new Exp.True(linenum);
		case TOKEN_FALSE:
			advance();
			return new Exp.False(linenum);
		case TOKEN_THIS:
			advance();
			return new Exp.This(linenum);
		case TOKEN_ID:
			s=current.lexeme;
			advance();
			return new Id(s,linenum);
		case TOKEN_NEW: {
			advance();
			switch (current.kind) {
			case TOKEN_INT:
				advance();
				eatToken(Kind.TOKEN_LBRACK);
				exp=parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new Exp.NewIntArray(exp,linenum);
			case TOKEN_ID:
				s=current.lexeme;
				advance();
				eatToken(Kind.TOKEN_LPAREN);
				eatToken(Kind.TOKEN_RPAREN);
				return new NewObject(s,linenum);
			default:
				error();
				return exp;
			}
		}
		default:
			error();
			return exp;
		}
		
	}

	// NotExp -> AtomExp
	// -> AtomExp .id (expList)
	// -> AtomExp [exp]
	// -> AtomExp .length
	private Exp.T parseNotExp() {
		Exp.T exp;
		
		exp=parseAtomExp();
		while (current.kind == Kind.TOKEN_DOT
				|| current.kind == Kind.TOKEN_LBRACK) {
			if (current.kind == Kind.TOKEN_DOT) {
				advance();
				if (current.kind == Kind.TOKEN_LENGTH) {
					advance();
					return new Exp.Length(exp,linenum);
				}
				//else Call
				String s=current.lexeme;
				eatToken(Kind.TOKEN_ID);
				eatToken(Kind.TOKEN_LPAREN);
				LinkedList<T> args=parseExpList();
				eatToken(Kind.TOKEN_RPAREN);
				return new Call(exp,s,args,linenum);
			} else {
				//[exp]
				advance();
				exp=parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return exp;
			}
		}
		return exp;
	}

	// TimesExp -> ! TimesExp
	// -> NotExp
	private Exp.T parseTimesExp() {
		Exp.T exp = null;
		Exp.T exp2=null;
		while (current.kind == Kind.TOKEN_NOT) {
			advance();
			
			
			exp2=parseTimesExp();
		}
		
		

		if(exp2!=null)
		return new Exp.Not(exp2,linenum);
		else 
			exp=parseNotExp();
			return exp;
	}

	// AddSubExp -> TimesExp * TimesExp
	// -> TimesExp
	private Exp.T parseAddSubExp() {
		Exp.T left,right = null;
		left=parseTimesExp();
		while (current.kind == Kind.TOKEN_TIMES) {
			advance();
			right=parseTimesExp();
			return new Exp.Times(left, right,linenum);
		}
		return left;
	}

	// LtExp -> AddSubExp + AddSubExp
	// -> AddSubExp - AddSubExp
	// -> AddSubExp
	private Exp.T parseLtExp() {
		Exp.T left,right=null;
		left=parseAddSubExp();
		while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
			advance();
			right=parseAddSubExp();
			return new Exp.Add(left, right,linenum);
		}
		return left;
	}

	// AndExp -> LtExp < LtExp
	// -> LtExp
	private Exp.T parseAndExp() {
		Exp.T left,right=null;
		left=parseLtExp();
		while (current.kind == Kind.TOKEN_LT) {
			advance();
			right=parseLtExp();
			return new Exp.Lt(left, right,linenum);
		}
		return left;
	}

	// Exp -> AndExp && AndExp
	// -> AndExp
	private Exp.T parseExp() {
		Exp.T left,right=null;
		left=parseAndExp();
		while (current.kind == Kind.TOKEN_AND) {
			advance();
			right=parseAndExp();
			return new Exp.And(left, right,linenum);
		}
		return left;//new Exp.Lt(new Exp.Id("num"), new Exp.Num(1));
	}

	// Statement -> { Statement* }
	// -> if ( Exp ) Statement else Statement
	// -> while ( Exp ) Statement
	// -> System.out.println ( Exp ) ;
	// -> id = Exp ;
	// -> id [ Exp ]= Exp ;
	private Stm.T parseStatement() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a statement.
		// new util.Todo();
		Exp.T exp;
		Exp.T condition;
		
		
		LinkedList<Stm.T> stms=new LinkedList<Stm.T>();
		switch (current.kind) {
		
		case TOKEN_LBRACE:
			LinkedList<Stm.T> block=new LinkedList<Stm.T>();
			eatToken(Kind.TOKEN_LBRACE);
			block=parseStatements();
			eatToken(Kind.TOKEN_RBRACE);
			return new Stm.Block(block,linenum);
			
		case TOKEN_IF:
			// Exp.T condition; T thenn; T elsee;
			
			eatToken(Kind.TOKEN_IF);
			eatToken(Kind.TOKEN_LPAREN);// the eatToken() can check the token
										// and
			 condition=parseExp(); // then get the next token automatically
			eatToken(Kind.TOKEN_RPAREN);
			
			Stm.T thenn=parseStatement();
			
			//behind the if,it must be else; make the decision that Statements or Statement
			//in the CASE:TOKEN_ELSE,not in here
			eatToken(Kind.TOKEN_ELSE);
				
			Stm.T elsee=parseStatement();
			
				
			return new If(condition, thenn, elsee,linenum);


		case TOKEN_WHILE:
			
			eatToken(Kind.TOKEN_WHILE);
			eatToken(Kind.TOKEN_LPAREN);
			exp=parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			Stm.T body=parseStatement();
			return new Stm.While(exp,body,linenum);

			
		case TOKEN_SYSTEM:
			
			eatToken(Kind.TOKEN_SYSTEM);
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_OUT);
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_PRINTLN);
			eatToken(Kind.TOKEN_LPAREN);
			exp=parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			eatToken(Kind.TOKEN_SEMI);
			return new Print(exp,linenum);
			
		case TOKEN_ID:
			String id=current.lexeme;
			
			if(isSpecial)//it means this is returned from VarDecls
			{
				current=currentNext;
				switch (current.kind) {
				case TOKEN_ASSIGN:
					eatToken(Kind.TOKEN_ASSIGN);
					exp=parseExp();
					eatToken(Kind.TOKEN_SEMI);
					
					isSpecial=false;
					return new Stm.Assign(id, exp,linenum);
				case TOKEN_LBRACK:
					eatToken(Kind.TOKEN_LBRACK);
					Exp.T index=parseExp();
					eatToken(Kind.TOKEN_RBRACK);
					eatToken(Kind.TOKEN_ASSIGN);
					exp=parseExp();
					eatToken(Kind.TOKEN_SEMI);
					stms.add(new Stm.AssignArray(id, index, exp,linenum));
					isSpecial=false;
					return new Stm.AssignArray(id, index, exp,linenum);
				default:
					error();
					return null;

				}
				
				
			}
			else
			{
			eatToken(Kind.TOKEN_ID);
			switch (current.kind) {
			case TOKEN_ASSIGN:
				eatToken(Kind.TOKEN_ASSIGN);
				exp=parseExp();
				eatToken(Kind.TOKEN_SEMI);
				return new Stm.Assign(id, exp,linenum);
			case TOKEN_LBRACK:
				eatToken(Kind.TOKEN_LBRACK);
				Exp.T index=parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				eatToken(Kind.TOKEN_ASSIGN);
				exp=parseExp();
				eatToken(Kind.TOKEN_SEMI);
				return new Stm.AssignArray(id, index, exp,linenum);
			default:
				error();
				return null;

			}
			}
			
		
		case TOKEN_ASSIGN:
			exp=parseExp();
			eatToken(Kind.TOKEN_SEMI);
			break;
		default:
			error();
			return null;
		}
		return null;
		

	}

	// Statements -> Statement Statements
	// ->
	private LinkedList<Stm.T> parseStatements() {
		LinkedList<Stm.T> stm=new LinkedList<Stm.T>();
		while (current.kind == Kind.TOKEN_LBRACE
				|| current.kind == Kind.TOKEN_IF
				//|| current.kind ==Kind.TOKEN_ELSE
				|| current.kind == Kind.TOKEN_WHILE
				|| current.kind == Kind.TOKEN_SYSTEM
				|| current.kind == Kind.TOKEN_ID) {// make return as the
														// terminal of the
														// statement
			stm.add(parseStatement());
		}
		return stm;
	}

	// Type -> int []
	// -> boolean
	// -> int
	// -> id
	private Type.T parseType() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a type. what does this method do?
		// new util.Todo();

		switch (current.kind) {
		case TOKEN_INT:
			eatToken(Kind.TOKEN_INT);
			if(current.kind==Kind.TOKEN_LBRACK)
			{
				eatToken(Kind.TOKEN_LBRACK);
				eatToken(Kind.TOKEN_RBRACK);
				return new Type.IntArray() ;
			}
			return new Type.Int();
		case TOKEN_BOOLEAN:
			eatToken(Kind.TOKEN_BOOLEAN);
			return new Type.Boolean();
		default:
			String s=current.lexeme;
			eatToken(Kind.TOKEN_ID);
			return new Type.ClassType(s);
		}
	}

	// VarDecl -> Type id ;
	private DecSingle parseVarDecl() {
		DecSingle dec;
		String id;
		// to parse the "Type" nonterminal in this method, instead of writing
		// a fresh one.
		if(!isSpecial){

		Type.T type=parseType();// reference to the return Exp
		id=current.lexeme;
		dec=new DecSingle(type,id);
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_SEMI);
		return dec;
		}
		else
		{
			Type.T type=new Type.ClassType(current.lexeme);
			current=currentNext;
			id=current.lexeme;
			dec=new DecSingle(type,id);
			eatToken(Kind.TOKEN_ID);
			eatToken(Kind.TOKEN_SEMI);
			isSpecial=false;
			
			return dec;
			
		}
	}

	// VarDecls -> VarDecl VarDecls
	// ->
	private LinkedList<Dec.T> parseVarDecls() {
		LinkedList<Dec.T> decs=new LinkedList<ast.Ast.Dec.T>();
		while (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) { // through the
													// while(),ensure muti
													// VarDecls
			if (current.kind != Kind.TOKEN_ID) {
				decs.add(parseVarDecl());
			} 
			else 
			{//the current must be TOKEN_ID
				String id=current.lexeme;
				int linenum=current.lineNum;
				eatToken(Kind.TOKEN_ID);//statement //I think it's need to goto Statement;
				if(current.kind==Kind.TOKEN_ASSIGN)
				{
					currentNext=current;
					current=new Token(Kind.TOKEN_ID,linenum,id);
					isSpecial=true;
					return decs;
					
				}//statement
				else if(current.kind==Kind.TOKEN_LBRACK)
				{
					currentNext=current;
					current=new Token(Kind.TOKEN_ID,linenum,id);
					isSpecial=true;
					return decs;
					
					
				}//VarDecl
				else
				{
					currentNext=current;
					current=new Token(Kind.TOKEN_ID,linenum,id);
					isSpecial=true;
					decs.add(parseVarDecl());
					
				}
			}
				
			
				
			
		}
		return decs;
	}

	// FormalList -> Type id FormalRest*
	// ->
	// FormalRest -> , Type id
	private LinkedList<Dec.T> parseFormalList() {
		LinkedList<Dec.T> formals=new LinkedList<Dec.T>();
		Type.T type;
		String id;
		if (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			type=parseType();
			id=current.lexeme;
			eatToken(Kind.TOKEN_ID);
			formals.add(new DecSingle(type,id));
			while (current.kind == Kind.TOKEN_COMMER) {
				advance();
				type=parseType();
				id=current.lexeme;
				eatToken(Kind.TOKEN_ID);
				formals.add(new DecSingle(type,id));
			}
		}
		return formals;
	}

	// Method -> public Type id ( FormalList )
	// { VarDecl* Statement* return Exp ;}
	private MethodSingle parseMethod() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a method.
		// new util.Todo();
		
		Type.T reType;
		String id;
		LinkedList<Dec.T> formals;
		LinkedList<Dec.T> locals;
		LinkedList<Stm.T> stms;
		Exp.T retExp;
		
		eatToken(Kind.TOKEN_PUBLIC);
		reType=parseType();
		id=current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LPAREN);
		formals=parseFormalList();
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);

		locals=parseVarDecls();
		stms=parseStatements();
		
		eatToken(Kind.TOKEN_RETURN);
		retExp=parseExp();
		eatToken(Kind.TOKEN_SEMI);

		eatToken(Kind.TOKEN_RBRACE);

		return new MethodSingle(reType,id,formals,locals,stms,retExp);
	}

	// MethodDecls -> MethodDecl MethodDecls
	// ->
	private LinkedList<Method.T> parseMethodDecls() {
		LinkedList<Method.T> methods=new LinkedList<ast.Ast.Method.T>();
		while (current.kind == Kind.TOKEN_PUBLIC) {
			methods.add(parseMethod());
		}
		return methods;
	}

	// ClassDecl -> class id { VarDecl* MethodDecl* }
	// -> class id extends id { VarDecl* MethodDecl* }
	private ClassSingle parseClassDecl() {
		String id;
		String extendss=null;
		LinkedList<Dec.T> decs=new LinkedList<ast.Ast.Dec.T>();
		LinkedList<Method.T> methods=new LinkedList<ast.Ast.Method.T>();
		eatToken(Kind.TOKEN_CLASS);
		id=current.lexeme;
		eatToken(Kind.TOKEN_ID);
		if (current.kind == Kind.TOKEN_EXTENDS) {
			extendss="extends";
			eatToken(Kind.TOKEN_EXTENDS);
			eatToken(Kind.TOKEN_ID);
		}
		eatToken(Kind.TOKEN_LBRACE);
		decs=parseVarDecls();
		methods=parseMethodDecls();
		eatToken(Kind.TOKEN_RBRACE);
		return  new ClassSingle(id,extendss,decs,methods);
	}

	// ClassDecls -> ClassDecl ClassDecls
	// ->
	private LinkedList<Class.T> parseClassDecls() { 
		LinkedList<Class.T> classed=new LinkedList<Class.T>();
		while (current.kind == Kind.TOKEN_CLASS) {
			classed.add(parseClassDecl());
		}
		return classed;
	}

	// MainClass -> class id
	// {
	// public static void main ( String [] id )
	// {
	// Statement
	// }
	// }
	private MainClassSingle parseMainClass() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a main class as described by the
		// grammar above.

		// main   String id, String arg, LinkedList<Stm.T>  stm
		String id;
		String arg;
		LinkedList<Stm.T> stm;
		
		eatToken(Kind.TOKEN_CLASS);
		id=current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LBRACE);
		eatToken(Kind.TOKEN_PUBLIC);
		eatToken(Kind.TOKEN_STATIC);
		eatToken(Kind.TOKEN_VOID);
		eatToken(Kind.TOKEN_MAIN);
		eatToken(Kind.TOKEN_LPAREN);
		eatToken(Kind.TOKEN_STRING);
		eatToken(Kind.TOKEN_LBRACK);
		eatToken(Kind.TOKEN_RBRACK);
		arg=current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);
		stm=parseStatements();
		eatToken(Kind.TOKEN_RBRACE);
		

		// new util.Todo();
		return new MainClassSingle(id,arg,stm) ;

	}

	// Program -> MainClass ClassDecl*
	private ProgramSingle parseProgram() {
		//MainClass.T mainClass, LinkedList<Class.T> classes
		MainClassSingle mainClass;
		LinkedList<Class.T> classed;
		
		mainClass=parseMainClass();
		
		eatToken(Kind.TOKEN_RBRACE);

		classed=parseClassDecls();
		eatToken(Kind.TOKEN_EOF);
		return new ProgramSingle(mainClass,classed);
	}

	public ast.Ast.Program.T parse() {
		Program.T s=
		parseProgram();
		return s;
	}
}
