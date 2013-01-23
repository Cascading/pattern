/*
 *
 */

package pattern.model.glm;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pattern.PMML;
import pattern.PatternException;
import pattern.model.Model;

import javax.xml.xpath.XPathConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;


public class GeneralizedRegressionModel extends Model implements Serializable
  {
  /** LOGGER */
  private static final Logger LOG = LoggerFactory.getLogger(GeneralizedRegressionModel.class);


  PPMatrix ppmatrix = new PPMatrix();
  ParamMatrix paramMatrix = new ParamMatrix();
  HashSet<String> covariate = new HashSet<String>();
  HashSet<String> factors = new HashSet<String>();
  HashSet<String> parameterList = new HashSet<String>();
  LinkFunction linkFunction;

  /**
   * Constructor for a General Regression Model as a standalone classifier (PMML
   * versions 4.1).
   *
   * @param pmml PMML model
   * @throws pattern.PatternException
   */
  public GeneralizedRegressionModel(PMML pmml) throws PatternException
    {
    schema = pmml.getSchema();
    schema.parseMiningSchema( pmml.getNodeList( "/PMML/GeneralRegressionModel/MiningSchema/MiningField" ) );

    ppmatrix.parsePPCell(pmml.getNodeList("/PMML/GeneralRegressionModel/PPMatrix/PPCell"));

    LOG.debug( ppmatrix.toString()  );

    paramMatrix.parsePCell(pmml.getNodeList("/PMML/GeneralRegressionModel/ParamMatrix/PCell"));

    LOG.debug(paramMatrix.toString());

    String node_expr = "/PMML/GeneralRegressionModel/ParameterList/Parameter";
      NodeList child_nodes = pmml.getNodeList( node_expr );
    //NodeList child_nodes = model_node.getChildNodes();


    for( int i = 0; i < child_nodes.getLength(); i++ )
      {
      Node child = child_nodes.item( i );

      if( child.getNodeType() == Node.ELEMENT_NODE )
        {

          String name = ((Element)child).getAttribute( "name" );
          parameterList.add(name);

        }
      }


      String node_expr_covariate = "/PMML/GeneralRegressionModel/CovariateList/Predictor";
      NodeList child_nodes_covariate=  pmml.getNodeList( node_expr_covariate );

      for( int i = 0; i < child_nodes_covariate.getLength(); i++ )
        {
        Node child = child_nodes_covariate.item( i );

        if( child.getNodeType() == Node.ELEMENT_NODE )
          {

          String name = ((Element)child).getAttribute( "name" );
          covariate.add(name);

          }
        }

      String node_expr_factors = "/PMML/GeneralRegressionModel/FactorList/Predictor";
      NodeList child_nodes_factors = pmml.getNodeList( node_expr_factors );



      for( int i = 0; i < child_nodes_factors.getLength(); i++ )
        {
        Node child = child_nodes_factors.item( i );

        if( child.getNodeType() == Node.ELEMENT_NODE )
          {

          String name = ((Element)child).getAttribute( "name" );
          factors.add(name);

          }
        }

      String node = "/PMML/GeneralRegressionModel/@linkFunction";
      String linkFunctionStr =  pmml.getReader().read( node, XPathConstants.STRING).toString();

      linkFunction = LinkFunction.getFunction(linkFunctionStr);



    }

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   */
  @Override
  public void prepare()
    {
    // not needed
    }

  /**
   * Classify an input tuple, returning the predicted label.
   *
   * @param values tuple values, Fields fields
   * @return String
   * @throws pattern.PatternException
   */
  @Override
  public String classifyTuple( Tuple values, Fields fields) throws PatternException
    {
      //TODO: Currently handling only logit and Covariate.
      double result = 0.0;
      for(String param :paramMatrix.keySet())
        {
        // if PPMatrix has the parameter
        if(ppmatrix.containsKey(param))
          {
          //Get the Betas from the paramMatrix for param.
          ArrayList<PCell> pCells = paramMatrix.get(param);
          //TODO : Handling the targetCategory
          PCell pCell= pCells.get(0);

          Double beta = Double.parseDouble(pCell.getBeta());

          // Get the corresponding PPCells to get the predictor name
          ArrayList<PPCell> ppCells = ppmatrix.get(param);
          double paramResult = 1.0;
          for (PPCell pc : ppCells)
            {
            int pos = fields.getPos(pc.getPredictorName());
            int power = Integer.parseInt(pc.getValue());
            if (pos != -1)
              {

              String data = values.getString(pos);

              //If in factor list
              if(factors.contains(param))
                {
                if(pc.getValue().equals(data))
                  {
                  paramResult *= 1.0;
                  }
                else
                  {
                    paramResult *= 0.0;
                  }
                }
              else   // Covariate list
                {
                  paramResult *= Math.pow(Double.parseDouble(data), power);
                }

              }
            else
              {
              throw new PatternException("XML and tuple fields mismatch");
              }
            }
          result += paramResult*beta ;

          }
        else
          {
          ArrayList<PCell> pCells = paramMatrix.get(param);

          //TODO : Handling the targetCategory
          PCell pCell= pCells.get(0);

          result += Double.parseDouble(pCell.getBeta());
          }
        }

    LOG.debug( "result: " +Double.toString(linkFunction.calc(result)) );
    //Apply Appropriate LinkFunction
    return Double.toString(linkFunction.calc(result));
    }

  /** @return String  */
  @Override
  public String toString()
    {
    StringBuilder buf = new StringBuilder();
    buf.append("GLM");
    return buf.toString();
    }
  }
