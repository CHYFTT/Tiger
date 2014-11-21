package lexer;

import static control.Control.ConLexer.dump;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import lexer.Token.Kind;

public class Lexer {
	String fname; // the input file name to be compiled
	PushbackInputStream fstream; // input stream for the above file

	public String s = "";
	int linenum = 1;
	Token behind = null;
	Token cmp = null;
	int ex = 0;

	public Lexer(String fname, PushbackInputStream fstream) {
		this.fname = fname;
		this.fstream = fstream;
	}

	// When called, return the next token (refer to the code "Token.java")
	// from the input stream.
	// Return TOKEN_EOF when reaching the end of the input stream.
	private Token nextTokenInternal() throws Exception {

		int c = this.fstream.read();

		if (-1 == c)

			// The value for "lineNum" is now "null",
			// you should modify this to an appropriate
			// line number for the "EOF" token.
			return new Token(Kind.TOKEN_EOF, linenum);

		// skip all kinds of "blanks"

		while ('\t' == c || '\n' == c) {
			if ('\n' == c)
				linenum++;
			c = this.fstream.read();
		}

		if (-1 == c)
			return new Token(Kind.TOKEN_EOF, linenum);

		// analysis the Comments
		if ('/' == c) {
			ex = c;
			c = this.fstream.read();
			isComments(c, ex);
			return null;
		}

		switch (c) {
		case ' ': {
			return printTokenforspace(c);

		}
		case '+': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_ADD, linenum);
			else
				return behind;
		}
		case '&': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return null;
			return behind;

		}
		case '=': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_ASSIGN, linenum);
			else
				return behind;
		}
		case ',': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_COMMER, linenum);
			else
				return behind;
		}
		case '.': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_DOT, linenum);
			else
				return behind;
		}
		case '{': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_LBRACE, linenum);
			else
				return behind;
		}
		case '[': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_LBRACK, linenum);
			else
				return behind;
		}
		case '(': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_LPAREN, linenum);
			else
				return behind;
		}
		case '<': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_LT, linenum);
			else
				return behind;
		}
		case '!': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_NOT, linenum);
			else
				return behind;
		}
		case '}': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_RBRACE, linenum);
			else
				return behind;
		}
		case ']': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_RBRACK, linenum);
			else
				return behind;
		}
		case ')': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_RPAREN, linenum);
			else
				return behind;
		}
		case ';': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_SEMI, linenum);
			else
				return behind;
		}
		case '-': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_SUB, linenum);
			else
				return behind;
		}
		case '*': {
			cmp = printTokenforspace(c);
			if (cmp == null)
				return new Token(Kind.TOKEN_TIMES, linenum);
			else
				return behind;
		}

		default:
			// Lab 1, exercise 2: supply missing code to
			// lex other kinds of tokens.
			// Hint: think carefully about the basic
			// data structure and algorithms. The code
			// is not that much and may be less than 50 lines. If you
			// find you are writing a lot of code, you
			// are on the wrong way.

			// new Todo();
			s += (char) (c);
			// System.out.println(s);
			return nextTokenInternal();
		}
	}

	public void isComments(int c, int ex) throws IOException {
		if (c == '/' || c == '*') {
			if (c == '/') {
				while (c != '\n')
					c = this.fstream.read();
				linenum++;

			} else {// confirm comment
				ex = this.fstream.read();
				while (c != '*' || ex != '/') {
					c = ex;
					ex = this.fstream.read();
				}
			}
		}
		// the else is well down
		else {
			new util.Bug();
		}
	}

	public Token printTokenforspace(int c) throws IOException {
		if (c == 38 && s == "&") {
			behind = new Token(Kind.TOKEN_AND, linenum);
			s = "";
		} else if (s != "") {
			Token token = new Token();
			Kind k = token.getkey(s);
			
			behind = new Token(k, linenum,s);
			
			if (c == 38)
				s = "&";
			else
				s = "";
			if (c != 32 && c != 38)
				fstream.unread(c);
			return behind;

		} else if (s == "" && c == 38) {
			s = "&";
			behind = null;
		} else
			return null;
		return behind;

	}

	public Token nextToken() {
		Token t = null;

		try {
			while (t == null)
				t = this.nextTokenInternal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (dump)
			System.out.println(t.toString());
		return t;
	}

	
	
}
