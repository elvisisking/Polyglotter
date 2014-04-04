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
package org.polyglotter.eclipse.view;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.modeshape.modeler.Model;
import org.modeshape.modeler.ModelObject;
import org.modeshape.modeler.ModelerException;
import org.polyglotter.common.Logger;
import org.polyglotter.common.Logger.Level;
import org.polyglotter.common.PolyglotterException;
import org.polyglotter.eclipse.Util;
import org.polyglotter.eclipse.focustree.FocusTree;

/**
 * A {@link FocusTree} content provider for Polyglotter {@link Model models}.
 */
public final class ModelContentProvider extends FocusTree.Model {

    private static final boolean DEBUG = false;

    private static Logger _logger;

    /**
     * No arg methods from {@link ModelObject} that will be added as properties
     */
    private static final String[] MODEL_OBJECT_METHODS =
        new String[] { "absolutePath",
                        "index",
                        "mixinTypes",
                        "modelRelativePath",
                        "name",
                        "primaryType"
        };

    /**
     * No arg methods from {@link Model} that will be added as properties
     */
    private static final String[] MODEL_METHODS =
        new String[] { "allDependenciesExist",
                        "dependencies",
                        "externalLocation",
                        "missingDependencies",
                        "modelType"
        };

    private static void debug( final String message,
                               final Object... args ) {
        if ( DEBUG ) {
            if ( _logger == null ) {
                _logger = Logger.getLogger( ModelContentProvider.class );
                _logger.setLevel( Level.DEBUG );
            }

            _logger.debug( message, args );
        }
    }

    private Collection< PropertyModel > addPropertiesFromMethods( final ModelObject modelObj,
                                                                  final String[] methodNames ) throws Exception {
        final Collection< PropertyModel > result = new ArrayList< PropertyModel >( methodNames.length );
        final Class< ? extends ModelObject > clazz = modelObj.getClass();

        for ( final String methodName : methodNames ) {
            final Method method = clazz.getMethod( methodName );
            final PropertyModel propModel = new PropertyModel( modelObj,
                                                               method.getName(),
                                                               method.getReturnType(),
                                                               method.invoke( modelObj ) );
            result.add( propModel );
        }

        return result;
    }

