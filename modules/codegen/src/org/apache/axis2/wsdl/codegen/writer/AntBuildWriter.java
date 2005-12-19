package org.apache.axis2.wsdl.codegen.writer;

import org.apache.axis2.util.FileWriter;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Iterator;
/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class AntBuildWriter extends ClassWriter{

    private String databindingFramework = ConfigPropertyFileLoader.getDefaultDBFramworkName();

     public AntBuildWriter(String outputFileLocation) {
        this.outputFileLocation = new File(outputFileLocation);
    }

    public AntBuildWriter(File outputFileLocation, String language) {
        this.outputFileLocation = outputFileLocation;
        this.language = language;
    }

    public void setDatabindingFramework(String databindingFramework) {
        this.databindingFramework = databindingFramework;
    }

    public void createOutFile(String packageName, String fileName) throws Exception {
        File outputFile = FileWriter.createClassFile(outputFileLocation,
                "",
                packageName.replaceAll("\\.","_")+"build",
                ".xml");
        //set the existing flag
        fileExists = outputFile.exists();
        if (!fileExists){
            this.stream = new FileOutputStream(outputFile);
        }
    }

     //overridden to get the correct behavior
    protected String findTemplate(Map languageSpecificPropertyMap) {
        String ownClazzName =  this.getClass().getName();
        String key;
        String propertyValue;
        String templateName = null;
        Iterator keys = languageSpecificPropertyMap.keySet().iterator();


        while (keys.hasNext()) {
            //check for template entries
            key = keys.next().toString();
            if (key.endsWith(TEMPLATE_SUFFIX)){
                // check if the class name is there
                propertyValue = languageSpecificPropertyMap.get(key).toString();
                if (propertyValue.startsWith(ownClazzName)){
                    if (key.indexOf(databindingFramework)!=-1){
                        templateName = propertyValue.substring(propertyValue.indexOf(SEPERATOR_STRING)+1) ;
                        break;
                    }
                }
            }

        }

        return templateName;

    }
}
