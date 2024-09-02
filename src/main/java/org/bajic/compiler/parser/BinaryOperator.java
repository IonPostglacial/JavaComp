package org.bajic.compiler.parser;

import java.util.Set;

public enum BinaryOperator
{
    Eq, Ne, Lt, Gt, Lte, Gte, 
    Cat, Add, Sub, Mul, Div, Pow,
    In, Or, OrElse, And, AndAlso, BitOr, BitAnd,
    Invalid;

    private static final Set<String> _ops = Set.of(
        "=", "<>", "<", ">", "<=", ">=", 
        "&", "+", "-", "*", "/", "^",
        "in", "or", "orelse", "and", "andalso", "bitor", "bitand"
    );

    public static boolean IsOperator(String tok)
    {
        for (String op : _ops)
        {
            if (op.equalsIgnoreCase(tok))
            {
                return true;
            }
        }
        return false;
    }
        
    public static BinaryOperator OperatorFromText(String tok)
    {
        int i = 0;
        for (var op : _ops) {
            if (op.equalsIgnoreCase(tok)) {
                return BinaryOperator.values()[i];
            }
            i++;
        }
        return BinaryOperator.Invalid;
    }

    public BindingPower BindingPower(BinaryOperator op)
    {
        return switch(op)
        {
            case BinaryOperator.In -> new BindingPower(1, 2);
            case BinaryOperator.Or, BinaryOperator.OrElse -> new BindingPower(3, 4);
            case BinaryOperator.And, BinaryOperator.AndAlso -> new BindingPower(5, 6);
            case BinaryOperator.BitOr -> new BindingPower(7, 8);
            case BinaryOperator.BitAnd -> new BindingPower(9, 10);
            case BinaryOperator.Lt, BinaryOperator.Gt, BinaryOperator.Lte, BinaryOperator.Gte -> new BindingPower(11, 12);
            case BinaryOperator.Eq, BinaryOperator.Ne -> new BindingPower(14, 13);
            case BinaryOperator.Cat, BinaryOperator.Add, BinaryOperator.Sub -> new BindingPower(15, 16);
            case BinaryOperator.Mul, BinaryOperator.Div -> new BindingPower(17, 18);
            case BinaryOperator.Pow -> new BindingPower(19, 20);
            default -> new BindingPower(0, 0);
        };
    }
}