/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
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

package pattern;

import pattern.rf.RandomForest;

import javax.xml.xpath.XPathConstants;


public class ClassifierFactory {
    /**
     * Parse the given PMML file, verify the model type, and create the appropriate Classifier object.
     * @param pmml_file PMML file
     * @return Classifier
     * @throws PatternException
     */
    public static Classifier getClassifier(String pmml_file) throws PatternException {
        XPathReader reader = new XPathReader(pmml_file);
        Classifier classifier = null;

        String expr = "/PMML/MiningModel/@modelName";
        String model_type = (String) reader.read(expr, XPathConstants.STRING);

        if ("randomForest_Model".equals(model_type)) {
            classifier = new RandomForest(reader);
        } else {
            throw new PatternException("unsupported model type: " + model_type);
        }

        return classifier;
    }
}
