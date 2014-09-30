/*
 * Chrysalix
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * Chrysalix is free software. Unless otherwise indicated, all code in Chrysalix
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * Chrysalix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.chrysalix.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chrysalix.ChrysalixException;
import org.chrysalix.ChrysalixI18n;
import org.chrysalix.transformation.Operation;
import org.chrysalix.transformation.OperationDescriptor;
import org.chrysalix.transformation.Transformation;
import org.chrysalix.transformation.TransformationFactory;
import org.chrysalix.transformation.ValidationProblem;
import org.chrysalix.transformation.ValidationProblems;
import org.chrysalix.transformation.Value;
import org.chrysalix.transformation.ValueDescriptor;
import org.modelspace.ModelObject;
import org.modelspace.ModelspaceException;

/**
 * Calculates the mode, or most common term, of a collection of terms.
 */
public final class Mode extends AbstractOperation< Number[] > {

    static final String DESCRIPTION = "Finds the mode of two or more numbers";
    private static final String INPUT_DESCRIPTION = "An input term used to determine the mode value of a set of terms.";
    private static final String INPUT_NAME = "Input";
    static final String NAME = "Mode";

    /**
     * The input term descriptor.
     */
    public static final ValueDescriptor< Number > TERM_DESCRIPTOR =
        TransformationFactory.createValueDescriptor( TransformationFactory.createId( Mode.class, "input" ),
                                                     ChrysalixI18n.localize( INPUT_DESCRIPTION ),
                                                     ChrysalixI18n.localize( INPUT_NAME ),
                                                     Number.class,
                                                     true,
                                                     1,
                                                     true );

    /**
     * The input descriptors.
     */
    private static final ValueDescriptor< ? >[] INPUT_DESCRIPTORS = { TERM_DESCRIPTOR };

    /**
     * The output descriptor.
     */
    public static final OperationDescriptor< Number[] > DESCRIPTOR =
        new AbstractOperationDescriptor< Number[] >( TransformationFactory.createId( Mode.class ),
                                                     ChrysalixI18n.localize( DESCRIPTION ),
                                                     ChrysalixI18n.localize( NAME ),
                                                     Number[].class,
                                                     INPUT_DESCRIPTORS ) {

            /**
             * {@inheritDoc}
             * 
             * @see org.chrysalix.transformation.OperationDescriptor#newInstance(org.modelspace.ModelObject,
             *      org.chrysalix.transformation.Transformation)
             */
            @Override
            public Operation< Number[] > newInstance( final ModelObject operation,
                                                      final Transformation transformation ) throws ModelspaceException, ChrysalixException {
                return new Mode( operation, transformation );
            }

        };

    /**
     * @param operation
     *        the operation model object (cannot be <code>null</code>)
     * @param transformation
     *        the transformation containing this operation (cannot be <code>null</code>)
     * @throws ModelspaceException
     *         if an error with the model object occurs
     * @throws ChrysalixException
     *         if a non-model object error occurs
     * @throws IllegalArgumentException
     *         if the input is <code>null</code>
     */
    Mode( final ModelObject operation,
          final Transformation transformation ) throws ModelspaceException, ChrysalixException {
        super( operation, transformation );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.chrysalix.operation.AbstractOperation#calculate()
     */
    @Override
    protected Number[] calculate() throws ChrysalixException {
        assert !problems().isError();
        final Map< Number, Integer > result = new HashMap<>();
        int max = 0;
        final List< Number > maxElems = new ArrayList<>();

        for ( final Value< ? > term : inputs() ) {
            final Number value = ( Number ) term.get();

            if ( result.containsKey( value ) ) {
                result.put( value, ( result.get( value ) + 1 ) );
            } else {
                result.put( value, 1 );
            }

            if ( result.get( value ) > max ) {
                max = result.get( value );
                maxElems.clear();
                maxElems.add( value );
            } else if ( result.get( value ) == max ) {
                maxElems.add( value );
            }
        }

        if ( maxElems.size() == inputs().length ) return new Number[ 0 ];
        return maxElems.toArray( new Number[ maxElems.size() ] );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.chrysalix.operation.AbstractOperation#problems()
     */
    @Override
    public ValidationProblems problems() throws ChrysalixException {
        this.problems.clear();

        // make sure there are terms
        if ( inputs().length == 0 ) {
            final ValidationProblem problem =
                TransformationFactory.createError( transformationId(),
                                                   ChrysalixI18n.localize( AbstractOperation.HAS_NO_TERMS, transformationId() ) );
            problems().add( problem );
        } else {
            if ( inputs().length < INPUT_DESCRIPTORS[ 0 ].requiredValueCount() ) {
                final ValidationProblem problem =
                    TransformationFactory.createError( transformationId(),
                                                       ChrysalixI18n.localize( AbstractOperation.INVALID_TERM_COUNT,
                                                                               NAME,
                                                                               transformationId(),
                                                                               inputs().length ) );
                problems().add( problem );
            }

            // make sure all the terms have types of Number
            for ( final Value< ? > term : inputs() ) {
                Object value;

                try {
                    value = term.get();

                    if ( !( value instanceof Number ) ) {
                        final ValidationProblem problem =
                            TransformationFactory.createError( transformationId(),
                                                               ChrysalixI18n.localize( AbstractOperation.INVALID_TERM_TYPE,
                                                                                       NAME,
                                                                                       transformationId() ) );
                        problems().add( problem );
                    }
                } catch ( final ChrysalixException e ) {
                    final ValidationProblem problem =
                        TransformationFactory.createError( transformationId(),
                                                           ChrysalixI18n.localize( AbstractOperation.OPERATION_VALIDATION_ERROR,
                                                                                   NAME,
                                                                                   transformationId() ) );
                    problems().add( problem );
                }
            }
        }

        return super.problems();
    }

}
