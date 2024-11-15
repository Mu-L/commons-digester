package org.apache.commons.digester3.examples.api.documentmarkup;

/*
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

import java.io.StringReader;

/**
 * A simple "test harness" which demonstrates how the MarkupDigester class
 * (plus the supporting interface/rule classes) can process "document-markup"
 * style XML data.
 * <p>
 * See the readme file included with this example for more information.
 */
public class Main
{

    /** See the run method. */
    public static void main( final String[] args )
        throws Exception
    {
        new Main().run();
    }

    /** The input XML to be parsed by this example. */
    String in = "<p>Hi, this is an <em>example</em> of some <strong>bold</strong> text.</p>";

    /**
     * Invoked via a standard Digester CallMethodRule, passing the
     * "body text" of the top-level XML element. This demonstrates
     * the default behavior of Digester (which is not suitable for
     * processing markup-style XML).
     */
    public void addAllText( final String text )
    {
        System.out.println( "And the merged text for the p element is [" + text + "]" );
    }

    /** Invoked when an &lt;b&gt; node is found in the parsed input. */
    public void addBold( final String text )
    {
        System.out.println( "Bold: [" + text + "]" );
    }

    /** Invoked when an &lt;i&gt; node is found in the parsed input. */
    public void addItalic( final String text )
    {
        System.out.println( "Italic: [" + text + "]" );
    }

    /** Invoked when a text segment is present in the parsed input. */
    public void addSegment( final String text )
    {
        System.out.println( "Text segment: [" + text + "]" );
    }

    /**
     * Main method of this test harness. Set up some digester rules,
     * then parse the input XML contained in the "in" member variable.
     * The rules cause methods on this object to be invoked, which just
     * dump information to standard output, to show the callbacks that
     * a real program could arrange to get when parsing markup input.
     */
    public void run()
        throws Exception
    {
        System.out.println( "Started." );
        final MarkupDigester d = new MarkupDigester();

        d.push( this );

        final SetTextSegmentRule r = new SetTextSegmentRule( "addSegment" );
        d.addRule( "p", r );
        d.addCallMethod( "p", "addAllText", 0 );

        d.addCallMethod( "p/i", "addItalic", 0 );
        d.addCallMethod( "p/b", "addBold", 0 );

        d.parse( new StringReader( in ) );

        System.out.println( "Finished." );
    }

}
