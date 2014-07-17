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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.polyglotter.PolyglotterI18n;
import org.polyglotter.TestConstants;
import org.polyglotter.common.PolyglotterException;
import org.polyglotter.grammar.Operation.Category;
import org.polyglotter.grammar.Term;

@SuppressWarnings( { "javadoc", "unchecked" } )
public final class AverageTest {

    private Average operation;

    @Before
    public void beforeEach() {
        this.operation = new Average( TestConstants.ID, TestConstants.TRANSFORM_ID );
    }

    @Test
    public void shouldAddOneTerm() throws PolyglotterException {
        this.operation.add( TestConstants.INT_1_TERM );
        assertThat( this.operation.terms().size(), is( 1 ) );
        assertThat( ( Term< Number > ) this.operation.get( TestConstants.INT_1_ID ), is( TestConstants.INT_1_TERM ) );
    }

    @Test
    public void shouldAverageIntegerAndDouble() throws PolyglotterException {
        this.operation.add( TestConstants.INT_1_TERM );
        this.operation.add( TestConstants.DOUBLE_1_TERM );
        assertThat( this.operation.terms(), hasItems( new Term< ? >[] { TestConstants.INT_1_TERM, TestConstants.DOUBLE_1_TERM } ) );
        assertThat( this.operation.result(), is( ( Number ) ( ( TestConstants.INT_1_VALUE + TestConstants.DOUBLE_1_VALUE ) / 2 ) ) );
    }

    @Test
    public void shouldAverageMultipleTerms() throws PolyglotterException {
        this.operation.add( TestConstants.INT_1_TERM, TestConstants.INT_2_TERM, TestConstants.DOUBLE_1_TERM );
        assertThat( this.operation.terms().size(), is( 3 ) );
        assertThat( this.operation.terms(), hasItems( new Term< ? >[] { TestConstants.INT_1_TERM, TestConstants.INT_2_TERM, TestConstants.DOUBLE_1_TERM } ) );
        assertThat( this.operation.result(), is( ( Number ) ( ( TestConstants.INT_1_VALUE + TestConstants.INT_2_VALUE + TestConstants.DOUBLE_1_VALUE ) / 3 ) ) );
    }

    @Test
    public void shouldCalculateIntegerResult() throws PolyglotterException {
        this.operation.add( TestConstants.INT_1_TERM );
        this.operation.add( TestConstants.INT_2_TERM );
        assertThat( this.operation.result().intValue(), is( ( TestConstants.INT_1_VALUE + TestConstants.INT_2_VALUE ) / 2 ) );
    }

    @Test
    public void shouldGetResultWithOnlyOneTerm() throws PolyglotterException {
        this.operation.add( TestConstants.INT_1_TERM );
        assertThat( this.operation.result(), is( ( Number ) TestConstants.INT_1_VALUE ) );
    }

    @Test
    public void shouldHaveAbbreviation() {
        assertThat( this.operation.descriptor().abbreviation(), is( "avg" ) );
    }

    @Test
    public void shouldHaveCorrectCategory() {
        assertThat( this.operation.descriptor().category(), is( Category.ARITHMETIC ) );
    }

    @Test
    public void shouldHaveErrorsAfterConstruction() {
        assertThat( this.operation.problems().isError(), is( true ) );
    }

    @Test
    public void shouldHaveErrorWhenAveragingWithStringTerm() throws PolyglotterException {
        this.operation.add( TestConstants.INT_1_TERM, TestConstants.INT_2_TERM ); // will get rid of current problems
        this.operation.add( TestConstants.STRING_1_TERM );
        assertThat( this.operation.problems().size(), is( 1 ) );
        assertThat( this.operation.problems().isError(), is( true ) );
    }

    @Test
    public void shouldHaveProblemsAfterConstruction() {
        assertThat( this.operation.problems().isEmpty(), is( false ) );
    }

    @Test( expected = PolyglotterException.class )
    public void shouldNotBeAbleToGetResultAfterConstruction() throws PolyglotterException {
        this.operation.result();
    }

    @Test( expected = UnsupportedOperationException.class )
    public void shouldNotBeAbleToModifyTermsList() {
        this.operation.terms().add( TestConstants.INT_1_TERM );
    }

    @Test
    public void shouldNotHaveProblemsWithTwoTermsOfCorrectType() throws PolyglotterException {
        this.operation.add( TestConstants.INT_1_TERM, TestConstants.INT_2_TERM );
        assertThat( this.operation.problems().isEmpty(), is( true ) );
        assertThat( this.operation.problems().isOk(), is( true ) );
    }

    @Test
    public void shouldNotHaveTermsAfterConstruction() {
        assertThat( this.operation.terms().isEmpty(), is( true ) );
    }

    @Test
    public void shouldProvideDescription() {
        assertThat( this.operation.description(), is( PolyglotterI18n.averageOperationDescription.text() ) );
    }

    @Test
    public void shouldProvideName() {
        assertThat( this.operation.name(), is( PolyglotterI18n.averageOperationName.text() ) );
    }

}