    /**
     * @param childName
     *        the name of the child (cannot be <code>null</code> or empty)
     * @param parent
     *        the parent of the child being requested (cannot be <code>null</code>)
     * @return the child or <code>null</code> if not found
     * @throws PolyglotterException
     *         if any error occurs
     */
    public Object child( final String childName,
                         final Object parent
                    ) throws PolyglotterException {
        for ( final Object obj : children( parent ) ) {
            final Object name = name( obj );

            if ( childName.equals( name ) ) {
                return obj;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#childCount(java.lang.Object)
     */
    @Override
    public int childCount( final Object item ) throws PolyglotterException {
        return children( item ).length;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#children(java.lang.Object)
     */
    @Override
    public Object[] children( final Object item ) throws PolyglotterException {
        if ( item instanceof ModelObject ) {
            try {
                return childrenOf( ( ModelObject ) item );
            } catch ( final Exception e ) {
                throw new PolyglotterException( e );
            }
        }

        if ( item instanceof ModelContent ) {
            try {
                return childrenOf( ( ModelContent ) item );
            } catch ( final Exception e ) {
                throw new PolyglotterException( e );
            }
        }

        return super.children( item );
    }

    private Object[] childrenOf( final ModelContent content ) throws Exception {
        return content.children();
    }

    private Object[] childrenOf( final ModelObject modelObj ) throws Exception {
        final List< Object > kids = new ArrayList<>();
        final ModelObject[] temp = modelObj.children();

        if ( temp.length != 0 ) {
            kids.addAll( Arrays.asList( temp ) );
        }

        // add methods from ModelObject that are treated as properties
        kids.addAll( addPropertiesFromMethods( modelObj, MODEL_OBJECT_METHODS ) );

        // add methods from Model that are treated as properties
        if ( modelObj instanceof Model ) {
            kids.addAll( addPropertiesFromMethods( modelObj, MODEL_METHODS ) );
        }

        // add properties as children
        for ( final String prop : modelObj.propertyNames() ) {
            final Object value = modelObj.value( prop );
            kids.add( new PropertyModel( modelObj, prop, value.getClass(), value ) );
        }

        return kids.toArray();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren( final Object item ) throws PolyglotterException {
        if ( item instanceof ModelObject ) {
            try {
                debug( "%s has children: %s", ( ( ModelObject ) item ).name(), ( ( ModelObject ) item ).hasChildren() );
                return ( children( item ).length != 0 );
            } catch ( final Exception e ) {
                throw new PolyglotterException( e );
            }
        }

        if ( item instanceof ModelContent ) return ( ( ModelContent ) item ).hasChildren();
        return super.hasChildren( item );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#hasName(java.lang.Object)
     */
    @Override
    public boolean hasName( final Object item ) throws PolyglotterException {
        if ( item instanceof ModelObject ) {
            try {
                return !Util.isBlank( ( ( ModelObject ) item ).name() );
            } catch ( final ModelerException e ) {
                throw new PolyglotterException( e );
            }
        }

        if ( item instanceof ModelContent ) return !Util.isBlank( ( ( ModelContent ) item ).name() );
        return super.hasName( item );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#hasType(java.lang.Object)
     */
    @Override
    public boolean hasType( final Object item ) {
        if ( item instanceof ModelObject ) return true;
        if ( item instanceof ModelContent ) return !Util.isBlank( ( ( ModelContent ) item ).type() );
        return super.hasType( item );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#hasValue(java.lang.Object)
     */
    @Override
    public boolean hasValue( final Object item ) throws PolyglotterException {
        return ( value( item ) != null );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#name(java.lang.Object)
     */
    @Override
    public Object name( final Object item ) throws PolyglotterException {
        if ( item instanceof ModelObject ) {
            try {
                return ( ( ModelObject ) item ).name();
            } catch ( final Exception e ) {
                throw new PolyglotterException( e );
            }
        }

        if ( item instanceof ModelContent ) return ( ( ModelContent ) item ).name();
        return super.name( item );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#qualifiedName(java.lang.Object)
     */
    @Override
    public Object qualifiedName( final Object item ) throws PolyglotterException {
        if ( item instanceof ModelObject ) {
            return ( ( ModelObject ) item ).absolutePath();
        }

        if ( item instanceof ModelContent ) {
            return ( ( ModelContent ) item ).qualifiedName();
        }

        return super.qualifiedName( item );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#type(java.lang.Object)
     */
    @Override
    public Object type( final Object item ) throws PolyglotterException {
        if ( item instanceof ModelObject ) {
            try {
                return FocusTree.Model.formatType( ( ( ModelObject ) item ).primaryType() );
            } catch ( final Exception e ) {
                throw new PolyglotterException( e );
            }
        }

        if ( item instanceof ModelContent ) return FocusTree.Model.formatType( ( ( ModelContent ) item ).type() );
        return super.type( item );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.polyglotter.eclipse.focustree.FocusTree.Model#value(java.lang.Object)
     */
    @Override
    public Object value( final Object item ) throws PolyglotterException {
        if ( item instanceof ModelObject ) return null;
        if ( item instanceof ModelContent ) return ( ( ModelContent ) item ).value();
        return super.value( item );
    }

    interface ModelContent {

        Object[] children() throws Exception;

        boolean hasChildren();

        String name();

        String qualifiedName();

        String type();

        Object value();

    }

    class PropertyModel implements ModelContent {

        private final ModelObject model;
        private final String name;
        private final Class< ? > type;
        private final Object value;

        PropertyModel( final ModelObject model,
                       final String propName,
                       final Class< ? > returnType,
                       final Object propValue ) {
            this.model = model;
            this.name = propName;
            this.type = returnType;
            this.value = propValue;
        }

        @Override
        public Object[] children() throws Exception {
            if ( isPrimitive() ) return Util.EMPTY_ARRAY;

            if ( this.model.propertyHasMultipleValues( this.name ) ) {
                final Object[] values = this.model.values( this.name );
                final Collection< ValueModel > result = new ArrayList<>( values.length );

                for ( final Object value : values ) {
                    result.add( new ValueModel( this, value.getClass(), value ) );
                }

                return result.toArray();
            }

            if ( isCollection() && ( this.value != null ) ) {
                final Collection< ? > values = ( ( Collection< ? > ) this.value );
                final Collection< ValueModel > result = new ArrayList<>( values.size() );

                for ( final Object value : values ) {
                    result.add( new ValueModel( this, value.getClass(), value ) );
                }

                return result.toArray();
            }

            if ( this.type.isArray() && ( this.value != null ) ) {
                final Object[] values = ( Object[] ) this.value;
                final Collection< ValueModel > result = new ArrayList<>( values.length );

                for ( final Object value : values ) {
                    result.add( new ValueModel( this, value.getClass(), value ) );
                }

                return result.toArray();
            }

            // must be a complex/custom type
            return ( ( this.value == null ) ? Util.EMPTY_ARRAY
                                           : new Object[] { new ValueModel( this, value.getClass(), this.value ) } );
        }

        @Override
        public boolean hasChildren() {
            return !isPrimitive();
        }

        private boolean isCollection() {
            return Collection.class.isAssignableFrom( this.type );
        }

        private boolean isPrimitive() {
            if ( this.type.equals( Boolean.class )
                 || this.type.equals( String.class )
                 || this.type.equals( Byte.class )
                 || this.type.equals( Character.class )
                 || this.type.equals( Short.class )
                 || this.type.equals( Integer.class )
                 || this.type.equals( Long.class )
                 || this.type.equals( Float.class )
                 || this.type.equals( Double.class ) ) {
                return true;
            }

            return this.type.isPrimitive();
        }

        @Override
        public String name() {
            return this.name;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.polyglotter.eclipse.view.ModelContentProvider.ModelContent#qualifiedName()
         */
        @Override
        public String qualifiedName() {
            return ( this.model.absolutePath() + '/' + this.name );
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String type() {
            return ( FocusTree.Model.formatType( this.type.getName() )
            + FocusTree.Model.collectionTypeSuffix( this.type, value() ) );
        }

        @Override
        public Object value() {
            return ( isPrimitive() ? this.value : null );
        }

    }

    class ValueModel implements ModelContent {

        private final PropertyModel parent;
        private final Class< ? > type;
        private final Object value;

        ValueModel( final PropertyModel propertyModel,
                    final Class< ? > modelType,
                    final Object modelValue ) {
            this.parent = propertyModel;
            this.type = modelType;
            this.value = modelValue;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.polyglotter.eclipse.view.ModelContentProvider.ModelContent#children()
         */
        @Override
        public Object[] children() {
            return Util.EMPTY_ARRAY;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.polyglotter.eclipse.view.ModelContentProvider.ModelContent#hasChildren()
         */
        @Override
        public boolean hasChildren() {
            return false;
        }

        @Override
        public String name() {
            return null;
        }

        PropertyModel parent() {
            return this.parent;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.polyglotter.eclipse.view.ModelContentProvider.ModelContent#qualifiedName()
         */
        @Override
        public String qualifiedName() {
            return null;
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return ( ( this.value == null ) ? null : this.value.toString() );
        }

        @Override
        public String type() {
            return ( FocusTree.Model.formatType( this.type.getName() )
            + FocusTree.Model.collectionTypeSuffix( this.type, this.value ) );
        }

        @Override
        public Object value() {
            return this.value;
        }

    }

}
