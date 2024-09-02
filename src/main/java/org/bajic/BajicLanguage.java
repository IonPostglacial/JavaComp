package org.bajic;

import org.bajic.runtime.BajicContext;

import com.oracle.truffle.api.TruffleLanguage;

public class BajicLanguage extends TruffleLanguage<BajicContext> {

    @Override
    protected BajicContext createContext(Env env) {
        return new BajicContext();
    }

}