/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.digester3;

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringReader;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Tests for situations where CallMethodRule instances and their parameters overlap each other.
 * </p>
 */
public class OverlappingCallMethodRuleTestCase
{

    String itemId;

    String itemName;

    public void setItemId( final String id )
    {
        itemId = id;
    }

    public void setItemName( final String name )
    {
        itemName = name;
    }

    @Test
    void testItem1() throws Exception
    {
        final StringBuilder input = new StringBuilder();
        input.append( "<root>" );
        input.append( " <item id='1'>anitem</item>" );
        input.append( "</root>" );

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root/item" ).callMethod( "setItemId" ).withParamCount( 1 )
                    .then()
                    .callParam().fromAttribute( "id" )
                    .then()
                    .callMethod( "setItemName" ).withParamCount( 1 )
                    .then()
                    .callParam();
            }

        }).newDigester();

        this.itemId = null;
        this.itemName = null;
        digester.push( this );
        digester.parse( new StringReader( input.toString() ) );

        assertEquals( "1", this.itemId );
        assertEquals( "anitem", this.itemName );
    }

    @Test
    void testItem2() throws Exception
    {
        final StringBuilder input = new StringBuilder();
        input.append( "<root>" );
        input.append( " <item id='1'>anitem</item>" );
        input.append( "</root>" );

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root/item" ).callMethod( "setItemId" ).withParamCount( 1 )
                    .then()
                    .callParam().fromAttribute( "id" )
                    .then()
                    .callMethod( "setItemName" ).withParamCount( 1 )
                    .then()
                    .callParam();
            }

        }).newDigester();

        this.itemId = null;
        this.itemName = null;
        digester.push( this );
        digester.parse( new StringReader( input.toString() ) );

        assertEquals( "1", this.itemId );
        assertEquals( "anitem", this.itemName );
    }

    @Test
    void testItem3() throws Exception
    {
        final StringBuilder input = new StringBuilder();
        input.append( "<root>" );
        input.append( " <item>1</item>" );
        input.append( "</root>" );

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root/item" ).callMethod( "setItemId" ).withParamCount( 1 )
                    .then()
                    .callParam().fromAttribute( "id" )
                    .then()
                    .callMethod( "setItemName" ).withParamCount( 1 )
                    .then()
                    .callParam();
            }

        }).newDigester();

        digester.addCallMethod( "root/item", "setItemId", 1 );
        digester.addCallParam( "root/item", 0 );
        digester.addCallMethod( "root/item", "setItemName", 1 );
        digester.addCallParam( "root/item", 0 );

        this.itemId = null;
        this.itemName = null;
        digester.push( this );
        digester.parse( new StringReader( input.toString() ) );

        assertEquals( "1", this.itemId );
        assertEquals( "1", this.itemName );
    }

    /**
     * This is an "anti-test" that demonstrates how digester can <em>fails</em> to produce the correct results, due to a
     * design flaw (or at least limitation) in the way that CallMethodRule and CallParamRule work.
     * <p>
     * The following sequence always fails:
     * <ul>
     * <li>CallMethodRule A fires (pushing params array)</li>
     * <li>CallMethodRule B fires (pushing params array)</li>
     * <li>params rule for A fires --> writes to params of method B!</li>
     * <li>params rule for B fires --> overwrites params for method B</li>
     * </ul>
     * The result is that method "b" appears to work ok, but method "a" loses its input parameters.
     * <p>
     * One solution is for CallParamRule objects to know which CallMethodRule they are associated with. Even this might
     * fail in corner cases where the same rule is associated with multiple patterns, or with wildcard patterns which
     * cause a rule to fire in a "recursive" manner. However implementing this is not possible with the current digester
     * design.
     */
    @Test
    void testItem4() throws Exception
    {
        final StringBuilder input = new StringBuilder();
        input.append( "<root>" );
        input.append( " <item>" );
        input.append( "  <id value='1'/>" );
        input.append( "  <name value='name'/>" );
        input.append( " </item>" );
        input.append( "</root>" );

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root/item" ).callMethod( "setItemId" ).withParamCount( 1 )
                    .then()
                    .callMethod( "setItemName" ).withParamCount( 1 );
                forPattern( "root/item/id" ).callParam().fromAttribute( "value" );
                forPattern( "root/item/name" ).callParam().fromAttribute( "value" );
            }

        }).newDigester();

        this.itemId = null;
        this.itemName = null;
        digester.push( this );
        digester.parse( new StringReader( input.toString() ) );

        // These are the "correct" results
        // assertEquals("1", this.itemId);
        // assertEquals("name", this.itemName);

        // These are what actually happens
        assertNull( this.itemId );
        assertEquals( "name", this.itemName );
    }

    /**
     * This test checks that CallParamRule instances which fetch data from XML attributes work ok when invoked
     * "recursively", ie a rule instances' methods gets called in the order
     * begin[1]/begin[2]/body[2]/end[2]/body[1]/end[1]
     */
    @Test
    void testWildcard1() throws Exception
    {
        final StringBuilder input = new StringBuilder();
        input.append( "<box id='A1'>" );
        input.append( " <box id='B1'>" );
        input.append( "  <box id='C1'/>" );
        input.append( "  <box id='C2'/>" );
        input.append( " </box>" );
        input.append( "</box>" );

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "*/box" ).createObject().ofType( Box.class )
                    .then()
                    .callMethod( "setId" ).withParamCount( 1 )
                    .then()
                    .callParam().fromAttribute( "id" )
                    .then()
                    .setNext( "addChild" );
            }

        }).newDigester();

        final Box root = new Box();
        root.setId( "root" );
        digester.push( root );
        digester.parse( new StringReader( input.toString() ) );

        // walk the object tree, concatenating the id strings
        final String ids = root.getIds();
        assertEquals( "root A1 B1 C1 C2", ids );
    }

    /**
     * This test checks that CallParamRule instances which fetch data from the XML element body work ok when invoked
     * "recursively", ie a rule instances' methods gets called in the order
     * begin[1]/begin[2]/body[2]/end[2]/body[1]/end[1]
     */
    @Test
    void testWildcard2() throws Exception
    {
        final StringBuilder input = new StringBuilder();
        input.append( "<box>A1" );
        input.append( " <box>B1" );
        input.append( "  <box>C1</box>" );
        input.append( "  <box>C2</box>" );
        input.append( " </box>" );
        input.append( "</box>" );

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "*/box" ).createObject().ofType( Box.class )
                    .then()
                    .callMethod( "setId" ).withParamCount( 1 )
                    .then()
                    .callParam()
                    .then()
                    .setNext( "addChild" );
            }

        }).newDigester();

        final Box root = new Box();
        root.setId( "root" );
        digester.push( root );
        digester.parse( new StringReader( input.toString() ) );

        // walk the object tree, concatenating the id strings
        final String ids = root.getIds();
        assertEquals( "root A1 B1 C1 C2", ids );
    }
}
