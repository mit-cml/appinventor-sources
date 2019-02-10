package com.google.appinventor.components.runtime.util;

import gnu.expr.ModuleMethod;
import gnu.lists.LList;
import gnu.lists.Pair;

public final class AnonymousProcedure {

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

    public static final Object VALUE_WHEN_NULL = false;

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

    /**
     * @param args
     * @return never be null, any null will be replaced by {@link AnonymousProcedure.VALUE_WHEN_NULL}
     */
    public Object call(Object... args) {
        Object returnVal = executable.execute(args);
        return returnVal == null ? VALUE_WHEN_NULL : returnVal;
    }

    public int numArgs() {
        return executable.numArgs();
    }

}