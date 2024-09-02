package org.bajic;

import org.bajic.compiler.parser.Tokenizer;
import org.bajic.compiler.parser.TokensBuffer;

public class Main {
    public static void main(String[] args) {
        var tokenizer = new Tokenizer("#10/30/1990#");
        var tokens = new TokensBuffer(0, 256);
        tokenizer.tokenize(tokens);
        for (int i = 0; i < tokens.length(); i++) {
            System.out.println("start: " + tokens.getStart(i) + ", end: " + tokens.getEnd(i) + ", kind: " + tokens.getKind(i));
        }
    }
}