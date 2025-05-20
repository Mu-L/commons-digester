package org.apache.commons.digester3.annotations.reflect;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * Class to supply the missing Java {@code AnnotatedElement} for method arguments.
 *
 * @since 2.1
 */
public final class MethodArgument
    implements AnnotatedElement
{

    /**
     * The method argument index.
     */
    private final int index;

    /**
     * The method argument type.
     */
    private final Class<?> parameterType;

    /**
     * The method argument annotations.
     */
    private final Annotation[] annotations;

    /**
     * Creates a new method argument as {@code AnnotatedElement}.
     *
     * @param index the method argument index.
     * @param parameterType the method argument type.
     * @param annotations the method argument annotations.
     */
    public MethodArgument( final int index, final Class<?> parameterType, final Annotation[] annotations )
    {
        if ( parameterType == null )
        {
            throw new IllegalArgumentException( "Argument 'parameterType' must be not null" );
        }
        if ( annotations == null )
        {
            throw new IllegalArgumentException( "Argument 'annotations' must be not null" );
        }

        this.index = index;
        this.parameterType = parameterType;
        this.annotations = annotations.clone();
    }

    @Override
    public <T extends Annotation> T getAnnotation( final Class<T> annotationType )
    {
        for ( final Annotation annotation : this.annotations )
        {
            if ( annotationType == annotation.annotationType() )
            {
                return annotationType.cast( annotation );
            }
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations()
    {
        return getAnnotationsArrayCopy();
    }

    /**
     * Returns an annotations array, copy of the declared annotations in this method argument.
     *
     * @return an annotations array, copy of the declared annotations in this method argument.
     */
    private Annotation[] getAnnotationsArrayCopy()
    {
        return this.annotations.clone();
    }

    @Override
    public Annotation[] getDeclaredAnnotations()
    {
        return getAnnotationsArrayCopy();
    }

    /**
     * Returns the method argument index.
     *
     * @return the method argument index.
     */
    public int getIndex()
    {
        return this.index;
    }

    /**
     * Returns the method argument type.
     *
     * @return the method argument type.
     */
    public Class<?> getParameterType()
    {
        return this.parameterType;
    }

    @Override
    public boolean isAnnotationPresent( final Class<? extends Annotation> annotationType )
    {
        for ( final Annotation annotation : this.annotations )
        {
            if ( annotationType == annotation.annotationType() )
            {
                return true;
            }
        }
        return false;
    }

}
