package org.bajic.compiler.parser;

public enum TokenKind {
    Num, Str, Date,
    Sep, Comma, Dot,
    OpenParens, CloseParens, 
    OpenSquare, OpenSecondSquare, CloseSquare,
    OpenBracket, CloseBracket,
    Dim,
    As,
    New,
    Not,
    Assign,
    FatArrow,
    Operator,
    Sym,
    Err,
    Class,
    Eof,
}