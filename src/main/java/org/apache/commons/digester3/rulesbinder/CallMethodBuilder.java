/* $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.digester3.rulesbinder;

import org.apache.commons.digester3.rule.CallMethodRule;

/**
 * Builder chained when invoking {@link LinkedRuleBuilder#callMethod(String)}.
 */
public interface CallMethodBuilder extends BackToLinkedRuleBuilder<CallMethodRule> {

    /**
     * Sets the location of the target object.
     *
     * Positive numbers are relative to the top of the digester object stack.
     * Negative numbers are relative to the bottom of the stack. Zero implies the top object on the stack.
     *
     * @param targetOffset location of the target object.
     * @return this builder instance
     */
    CallMethodBuilder withTargetOffset(int targetOffset);

    /**
     * Prepare the {@link CallMethodRule} to be invoked using the matching element body as argument.
     *
     * @return this builder instance
     */
    CallMethodBuilder usingElementBodyAsArgument();

    /**
     * Sets the Java class names that represent the parameter types of the method arguments.
     *
     * If you wish to use a primitive type, specify the corresonding Java wrapper class instead,
     * such as {@code java.lang.Boolean.TYPE} for a {@code boolean} parameter.
     *
     * @param The Java classe names that represent the parameter types of the method arguments
     * @return this builder instance
     */
    CallMethodBuilder withParamTypes(String...paramTypeNames);

    /**
     * Sets the Java classes that represent the parameter types of the method arguments.
     *
     * If you wish to use a primitive type, specify the corresonding Java wrapper class instead,
     * such as {@code java.lang.Boolean.TYPE} for a {@code boolean} parameter.
     *
     * @param paramTypes The Java classes that represent the parameter types of the method arguments
     * @return this builder instance
     */
    CallMethodBuilder withParamTypes(Class<?>...paramTypes);

    /**
     * Should <code>MethodUtils.invokeExactMethod</code> be used for the reflection.
     *
     * @param useExactMatch Flag to mark exact matching or not
     * @return this builder instance
     */
    CallMethodBuilder useExactMatch(boolean useExactMatch);

    /**
     * The number of parameters to collect, or zero for a single argument from the body of this element.
     *
     * @param paramCount The number of parameters to collect, or zero for a single argument
     *        from the body of this element.
     * @return this builder instance
     */
    CallMethodBuilder withParamCount(int paramCount);

}
