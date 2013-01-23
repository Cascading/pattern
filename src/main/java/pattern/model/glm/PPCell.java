package pattern.model.glm;

import java.io.Serializable;

/**
 * This Class represents PPCell inside PPMatrix
 */
public class PPCell implements Serializable
  {


  private String predictorName;
  private String value;
  private String parameterName;

  public String getPredictorName()
    {
    return predictorName;
    }

  public void setPredictorName(String predictorName)
    {
    this.predictorName = predictorName;
    }

  public String getValue()
    {
    return value;
    }

  public void setValue(String value)
    {
    this.value = value;
    }

  public String getParameterName()
    {
    return parameterName;
    }

  public void setParameterName(String parameterName)
    {
    this.parameterName = parameterName;
    }


    /** @return String  */
  public String toString()
    {
    StringBuilder buf = new StringBuilder();
    buf.append("parameterName = ");
    buf.append(parameterName+",");
    buf.append("predictorName = ");
    buf.append(predictorName+",");
    buf.append("value = ");
    buf.append(value) ;

    return buf.toString();
    }


  }
