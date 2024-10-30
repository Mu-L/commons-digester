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

import static java.lang.System.getProperty;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Future;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

public final class AsyncReaderTestCase
{

    private final DigesterLoader digesterLoader = newLoader( new AbstractRulesModule()
    {

        @Override
        protected void configure()
        {
            forPattern( "employee" ).createObject().ofType( Employee.class );
        }

    } ).setExecutorService( newFixedThreadPool( 1 ) );

    private Digester digester;

    @BeforeEach
    public void setUp()
    {
        digester = digesterLoader.newDigester();
    }

    @AfterEach
    public void tearDown()
    {
        digester = null;
    }

    @Test
    public void testParseFromClasspathURL()
        throws Exception
    {
        final Future<Employee> future = digester.asyncParse( getClass().getResource( "Test9.xml" ) );
        verify( future );
    }

    @Test
    public void testParseFromFile()
        throws Exception
    {
        final Future<Employee> future = digester.asyncParse( new File( getProperty( "user.dir" ),
            "src/test/resources/org/apache/commons/digester3/Test9.xml" ) );
        verify( future );
    }

    @Test
    public void testParseFromInputSource()
        throws Exception
    {
        final Future<Employee> future =
            digester.asyncParse( new InputSource( getClass().getResource( "Test9.xml" ).openStream() ) );
        verify( future );
    }

    @Test
    public void testParseFromInputStream()
        throws Exception
    {
        final Future<Employee> future = digester.asyncParse( getClass().getResource( "Test9.xml" ).openStream() );
        verify( future );
    }

    @Test
    public void testParseFromReader()
        throws Exception
    {
        final Future<Employee> future =
            digester.asyncParse( new InputStreamReader( getClass().getResource( "Test9.xml" ).openStream() ) );
        verify( future );
    }

    @Test
    public void testParseFromUri()
        throws Exception
    {
        final Future<Employee> future = digester.asyncParse( getClass().getResource( "Test9.xml" ).toExternalForm() );
        verify( future );
    }

    private void verify( final Future<Employee> result )
        throws Exception
    {
        final Employee employee = result.get();
        assertNotNull( employee );
    }

}
