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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

//import org.apache.commons.logging.impl.SimpleLog;

/**
 * <p>
 * Tests for the {@code CallMethodRule} and associated {@code CallParamRule}.
 */
public class CallMethodRuleTestCase
{

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
     * Test method calls with the CallMethodRule rule. It should be possible to call a method with no arguments using
     * several rule syntaxes.
     */
    @Test
    void testBasic() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class );
                // try all syntax permutations
                forPattern( "employee" ).callMethod( "toString" ).withParamCount( 0 ).withParamTypes( (Class[]) null )
                    .then()
                    .callMethod( "toString" ).withParamCount( 0 ).withParamTypes( (String[]) null )
                    .then()
                    .callMethod( "toString" ).withParamCount( 0 ).withParamTypes( new Class[] {} )
                    .then()
                    .callMethod( "toString" ).withParamCount( 0 ).withParamTypes( new String[] {} )
                    .then()
                    .callMethod( "toString" );
            }

        }).newDigester();

        // Parse our test input.
        // An exception will be thrown if the method can't be found
        final Employee root1 = digester.parse( getInputStream( "Test5.xml" ) );
        assertNotNull( root1 );
    }

    /**
     * Test invoking an object which does not exist on the stack.
     */
    @Test
    void testCallInvalidTarget()
    {

        final Digester digester = new Digester();
        digester.addObjectCreate( "employee", HashMap.class );

        // there should be only one object on the stack (index zero),
        // so selecting a target object with index 1 on the object stack
        // should result in an exception.
        final CallMethodRule r = new CallMethodRule( 1, "put", 0 );
        digester.addRule( "employee", r );

        assertThrows( SAXException.class, () -> digester.parse( getInputStream( "Test5.xml" ) ), "Exception should be thrown for invalid target offset" );
    }

    /**
     * Test method calls with the CallMethodRule reading from the element body, with no CallParamMethod rules added.
     */
    @Test
    void testCallMethodOnly()
        throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class );
                forPattern( "employee/firstName" ).callMethod( "setFirstName" ).usingElementBodyAsArgument();
                forPattern( "employee/lastName" ).callMethod( "setLastName" ).usingElementBodyAsArgument();
            }

        }).newDigester();

        // Parse our test input
        final Employee employee = digester.parse( getInputStream( "Test9.xml" ) );
        assertNotNull( employee, "parsed an employee" );

        // Validate that the property setters were called
        assertEquals( "First Name", employee.getFirstName(), "Set first name" );
        assertEquals( "Last Name", employee.getLastName(), "Set last name" );
    }

    /**
     * Test invoking an object which is at top-1 on the stack, like SetNextRule does...
     */
    @Test
    void testCallNext()
        throws Exception
    {

        final Digester digester = new Digester();
        digester.addObjectCreate( "employee", HashMap.class );

        digester.addObjectCreate( "employee/address", Address.class );
        digester.addSetNestedProperties( "employee/address" );
        final CallMethodRule r = new CallMethodRule( 1, "put", 2 );
        digester.addRule( "employee/address", r );
        digester.addCallParam( "employee/address/type", 0 );
        digester.addCallParam( "employee/address", 1, 0 );

        final HashMap<String, Address> map = digester.parse( getInputStream( "Test5.xml" ) );

        assertNotNull( map );
        final Set<String> keys = map.keySet();
        assertEquals( 2, keys.size() );
        final Address home = map.get( "home" );
        assertNotNull( home );
        assertEquals( "HmZip", home.getZipCode() );
        final Address office = map.get( "office" );
        assertNotNull( office );
        assertEquals( "OfZip", office.getZipCode() );
    }

    /**
     * Test invoking an object which is at the root of the stack, like SetRoot does...
     */
    @Test
    void testCallRoot()
        throws Exception
    {

        final Digester digester = new Digester();
        digester.addObjectCreate( "employee", HashMap.class );

        digester.addObjectCreate( "employee/address", Address.class );
        digester.addSetNestedProperties( "employee/address" );
        final CallMethodRule r = new CallMethodRule( -1, "put", 2 );
        digester.addRule( "employee/address", r );
        digester.addCallParam( "employee/address/type", 0 );
        digester.addCallParam( "employee/address", 1, 0 );

        final HashMap<String, Address> map = digester.parse( getInputStream( "Test5.xml" ) );

        assertNotNull( map );
        final Set<String> keys = map.keySet();
        assertEquals( 2, keys.size() );
        final Address home = map.get( "home" );
        assertNotNull( home );
        assertEquals( "HmZip", home.getZipCode() );
        final Address office = map.get( "office" );
        assertNotNull( office );
        assertEquals( "OfZip", office.getZipCode() );
    }

    @Test
    void testFromStack()
        throws Exception
    {

        final StringReader reader =
            new StringReader( "<?xml version='1.0' ?><root><one/><two/><three/><four/><five/></root>" );

        final Digester digester = new Digester();

        final Class<?>[] params = { String.class };

        digester.addObjectCreate( "root/one", NamedBean.class );
        digester.addSetNext( "root/one", "add" );
        digester.addCallMethod( "root/one", "setName", 1, params );
        digester.addCallParam( "root/one", 0, 2 );

        digester.addObjectCreate( "root/two", NamedBean.class );
        digester.addSetNext( "root/two", "add" );
        digester.addCallMethod( "root/two", "setName", 1, params );
        digester.addCallParam( "root/two", 0, 3 );

        digester.addObjectCreate( "root/three", NamedBean.class );
        digester.addSetNext( "root/three", "add" );
        digester.addCallMethod( "root/three", "setName", 1, params );
        digester.addCallParam( "root/three", 0, 4 );

        digester.addObjectCreate( "root/four", NamedBean.class );
        digester.addSetNext( "root/four", "add" );
        digester.addCallMethod( "root/four", "setName", 1, params );
        digester.addCallParam( "root/four", 0, 5 );

        digester.addObjectCreate( "root/five", NamedBean.class );
        digester.addSetNext( "root/five", "add" );
        final Class<?>[] newParams = { String.class, String.class };
        digester.addCallMethod( "root/five", "test", 2, newParams );
        digester.addCallParam( "root/five", 0, 10 );
        digester.addCallParam( "root/five", 1, 3 );

        // prepare stack
        digester.push( "That lamb was sure to go." );
        digester.push( "And everywhere that Mary went," );
        digester.push( "It's fleece was white as snow." );
        digester.push( "Mary had a little lamb," );

        final ArrayList<NamedBean> list = new ArrayList<>();
        digester.push( list );
        digester.parse( reader );

        assertEquals( 5, list.size(), "Wrong number of beans in list" );
        NamedBean bean = list.get( 0 );
        assertEquals( "Mary had a little lamb,", bean.getName(), "Parameter not set from stack (1)" );
        bean = list.get( 1 );
        assertEquals( "It's fleece was white as snow.", bean.getName(), "Parameter not set from stack (2)" );
        bean = list.get( 2 );
        assertEquals( "And everywhere that Mary went,", bean.getName(), "Parameter not set from stack (3)" );
        bean = list.get( 3 );
        assertEquals( "That lamb was sure to go.", bean.getName(), "Parameter not set from stack (4)" );
        bean = list.get( 4 );
        assertNull( bean.getName(), "Out of stack not set to null" );
    }

    @Test
    void testNestedBody()
        throws Exception
    {

        final StringReader reader =
            new StringReader( "<?xml version='1.0' ?><root><spam>Simple</spam>"
                + "<spam>Complex<spam>Deep<spam>Deeper<spam>Deepest</spam></spam></spam></spam></root>" );

        final Digester digester = new Digester();

        // SimpleLog log = new SimpleLog("[testPrimitiveReading:Digester]");
        // log.setLevel(SimpleLog.LOG_LEVEL_TRACE);
        // digester.setLogger(log);

        digester.addObjectCreate( "root/spam", NamedBean.class );
        digester.addSetRoot( "root/spam", "add" );
        digester.addCallMethod( "root/spam", "setName", 1 );
        digester.addCallParam( "root/spam", 0 );

        digester.addObjectCreate( "root/spam/spam", NamedBean.class );
        digester.addSetRoot( "root/spam/spam", "add" );
        digester.addCallMethod( "root/spam/spam", "setName", 1 );
        digester.addCallParam( "root/spam/spam", 0 );

        digester.addObjectCreate( "root/spam/spam/spam", NamedBean.class );
        digester.addSetRoot( "root/spam/spam/spam", "add" );
        digester.addCallMethod( "root/spam/spam/spam", "setName", 1 );
        digester.addCallParam( "root/spam/spam/spam", 0 );

        digester.addObjectCreate( "root/spam/spam/spam/spam", NamedBean.class );
        digester.addSetRoot( "root/spam/spam/spam/spam", "add" );
        digester.addCallMethod( "root/spam/spam/spam/spam", "setName", 1 );
        digester.addCallParam( "root/spam/spam/spam/spam", 0 );

        final ArrayList<NamedBean> list = new ArrayList<>();
        digester.push( list );
        digester.parse( reader );

        NamedBean bean = list.get( 0 );
        assertEquals( "Simple", bean.getName(), "Wrong name (1)" );
        // these are added in deepest first order by the addRootRule
        bean = list.get( 4 );
        assertEquals( "Complex", bean.getName(), "Wrong name (2)" );
        bean = list.get( 3 );
        assertEquals( "Deep", bean.getName(), "Wrong name (3)" );
        bean = list.get( 2 );
        assertEquals( "Deeper", bean.getName(), "Wrong name (4)" );
        bean = list.get( 1 );
        assertEquals( "Deepest", bean.getName(), "Wrong name (5)" );
    }

    /**
     * Test that the target object for a CallMethodRule is the object that was on top of the object stack when the
     * CallMethodRule fired, even when other rules fire between the CallMethodRule and its associated CallParamRules.
     * <p>
     * The current implementation of CallMethodRule ensures this works by firing only at the end of the tag that
     * CallMethodRule triggered on.
     */
    @Test
    void testOrderNestedPartA()
        throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // Here, we use the "grandchild element name" as a parameter to
                // the created element, to ensure that all the params aren't
                // available to the CallMethodRule until some other rules have fired,
                // in particular an ObjectCreateRule. The CallMethodRule should still
                // function correctly in this scenario.
                forPattern( "toplevel/element" ).createObject().ofType( NamedBean.class )
                    .then()
                    .callMethod( "setName" ).withParamCount( 1 );
                forPattern( "toplevel/element/element/element" ).callParam().ofIndex( 0 ).fromAttribute( "name" );
                forPattern( "toplevel/element/element" ).createObject().ofType( NamedBean.class );
            }

        }).newDigester();

       // Parse our test input
       // an exception will be thrown if the method can't be found
        final NamedBean root1 = digester.parse( getInputStream( "Test8.xml" ) );

        // if the CallMethodRule were to incorrectly invoke the method call
        // on the second-created NamedBean instance, then the root one would
        // have a null name. If it works correctly, the target element will
        // be the first-created (root) one, despite the fact that a second
        // object instance was created between the firing of the
        // CallMethodRule and its associated CallParamRule.
        assertEquals( "C", root1.getName(), "Wrong method call order" );
    }

    /**
     * Test nested CallMethod rules.
     * <p>
     * The current implementation of CallMethodRule, in which the method is invoked in its end() method, causes
     * behavior which some users find non-intuitive. In this test it can be seen to "reverse" the order of data
     * processed. However this is the way CallMethodRule has always behaved, and it is expected that apps out there rely
     * on this call order so this test is present to ensure that no-one changes this behavior.
     */
    @Test
    void testOrderNestedPartB()
        throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "*/element" ).callMethod( "append" ).withParamCount( 1 )
                    .then()
                    .callParam().ofIndex( 0 ).fromAttribute( "name" );
            }

        }).newDigester();

        // Configure the digester as required
        final StringBuilder word = new StringBuilder();
        digester.push( word );

        // Parse our test input
        // an exception will be thrown if the method can't be found
        assertNotNull( digester.parse( getInputStream( "Test8.xml" ) ) );

        assertEquals( "CBA", word.toString(), "Wrong method call order" );
    }

    /**
     * This tests the call methods params enhancement that provides for more complex stack-based calls.
     */
    @Test
    void testParamsFromStack() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "map" ).createObject().ofType( HashMap.class )
                    .then()
                    .callMethod( "put" ).withParamCount( 2 );
                forPattern( "map/key" ).createObject().ofType( AlphaBean.class )
                    .then()
                    .setProperties()
                    .then()
                    .callParam().fromStack( true );
                forPattern( "map/value" ).createObject().ofType( BetaBean.class )
                    .then()
                    .setProperties()
                    .then()
                    .callParam().ofIndex( 1 ).fromStack( true );
            }

        }).newDigester();

        final StringBuilder xml =
            new StringBuilder().append( "<?xml version='1.0'?>" ).append( "<map>" ).append( "  <key name='The key'/>" ).append( "  <value name='The value'/>" ).append( "</map>" );

        final HashMap<AlphaBean, BetaBean> map = digester.parse( new StringReader( xml.toString() ) );

        assertNotNull( map );
        assertEquals( 1, map.size() );
        assertEquals( "The key", map.keySet().iterator().next().getName() );
        assertEquals( "The value", map.values().iterator().next().getName() );
    }

    /** Test for the PathCallParamRule */
    @Test
    void testPathCallParam()
        throws Exception
    {
        final String xml =
            "<?xml version='1.0'?><main><alpha><beta>Ignore this</beta></alpha>"
                + "<beta><epsilon><gamma>Ignore that</gamma></epsilon></beta></main>";

        final SimpleTestBean bean = new SimpleTestBean();
        bean.setAlphaBeta( "[UNSET]", "[UNSET]" );

        final StringReader in = new StringReader( xml );
        final Digester digester = new Digester();
        digester.setRules( new ExtendedBaseRules() );
        digester.addCallParamPath( "*/alpha/?", 0 );
        digester.addCallParamPath( "*/epsilon/?", 1 );
        digester.addCallMethod( "main", "setAlphaBeta", 2 );

        digester.push( bean );

        digester.parse( in );

        assertEquals( "main/alpha/beta", bean.getAlpha(), "Test alpha property setting" );
        assertEquals( "main/beta/epsilon/gamma", bean.getBeta(), "Test beta property setting" );
    }

    @Test
    void testPrimitiveReading()
        throws Exception
    {
        final StringReader reader =
            new StringReader( "<?xml version='1.0' ?><root><bean good='true'/><bean good='false'/><bean/>"
                + "<beanie bad='Fee Fie Foe Fum' good='true'/><beanie bad='Fee Fie Foe Fum' good='false'/>"
                + "<beanie bad='Fee Fie Foe Fum'/></root>" );

        final Digester digester = new Digester();

        // SimpleLog log = new SimpleLog("[testPrimitiveReading:Digester]");
        // log.setLevel(SimpleLog.LOG_LEVEL_TRACE);
        // digester.setLogger(log);

        digester.addObjectCreate( "root/bean", PrimitiveBean.class );
        digester.addSetNext( "root/bean", "add" );
        final Class<?>[] params = { Boolean.TYPE };
        digester.addCallMethod( "root/bean", "setBoolean", 1, params );
        digester.addCallParam( "root/bean", 0, "good" );

        digester.addObjectCreate( "root/beanie", PrimitiveBean.class );
        digester.addSetNext( "root/beanie", "add" );
        final Class<?>[] beanieParams = { String.class, Boolean.TYPE };
        digester.addCallMethod( "root/beanie", "testSetBoolean", 2, beanieParams );
        digester.addCallParam( "root/beanie", 0, "bad" );
        digester.addCallParam( "root/beanie", 1, "good" );

        final ArrayList<PrimitiveBean> list = new ArrayList<>();
        digester.push( list );
        digester.parse( reader );

        assertEquals( 6, list.size(), "Wrong number of beans in list" );
        PrimitiveBean bean = list.get( 0 );
        assertTrue( bean.getSetBooleanCalled(), "Bean 0 property not called" );
        assertTrue( bean.getBoolean(), "Bean 0 property incorrect" );
        bean = list.get( 1 );
        assertTrue( bean.getSetBooleanCalled(), "Bean 1 property not called" );
        assertFalse( bean.getBoolean(), "Bean 1 property incorrect" );
        bean = list.get( 2 );
        // no attribute, no call is what's expected
        assertFalse( bean.getSetBooleanCalled(), "Bean 2 property called" );
        bean = list.get( 3 );
        assertTrue( bean.getSetBooleanCalled(), "Bean 3 property not called" );
        assertTrue( bean.getBoolean(), "Bean 3 property incorrect" );
        bean = list.get( 4 );
        assertTrue( bean.getSetBooleanCalled(), "Bean 4 property not called" );
        assertFalse( bean.getBoolean(), "Bean 4 property incorrect" );
        bean = list.get( 5 );
        assertTrue( bean.getSetBooleanCalled(), "Bean 5 property not called" );
        assertFalse( bean.getBoolean(), "Bean 5 property incorrect" );
    }

    @Test
    void testProcessingHook()
        throws Exception
    {

        final class TestCallMethodRule
            extends CallMethodRule
        {
            Object result;

            TestCallMethodRule( final String methodName, final int paramCount )
            {
                super( methodName, paramCount );
            }

            @Override
            protected void processMethodCallResult( final Object result )
            {
                this.result = result;
            }
        }

        final StringReader reader =
            new StringReader( "<?xml version='1.0' ?><root>"
                + "<param class='float' coolness='false'>90</param></root>" );

        final Digester digester = new Digester();
        // SimpleLog log = new SimpleLog("{testTwoCalls:Digester]");
        // log.setLevel(SimpleLog.LOG_LEVEL_TRACE);
        // digester.setLogger(log);

        digester.addObjectCreate( "root/param", ParamBean.class );
        digester.addSetNext( "root/param", "add" );
        final TestCallMethodRule rule = new TestCallMethodRule( "setThisAndThat", 2 );
        digester.addRule( "root/param", rule );
        digester.addCallParam( "root/param", 0, "class" );
        digester.addCallParam( "root/param", 1, "coolness" );

        final ArrayList<ParamBean> list = new ArrayList<>();
        digester.push( list );
        digester.parse( reader );

        assertEquals( 1, list.size(), "Wrong number of objects created" );
        assertEquals( "The Other", rule.result, "Result not passed into hook" );
    }

    /**
     * Test CallMethodRule variants which specify the classes of the parameters to target methods. String, int, boolean,
     * float should all be acceptable as parameter types.
     */
    @Test
    void testSettingProperties() throws Exception
    {
        Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class )
                    .then()
                    .callMethod( "setLastName" ).withParamTypes( "java.lang.String" );
                forPattern( "employee/lastName" ).callParam().ofIndex( 0 );
            }

        }).newDigester();

        // Parse our test input

        // an exception will be thrown if the method can't be found
        Employee employee = digester.parse( getInputStream( "Test5.xml" ) );
        assertEquals( "Last Name", employee.getLastName(), "Failed to call Employee.setLastName" );

        digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class )
                    .then()
                    .callMethod( "setAge" ).withParamTypes( int.class );
                forPattern( "employee/age" ).callParam();
            }

        }).newDigester();

        // Parse our test input
        // an exception will be thrown if the method can't be found
        employee = digester.parse( getInputStream( "Test5.xml" ) );
        assertEquals( 21, employee.getAge(), "Failed to call Employee.setAge" );

        digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class )
                    .then()
                    .callMethod( "setActive" ).withParamTypes( boolean.class );
                forPattern( "employee/active" ).callParam();
            }

        }).newDigester();

        // Parse our test input
        // an exception will be thrown if the method can't be found
        employee = digester.parse( getInputStream( "Test5.xml" ) );
        assertTrue( employee.isActive(), "Failed to call Employee.setActive" );

        digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class )
                    .then()
                    .callMethod( "setSalary" ).withParamTypes( float.class );
                forPattern( "employee/salary" ).callParam();
            }

        }).newDigester();

        // Parse our test input
        // an exception will be thrown if the method can't be found
        employee = digester.parse( getInputStream( "Test5.xml" ) );
        assertEquals( 1000000.0f, employee.getSalary(), 0.1f, "Failed to call Employee.setSalary" );
    }

    @Test
    void testTwoCalls()
        throws Exception
    {

        final StringReader reader =
            new StringReader( "<?xml version='1.0' ?><root><param class='int' coolness='true'>25</param>"
                + "<param class='long'>50</param><param class='float' coolness='false'>90</param></root>" );

        final Digester digester = new Digester();
        // SimpleLog log = new SimpleLog("{testTwoCalls:Digester]");
        // log.setLevel(SimpleLog.LOG_LEVEL_TRACE);
        // digester.setLogger(log);

        digester.addObjectCreate( "root/param", ParamBean.class );
        digester.addSetNext( "root/param", "add" );
        digester.addCallMethod( "root/param", "setThisAndThat", 2 );
        digester.addCallParam( "root/param", 0, "class" );
        digester.addCallParam( "root/param", 1 );
        digester.addCallMethod( "root/param", "setCool", 1, new Class[] { boolean.class } );
        digester.addCallParam( "root/param", 0, "coolness" );

        final ArrayList<ParamBean> list = new ArrayList<>();
        digester.push( list );
        digester.parse( reader );

        assertEquals(3, list.size(), "Wrong number of objects created");
        ParamBean bean = list.get( 0 );
        assertTrue( bean.isCool(), "Coolness wrong (1)" );
        assertEquals( "int", bean.getThis(), "This wrong (1)" );
        assertEquals( "25", bean.getThat(), "That wrong (1)" );
        bean = list.get( 1 );
        assertFalse(bean.isCool(), "Coolness wrong (2)" );
        assertEquals( "long", bean.getThis(), "This wrong (2)" );
        assertEquals( "50", bean.getThat(), "That wrong (2)" );
        bean = list.get( 2 );
        assertFalse( bean.isCool(), "Coolness wrong (3)" );
        assertEquals( "float", bean.getThis(), "This wrong (3)" );
        assertEquals( "90", bean.getThat(), "That wrong (3)" );
    }

}
