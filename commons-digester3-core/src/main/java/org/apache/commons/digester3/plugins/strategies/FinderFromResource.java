package org.apache.commons.digester3.plugins.strategies;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.plugins.PluginException;
import org.apache.commons.digester3.plugins.RuleFinder;
import org.apache.commons.digester3.plugins.RuleLoader;

/**
 * A rule-finding algorithm which expects the user to specify a resource name (ie a file in the classpath). The file is
 * expected to contain Digester rules in xmlrules format.
 *
 * @since 1.6
 */
public class FinderFromResource
    extends RuleFinder
{

    /**
     * Default name of XML attribute on the plugin declaration which is used to configure rule-loading
     * for that declaration.
     */
    private static final String DFLT_RESOURCE_ATTR = "resource";

    /**
     * Open the specified resource file (ie a file in the classpath, including being within a jar in the classpath), run
     * it through the xmlrules module and return an object encapsulating those rules.
     *
     * @param d is the digester into which rules will eventually be loaded.
     * @param pluginClass is the class whose XML params the rules are parsing.
     * @param is is where the xmlrules will be read from, and must be non-null.
     * @param resourceName is a string describing the source of the xmlrules, for use in generating error messages.
     * @return a source of digester rules for the specified plugin class.
     * @throws PluginException if any error occurs
     */
    public static RuleLoader loadRules( final Digester d, final Class<?> pluginClass, final InputStream is, final String resourceName )
        throws PluginException
    {
        try
        {
            return new LoaderFromStream( is );
        }
        catch ( final Exception e )
        {
            throw new PluginException( "Unable to load xmlrules from resource [" + resourceName + "]", e );
        }
        finally
        {
            try
            {
                is.close();
            }
            catch ( final IOException ioe )
            {
                throw new PluginException( "Unable to close stream for resource [" + resourceName + "]", ioe );
            }
        }
    }

    /** See {@link #findLoader}. */
    private final String resourceAttr;

    /** Constructs a new instance. */
    public FinderFromResource()
    {
        this( DFLT_RESOURCE_ATTR );
    }

    /**
     * See {@link #findLoader}.
     *
     * @param resourceAttr Name of XML attribute on the plugin declaration which is used to configure rule-loading
     *        for that declaration
     */
    public FinderFromResource( final String resourceAttr )
    {
        this.resourceAttr = resourceAttr;
    }

    /**
     * If there exists a property with the name matching constructor param resourceAttr, then load that file, run it
     * through the xmlrules module and return an object encapsulating those rules.
     * <p>
     * If there is no matching property provided, then just return null.
     * <p>
     * The returned object (when non-null) will add the selected rules to the digester whenever its addRules method is
     * invoked.
     *
     * @param d The digester instance where locating plugin classes
     * @param pluginClass The plugin Java class
     * @param p The properties object that holds any XML attributes the user may have specified on the plugin
     *          declaration in order to indicate how to locate the plugin rules.
     * @return a source of digester rules for the specified plugin class.
     * @throws PluginException if the algorithm finds a source of rules, but there is something invalid
     *         about that source.
     */
    @Override
    public RuleLoader findLoader( final Digester d, final Class<?> pluginClass, final Properties p )
        throws PluginException
    {
        final String resourceName = p.getProperty( resourceAttr );
        if ( resourceName == null )
        {
            // nope, user hasn't requested dynamic rules to be loaded
            // from a specific file.
            return null;
        }

        final InputStream is = pluginClass.getClassLoader().getResourceAsStream( resourceName );

        if ( is == null )
        {
            throw new PluginException( "Resource " + resourceName + " not found." );
        }

        return loadRules( d, pluginClass, is, resourceName );
    }

}
