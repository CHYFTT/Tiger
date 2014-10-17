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

	public Parser(String fname, java.io.PushbackInputStream f) {
		lexer = new Lexer(fname, f);
		current = lexer.nextToken();
	}

	// /////////////////////////////////////////////
	// utility methods to connect the lexer
	// and the parser.

	private void advance() // advance() can get the nextToken
	{
		System.out.println(current.kind.toString() + "  "+current.lexeme+"   " + current.lineNum);
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
		System.out.println("Syntax error: compilation aborting...\n");
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
			return new Exp.Num(i);
		case TOKEN_TRUE:
			advance();
			return new Exp.True();
		case TOKEN_FALSE:
			advance();
			return new Exp.False();
		case TOKEN_THIS:
			advance();
			return new Exp.This();
		case TOKEN_ID:
			s=current.lexeme;
			advance();
			return new Id(s);
		case TOKEN_NEW: {
			advance();
			switch (current.kind) {
			case TOKEN_INT:
				advance();
				eatToken(Kind.TOKEN_LBRACK);
				exp=parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new Exp.NewIntArray(exp);
			case TOKEN_ID:
				s=current.lexeme;
				advance();
				eatToken(Kind.TOKEN_LPAREN);
				eatToken(Kind.TOKEN_RPAREN);
				return new Id(s);
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
					return new Exp.Length(exp);
				}
				//else Call
				String s=current.lexeme;
				eatToken(Kind.TOKEN_ID);
				eatToken(Kind.TOKEN_LPAREN);
				LinkedList<T> args=parseExpList();
				eatToken(Kind.TOKEN_RPAREN);
				return new Call(exp,s,args);
			} else {
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
		while (current.kind == Kind.TOKEN_NOT) {
			advance();
			return new Exp.Not(exp);
		}
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
			return new Exp.Times(left, right);
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
			return new Exp.Add(left, right);
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
			return new Exp.Lt(left, right);
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
			return new Exp.And(left, right);
		}
		return left;//new Exp.Lt(new Exp.Id("num"), new Exp.Num(1));
	}

	// Statement -> { Statement* }
	// -> if ( Exp ) Statement else Statement
	// -> while ( Exp ) Statement
	// -> System.out.println ( Exp ) ;
	// -> id = Exp ;
	// -> id [ Exp ]= Exp ;
	private LinkedList<Stm.T> parseStatement() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a statement.
		// new util.Todo();
		Exp.T exp;
		LinkedList<Stm.T> stms=new LinkedList<Stm.T>();
		switch (current.kind) {
		
		case TOKEN_LBRACE:
			eatToken(Kind.TOKEN_LBRACE);
			stms=parseStatements();
			eatToken(Kind.TOKEN_RBRACE);
			return stms;
		case TOKEN_IF:
			// Exp.T condition; LinkedList<Stm.T> thenn; LinkedList<Stm.T> elsee;
			Exp.T condition;
			LinkedList<Stm.T> thenn;
			LinkedList<Stm.T> elsee;
			eatToken(Kind.TOKEN_IF);
			eatToken(Kind.TOKEN_LPAREN);// the eatToken() can check the token
										// and
			condition=parseExp(); // then get the next token automatically
			eatToken(Kind.TOKEN_RPAREN);
			thenn=parseStatements();
			advance();
			if(current.kind==Kind.TOKEN_ELSE)
			{
				eatToken(Kind.TOKEN_ELSE);
				elsee=parseStatements();
			}
			else
			{
				elsee=null;
			}
			
			stms.add(new If(condition, thenn, elsee));
			return stms;
			

		case TOKEN_WHILE:
			LinkedList<Stm.T> body=new LinkedList<Stm.T>();
			eatToken(Kind.TOKEN_WHILE);
			eatToken(Kind.TOKEN_LPAREN);
			exp=parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			body=parseStatements();
			stms.add(new Stm.While(exp,body));

			return stms;
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
			stms.add(new Print(exp));
			return stms;
		case TOKEN_ID:
			String id=current.lexeme;
			eatToken(Kind.TOKEN_ID);
			switch (current.kind) {
			case TOKEN_ASSIGN:
				eatToken(Kind.TOKEN_ASSIGN);
				exp=parseExp();
				eatToken(Kind.TOKEN_SEMI);
				stms.add(new Stm.Assign(id, exp));
				return stms;
			case TOKEN_LBRACK:
				eatToken(Kind.TOKEN_LBRACK);
				parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				eatToken(Kind.TOKEN_ASSIGN);
				parseExp();
				eatToken(Kind.TOKEN_SEMI);
				break;
			default:
				error();
				return null;

			}
			break;
		
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
				|| current.kind ==Kind.TOKEN_ELSE
				|| current.kind == Kind.TOKEN_WHILE
				|| current.kind == Kind.TOKEN_SYSTEM
				|| current.kind == Kind.TOKEN_ID) {// make return as the
														// terminal of the
														// statement
			stm.addAll(parseStatement());
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

		Type.T type=parseType();// reference to the return Exp
		id=current.lexeme;
		dec=new DecSingle(type,id);
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_SEMI);
		return dec;
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
			{
				eatToken(Kind.TOKEN_ID);//statement 
				if(current.kind==Kind.TOKEN_ASSIGN)
				{
					eatToken(Kind.TOKEN_ASSIGN);
					parseExp();
					eatToken(Kind.TOKEN_SEMI);
					
				}//statement
				else if(current.kind==Kind.TOKEN_LBRACK)
				{
					eatToken(Kind.TOKEN_LBRACK);
					parseExp();
					eatToken(Kind.TOKEN_RBRACK);
					eatToken(Kind.TOKEN_ASSIGN);
					parseExp();
					eatToken(Kind.TOKEN_SEMI);
					
				}//VarDecl
				else
				{
					eatToken(Kind.TOKEN_ID);
					eatToken(Kind.TOKEN_SEMI);
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
		eatToken(Kind.TOKEN_PUBLIC);// 每一次执行完eatToken()current都会改变
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
