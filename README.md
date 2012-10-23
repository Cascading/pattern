cascading.pattern
=================

_Pattern_ sub-project for Cascading.org which uses flows as containers
for machine learning models, importing
[PMML](http://en.wikipedia.org/wiki/Predictive_Model_Markup_Language)
model descriptions from _R_, _SAS_, _Weka_, _Matlab_, etc.


generate baseline
-----------------

The following scripts generate a baseline, which includes a sample
data set (ecommerce orders) plus a predictive model:

    ./src/py/rf_sample.py 200 > data/orders.tsv
    R --vanilla --slave < src/r/rf_model.R > model.log


build a JAR
-----------

    gradle clean jar
    rm -rf output
    hadoop jar build/libs/pattern.jar data/sample.xml data/sample.tsv output/classify output/measure

The _confusion matrix_ shown in `output/measure/part*` should match
the one in `model.log` from the _R_ and _Python_ baseline.
