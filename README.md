cascading.pattern
=================

_Pattern_ sub-project for Cascading.org which uses flows as containers
for machine learning models, importing
[PMML](http://en.wikipedia.org/wiki/Predictive_Model_Markup_Language)
model descriptions from _R_, _SAS_, _Weka_, _Matlab_, etc.


generate a baseline example data set
------------------------------------

    ./src/py/rf_sample.py 100 > data/sample.tsv


generate a baseline example model
---------------------------------

    R --vanilla --slave < src/r/rf_model.R > model.log
    ./src/py/rf_eval.py data/sample.xml > tmp.py
    python tmp.py < data/sample.tsv > eval.log

At this point, the _confusion matrix_ shown in both log files should
have the same values.


generate a JAR for Hadoop
-------------------------

    gradle clean jar
    rm -rf output
    hadoop jar build/libs/pattern.jar data/sample.xml data/sample.tsv output/eval output/confuse

The _confusion matrix_ shown in `output/confuse/part*` should match
the ones in the _R_ and _Python_ baseline examples.
