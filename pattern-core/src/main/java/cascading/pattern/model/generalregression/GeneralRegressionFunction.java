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

package cascading.pattern.model.generalregression;

import java.util.ArrayList;

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.pattern.PatternException;
import cascading.pattern.model.ClassifierFunction;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class GeneralRegressionFunction extends ClassifierFunction<GeneralRegressionSpec, Void>
  {
  private static final Logger LOG = LoggerFactory.getLogger( GeneralRegressionFunction.class );

  public GeneralRegressionFunction( GeneralRegressionSpec param )
    {
    super( param );
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<Void>> functionCall )
    {
    Fields fields = functionCall.getArguments().getFields();
    Tuple values = functionCall.getArguments().getTuple();

    //TODO: Currently handling only logit and Covariate.
    double result = 0.0;

    for( String param : getSpec().paramMatrix.keySet() )
      {
      // if PPMatrix has the parameter
      if( getSpec().ppMatrix.containsKey( param ) )
        {
        //get the Betas from the paramMatrix for param
        ArrayList<PCell> pCells = getSpec().paramMatrix.get( param );
        //TODO : Handling the targetCategory
        PCell pCell = pCells.get( 0 );
        Double beta = pCell.getBeta();

        // get the corresponding PPCells to get the predictor name
        ArrayList<PPCell> ppCells = getSpec().ppMatrix.get( param );
        double paramResult = 1.0;

        for( PPCell pc : ppCells )
          {
          int pos = fields.getPos( pc.getPredictorName() );
          int power = Integer.parseInt( pc.getValue() );

          if( pos != -1 )
            {
            String data = values.getString( pos );

            // if in factor list
            if( getSpec().factors.contains( param ) )
              {
              if( pc.getValue().equals( data ) )
                paramResult *= 1.0;
              else
                paramResult *= 0.0;
              }
            else // Covariate list
              {
              paramResult *= Math.pow( Double.parseDouble( data ), power );
              }
            }
          else
            throw new PatternException( "XML and tuple fields mismatch" );
          }

        result += paramResult * beta;
        }
      else
        {
        ArrayList<PCell> pCells = getSpec().paramMatrix.get( param );

        //TODO: handling the targetCategory
        PCell pCell = pCells.get( 0 );
        result += pCell.getBeta();
        }
      }

    String linkResult = getSpec().linkFunction.calc( result );
    LOG.debug( "result: {}", linkResult );

    functionCall.getOutputCollector().add( functionCall.getContext().result( linkResult ) );
    }
  }
