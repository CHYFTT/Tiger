package lexer;

import java.util.HashMap;

import lexer.Token.Kind;

public class TokenMap {
	
	public HashMap<String,Kind> tokenMap; 
	
	
	public TokenMap()
	{
		tokenMap=new HashMap<String,Kind>();
		
		tokenMap.put("+", Kind.TOKEN_ADD);
		tokenMap.put("&&", Kind.TOKEN_AND);
		tokenMap.put("=", Kind.TOKEN_ASSIGN);
		tokenMap.put("boolean", Kind.TOKEN_BOOLEAN);
		tokenMap.put("class", Kind.TOKEN_CLASS);
		tokenMap.put(",", Kind.TOKEN_COMMER);
		tokenMap.put(".", Kind.TOKEN_DOT);
		tokenMap.put("else", Kind.TOKEN_ELSE);
		tokenMap.put("EOF", Kind.TOKEN_EOF);
		tokenMap.put("extends", Kind.TOKEN_EXTENDS);
		tokenMap.put("false", Kind.TOKEN_FALSE);
		//id
		tokenMap.put("if", Kind.TOKEN_IF);
		tokenMap.put("int", Kind.TOKEN_INT);
		tokenMap.put("{", Kind.TOKEN_LBRACE);
		tokenMap.put("[", Kind.TOKEN_LBRACK);
		tokenMap.put("length", Kind.TOKEN_LENGTH);
		tokenMap.put("(", Kind.TOKEN_LPAREN);
		tokenMap.put("<", Kind.TOKEN_LT);
		tokenMap.put("main", Kind.TOKEN_MAIN);
		tokenMap.put("new", Kind.TOKEN_NEW);
		tokenMap.put("!", Kind.TOKEN_NOT);
		//number
		tokenMap.put("out", Kind.TOKEN_OUT);
		tokenMap.put("println", Kind.TOKEN_PRINTLN);
		tokenMap.put("public", Kind.TOKEN_PUBLIC);
		tokenMap.put("}", Kind.TOKEN_RBRACE);
		tokenMap.put("]", Kind.TOKEN_RBRACK);
		tokenMap.put("return", Kind.TOKEN_RETURN);
		tokenMap.put(")", Kind.TOKEN_RPAREN);
		tokenMap.put(";", Kind.TOKEN_SEMI);
		tokenMap.put("static", Kind.TOKEN_STATIC);
		tokenMap.put("String", Kind.TOKEN_STRING);
		tokenMap.put("-", Kind.TOKEN_SUB);
		tokenMap.put("System", Kind.TOKEN_SYSTEM);
		tokenMap.put("this", Kind.TOKEN_THIS);
		tokenMap.put("*", Kind.TOKEN_TIMES);
		tokenMap.put("true", Kind.TOKEN_TRUE);
		tokenMap.put("void", Kind.TOKEN_VOID);
		tokenMap.put("while", Kind.TOKEN_WHILE);
	}
	
	public Kind getKind(String s)
	{
		Kind k=tokenMap.get(s);
		return k;
	}

}
