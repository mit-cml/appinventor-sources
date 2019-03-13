package com.google.appinventor.components.runtime.util;

import android.util.Log;
import gnu.expr.Language;
import gnu.lists.LList;
import gnu.mapping.Procedure;
import gnu.mapping.Values;
import kawa.standard.Scheme;

public final class YailProcedure {

    public static interface Executable {
        public Object execute(Object... args);
        public int numArgs();
    }

    // static methods are called in runtime.scm
    public static final YailProcedure create(Procedure method) {
        return new YailProcedure(method);
    }
    public static final YailProcedure createWithName(String procedureName) {
        // see Blockly.LexicalVariable.checkIdentifier
        if (!procedureName.matches(
                "^[^-0-9!&%^/>=<`'\"#:;,\\\\\\^\\*\\+\\.\\(\\)\\|\\{\\}\\[\\]\\ ]"
                + "[^-!&%^/>=<'\"#:;,\\\\\\^\\*\\+\\.\\(\\)\\|\\{\\}\\[\\]\\ ]*$")) {
            throw new RuntimeException("Procedure name is not legal: " + procedureName);
        }
        try {
            Language scheme = Scheme.getInstance("scheme");
            scheme.eval("(set-this-form)");
            return new YailProcedure((Procedure) scheme.eval("(get-var p$" + procedureName + ")"));
        } catch (Throwable e) {
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
                // returning minArgs because the numArgs of Procedure = minArgs | maxArgs<<12,
                // and sometimes maxArgs may be -1 for unlimited
                return methodToCall.minArgs();
            }
        });
        Log.d("YailProcedure", "String.valueOf(methodToCall)" + String.valueOf(methodToCall));
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