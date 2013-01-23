package pattern.model.glm;

/*
* Enum for the LinkFunction in GLM
*/
public enum LinkFunction
  {

    NONE("none")
      {
      public double calc(double value)
        {
         return Double.NaN;
        }
      },

    LOGIT ("logit")
      {
      public double calc(double value)
        {
        return 1.0 / (1.0 + Math.exp(-(value )));
        }
      },
   CLOGLOG ("cloglog")
      {
      public double calc(double value)
        {
        return 1.0 - Math.exp(-Math.exp(value ));
        }
      },
    LOGLOG ("loglog")
      {
      public double calc(double value)
        {
        return Math.exp(-Math.exp(-(value)));
        }
      },
    CAUCHIT ("cauchit")
      {
      public double calc(double value)
        {
        return 0.5 + (1.0 / Math.PI) * Math.atan(value);
        }

      };
  public String function;

  private LinkFunction(String function)
    {
    this.function = function;
    }
    /**
     *  Returns to corresponding linkFunction
     *
     * @param functionName String
     * @return  LinkFunction
     */
  public static LinkFunction getFunction(String functionName)
    {

    for(LinkFunction lf:values())
      {
      if(lf.function.matches(functionName))
        {
        return lf;
        }
      }
    return  LinkFunction.NONE;


    }
  public abstract double calc(double value);



  }
