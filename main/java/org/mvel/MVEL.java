/**
 * MVEL (The MVFLEX Expression Language)
 *
 * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the Codehaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mvel;

import org.mvel.integration.VariableResolverFactory;
import org.mvel.integration.impl.MapVariableResolverFactory;
import org.mvel.optimizers.impl.refl.GetterAccessor;
import org.mvel.optimizers.impl.refl.ReflectiveOptimizer;
import org.mvel.util.ParseTools;

import java.io.Serializable;
import static java.lang.String.valueOf;
import java.util.Map;

public class
        MVEL {
    public static final String NAME = "MVEL (MVFLEX Expression Language)";
    public static final String VERSION = "1.2";
    public static final String VERSION_SUB = "beta2";
    public static final String CODENAME = "horizon";

    static boolean THREAD_SAFE = Boolean.getBoolean("mvel.threadsafety");

    /**
     * Force MVEL to use thread-safe caching.  This can also be specified enivromentally using the
     * <tt>mvflex.expression.threadsafety</tt> system property.
     *
     * @param threadSafe - true enabled thread-safe caching - false disables thread-safety.
     */
    public static void setThreadSafe(boolean threadSafe) {
        THREAD_SAFE = threadSafe;
        PropertyAccessor.configureFactory();
        Interpreter.configureFactory();
        ExpressionParser.configureFactory();
    }

    public static boolean isThreadSafe() {
        return THREAD_SAFE;
    }

    public static Object eval(String expression, Object ctx) {
        return new ExpressionParser(expression, ctx).parse();
    }

    public static Object eval(String expression, VariableResolverFactory resolverFactory) {
        return new ExpressionParser(expression, resolverFactory).parse();
    }

    public static Object eval(char[] expression, Object ctx, VariableResolverFactory resolverFactory) {
        return new ExpressionParser(expression, ctx, resolverFactory).parse();
    }

    public static Object eval(String expression, Object ctx, VariableResolverFactory resolverFactory) {
        return new ExpressionParser(expression, ctx, resolverFactory).parse();
    }

    @SuppressWarnings({"unchecked"})
    public static Object eval(String expression, Map tokens) {
        return new ExpressionParser(expression, null, tokens).parse();
    }

    @SuppressWarnings({"unchecked"})
    public static Object eval(String expression, Object ctx, Map tokens) {
        return new ExpressionParser(expression, ctx, tokens).parse();
    }

    /**
     * Compiles an expression and returns a Serializable object containing the compiled
     * expression.
     *
     * @param expression - the expression to be compiled
     * @return -
     */
    public static Serializable compileExpression(String expression) {
        ExpressionParser parser = new ExpressionParser(expression);

        TokenIterator tokens = parser.compileTokens();

        /**
         * If there is only one token, and it's an identifier, we can optimize this as an accessor expression.
         */
        if (tokens.size() == 1) {
            if (tokens.firstToken().isIdentifier()) {
                return new ExecutableAccessor(tokens.firstToken(), parser.isBooleanModeOnly(), parser.isReturnBigDecimal());
            }
            else if (tokens.firstToken().isLiteral()) {
                return new ExecutableLiteral(tokens.firstToken().getLiteralValue());
            }
        }


        return new CompiledExpression(tokens);
    }

    /**
     * Compiles an expression and returns a Serializable object containing the compiled
     * expression.
     *
     * @param expression - the expression to be compiled
     * @return -
     */
    public static Serializable compileExpression(char[] expression) {
        ExpressionParser parser = new ExpressionParser(expression);

        TokenIterator tokens = parser.compileTokens();

        /**
         * If there is only one token, and it's an identifier, we can optimize this as an accessor expression.
         */
        if (tokens.size() == 1) {
            if (tokens.firstToken().isIdentifier()) {
                return new ExecutableAccessor(tokens.firstToken(), parser.isBooleanModeOnly(), parser.isReturnBigDecimal());
            }
            else if (tokens.firstToken().isLiteral()) {
                return new ExecutableLiteral(tokens.firstToken().getLiteralValue());
            }
        }


        return new CompiledExpression(tokens);
    }

    public static Object executeExpression(Object compiledExpression) {
        return ((ExecutableStatement) compiledExpression).getValue(null, null);
    }

    /**
     * Executes a compiled expression.
     *
     * @param compiledExpression -
     * @param ctx                -
     * @param vars               -
     * @return -
     * @see #compileExpression(String)
     */
    @SuppressWarnings({"unchecked"})
    public static Object executeExpression(final Object compiledExpression, final Object ctx, final Map vars) {
        return ParseTools.handleParserEgress(((ExecutableStatement) compiledExpression).getValue(ctx, new MapVariableResolverFactory(vars)),
                false, false);
    }

    public static Object executeExpression(final Object compiledExpression, final Object ctx, final VariableResolverFactory resolverFactory) {
        return ParseTools.handleParserEgress(((ExecutableStatement) compiledExpression).getValue(ctx, resolverFactory), false, false);
    }

    /**
     * Executes a compiled expression.
     *
     * @param compiledExpression -
     * @param factory            -
     * @return -
     * @see #compileExpression(String)
     */
    public static Object executeExpression(final Object compiledExpression, final VariableResolverFactory factory) {
        return ((ExecutableStatement) compiledExpression).getValue(null, factory);
    }

    /**
     * Executes a compiled expression.
     *
     * @param compiledExpression -
     * @param ctx                -
     * @return -
     * @see #compileExpression(String)
     */
    public static Object executeExpression(final Object compiledExpression, final Object ctx) {
        return ((ExecutableStatement) compiledExpression).getValue(ctx, null);
    }

    /**
     * Executes a compiled expression.
     *
     * @param compiledExpression -
     * @param vars               -
     * @return -
     * @see #compileExpression(String)
     */
    @SuppressWarnings({"unchecked"})
    public static Object executeExpression(final Object compiledExpression, final Map vars) {
        return ((ExecutableStatement) compiledExpression).getValue(null, new MapVariableResolverFactory(vars));
    }

    /**
     * Execute a compiled expression and convert the result to a type
     *
     * @param compiledExpression -
     * @param ctx                -
     * @param vars               -
     * @param toType             -
     * @return -
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T executeExpression(final Object compiledExpression, final Object ctx, final Map vars, Class<T> toType) {
        return DataConversion.convert(executeExpression(compiledExpression, ctx, vars), toType);
    }

    /**
     * Execute a compiled expression and convert the result to a type
     *
     * @param compiledExpression -
     * @param vars               -
     * @param toType             -
     * @return -
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T executeExpression(final Object compiledExpression, Map vars, Class<T> toType) {
        return DataConversion.convert(executeExpression(compiledExpression, vars), toType);
    }

    /**
     * Execute a compiled expression and convert the result to a type.
     *
     * @param compiledExpression -
     * @param ctx                -
     * @param toType             -
     * @return -
     */
    public static <T> T executeExpression(final Object compiledExpression, final Object ctx, Class<T> toType) {
        return DataConversion.convert(executeExpression(compiledExpression, ctx), toType);
    }

    public static Object[] executeAllExpression(Serializable[] compiledExpressions, Object ctx, VariableResolverFactory vars) {
        if (compiledExpressions == null) return GetterAccessor.EMPTY;

        Object[] o = new Object[compiledExpressions.length];
        for (int i = 0; i < compiledExpressions.length; i++) {
            o[i] = executeExpression(compiledExpressions[i], ctx, vars);
        }
        return o;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T eval(char[] expression, Object ctx, Map vars, Class<T> toType) {
        return DataConversion.convert(new ExpressionParser(expression, ctx, vars).parse(), toType);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T eval(char[] expression, Map vars, Class<T> toType) {
        return DataConversion.convert(new ExpressionParser(expression, null, vars).parse(), toType);
    }

    @SuppressWarnings({"unchecked"})
    public static Object eval(char[] expression, Object ctx, Map vars) {
        return new ExpressionParser(expression, ctx, vars).parse();
    }

    public static String evalToString(String expression, Object ctx) {
        return valueOf(eval(expression, ctx));
    }

    @SuppressWarnings({"unchecked"})
    public static String evalToString(String expression, Map vars) {
        return valueOf(eval(expression, vars));
    }

    @SuppressWarnings({"unchecked"})
    public static String evalToString(String expression, Object ctx, Map vars) {
        return valueOf(eval(expression, ctx, vars));
    }

    /**
     * Evaluate an expression in Boolean-only mode.
     *
     * @param expression -
     * @param ctx        -
     * @param vars       -
     * @return -
     */
    @SuppressWarnings({"unchecked"})
    public static Boolean evalToBoolean(String expression, Object ctx, Map vars) {
        return (Boolean) new ExpressionParser(expression, ctx, vars, true).parse();
    }

    /**
     * Evaluate an expression in Boolean-only mode.
     *
     * @param expression -
     * @param ctx        -
     * @return -
     */
    public static Boolean evalToBoolean(String expression, Object ctx) {
        return (Boolean) new ExpressionParser(expression, ctx, true).parse();
    }

    /**
     * Evaluate an expression in Boolean-only mode.
     *
     * @param expression -
     * @param ctx        -
     * @param factory    -
     * @return -
     */
    public static Boolean evalToBoolean(String expression, Object ctx, VariableResolverFactory factory) {
        return (Boolean) new ExpressionParser(expression, ctx, factory, true).parse();
    }

    /**
     * Evaluate an expression in Boolean-only mode.
     *
     * @param expression -
     * @param factory    -
     * @return -
     */
    public static Boolean evalToBoolean(String expression, VariableResolverFactory factory) {
        return (Boolean) new ExpressionParser(expression, null, factory, true).parse();
    }

    /**
     * Evaluate an expression in Boolean-only mode.
     *
     * @param expression -
     * @param vars       -
     * @return -
     */
    public static Boolean evalToBoolean(String expression, Map vars) {
        return evalToBoolean(expression, null, vars);
    }

    public static Object getProperty(String property, Object ctx) {
        return ReflectiveOptimizer.get(property, ctx);
    }

    public static void setProperty(Object ctx, String property, Object value) {
        PropertyAccessor.set(ctx, property, value);
    }
}
