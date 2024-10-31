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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

/**
 * <p>
 * {@code Rules} <em>Decorator</em> that returns default rules when no matches are returned by the wrapped
 * implementation.
 * </p>
 * <p>
 * This allows default {@code Rule} instances to be added to any existing {@code Rules} implementation. These
 * default {@code Rule} instances will be returned for any match for which the wrapped implementation does not
 * return any matches.
 * </p>
 * <p>
 * For example,
 * </p>
 * <pre>
 *   Rule alpha;
 *   ...
 *   WithDefaultsRulesWrapper rules = new WithDefaultsRulesWrapper(new BaseRules());
 *   rules.addDefault(alpha);
 *   ...
 *   digester.setRules(rules);
 *   ...
 * </pre>
 * <p>
 * when a pattern does not match any other rule, then rule alpha will be called.
 * </p>
 * <p>
 * {@code WithDefaultsRulesWrapper} follows the <em>Decorator</em> pattern.
 * </p>
 *
 * @since 1.6
 */
public class WithDefaultsRulesWrapper
    implements Rules
{

    /** The Rules implementation that this class wraps. */
    private final Rules wrappedRules;

    /** Rules to be fired when the wrapped implementations returns none. */
    private final List<Rule> defaultRules = new ArrayList<>();

    /** All rules (preserves order in which they were originally added) */
    private final List<Rule> allRules = new ArrayList<>();

    /**
     * Base constructor.
     *
     * @param wrappedRules the wrapped {@code Rules} implementation, not null
     */
    public WithDefaultsRulesWrapper( final Rules wrappedRules )
    {
        if ( wrappedRules == null )
        {
            throw new IllegalArgumentException( "Wrapped rules must not be null" );
        }
        this.wrappedRules = wrappedRules;
    }

    @Override
    public void add( final String pattern, final Rule rule )
    {
        wrappedRules.add( pattern, rule );
        allRules.add( rule );
    }

    /**
     * Adds a rule to be fired when wrapped implementation returns no matches
     *
     * @param rule a Rule to be fired when wrapped implementation returns no matches
     **/
    public void addDefault( final Rule rule )
    {
        // set up rule
        if ( wrappedRules.getDigester() != null )
        {
            rule.setDigester( wrappedRules.getDigester() );
        }

        if ( wrappedRules.getNamespaceURI() != null )
        {
            rule.setNamespaceURI( wrappedRules.getNamespaceURI() );
        }

        defaultRules.add( rule );
        allRules.add( rule );
    }

    @Override
    public void clear()
    {
        wrappedRules.clear();
        allRules.clear();
        defaultRules.clear();
    }

    /**
     * Gets Rule's which will be fired when the wrapped implementation returns no matches
     *
     * @return Rule's which will be fired when the wrapped implementation returns no matches
     **/
    public List<Rule> getDefaults()
    {
        return defaultRules;
    }

    @Override
    public Digester getDigester()
    {
        return wrappedRules.getDigester();
    }

    @Override
    public String getNamespaceURI()
    {
        return wrappedRules.getNamespaceURI();
    }

    @Override
    public List<Rule> match( final String namespaceURI, final String pattern, final String name, final Attributes attributes )
    {
        final List<Rule> matches = wrappedRules.match( namespaceURI, pattern, name, attributes );
        if ( matches == null || matches.isEmpty() )
        {
            // a little bit of defensive programming
            return new ArrayList<>( defaultRules );
        }
        // otherwise
        return matches;
    }

    @Override
    public List<Rule> rules()
    {
        return allRules;
    }

    @Override
    public void setDigester( final Digester digester )
    {
        wrappedRules.setDigester( digester );
        for ( final Rule rule : defaultRules )
        {
            rule.setDigester( digester );
        }
    }

    @Override
    public void setNamespaceURI( final String namespaceURI )
    {
        wrappedRules.setNamespaceURI( namespaceURI );
    }

}
