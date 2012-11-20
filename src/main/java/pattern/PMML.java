/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;


public class PMML
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( PMML.class );

  protected XPathReader reader;
  protected Schema schema = new Schema();

  /** Implemented model types */
  public enum Models { UNKNOWN, TREE };

  /**
   * Parse the XML in the PMML description.
   *
   * @param pmml_file PMML file
   * @throws PatternException
   */
  public PMML( String pmml_file ) throws PatternException
    {
    reader = new XPathReader( pmml_file );
    schema.parseDictionary( this );
    schema.parseMiningSchema( this );
    }

  /**
   * Getter for the XML document reader.
   *
   * @return XPathReader
   */
  public XPathReader getReader()
    {
    return reader;
    }

  /**
   * Getter for the PMML data dictionary.
   *
   * @return Schema
   */
  public Schema getSchema()
    {
    return schema;
    }

  /**
   * Extract the model type.
   *
   * @return Models
   */
  public Models getModelType()
    {
    Models model_type = Models.UNKNOWN;

    if( reader.read( "//TreeModel[functionName='classification'][1]", XPathConstants.STRING ) != null )
      return Models.TREE;

    return model_type;
    }

  /**
   * Extract an XML node list for the DataDictionary.
   *
   * @return NodeList
   */
  public NodeList getDataDictionary()
    {
    String expr = "/PMML/DataDictionary/DataField";
    return (NodeList) reader.read( expr, XPathConstants.NODESET );
    }

  /**
   * Extract an XML node list for the MiningSchema.
   *
   * @return NodeList
   */
  public NodeList getMiningSchema()
    {
    String expr = "/PMML/MiningModel/MiningSchema/MiningField";
    return (NodeList) reader.read( expr, XPathConstants.NODESET );
    }
  }
