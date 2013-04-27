/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cascading.pattern.pmml.tree;

import java.lang.reflect.Type;
import java.util.List;

import cascading.pattern.datafield.DataField;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.predicate.EqualsToPredicate;
import cascading.pattern.model.tree.predicate.GreaterOrEqualThanPredicate;
import cascading.pattern.model.tree.predicate.GreaterThanPredicate;
import cascading.pattern.model.tree.predicate.IsInSetPredicate;
import cascading.pattern.model.tree.predicate.IsMissingPredicate;
import cascading.pattern.model.tree.predicate.IsNotInSetPredicate;
import cascading.pattern.model.tree.predicate.IsNotMissingPredicate;
import cascading.pattern.model.tree.predicate.LessOrEqualThanPredicate;
import cascading.pattern.model.tree.predicate.LessThanPredicate;
import cascading.pattern.model.tree.predicate.NotEqualsToPredicate;
import cascading.pattern.pmml.PMMLUtil;
import cascading.tuple.coerce.Coercions;
import org.dmg.pmml.ArrayType;
import org.dmg.pmml.Node;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.TreeModel;

/**
 *
 */
public class TreeUtil
  {
  public static Tree createTree( TreeModel model, ModelSchema modelSchema )
    {
    Node parent = model.getNode();

    Tree tree = new Tree( parent.getId() );

    buildTree( modelSchema, tree, parent );

    return tree;
    }

  private static void buildTree( ModelSchema modelSchema, Tree tree, Node parent )
    {
    for( Node child : parent.getNodes() )
      {
      Predicate predicate = child.getPredicate();

      tree.addPredicate( parent.getId(), child.getId(), getPredicateFor( modelSchema, predicate ), child.getScore() );

      buildTree( modelSchema, tree, child );
      }
    }

  private static cascading.pattern.model.tree.predicate.Predicate getPredicateFor( ModelSchema modelSchema, Predicate predicate )
    {
    if( predicate instanceof SimplePredicate )
      {
      String fieldName = ( (SimplePredicate) predicate ).getField().getValue();
      String value = ( (SimplePredicate) predicate ).getValue();
      SimplePredicate.Operator operator = ( (SimplePredicate) predicate ).getOperator();
      DataField expectedField = modelSchema.getExpectedField( fieldName );

      if( expectedField == null )
        throw new IllegalStateException( "missing field declaration in dictionary for: " + fieldName );

      Type expectedFieldType = expectedField.getType();

      switch( operator )
        {
        case EQUAL:
          return new EqualsToPredicate( fieldName, Coercions.coerce( value, expectedFieldType ) );
        case NOT_EQUAL:
          return new NotEqualsToPredicate( fieldName, Coercions.coerce( value, expectedFieldType ) );
        case LESS_THAN:
          return new LessThanPredicate( fieldName, (Comparable) Coercions.coerce( value, expectedFieldType ) );
        case LESS_OR_EQUAL:
          return new LessOrEqualThanPredicate( fieldName, (Comparable) Coercions.coerce( value, expectedFieldType ) );
        case GREATER_THAN:
          return new GreaterThanPredicate( fieldName, (Comparable) Coercions.coerce( value, expectedFieldType ) );
        case GREATER_OR_EQUAL:
          return new GreaterOrEqualThanPredicate( fieldName, (Comparable) Coercions.coerce( value, expectedFieldType ) );
        case IS_MISSING:
          return new IsMissingPredicate( fieldName );
        case IS_NOT_MISSING:
          return new IsNotMissingPredicate( fieldName );
        }
      }

    if( predicate instanceof SimpleSetPredicate )
      {
      String fieldName = ( (SimpleSetPredicate) predicate ).getField().getValue();
      ArrayType valueArray = ( (SimpleSetPredicate) predicate ).getArray();
      SimpleSetPredicate.BooleanOperator operator = ( (SimpleSetPredicate) predicate ).getBooleanOperator();
      DataField expectedField = modelSchema.getExpectedField( fieldName );

      if( expectedField == null )
        throw new IllegalStateException( "missing field declaration in dictionary for: " + fieldName );

      List list = PMMLUtil.parseArray( valueArray ); // performs coercions

      switch( operator )
        {
        case IS_IN:
          return new IsInSetPredicate( fieldName, list );
        case IS_NOT_IN:
          return new IsNotInSetPredicate( fieldName, list );
        }
      }

    throw new UnsupportedOperationException( "predicate typs is unsupported: " + predicate );
    }
  }
