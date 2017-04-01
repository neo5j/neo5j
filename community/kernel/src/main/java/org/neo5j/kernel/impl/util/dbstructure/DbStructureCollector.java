/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo5j.
 *
 * Neo5j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo5j.kernel.impl.util.dbstructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo5j.helpers.collection.Iterators;
import org.neo5j.helpers.collection.Pair;
import org.neo5j.kernel.api.schema_new.LabelSchemaDescriptor;
import org.neo5j.kernel.api.schema_new.SchemaDescriptorFactory;
import org.neo5j.kernel.api.schema_new.constaints.NodeExistenceConstraintDescriptor;
import org.neo5j.kernel.api.schema_new.constaints.RelExistenceConstraintDescriptor;
import org.neo5j.kernel.api.schema_new.constaints.UniquenessConstraintDescriptor;
import org.neo5j.kernel.api.schema_new.index.NewIndexDescriptor;

import static java.lang.String.format;

public class DbStructureCollector implements DbStructureVisitor
{
    private final TokenMap labels = new TokenMap( "label" );
    private final TokenMap propertyKeys = new TokenMap( "property key" );
    private final TokenMap relationshipTypes = new TokenMap( "relationship types" );
    private final IndexDescriptorMap regularIndices = new IndexDescriptorMap( "regular" );
    private final IndexDescriptorMap uniqueIndices = new IndexDescriptorMap( "unique" );
    private final Set<UniquenessConstraintDescriptor> uniquenessConstraints = new HashSet<>();
    private final Set<NodeExistenceConstraintDescriptor> nodePropertyExistenceConstraints = new HashSet<>();
    private final Set<RelExistenceConstraintDescriptor> relPropertyExistenceConstraints = new HashSet<>();
    private final Map<Integer, Long> nodeCounts = new HashMap<>();
    private final Map<RelSpecifier, Long> relCounts = new HashMap<>();
    private long allNodesCount = -1L;

    public DbStructureLookup lookup()
    {
        return new DbStructureLookup()
        {
            @Override
            public Iterator<Pair<Integer, String>> labels()
            {
                return labels.iterator();
            }

            @Override
            public Iterator<Pair<Integer, String>> properties()
            {
                return propertyKeys.iterator();
            }

            @Override
            public Iterator<Pair<Integer, String>> relationshipTypes()
            {
                return relationshipTypes.iterator();
            }

            @Override
            public Iterator<Pair<String,String[]>> knownIndices()
            {
                return regularIndices.iterator();
            }

            @Override
            public Iterator<Pair<String,String[]>> knownUniqueIndices()
            {
                return uniqueIndices.iterator();
            }

            @Override
            public Iterator<Pair<String,String[]>> knownUniqueConstraints()
            {
                return Iterators.map( uniquenessConstraint ->
                {
                    String label = labels.byIdOrFail( uniquenessConstraint.schema().getLabelId() );
                    String[] propertyKeyNames = propertyKeys
                            .byIdOrFail( uniquenessConstraint.schema().getPropertyIds() );
                    return Pair.of( label, propertyKeyNames );
                }, uniquenessConstraints.iterator() );
            }

            @Override
            public Iterator<Pair<String,String[]>> knownNodePropertyExistenceConstraints()
            {
                return Iterators.map( uniquenessConstraint ->
                {
                    String label = labels.byIdOrFail( uniquenessConstraint.schema().getLabelId() );
                    String[] propertyKeyNames = propertyKeys
                            .byIdOrFail( uniquenessConstraint.schema().getPropertyIds() );
                    return Pair.of( label, propertyKeyNames );
                }, nodePropertyExistenceConstraints.iterator() );
            }

            @Override
            public Iterator<Pair<String,String[]>> knownRelationshipPropertyExistenceConstraints()
            {
                return Iterators.emptyIterator();
            }

            @Override
            public long nodesWithLabelCardinality( int labelId )
            {
                Long result = labelId == -1 ? allNodesCount : nodeCounts.get( labelId );
                return result == null ? 0L : result;
            }

            @Override
            public long cardinalityByLabelsAndRelationshipType( int fromLabelId, int relTypeId, int toLabelId )
            {
                RelSpecifier specifier = new RelSpecifier( fromLabelId, relTypeId, toLabelId );
                Long result = relCounts.get( specifier );
                return result == null ? 0L : result;
            }

            @Override
            public double indexSelectivity( int labelId, int... propertyKeyIds )
            {
                LabelSchemaDescriptor descriptor = SchemaDescriptorFactory.forLabel( labelId, propertyKeyIds );
                IndexStatistics result1 = regularIndices.getIndex( descriptor );
                IndexStatistics result2 = result1 == null ? uniqueIndices.getIndex( descriptor ) : result1;
                return result2 == null ? Double.NaN : result2.uniqueValuesPercentage;
            }

            @Override
            public double indexPropertyExistsSelectivity( int labelId, int... propertyKeyIds )
            {
                LabelSchemaDescriptor descriptor = SchemaDescriptorFactory.forLabel( labelId, propertyKeyIds );
                IndexStatistics result1 = regularIndices.getIndex( descriptor );
                IndexStatistics result2 = result1 == null ? uniqueIndices.getIndex( descriptor ) : result1;
                return result2 == null ? Double.NaN : result2.size;
            }
        };
    }

