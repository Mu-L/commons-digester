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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Simple regex pattern matching algorithm.
 * </p>
 * <p>
 * This uses just two wildcards:
 * </p>
 * <ul>
 * <li>{@code *} matches any sequence of none, one or more characters
 * <li>{@code ?} matches any one character
 * </ul>
 * <p>
 * Escaping these wildcards is not supported.
 * </p>
 *
 * @since 1.5
 */
public class SimpleRegexMatcher
    extends RegexMatcher
{

    /** Default log (class wide) */
    private static final Log BASE_LOG = LogFactory.getLog( SimpleRegexMatcher.class );

    /** Custom log (can be set per object) */
    private Log log = BASE_LOG;

    /**
     * Gets the {@code Log} implementation.
     *
     * @return the {@code Log} implementation.
     */
    public Log getLog()
    {
        return log;
    }

    @Override
    public boolean match( final String basePattern, final String regexPattern )
    {
        // check for nulls
        if ( basePattern == null || regexPattern == null )
        {
            return false;
        }
        return match( basePattern, regexPattern, 0, 0 );
    }

    /**
     * Implements a regex matching algorithm. This calls itself recursively.
     *
     * @param basePattern the standard digester path representing the element
     * @param regexPattern the regex pattern the path will be tested against
     * @param baseAt FIXME
     * @param regexAt FIXME
     */
    private boolean match( final String basePattern, final String regexPattern, int baseAt, int regexAt )
    {
        if ( log.isTraceEnabled() )
        {
            log.trace( "Base: " + basePattern );
            log.trace( "Regex: " + regexPattern );
            log.trace( "Base@" + baseAt );
            log.trace( "Regex@" + regexAt );
        }

        // check bounds
        if ( regexAt >= regexPattern.length() )
        {
            // maybe we've got a match
            if ( baseAt >= basePattern.length() )
            {
                // ok!
                return true;
            }
            // run out early
            return false;

        }
        if ( baseAt >= basePattern.length() )
        {
            // run out early
            return false;
        }

        // ok both within bounds
        final char regexCurrent = regexPattern.charAt( regexAt );
        switch ( regexCurrent )
        {
            case '*':
                // this is the tricky case
                // check for terminal
                if ( ++regexAt >= regexPattern.length() )
                {
                    // this matches anything let - so return true
                    return true;
                }
                // go through every subsequent appearance of the next character
                // and so if the rest of the regex matches
                final char nextRegex = regexPattern.charAt( regexAt );
                if ( log.isTraceEnabled() )
                {
                    log.trace( "Searching for next '" + nextRegex + "' char" );
                }
                int nextMatch = basePattern.indexOf( nextRegex, baseAt );
                while ( nextMatch != -1 )
                {
                    if ( log.isTraceEnabled() )
                    {
                        log.trace( "Trying '*' match@" + nextMatch );
                    }
                    if ( match( basePattern, regexPattern, nextMatch, regexAt ) )
                    {
                        return true;
                    }
                    nextMatch = basePattern.indexOf( nextRegex, nextMatch + 1 );
                }
                log.trace( "No matches found." );
                return false;

            case '?':
                // this matches anything
                return match( basePattern, regexPattern, ++baseAt, ++regexAt );

            default:
                if ( log.isTraceEnabled() )
                {
                    log.trace( "Comparing " + regexCurrent + " to " + basePattern.charAt( baseAt ) );
                }
                if ( regexCurrent == basePattern.charAt( baseAt ) )
                {
                    // still got more to go
                    return match( basePattern, regexPattern, ++baseAt, ++regexAt );
                }
                return false;
        }
    }

    /**
     * Sets the current {@code Log} implementation used by this class.
     *
     * @param log the current {@code Log} implementation used by this class.
     */
    public void setLog( final Log log )
    {
        this.log = log;
    }

}
