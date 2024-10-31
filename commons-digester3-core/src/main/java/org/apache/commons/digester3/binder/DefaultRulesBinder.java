package org.apache.commons.digester3.binder;

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

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester3.RuleSet;

/**
 * The default Digester EDSL implementation.
 *
 * @since 3.0
 */
final class DefaultRulesBinder
    implements RulesBinder
{

    /**
     * Errors that can occur during binding time or rules creation.
     */
    private final List<ErrorMessage> errors = new ArrayList<>();

    /**
     */
    private final FromBinderRuleSet fromBinderRuleSet = new FromBinderRuleSet();

    /**
     */
    private ClassLoader classLoader;

    /**
     * Records an error, the full details of which will be logged, and the message of which will be presented to the
     * user at a later time.
     *
     * @param errorMessage The error to record.
     */
    private void addError( final ErrorMessage errorMessage )
    {
        this.errors.add( errorMessage );
    }

    @Override
    public void addError( String messagePattern, final Object... arguments )
    {
        final StackTraceElement[] stackTrace = new Exception().getStackTrace();
        StackTraceElement element = null;

        int stackIndex = stackTrace.length - 1;
        while ( element == null && stackIndex > 0 ) // O(n) there's no better way
        {
            Class<?> moduleClass;
            try
            {
                // check if the set ClassLoader resolves the Class in the StackTrace
                moduleClass = Class.forName( stackTrace[stackIndex].getClassName(), false, this.classLoader );
            }
            catch ( final ClassNotFoundException e )
            {
                try
                {
                    // try otherwise with current ClassLoader
                    moduleClass =
                        Class.forName( stackTrace[stackIndex].getClassName(), false, this.getClass().getClassLoader() );
                }
                catch ( final ClassNotFoundException e1 )
                {
                    // Class in the StackTrace can't be found, don't write the file name:line number detail in the
                    // message
                    moduleClass = null;
                }
            }

            if ( moduleClass != null && RulesModule.class.isAssignableFrom( moduleClass ) )
            {
                element = stackTrace[stackIndex];
            }

            stackIndex--;
        }

        if ( element != null )
        {
            messagePattern = format( "%s (%s:%s)", messagePattern, element.getFileName(), element.getLineNumber() );
        }
        addError( new ErrorMessage( messagePattern, arguments ) );
    }

    @Override
    public void addError( final Throwable t )
    {
        final String message = "An exception was caught and reported. Message: " + t.getMessage();
        addError( new ErrorMessage( message, t ) );
    }

    /**
     *
     *
     * @return
     */
    int errorsSize()
    {
        return errors.size();
    }

    @Override
    public LinkedRuleBuilder forPattern( final String pattern )
    {
        final String keyPattern;

        if ( pattern == null || pattern.isEmpty() )
        {
            addError( "Null or empty pattern is not valid" );
            keyPattern = null;
        }
        else if ( pattern.endsWith( "/" ) )
        {
            // to help users who accidently add '/' to the end of their patterns
            keyPattern = pattern.substring( 0, pattern.length() - 1 );
        }
        else
        {
            keyPattern = pattern;
        }

        return new LinkedRuleBuilder( this, fromBinderRuleSet, classLoader, keyPattern );
    }

    @Override
    public ClassLoader getContextClassLoader()
    {
        return this.classLoader;
    }

    /**
     *
     *
     * @return
     */
    Iterable<ErrorMessage> getErrors()
    {
        return errors;
    }

    /**
     *
     *
     * @return
     */
    RuleSet getFromBinderRuleSet()
    {
        return fromBinderRuleSet;
    }

    /**
     *
     *
     * @return
     */
    boolean hasError()
    {
        return !errors.isEmpty();
    }

    /**
     *
     *
     * @param classLoader
     */
    void initialize( final ClassLoader classLoader )
    {
        this.classLoader = classLoader;
        fromBinderRuleSet.clear();
        errors.clear();
    }

    @Override
    public void install( final RulesModule rulesModule )
    {
        rulesModule.configure( this );
    }

}
