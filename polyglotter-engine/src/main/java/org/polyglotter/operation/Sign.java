/*
 * Polyglotter (http://polyglotter.org)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * Polyglotter is free software. Unless otherwise indicated, all code in Polyglotter
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * Polyglotter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.polyglotter.operation;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.polyglotter.PolyglotterI18n;
import org.polyglotter.grammar.GrammarFactory;
import org.polyglotter.grammar.ValidationProblem;

/**
 * Calculates the sign (0, 1, or -1) of a number.
 * 
 * @see Math#signum(double)
 * @see Math#signum(float)
 * @see BigInteger#signum()
 * @see BigDecimal#signum()
 */
public class Sign extends BaseOperation< Integer > {

    /**
     * @param id
     *        the sign operation's unique identifier (cannot be <code>null</code>)
     * @param transformId
     *        the owning transform identifier (cannot be <code>null</code>)
     * @throws IllegalArgumentException
     *         if any inputs are <code>null</code>
     */
    public Sign( final QName id,
                 final QName transformId ) {
        super( id, transformId );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.operation.BaseOperation#abbreviation()
     */
    @Override
    public String abbreviation() {
        return "signum";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.operation.BaseOperation#calculate()
     */
    @Override
    protected Integer calculate() {
        assert !problems().isError();
        final Number value = ( Number ) terms().get( 0 ).value();

        if ( value instanceof BigDecimal ) return ( ( BigDecimal ) value ).signum();
        if ( value instanceof BigInteger ) return ( ( BigInteger ) value ).signum();
        if ( value instanceof Float ) return Double.valueOf( Math.signum( ( Float ) value ) ).intValue();

        return Double.valueOf( Math.signum( value.doubleValue() ) ).intValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.grammar.Operation#category()
     */
    @Override
    public Category category() {
        return Category.ARITHMETIC;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.grammar.GrammarPart#description()
     */
    @Override
    public String description() {
        return PolyglotterI18n.signOperationDescription.text();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.operation.BaseOperation#maxTerms()
     */
    @Override
    public int maxTerms() {
        return 1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.operation.BaseOperation#minTerms()
     */
    @Override
    public int minTerms() {
        return 1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.grammar.GrammarPart#name()
     */
    @Override
    public String name() {
        return PolyglotterI18n.signOperationName.text();
    }

    /**
     * Validates the operation's state.
     */
    @Override
    protected void validate() {
        // make sure there is one term
        if ( terms().size() != 1 ) {
            final ValidationProblem problem =
                GrammarFactory.createError( id(), PolyglotterI18n.signOperationMustHaveOneTerm.text( id() ) );
            problems().add( problem );
        } else {
            // must be a number
            final Object value = terms().get( 0 ).value();

            if ( !( value instanceof Number ) ) {
                final ValidationProblem problem =
                    GrammarFactory.createError( id(),
                                                PolyglotterI18n.signOperationInvalidTermType.text( id(),
                                                                                                   terms().get( 0 ).id() ) );
                problems().add( problem );
            }
        }
    }

}
