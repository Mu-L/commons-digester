/*
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

package org.apache.commons.digester3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Tests for XInclude aware parsing.
 * </p>
 */
public class XMLSchemaTestCase
{

    static final class TestErrorHandler
        implements ErrorHandler
    {
        public boolean clean = true;

        public TestErrorHandler()
        {
        }

        @Override
        public void error( final SAXParseException exception )
        {
            clean = false;
        }

        @Override
        public void fatalError( final SAXParseException exception )
        {
            clean = false;
        }

        @Override
        public void warning( final SAXParseException exception )
        {
            clean = false;
        }
    }

    /**
     * The digester instance we will be processing.
     */
    protected Digester digester;

    /**
     * Gets an appropriate InputStream for the specified test file (which must be inside our current package.
     *
     * @param name Name of the test file we want
     * @throws IOException if an input/output error occurs
     */
    protected InputStream getInputStream( final String name )
    {

        return this.getClass().getResourceAsStream( "/org/apache/commons/digester3/" + name );

    }

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp() throws Exception
    {

        digester = new Digester();

        // Use the test schema
        digester.setNamespaceAware( true );
        final Schema test13schema =
            SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI ).newSchema( this.getClass().getClassLoader().getResource( "org/apache/commons/digester3/Test13.xsd" ) );
        digester.setXMLSchema( test13schema );

        // Configure the digester as required
        digester.addObjectCreate( "employee", Employee.class );
        digester.addCallMethod( "employee/firstName", "setFirstName", 0 );
        digester.addCallMethod( "employee/lastName", "setLastName", 0 );

        digester.addObjectCreate( "employee/address", Address.class );
        digester.addCallMethod( "employee/address/type", "setType", 0 );
        digester.addCallMethod( "employee/address/city", "setCity", 0 );
        digester.addCallMethod( "employee/address/state", "setState", 0 );
        digester.addSetNext( "employee/address", "addAddress" );

    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown()
    {

        digester = null;

    }

    @Test
    public void testBadDocument() throws Exception
    {

        // Listen to validation errors
        final TestErrorHandler teh = new TestErrorHandler();
        digester.setErrorHandler( teh );

        // Parse our test input
        digester.parse( getInputStream( "Test13-02.xml" ) );
        assertFalse( teh.clean, "Test13-02 should generate errors in Schema validation" );

    }

    /**
     * Test XML Schema validation.
     */
    @Test
    public void testGoodDocument() throws Exception
    {

        // Listen to validation errors
        final TestErrorHandler teh = new TestErrorHandler();
        digester.setErrorHandler( teh );

        // Parse our test input
        final Employee employee = digester.parse( getInputStream( "Test13-01.xml" ) );
        assertNotNull( employee, "failed to parse an employee" );
        assertTrue( teh.clean, "Test13-01 should not generate errors in Schema validation" );

        // Test document has been processed
        final Address ha = employee.getAddress( "home" );
        assertNotNull( ha );
        assertEquals( "Home City", ha.getCity() );
        assertEquals( "HS", ha.getState() );

    }

}
