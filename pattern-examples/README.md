Pattern Examples
================
_Pattern_ sub-project for http://Cascading.org/ which uses flows as
containers for machine learning models, importing
[PMML](http://en.wikipedia.org/wiki/Predictive_Model_Markup_Language)
model descriptions from _R_, _SAS_, _Weka_, _RapidMiner_, _KNIME_,
_SQL Server_, etc.

Current support for PMML includes:

 * [Random Forest](http://en.wikipedia.org/wiki/Random_forest) in [PMML 4.0+](http://www.dmg.org/v4-0-1/MultipleModels.html) exported from [R/Rattle](http://cran.r-project.org/web/packages/rattle/index.html)
 * [Linear Regression](http://en.wikipedia.org/wiki/Linear_regression) in [PMML 1.1+](http://www.dmg.org/v1-1/generalregression.html)
 * [Hierarchical Clustering](http://en.wikipedia.org/wiki/Hierarchical_clustering) and [K-Means Clustering](http://en.wikipedia.org/wiki/K-means_clustering) in [PMML 2.0+](http://www.dmg.org/v2-0/ClusteringModel.html)
 * [Logistic Regression](http://en.wikipedia.org/wiki/Logistic_regression) in [PMML 4.0.1+](http://www.dmg.org/v4-0-1/Regression.html)
 * [Multinomial Model](http://en.wikipedia.org/wiki/Multinomial_distribution) in [PMML 2.0+](http://www.dmg.org/v2-0/Regression.html)


Build Instructions
------------------
To build _Pattern_ and then run its unit tests:

    gradle --info --stacktrace clean test

The following script generates a data set for training a model based 
on the _Random Forest_ algorithm. This reference data set has 3 
independent variables and 200 rows of simulated ecommerce orders:

    ./examples/py/gen_orders.py 200 3 > sample.tsv

This snippet in R shows how to train a Random Forest model,
then generate PMML as a file called `sample.rf.xml`:

    f <- as.formula("as.factor(label) ~ .")
    fit <- randomForest(f, data_train, ntree=50)
    saveXML(pmml(fit), file="sample.rf.xml")

This will generate `sample.rf.xml` as the PMML export for a Random
Forest classifier.

To build _Pattern_ and run a model:

    gradle clean jar
    rm -rf out
    hadoop jar build/libs/pattern-examples-*.jar \
     data/sample.tsv out/classify --pmml data/sample.rf.xml


Use in Cascading Apps
---------------------
Alternatively if you want to use the `PMMLPlanner` assembly in
your own Cascading app, the following code snippet shows an
example. It references the PMML file in a command line argument
called `pmmlPath`:

    // connect the taps, pipes, etc., into a flow
    FlowDef flowDef = FlowDef.flowDef()
      .setName( "classify" )
      .addSource( "input", inputTap )
      .addSink( "classify", classifyTap );

    // define a "Classifier" model from the PMML description

    PMMLPlanner pmmlPlanner = new PMMLPlanner()
      .setPMMLInput( new File( pmmlPath ) )
      .retainOnlyActiveIncomingFields()
      .setDefaultPredictedField( new Fields( "predict", Double.class ) );

    flowDef.addAssemblyPlanner( pmmlPlanner );

When you run that Cascading app, provide a reference to
`sample.rf.xml` for the `pmmlPath` argument.

An architectural diagram for common use case patterns is shown in
`docs/pattern.graffle` which is an OmniGraffle document.


Example Models
--------------
Check the `examples/r/rattle_pmml.R` script for examples of predictive
models which are created in R, then exported using _Rattle_.
These examples use the popular
[Iris](http://en.wikipedia.org/wiki/Iris_flower_data_set) data set.

 * random forest (rf)
 * linear regression (lm)
 * hierarchical clustering (hclust)
 * k-means clustering (kmeans)
 * logistic regression (glm)
 * recursive partition classification tree (rpart)
 * multinomial model (multinom)
 * support vector machine (ksvm)
 * single hidden-layer neural network (nnet)
 * association rules

To execute the R script:

    R --vanilla < examples/r/rattle_pmml.R

It is possible to extend PMML support for other kinds of modeling in R
and other analytics platforms.  Contact the developers to discuss on
the [cascading-user](https://groups.google.com/forum/?fromgroups#!forum/cascading-user)
email forum.


PMML Resources
--------------
 * [Data Mining Group](http://www.dmg.org/) XML standards and supported vendors
 * [PMML In Action](http://www.amazon.com/dp/1470003244) book 
 * [PMML validator](http://www.zementis.com/pmml_tools.htm)
 * [Enterprise Data Workflows with Cascading](http://shop.oreilly.com/product/0636920028536.do) book
