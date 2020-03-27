/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.painless.node;

import org.elasticsearch.painless.Location;
import org.elasticsearch.painless.Scope;
import org.elasticsearch.painless.ir.ClassNode;
import org.elasticsearch.painless.ir.NullSafeSubNode;
import org.elasticsearch.painless.symbol.ScriptRoot;

import static java.util.Objects.requireNonNull;

/**
 * Implements a call who's value is null if the prefix is null rather than throwing an NPE.
 */
public class PSubNullSafeCallInvoke extends AExpression {

    /**
     * The expression guarded by the null check. Required at construction time and replaced at analysis time.
     */
    protected final AExpression guarded;

    public PSubNullSafeCallInvoke(Location location, AExpression guarded) {
        super(location);
        this.guarded = requireNonNull(guarded);
    }

    @Override
    Output analyze(ClassNode classNode, ScriptRoot scriptRoot, Scope scope, Input input) {
        Output output = new Output();

        Output guardedOutput = guarded.analyze(classNode, scriptRoot, scope, new Input());
        output.actual = guardedOutput.actual;
        if (output.actual.isPrimitive()) {
            throw new IllegalArgumentException("Result of null safe operator must be nullable");
        }

        NullSafeSubNode nullSafeSubNode = new NullSafeSubNode();

        nullSafeSubNode.setChildNode(guardedOutput.expressionNode);

        nullSafeSubNode.setLocation(location);
        nullSafeSubNode.setExpressionType(output.actual);

        output.expressionNode = nullSafeSubNode;

        return output;
    }

    @Override
    public String toString() {
        return singleLineToString(guarded);
    }
}
