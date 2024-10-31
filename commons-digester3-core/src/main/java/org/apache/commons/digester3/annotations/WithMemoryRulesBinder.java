package org.apache.commons.digester3.annotations;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.digester3.binder.LinkedRuleBuilder;
import org.apache.commons.digester3.binder.RulesBinder;
import org.apache.commons.digester3.binder.RulesModule;

/**
 * A {@link RulesBinder} implementation with memory to maintain
 * which classes have already been analyzed.
 *
 * @since 3.0
 */
final class WithMemoryRulesBinder
    implements RulesBinder
{

    /**
     * Maintains all the classes that this RuleSet produces mapping for.
     */
    private final Set<Class<?>> boundClasses = new HashSet<>();

    private final RulesBinder wrappedRulesBinder;

    WithMemoryRulesBinder( final RulesBinder wrappedRulesBinder )
    {
        this.wrappedRulesBinder = wrappedRulesBinder;
    }

    @Override
    public void addError( final String messagePattern, final Object... arguments )
    {
        wrappedRulesBinder.addError( messagePattern, arguments );
    }

    @Override
    public void addError( final Throwable t )
    {
        wrappedRulesBinder.addError( t );
    }

    @Override
    public LinkedRuleBuilder forPattern( final String pattern )
    {
        return wrappedRulesBinder.forPattern( pattern );
    }

    @Override
    public ClassLoader getContextClassLoader()
    {
        return wrappedRulesBinder.getContextClassLoader();
    }

    @Override
    public void install( final RulesModule rulesModule )
    {
        wrappedRulesBinder.install( rulesModule );
    }

    /**
     *
     *
     * @param bindingClass
     * @return true if the specified element has been marked
     */
    public boolean isAlreadyBound( final Class<?> bindingClass )
    {
        return boundClasses.contains( bindingClass );
    }

    /**
     *
     *
     * @param bindingClass
     * @return true if the specified element has not yet been marked
     */
    public boolean markAsBound( final Class<?> bindingClass )
    {
        return boundClasses.add( bindingClass );
    }

}
