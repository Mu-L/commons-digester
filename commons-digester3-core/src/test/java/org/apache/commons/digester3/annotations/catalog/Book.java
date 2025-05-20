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
package org.apache.commons.digester3.annotations.catalog;

import java.util.Objects;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.FactoryCreate;

/**
 * @since 2.1
 */
@FactoryCreate( pattern = "catalog/book", factoryClass = BookFactory.class )
public final class Book
    implements Item
{

    private final String isbn;

    @BeanPropertySetter( pattern = "catalog/book/title" )
    private String title;

    @BeanPropertySetter( pattern = "catalog/book/author" )
    private String author;

    @BeanPropertySetter( pattern = "catalog/book/desc" )
    private String desc;

    public Book( final String isbn )
    {
        this.isbn = isbn;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Book other = (Book) obj;
        if ( !Objects.equals(this.author, other.getAuthor()) )
        {
            return false;
        }
        if ( !Objects.equals(this.desc, other.getDesc()) )
        {
            return false;
        }
        if ( !Objects.equals(this.isbn, other.getIsbn()) )
        {
            return false;
        }
        if ( !Objects.equals(this.title, other.getTitle()) )
        {
            return false;
        }
        return true;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public String getDesc()
    {
        return this.desc;
    }

    public String getIsbn()
    {
        return this.isbn;
    }

    public String getTitle()
    {
        return this.title;
    }

    @Override
    public void print()
    {
        System.out.println( toString() );
    }

    public void setAuthor( final String author )
    {
        this.author = author;
    }

    public void setDesc( final String desc )
    {
        this.desc = desc;
    }

    public void setTitle( final String title )
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return "Book [author=" + author + ", desc=" + desc + ", isbn=" + isbn + ", title=" + title + "]";
    }

}
