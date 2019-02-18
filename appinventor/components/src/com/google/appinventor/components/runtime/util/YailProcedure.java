package com.google.appinventor.components.runtime.util;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;

import android.util.Log;
import gnu.expr.ModuleMethod;
import gnu.lists.LList;
import gnu.mapping.Procedure;
import gnu.mapping.SimpleSymbol;
import gnu.mapping.Values;

public final class YailProcedure {

    public static interface Executable {
        public Object execute(Object... args);
        public int numArgs();
    }

    // static methods are called in runtime.scm
    public static final YailProcedure create(ModuleMethod method) {
        return new YailProcedure(method);
    }
    public static final YailProcedure create(String procedureName) {
        try {
            Class.forName("com.google.youngandroid.runtime")
                .getMethod("setThisForm")
                .invoke(null);
            return new YailProcedure(
                (Procedure) Class.forName("com.google.youngandroid.runtime")
                    .getMethod("lookupGlobalVarInCurrentFormEnvironment", gnu.mapping.Symbol.class, Object.class)
                    .invoke(
                        /* invoking instance (null for static method) */ null,
                        /* global var name */ new SimpleSymbol("p$" + procedureName).readResolve(),
                        /* default value */ null));
        } catch (ClassNotFoundException | NoSuchMethodException |
                 IllegalAccessException | InvocationTargetException impossible) {
            // impossible with static & specified class.methods
            Log.wtf("YailProcedure", impossible);
            throw new RuntimeException("Cannot read global procedure \"" + procedureName + "\" (internal error)",
                                       impossible);
        } catch (ObjectStreamException e) {
            throw new RuntimeException("Cannot read global procedure \"" + procedureName + "\"", e);
        }
    }
    public static final Object callProcedure(YailProcedure procedure, LList args) {
        return procedure.call(args.toArray());
    }
    public static final int numArgs(YailProcedure procedure) {
        return procedure.numArgs();
    }

    public static final Object RETURN_VALUE_WHEN_NULL = false;

    private final Executable executable;

    public YailProcedure(Executable executable) {
        this.executable = executable;
    }
    public YailProcedure(final Procedure methodToCall) {
        this(new Executable(){
            @Override
            public Object execute(Object... args) {
                if (args.length < numArgs()) {
                    throw new RuntimeException("Unable to call YailProcedure: not enough arguments: require "
                        + numArgs() + ", get " + args.length);
                } else if (args.length > numArgs()) {
                    throw new RuntimeException("Unable to call YailProcedure: too many arguments: require "
                        + numArgs() + ", get " + args.length);
                }
                try {
                    return methodToCall.applyN(args);
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to call YailProcedure", t);
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
     * @return never be null, any null will be replaced by {@link YailProcedure.RETURN_VALUE_WHEN_NULL}
     */
    public Object call(Object... args) {
        Object returnVal = executable.execute(args);
        return (returnVal == null ||
                // for case that calling a procedure that have no return (defined by block)
                (returnVal instanceof Values && ((Values)returnVal).getValues().length == 0))
             ? RETURN_VALUE_WHEN_NULL
             : returnVal;
    }

    public int numArgs() {
        return executable.numArgs();
    }

}