    @Override
    public void visitLabel( int labelId, String labelName )
    {
        labels.putToken( labelId, labelName );
    }

    @Override
    public void visitPropertyKey( int propertyKeyId, String propertyKeyName )
    {
        propertyKeys.putToken( propertyKeyId, propertyKeyName );
    }

    @Override
    public void visitRelationshipType( int relTypeId, String relTypeName )
    {
        relationshipTypes.putToken( relTypeId, relTypeName );
    }

    @Override
    public void visitIndex( NewIndexDescriptor descriptor, String userDescription, double uniqueValuesPercentage, long
            size )
    {
        regularIndices.putIndex( descriptor.schema(), userDescription, uniqueValuesPercentage, size );
    }

    @Override
    public void visitUniqueIndex( NewIndexDescriptor descriptor, String userDescription, double uniqueValuesPercentage, long size )
    {
        uniqueIndices.putIndex( descriptor.schema(), userDescription, uniqueValuesPercentage, size );
    }

    @Override
    public void visitUniqueConstraint( UniquenessConstraintDescriptor constraint, String userDescription )
    {
        if ( !uniquenessConstraints.add( constraint ) )
        {
            throw new IllegalArgumentException(
                    format( "Duplicated unique constraint %s for %s", constraint, userDescription )
            );
        }
    }

    @Override
    public void visitNodePropertyExistenceConstraint( NodeExistenceConstraintDescriptor constraint,
            String userDescription )
    {
        if ( !nodePropertyExistenceConstraints.add( constraint ) )
        {
            throw new IllegalArgumentException(
                    format( "Duplicated node property existence constraint %s for %s", constraint, userDescription )
            );
        }
    }

    @Override
    public void visitRelationshipPropertyExistenceConstraint( RelExistenceConstraintDescriptor constraint,
            String userDescription )
    {
        if ( !relPropertyExistenceConstraints.add( constraint ) )
        {
            throw new IllegalArgumentException(
                    format( "Duplicated relationship property existence constraint %s for %s",
                            constraint, userDescription )
            );
        }
    }

    @Override
    public void visitAllNodesCount( long nodeCount )
    {
        if ( allNodesCount < 0 )
        {
            allNodesCount = nodeCount;
        }
        else
        {
            throw new IllegalStateException( "Already received node count" );
        }
    }

    @Override
    public void visitNodeCount( int labelId, String labelName, long nodeCount )
    {
        if ( nodeCounts.put( labelId, nodeCount ) != null )
        {
            throw new IllegalArgumentException(
                    format( "Duplicate node count %s for label with id %s", nodeCount, labelName )
            );
        }
    }

    @Override
    public void visitRelCount( int startLabelId, int relTypeId, int endLabelId, String relCountQuery, long relCount )
    {
        RelSpecifier specifier = new RelSpecifier( startLabelId, relTypeId, endLabelId );

        if ( relCounts.put( specifier, relCount ) != null )
        {
            throw new IllegalArgumentException(
                    format( "Duplicate rel count %s for relationship specifier %s (corresponding query: %s)", relCount,
                            specifier, relCountQuery )
            );
        }
    }

    private static class RelSpecifier
    {
        public final int fromLabelId;
        public final int relTypeId;
        public final int toLabelId;

        RelSpecifier( int fromLabelId, int relTypeId, int toLabelId )
        {
            this.fromLabelId = fromLabelId;
            this.relTypeId = relTypeId;
            this.toLabelId = toLabelId;
        }

