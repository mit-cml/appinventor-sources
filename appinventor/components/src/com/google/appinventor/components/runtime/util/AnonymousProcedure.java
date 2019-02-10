package com.google.appinventor.components.runtime.util;

import gnu.expr.ModuleMethod;
import gnu.lists.LList;
import gnu.lists.Pair;

public class AnonymousProcedure {

    public static interface Executable {
        public Object execute(Object... args);
        public int numArgs();
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
    public static final int numArgs(AnonymousProcedure procedure) {
        return procedure.numArgs();
    }

    private final Executable executable;

    public AnonymousProcedure(Executable executable) {
        this.executable = executable;
    }
    public AnonymousProcedure(final ModuleMethod methodToCall) {
        this(new Executable(){
            @Override
            public Object execute(Object... args) {
                if (args.length < numArgs()) {
                    throw new RuntimeException("Unable to call anonymousProcedure: not enough arguments: require "
                        + numArgs() + ", get " + args.length);
                } else if (args.length > numArgs()) {
                    throw new RuntimeException("Unable to call anonymousProcedure: too many arguments: require "
                        + numArgs() + ", get " + args.length);
                }
                try {
                    return methodToCall.applyN(args);
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to call anonymousProcedure", t);
                }
            }
            @Override
            public int numArgs() {
                // returning minArgs because the numArgs of ModuleMethod = minArgs | maxArgs<<12,
                // and sometimes maxArgs may be -1 for unlimited
                return methodToCall.minArgs();
            }
        });
    }

    public Object call(Object... args) {
        return executable.execute(args);
    }

    public int numArgs() {
        return executable.numArgs();
    }

}