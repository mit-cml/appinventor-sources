package com.google.appinventor.components.runtime.util;

import gnu.expr.ModuleMethod;
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
        Object[] argsObj = new Object[args.length()];
        for (int i=0; i<args.length(); i++) {
            argsObj[i] = args.get(i);
        }
        return procedure.call(argsObj);
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