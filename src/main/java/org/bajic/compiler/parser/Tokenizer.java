package org.bajic.compiler.parser;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private final String _src;

    public Tokenizer(String source) {
        _src = source;
    }

    private static final Set<Character> SEPARATORS = Set.of(':', ',', '(', ')', '[', ']', '{', '}', ' ', '\t', '\r', '\n',
            '=', '<', '>', '&', '+', '-', '*', '/', '.', '²', '`');
    private static final Set<Character> OPERATORS = Set.of(':', ',', '(', ')', '[', ']', '{', '}', '=', '<', '>', '&', '+', '-', '*', '/', '^', '.', '²', '`');
    private static final Set<String> TWO_CHAR_OPERATORS = Set.of(":=", "<=", ">=", "=>", "`[", "²[", "<>");
    private static final Pattern DATE_REGEX = Pattern.compile("(\\d{2})\\/(\\d{2})\\/(\\d{4})(?:\\s(\\d{2}):(\\d{2}):(\\d{2}))?");

    private int _pos = 0;

    private boolean hasNextChar() {
        return _pos < _src.length() - 1;
    }

    private char currentChar() {
        return _src.charAt(_pos);
    }

    private char nextChar() {
        return _src.charAt(_pos + 1);
    }

    private void consumeChar() {
        _pos++;
    }

    private void consumeUntilChar(char c) {
        do {
            consumeChar();
        } while (currentChar() != c && hasNextChar());
    }

    private boolean consumeString(TokensBuffer buffer) {
        int tokStart = _pos;
        consumeUntilChar('"');
        while (currentChar() == '"' && hasNextChar() && nextChar() == '"') {
            consumeChar();
            consumeUntilChar('"');
        }
        consumeChar();
        int tokEnd = _pos;
        buffer.push(tokStart, tokEnd, TokenKind.Str);
        return true;
    }

    public boolean consumeSymbol(TokensBuffer buffer) {
        int tokStart = _pos;
        while (_pos < _src.length() && !SEPARATORS.contains(currentChar())) {
            consumeChar();
        }
        int tokEnd = _pos;
        String sym = _src.substring(tokStart, tokEnd);
        TokenKind kind;
        if (BinaryOperator.IsOperator(sym)) {
            kind = TokenKind.Operator;
        } else if (sym.equalsIgnoreCase("not")) {
            kind = TokenKind.Not;
        } else if (sym.equalsIgnoreCase("dim")) {
            kind = TokenKind.Dim;
        } else if (sym.equalsIgnoreCase("as")) {
            kind = TokenKind.As;
        } else if (sym.equalsIgnoreCase("new")) {
            kind = TokenKind.New;
        } else if (sym.equalsIgnoreCase("class")) {
            kind = TokenKind.Class;
        } else {
            kind = TokenKind.Sym;
        }
        buffer.push(tokStart, tokEnd, kind);
        return true;
    }

    private static boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean consumeNumber(TokensBuffer buffer) {
        int tokStart = _pos;
        while (hasNextChar() && (nextChar() == '.' || isNumeric(nextChar()))) {
            consumeChar();
        }
        consumeChar();
        int tokEnd = _pos;
        buffer.push(tokStart, tokEnd, TokenKind.Num);
        return true;
    }

    public boolean consumeDate(TokensBuffer buffer) {
        int tokStart = _pos;
        consumeUntilChar('#');
        int tokEnd = _pos;
        Matcher matcher = DATE_REGEX.matcher(_src.substring(tokStart + 1, tokEnd));
        TokenKind kind;
        if (matcher.groupCount() == 0) {
            kind = TokenKind.Err;
        } else {
            kind = TokenKind.Date;
        }
        consumeChar();
        buffer.push(tokStart, tokEnd + 1, kind);
        return true;
    }

    private boolean consumeOperator(TokensBuffer buffer) {
        int tokStart = _pos;
        int tokEnd = _pos + 1;
        if (tokStart + 2 <= _src.length() && TWO_CHAR_OPERATORS.contains(_src.substring(tokStart, tokStart + 2))) {
            consumeChar();
            tokEnd++;
        }
        String op = _src.substring(tokStart, tokEnd);
        TokenKind kind;
        if (BinaryOperator.IsOperator(op)) {
            kind = TokenKind.Operator;
        } else if (op.equals("(")) {
            kind = TokenKind.OpenParens;
        } else if (op.equals(")")) {
            kind = TokenKind.CloseParens;
        } else if (op.equals("[")) {
            kind = TokenKind.OpenSquare;
        } else if (op.equals("²[") || op.equals("`[")) {
            kind = TokenKind.OpenSecondSquare;
        } else if (op.equals("]")) {
            kind = TokenKind.CloseSquare;
        } else if (op.equals("{")) {
            kind = TokenKind.OpenBracket;
        } else if (op.equals("}")) {
            kind = TokenKind.CloseBracket;
        } else if (op.equals(",")) {
            kind = TokenKind.Comma;
        } else if (op.equals(".")) {
            kind = TokenKind.Dot;
        } else if (op.equals(":")) {
            kind = TokenKind.Sep;
        } else if (op.equals(":=")) {
            kind = TokenKind.Assign;
        } else if (op.equals("=>")) {
            kind = TokenKind.FatArrow;
        } else {
            kind = TokenKind.Operator;
        }
        consumeChar();
        buffer.push(tokStart, tokEnd, kind);
        return true;
    }

    private boolean next(TokensBuffer buffer) {
        while (_pos < _src.length()) {
            switch (currentChar()) {
                case '\n', '\r', '\t', ' ':
                    consumeChar();
                    continue;
                case '\'':
                    consumeUntilChar('\n');
                    return next(buffer);
                case '"':
                    return consumeString(buffer);
                case '#':
                    return consumeDate(buffer);
                default:
                    if (isNumeric(currentChar())) {
                        return consumeNumber(buffer);
                    } else if (OPERATORS.contains(currentChar())) {
                        return consumeOperator(buffer);
                    } else {
                        return consumeSymbol(buffer);
                    }
            }
        }
        return false;
    }

    public void tokenize(TokensBuffer buffer) {
        while (next(buffer)) {}
    }

    public String asRawString(TokensBuffer buffer, int i) {
        return _src.substring(buffer.getStart(i), buffer.getEnd(i));
    }

    public String asString(TokensBuffer buffer, int i) {
        return _src.substring(buffer.getStart(i) + 1, buffer.getEnd(i) - 1).replace("\"\"", "\"");
    }

    public Symbol asSymbol(TokensBuffer buffer, int i) {
        return Symbol.FromText(asRawString(buffer, i));
    }

    public double asNumber(TokensBuffer buffer, int i) {
        return Double.parseDouble(asRawString(buffer, i));
    }

    public boolean tokenIsSeparator(TokensBuffer buffer, int i) {
        String tok = asRawString(buffer, i);
        return !tok.isEmpty() && SEPARATORS.contains(tok.charAt(0));
    }

    public BinaryOperator tokenToBinaryOperator(TokensBuffer buffer, int i) {
        return BinaryOperator.OperatorFromText(asRawString(buffer, i));
    }

    public DateValue asDate(TokensBuffer buffer, int i) {
        Matcher matcher = DATE_REGEX.matcher(asRawString(buffer, i));
        Time time = null;
        if (matcher.groupCount() == 7) {
            time = new Time(
                Integer.parseInt(matcher.group(4)),
                Integer.parseInt(matcher.group(5)),
                Integer.parseInt(matcher.group(6))
            );
        }
        return new DateValue(
            Integer.parseInt(matcher.group(3)),
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            time
        );
    }
}
