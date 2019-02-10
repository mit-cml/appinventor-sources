package com.google.appinventor.components.runtime.util;

import gnu.expr.ModuleMethod;
import gnu.lists.LList;
import gnu.lists.Pair;

public class AnonymousProcedure {

    public static interface Executable {
        public Object execute(Object... args);
    }

    // static methods are called in runtime.scm
    public static final AnonymousProcedure create(ModuleMethod method) {
        return new AnonymousProcedure(method);
    }
    public static final Object callProcedure(AnonymousProcedure procedure, Pair args) {
        return callProcedure(procedure, (LList) args);
    }
    public static final Object callProcedure(AnonymousProcedure procedure, LList args) {
        return procedure.call(args.toArray());
    }

    private final Executable executable;

    public AnonymousProcedure(Executable executable) {
        this.executable = executable;
    }
    public AnonymousProcedure(final ModuleMethod methodToCall) {
        this(new Executable(){
            @Override
            public Object execute(Object... args) {
                try {
                    return methodToCall.applyN(args);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        });
    }

    public Object call(Object... args) {
        return executable.execute(args);
    }

}