        @Override
        public String toString()
        {
            return format(
                "RelSpecifier{fromLabelId=%d, relTypeId=%d, toLabelId=%d}", fromLabelId, relTypeId, toLabelId
            );
        }

        @Override
        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            RelSpecifier that = (RelSpecifier) o;
            return fromLabelId == that.fromLabelId && relTypeId == that.relTypeId && toLabelId == that.toLabelId;
        }

        @Override
        public int hashCode()
        {
            int result = fromLabelId;
            result = 31 * result + relTypeId;
            result = 31 * result + toLabelId;
            return result;
        }
    }

    private class IndexStatistics
    {
        private final double uniqueValuesPercentage;
        private final long size;

        private IndexStatistics(double uniqueValuesPercentage, long size)
        {
            this.uniqueValuesPercentage = uniqueValuesPercentage;
            this.size = size;
        }
    }

    private class IndexDescriptorMap implements Iterable<Pair<String,String[]>>
    {
        private final String indexType;
        private final Map<LabelSchemaDescriptor, IndexStatistics> indexMap = new HashMap<>();

        IndexDescriptorMap( String indexType )
        {
            this.indexType = indexType;
        }

        public void putIndex( LabelSchemaDescriptor descriptor, String userDescription, double uniqueValuesPercentage, long size )
        {
            if ( indexMap.containsKey( descriptor ) )
            {
                throw new IllegalArgumentException(
                        format( "Duplicate index descriptor %s for %s index %s", descriptor, indexType,
                                userDescription )
                );
            }

            indexMap.put( descriptor, new IndexStatistics(uniqueValuesPercentage, size) );
        }

        public IndexStatistics getIndex( LabelSchemaDescriptor descriptor )
        {
            return indexMap.get( descriptor );
        }

        public Iterator<Pair<String,String[]>> iterator()
        {
            final Iterator<LabelSchemaDescriptor> iterator = indexMap.keySet().iterator();
            return new Iterator<Pair<String,String[]>>()
            {
                @Override
                public boolean hasNext()
                {
                    return iterator.hasNext();
                }

                @Override
                public Pair<String,String[]> next()
                {
                    //TODO: Add support for composite indexes
                    LabelSchemaDescriptor next = iterator.next();
                    String label = labels.byIdOrFail( next.getLabelId() );
                    String[] propertyKeyNames = propertyKeys.byIdOrFail( next.getPropertyIds() );
                    return Pair.of( label, propertyKeyNames );
                }

                @Override
                public void remove()
                {
                    iterator.remove();
                }
            };
        }
    }

    private static class TokenMap implements Iterable<Pair<Integer, String>>
    {
        private final String tokenType;
        private final Map<Integer, String> forward = new HashMap<>();
        private final Map<String, Integer> backward = new HashMap<>();

        TokenMap( String tokenType )
        {
            this.tokenType = tokenType;
        }

        public String byIdOrFail( int token )
        {
            String result = forward.get( token );
            if ( result == null )
            {
                throw new IllegalArgumentException( format( "Didn't find %s token with id %s", tokenType, token ) );
            }
            return result;
        }

        public String[] byIdOrFail( int[] tokens )
        {
            String[] results = new String[tokens.length];
            for ( int i = 0; i < tokens.length; i++ )
            {
                results[i] = byIdOrFail( tokens[i] );
            }
            return results;
        }

        public void putToken( int token, String name )
        {
            if ( forward.containsKey( token ) )
            {
                throw new IllegalArgumentException(
                        format( "Duplicate id %s for name %s in %s token map", token, name, tokenType )
                );
            }

            if ( backward.containsKey( name ) )
            {
                throw new IllegalArgumentException(
                        format( "Duplicate name %s for id %s in %s token map", name, token, tokenType )
                );
            }

            forward.put( token, name );
            backward.put( name, token );
        }

        @Override
        public Iterator<Pair<Integer, String>> iterator()
        {
            final Iterator<Map.Entry<Integer, String>> iterator = forward.entrySet().iterator();
            return new Iterator<Pair<Integer, String>>()
            {
                @Override
                public boolean hasNext()
                {
                    return iterator.hasNext();
                }

                @Override
                public Pair<Integer, String> next()
                {
                    Map.Entry<Integer, String> next = iterator.next();
                    return Pair.of( next.getKey(), next.getValue() );
                }

                @Override
                public void remove()
                {
                    iterator.remove();
                }
            };
        }
    }
}