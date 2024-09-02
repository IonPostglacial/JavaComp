package org.bajic.compiler.parser;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

record Token(TokenKind kind, int start, int end) {}

public class TokenizerTests
{
    @ParameterizedTest
    @CsvSource({
        "0, Num, 0, 1",
        "1, Num, 0, 1",
        "01, Num, 0, 2",
        "1.0, Num, 0, 3",
        "01.01, Num, 0, 5",

        "\"\", Str, 0, 2",
        "\" \", Str, 0, 3",
        "\"\"\"\", Str, 0, 4",

        "\"hello\", Str, 0, 7",

        "#10/30/1990#, Date, 0, 12",
        "#10/30/1990 09:42:00#, Date, 0, 21",

        "+, Operator, 0, 1",
        "<, Operator, 0, 1",
        "<>, Operator, 0, 2",
        "and, Operator, 0, 3",
        "AND, Operator, 0, 3",
        "And, Operator, 0, 3",

        "=>, FatArrow, 0, 2",
        ":=, Assign, 0, 2",
        "not, Not, 0, 3",
        "., Dot, 0, 1",
        "New, New, 0, 3",
        "class, Class, 0, 5",

        "(, OpenParens, 0, 1",
        "), CloseParens, 0, 1",
        "[, OpenSquare, 0, 1",
        "²[, OpenSecondSquare, 0, 2",
        "`[, OpenSecondSquare, 0, 2",
        "}, CloseBracket, 0, 1",

        "a, Sym, 0, 1",
        "ab, Sym, 0, 2",
        "a@, Sym, 0, 2",
        "hello@, Sym, 0, 6",
        "cruxe#, Sym, 0, 6",
        "balad$, Sym, 0, 6",
        "radar£, Sym, 0, 6",
        "bonusµ, Sym, 0, 6",
    })
    public void SingleToken(String source, TokenKind kind, int start, int end)
    {
        var tokenizer = new Tokenizer(source);
        var buffer = new TokensBuffer(0, 1);
        tokenizer.tokenize(buffer);
        Assertions.assertAll(() -> {
            Assertions.assertEquals(kind, buffer.getKind(0));
            Assertions.assertEquals(start, buffer.getStart(0));
            Assertions.assertEquals(end, buffer.getEnd(0));
        });
    }

    @ParameterizedTest
    @ArgumentsSource(MultipleTokenCases.class)
    void MultipleToken(TokenCase tc) {
        var tokenizer = new Tokenizer(tc.source());
        var buffer = new TokensBuffer(0, 32);
        tokenizer.tokenize(buffer);
        Assertions.assertEquals(tc.expected().length, buffer.length());
        for (int i = 0; i < buffer.length(); i++) {
            final int j = i;
            Assertions.assertAll(() -> {
                Assertions.assertTrue(j < tc.expected().length);
                if (j < tc.expected().length) {
                    Assertions.assertEquals(tc.expected()[j].start(), buffer.getStart(j));
                    Assertions.assertEquals(tc.expected()[j].end(), buffer.getEnd(j));
                    Assertions.assertEquals(tc.expected()[j].kind(), buffer.getKind(j));
                }
            });
            i++;
        }
    }
}


record TokenCase(String source, Token[] expected) {}

class MultipleTokenCases implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
            new TokenCase("1", new Token[] { new Token(TokenKind.Num, 0, 1)}),
            new TokenCase("-1", new Token[] { new Token(TokenKind.Operator, 0, 1), new Token(TokenKind.Num, 1, 2)}),
            new TokenCase("1 + 1", new Token[] { new Token(TokenKind.Num, 0, 1), new Token(TokenKind.Operator, 2, 3), new Token(TokenKind.Num, 4, 5)}),
            new TokenCase("1*1", new Token[] { new Token(TokenKind.Num, 0, 1), new Token(TokenKind.Operator, 1, 2), new Token(TokenKind.Num, 2, 3)}),
            new TokenCase("()=>(1)", new Token[] { 
                new Token(TokenKind.OpenParens, 0, 1), 
                new Token(TokenKind.CloseParens, 1, 2),
                new Token(TokenKind.FatArrow, 2, 4), 
                new Token(TokenKind.OpenParens, 4, 5),
                new Token(TokenKind.Num, 5, 6), 
                new Token(TokenKind.CloseParens, 6, 7)}),
            new TokenCase("hello[world]", new Token[] { 
                new Token(TokenKind.Sym, 0, 5), 
                new Token(TokenKind.OpenSquare, 5, 6),
                new Token(TokenKind.Sym, 6, 11),
                new Token(TokenKind.CloseSquare, 11, 12)
            }),
            new TokenCase("If(True,1,2)", new Token[] { 
                new Token(TokenKind.Sym, 0, 2), 
                new Token(TokenKind.OpenParens, 2, 3),
                new Token(TokenKind.Sym, 3, 7),
                new Token(TokenKind.Comma, 7, 8),
                new Token(TokenKind.Num, 8, 9),
                new Token(TokenKind.Comma, 9, 10),
                new Token(TokenKind.Num, 10, 11),
                new Token(TokenKind.CloseParens, 11, 12)
            }),
            new TokenCase("mama#(world)", new Token[] { 
                new Token(TokenKind.Sym, 0, 5), 
                new Token(TokenKind.OpenParens, 5, 6),
                new Token(TokenKind.Sym, 6, 11),
                new Token(TokenKind.CloseParens, 11, 12)
            }),
            new TokenCase("papa²[world]", new Token[] { 
                new Token(TokenKind.Sym, 0, 4), 
                new Token(TokenKind.OpenSecondSquare, 4, 6),
                new Token(TokenKind.Sym, 6, 11),
                new Token(TokenKind.CloseSquare, 11, 12)
            }),
            new TokenCase("If(0,1:2,3:4):5", new Token[] { 
                new Token(TokenKind.Sym, 0, 2), 
                new Token(TokenKind.OpenParens, 2, 3),
                new Token(TokenKind.Num, 3, 4),
                new Token(TokenKind.Comma, 4, 5),
                new Token(TokenKind.Num, 5, 6),
                new Token(TokenKind.Sep, 6, 7),
                new Token(TokenKind.Num, 7, 8),
                new Token(TokenKind.Comma, 8, 9),
                new Token(TokenKind.Num, 9, 10),
                new Token(TokenKind.Sep, 10, 11),
                new Token(TokenKind.Num, 11, 12),
                new Token(TokenKind.CloseParens, 12, 13),
                new Token(TokenKind.Sep, 13, 14),
                new Token(TokenKind.Num, 14, 15)
            }),
            new TokenCase("T{1}", new Token[] { 
                new Token(TokenKind.Sym, 0, 1),
                new Token(TokenKind.OpenBracket, 1, 2),
                new Token(TokenKind.Num, 2, 3),
                new Token(TokenKind.CloseBracket, 3, 4)
            }),
            new TokenCase("T.Count{U>3}", new Token[] { 
                new Token(TokenKind.Sym, 0, 1),
                new Token(TokenKind.Dot, 1, 2),
                new Token(TokenKind.Sym, 2, 7),
                new Token(TokenKind.OpenBracket, 7, 8),
                new Token(TokenKind.Sym, 8, 9),
                new Token(TokenKind.Operator, 9, 10),
                new Token(TokenKind.Num, 10, 11),
                new Token(TokenKind.CloseBracket, 11, 12)
            })
        ).map(Arguments::of);
    }
}