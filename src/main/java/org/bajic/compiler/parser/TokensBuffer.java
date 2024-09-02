package org.bajic.compiler.parser;

import java.util.Arrays;

public class TokensBuffer {
    private int _length = 0;
    private int[] _positions;

    public TokensBuffer(int length, int capacity) {
        _length = length;
        _positions = new int[capacity * 3];
    }

    public int getStart(int i) {
        return _positions[i * 3];
    }

    public int getEnd(int i) {
        return _positions[i * 3 + 1];
    }
    
    public TokenKind getKind(int i) {
        return TokenKind.values()[_positions[i * 3 + 2]];
    }

    public int length() {
        return _length;
    }

    public void push(int start, int end, TokenKind kind) {
        if (_length * 3 >= _positions.length) {
            _positions = Arrays.copyOf(_positions, _positions.length * 2);
        }
        _positions[_length * 3] = start;
        _positions[_length * 3 + 1] = end;
        _positions[_length * 3 + 2] = kind.ordinal();
        _length++;
    }
}