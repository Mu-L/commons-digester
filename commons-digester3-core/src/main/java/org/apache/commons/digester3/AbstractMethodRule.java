package org.apache.commons.digester3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static java.lang.String.format;
import static org.apache.commons.beanutils.MethodUtils.invokeExactMethod;
import static org.apache.commons.beanutils.MethodUtils.invokeMethod;

import org.xml.sax.Attributes;

/**
 * Abstract implementation for {@link org.apache.commons.digester3.SetNextRule},
 * {@link org.apache.commons.digester3.SetRootRule} and {@link org.apache.commons.digester3.SetTopRule} rules.
 *
 * @since 3.0
 */
public abstract class AbstractMethodRule
    extends Rule
{

    /**
     * The method name to call on the parent object.
     */
    protected String methodName;

    /**
     * The Java class name of the parameter type expected by the method.
     */
    protected String paramTypeName;

    /**
     * The Java class name of the parameter type expected by the method.
     */
    protected Class<?> paramType;

    /**
     * Should we use exact matching. Default is no.
     */
    protected boolean useExactMatch;

    /**
     * Should this rule be invoked when {@link #begin(String, String, Attributes)} (true)
     * or {@link #end(String, String)} (false) methods are invoked, false by default.
     */
    protected boolean fireOnBegin;

    /**
     * Constructs a "set next" rule with the specified method name. The method's argument type is assumed to be the class
     * of the child object.
     *
     * @param methodName Method name of the parent method to call
     */
    public AbstractMethodRule( final String methodName )
    {
        this( methodName, (String) null );
    }

    /**
     * Constructs a "set next" rule with the specified method name.
     *
     * @param methodName Method name of the parent method to call
     * @param paramType Java class of the parent method's argument (if you wish to use a primitive type, specify the
     *            corresponding Java wrapper class instead, such as {@link Boolean} for a
     *            {@code boolean} parameter)
     */
    public AbstractMethodRule( final String methodName, final Class<?> paramType )
    {
        this( methodName, paramType.getName() );
        this.paramType = paramType;
    }

    /**
     * Constructs a "set next" rule with the specified method name.
     *
     * @param methodName Method name of the parent method to call
     * @param paramTypeName Java class of the parent method's argument (if you wish to use a primitive type, specify the
     *            corresponding Java wrapper class instead, such as {@link Boolean} for a
     *            {@code boolean} parameter)
     */
    public AbstractMethodRule( final String methodName, final String paramTypeName )
    {
        this.methodName = methodName;
        this.paramTypeName = paramTypeName;
    }

    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
        throws Exception
    {
        if ( fireOnBegin )
        {
            invoke();
        }
    }

    @Override
    public void end( final String namespace, final String name )
        throws Exception
    {
        if ( !fireOnBegin )
        {
            invoke();
        }
    }

    /**
     * Returns the argument object of method has to be invoked.
     *
     * @return the argument object of method has to be invoked.
     */
    protected abstract Object getChild();

    /**
     * Returns the target object of method has to be invoked.
     *
     * @return the target object of method has to be invoked.
     */
    protected abstract Object getParent();

    /**
     * Just performs the method execution.
     *
     * @throws Exception if any error occurs.
     */
    private void invoke()
        throws Exception
    {
        // Identify the objects to be used
        final Object child = getChild();
        final Object parent = getParent();
        if ( getDigester().getLogger().isDebugEnabled() )
        {
            if ( parent == null )
            {
                getDigester().getLogger().debug( format( "[%s]{%s} Call [NULL PARENT].%s(%s)",
                                                         getClass().getSimpleName(),
                                                         getDigester().getMatch(),
                                                         methodName,
                                                         child ) );
            }
            else
            {
                getDigester().getLogger().debug( format( "[%s]{%s} Call %s.%s(%s)",
                                                         getClass().getSimpleName(),
                                                         getDigester().getMatch(),
                                                         parent.getClass().getName(),
                                                         methodName,
                                                         child ) );
            }
        }

        // Call the specified method
        final Class<?>[] paramTypes = new Class<?>[1];
        if ( paramType != null )
        {
            paramTypes[0] = getDigester().getClassLoader().loadClass( paramTypeName );
        }
        else
        {
            paramTypes[0] = child.getClass();
        }

        if ( useExactMatch )
        {
            invokeExactMethod( parent, methodName, new Object[] { child }, paramTypes );
        }
        else
        {
            invokeMethod( parent, methodName, new Object[] { child }, paramTypes );
        }
    }

    /**
     * <p>
     * Is exact matching being used.
     * </p>
     * <p>
     * This rule uses {@code org.apache.commons.beanutils.MethodUtils} to introspect the relevant objects so that
     * the right method can be called. Originally, {@code MethodUtils.invokeExactMethod} was used. This matches
     * methods very strictly and so may not find a matching method when one exists. This is still the behavior when
     * exact matching is enabled.
     * </p>
     * <p>
     * When exact matching is disabled, {@code MethodUtils.invokeMethod} is used. This method finds more methods
     * but is less precise when there are several methods with correct signatures. So, if you want to choose an exact
     * signature you might need to enable this property.
     * </p>
     * <p>
     * The default setting is to disable exact matches.
     * </p>
     *
     * @return true if exact matching is enabled
     * @since 1.1.1
     */
    public boolean isExactMatch()
    {
        return useExactMatch;
    }

    /**
     * Returns the flag this rule be invoked when {@link #begin(String, String, Attributes)} (true)
     * or {@link #end(String, String)} (false) methods are invoked, false by default.
     *
     * @return the flag this rule be invoked when {@link #begin(String, String, Attributes)} (true)
     * or {@link #end(String, String)} (false) methods are invoked, false by default.
     */
    public boolean isFireOnBegin()
    {
        return fireOnBegin;
    }

    /**
     * <p>
     * Sets whether exact matching is enabled.
     * </p>
     * <p>
     * See {@link #isExactMatch()}.
     * </p>
     *
     * @param useExactMatch should this rule use exact method matching
     * @since 1.1.1
     */
    public void setExactMatch( final boolean useExactMatch )
    {
        this.useExactMatch = useExactMatch;
    }

    /**
     * Sets this rule be invoked when {@link #begin(String, String, Attributes)} (true)
     * or {@link #end(String, String)} (false) methods are invoked, false by default.
     *
     * @param fireOnBegin flag to mark this rule be invoked when {@link #begin(String, String, Attributes)} (true)
     *                    or {@link #end(String, String)} (false) methods are invoked, false by default.
     */
    public void setFireOnBegin( final boolean fireOnBegin )
    {
        this.fireOnBegin = fireOnBegin;
    }

    @Override
    public final String toString()
    {
        return format( "%s[methodName=%s, paramType=%s, paramTypeName=%s, useExactMatch=%s, fireOnBegin=%s]",
                       getClass().getSimpleName(), methodName, paramType, paramTypeName, useExactMatch, fireOnBegin );
    }

}
