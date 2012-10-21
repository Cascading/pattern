cascading.pattern
=================

_Pattern_ sub-project for Cascading.org which uses flows as containers
for machine learning models, importing PMML model descriptions from R,
SAS, Weka, Matlab, etc.


generate an example data set
----------------------------

    ./src/py/rf_sample.py 100 > data/sample.tsv


generate an example model
-------------------------

    R --vanilla --slave < src/r/rf_model.R > model.log
    ./src/py/rf_eval.py data/sample.xml > tmp.py
    python tmp.py < data/sample.tsv > eval.log

At this point, the _confusion matrix_ shown in both log files should
have the same values.


generate a JAR for Hadoop
-------------------------

    gradle clean jar
    hadoop jar build/libs/pattern.jar data/sample.xml data/sample.tsv

The _confusion matrix_ shown in the log file should match the ones in
the _R_ and _Python_ baseline examples.
