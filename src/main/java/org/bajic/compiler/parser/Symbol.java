package org.bajic.compiler.parser;

import java.util.Objects;
import java.util.Set;

public record Symbol(SymKind Kind, String Path, String Name)
{
    private static final Set<Character> sigils = Set.of('@', '$', '#', '%', '£', 'μ');

    public boolean IsGlobalBuiltIn() { return Kind == SymKind.Builtin && Path.isEmpty(); }

    @Override
    public boolean equals(Object symbol) {
        if (symbol instanceof Symbol sym)
        {
            return
                Kind == sym.Kind && 
                Path.equalsIgnoreCase(sym.Path) && 
                Name.equalsIgnoreCase(sym.Name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            Kind, 
            Path.toLowerCase(), 
            Name.toLowerCase()
        );
    }

    private static SymKind SymKindFromSigil(char sigil)
    {
        return switch(sigil) {
            case '@' -> SymKind.Local;
            case '$' -> SymKind.PageScope;
            case '#' -> SymKind.Constant;
            case '%' -> SymKind.Context;
            case '£' -> SymKind.Family;
            case 'μ' -> SymKind.Global;
            default -> SymKind.Builtin;
        };
    }

    public static Symbol FromText(String sym)
    {
        SymKind kind = SymKind.Builtin;
        char sigil = sym.charAt(sym.length()-1);

        if (sigils.contains(sigil))
        {
            kind = SymKindFromSigil(sigil);
            sym = sym.substring(sym.length()-1);
        }
        String[] pathSym = sym.split("!");
        if (pathSym.length == 2)
            return new Symbol(kind, pathSym[0], pathSym[1]);
        else
            return new Symbol(kind, "", sym);
    }

    public static Symbol BuiltIn(String name)
    {
        return new Symbol(SymKind.Builtin, "", name);
    }

    public static Symbol Local(String name)
    {
        return new Symbol(SymKind.Local, "", name);
    }

    public static Symbol Context(String name)
    {
        return new Symbol(SymKind.Context, "", name);
    }
}