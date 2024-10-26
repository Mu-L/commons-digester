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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.reflect.Method;

/**
 * Detached representation of a method invocation.
 * From Commons [proxy] v2 branch.
 *
 * @since 3.2
 */
final class RecordedInvocation
{

    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    private final Method invokedMethod;

    private final Object[] arguments;

    //******************************************************************************************************************
    // Constructors
    //******************************************************************************************************************

    /**
     * Create a new RecordedInvocation instance.
     *
     * @param invokedMethod
     * @param arguments
     */
    public RecordedInvocation( final Method invokedMethod, final Object[] arguments )
    {
        this.invokedMethod = invokedMethod;
        this.arguments = arguments;
    }

    //******************************************************************************************************************
    // Canonical Methods
    //******************************************************************************************************************

    /**
     * Add a string representation of {@code input} to {@code buffer}.
     *
     * @param buffer the buffer to append the string representation of the input object.
     * @param input the input object has to be serialized to string.
     */
    protected void convert( final StringBuilder buffer, final Object input )
    {
        if ( input == null )
        {
            buffer.append( "<null>" );
            return;
        }

        // Primitive types, and non-object arrays
        // use toString().
        if ( !( input instanceof Object[] ) )
        {
            buffer.append( input );
        }
        else
        {
            buffer.append( "(" );
            buffer.append( input.getClass().getSimpleName() );
            buffer.append( "){" );
            final Object[] array = (Object[]) input;
            final int count = array.length;
            for ( int i = 0; i < count; i++ )
            {
                if ( i > 0 )
                {
                    buffer.append( ", " );
                }
                // We use convert() again, because it could be a multi-dimensional array
                // where each element must be converted.
                convert( buffer, array[i] );
            }
            buffer.append( "}" );
        }
    }

    /**
     * Gets the arguments.
     *
     * @return Object[]
     */
    public Object[] getArguments()
    {
        return arguments;
    }

    /**
     * Gets the invokedMethod.
     *
     * @return Method
     */
    public Method getInvokedMethod()
    {
        return invokedMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append( invokedMethod.getDeclaringClass().getName() );
        buffer.append( "." );
        buffer.append( invokedMethod.getName() );
        buffer.append( "(" );
        final int count = arguments.length;
        for ( int i = 0; i < count; i++ )
        {
            final Object arg = arguments[i];
            if ( i > 0 )
            {
                buffer.append( ", " );
            }
            convert( buffer, arg );
        }
        buffer.append( ")" );
        return buffer.toString();
    }

}
