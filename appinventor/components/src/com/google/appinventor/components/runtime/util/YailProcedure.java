package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;

import android.util.Log;
import gnu.lists.LList;
import gnu.mapping.Procedure;
import gnu.mapping.SimpleSymbol;
import gnu.mapping.Symbol;
import gnu.mapping.Values;

public final class YailProcedure {

    public static interface Executable {
        public Object execute(Object... args);
        public int numArgs();
    }

    // static methods are called in runtime.scm
    public static final YailProcedure create(Procedure procedure) {
        if (procedure == null) {
            throw new RuntimeException("Cannot create YailProcedure since the procedure is null");
        }
        return new YailProcedure(procedure);
    }
    public static final YailProcedure createWithName(String procedureName) {
        // see Blockly.LexicalVariable.checkIdentifier
        if (!procedureName.matches(
                "^[^-0-9!&%^/>=<`'\"#:;,\\\\\\^\\*\\+\\.\\(\\)\\|\\{\\}\\[\\]\\ ]"
                + "[^-!&%^/>=<'\"#:;,\\\\\\^\\*\\+\\.\\(\\)\\|\\{\\}\\[\\]\\ ]*$")) {
            throw new RuntimeException("Procedure name is not legal: " + procedureName);
        }
        try {
            Log.d("YailProcedure", "createWithName: procedureName=" + procedureName);
            String processedName;
            if (Form.getActiveForm() instanceof ReplForm) {
                // Need to be escaped
                // For non-ascii character 例 (\u4f8b)
                // Symbol 'p$例 == 'p$u4f8b == (string->symbol "p$u4f8b") != (string->symbol "p$例")
                processedName = escapeUnicode(procedureName);
                Log.d("YailProcedure", "createWithName: escaped procedureName for Repl: " + processedName);
            } else {
                processedName = procedureName;
            }
            return create((Procedure) Class.forName("com.google.youngandroid.runtime")
                .getMethod("lookupGlobalVarInCurrentFormEnvironment", Symbol.class, Object.class)
                .invoke(
                    /* invoking instance (null for static method) */ null,
                    /* global var name */ new SimpleSymbol("p$" + processedName).readResolve(),
                    /* default value */ null));
        } catch (Throwable e) {
            throw new RuntimeException("Cannot read global procedure: " + procedureName, e);
        }
    }
    private static String escapeUnicode(String str) {
        StringBuilder b = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c <= 0x007F) { // ASCII
                b.append(c);
            } else {
                b.append("u").append(Integer.toHexString(c));
            }
        }
        return b.toString();
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