package com.google.appinventor.components.runtime.util;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;

import android.util.Log;
import gnu.expr.ModuleMethod;
import gnu.lists.LList;
import gnu.mapping.Procedure;
import gnu.mapping.SimpleSymbol;

public final class AnonymousProcedure {

    public static interface Executable {
        public Object execute(Object... args);
        public int numArgs();
    }

    // static methods are called in runtime.scm
    public static final AnonymousProcedure create(ModuleMethod method) {
        return new AnonymousProcedure(method);
    }
    public static final AnonymousProcedure create(String procedureName) {
        try {
            Class.forName("com.google.youngandroid.runtime")
                .getMethod("setThisForm")
                .invoke(null);
            return new AnonymousProcedure(
                (Procedure) Class.forName("com.google.youngandroid.runtime")
                    .getMethod("lookupGlobalVarInCurrentFormEnvironment", gnu.mapping.Symbol.class, Object.class)
                    .invoke(
                        /* invoking instance (null for static method) */ null,
                        /* global var name */ new SimpleSymbol("p$" + procedureName).readResolve(),
                        /* default value */ null));
        } catch (ClassNotFoundException | NoSuchMethodException |
                 IllegalAccessException | InvocationTargetException impossible) {
            // impossible with static & specified class.methods
            Log.wtf("AnonymousProcedure", impossible);
            throw new RuntimeException("Cannot read global procedure \"" + procedureName + "\" (internal error)",
                                       impossible);
        } catch (ObjectStreamException e) {
            throw new RuntimeException("Cannot read global procedure \"" + procedureName + "\"", e);
        }
    }
    public static final Object callProcedure(AnonymousProcedure procedure, LList args) {
        return procedure.call(args.toArray());
    }
    public static final int numArgs(AnonymousProcedure procedure) {
        return procedure.numArgs();
    }

    public static final Object RETURN_VALUE_WHEN_NULL = false;

    private final Executable executable;

    public AnonymousProcedure(Executable executable) {
        this.executable = executable;
    }
    public AnonymousProcedure(final Procedure methodToCall) {
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
     * @return never be null, any null will be replaced by {@link AnonymousProcedure.RETURN_VALUE_WHEN_NULL}
     */
    public Object call(Object... args) {
        Object returnVal = executable.execute(args);
        return returnVal == null ? RETURN_VALUE_WHEN_NULL : returnVal;
    }

    public int numArgs() {
        return executable.numArgs();
    }

}