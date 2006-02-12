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


package org.apache.axis2.addressing;

import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class EndpointReference
 * This class models the WS-A EndpointReferenceType. But this can be used without any WS-A handlers as well
 * Since the models for this in Submission and Final versions are different, lets make this to comply with
 * WS-A Final version. So any information found with WS-A submission will be "pumped" in to this model.
 */
public class EndpointReference implements Serializable {

    private static final long serialVersionUID = 5278892171162372439L;

    /**
     * <EndpointReference>
     * <Address>xs:anyURI</Address>
     * <ReferenceParameters>xs:any*</ReferenceParameters>
     * <MetaData>xs:any*</MetaData>
     * <!-- In addition to this, EPR can contain any number of OMElements -->
     * </EndpointReference>
     */


    private String address;
    private OMElement metaData;
    private Map referenceParameters;
    private ArrayList omElements;


    /**
     * 
     * @param address
     */
    public EndpointReference(String address) {
        this.address = address;
    }

    /**
     * @param omElement
     */
    public void addReferenceParameter(OMElement omElement) {
        if (omElement == null) {
            return;
        }
        if (referenceParameters == null) {
            referenceParameters = new HashMap();
        }
        referenceParameters.put(omElement.getQName(), omElement);
    }

    /**
     * @param qname
     * @param value - the text of the OMElement. Remember that this is a convenient method for the user,
     *              which has limited capability. If you want more power use @See EndpointReference#addReferenceParameter(OMElement)
     */
    public void addReferenceParameter(QName qname, String value) {
        if (qname == null) {
            return;
        }
        OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement(qname, null);
        omElement.setText(value);
        addReferenceParameter(omElement);
    }

    /**
     * This will return a Map of reference parameters with QName as the key and an OMElement
     * as the value
     *
     * @return - map of the reference parameters, where the key is the QName of the reference parameter
     *         and the value is an OMElement
     */
    public Map getAllReferenceParameters() {
        return referenceParameters;
    }

    public String getAddress() {
        return address;
    }

    /**
     *
     * @param address - xs:anyURI
     */
    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList getOmElements() {
        return omElements;
    }

    /**
     * {any}
     * @param omElements
     */
    public void setOmElements(ArrayList omElements) {
        this.omElements = omElements;
    }

    public OMElement getMetaData() {
        return metaData;
    }

    public void setMetaData(OMElement metaData) {
        this.metaData = metaData;
    }

    /**
     * Set a Map with QName as the key and an OMElement
     * as the value
     *
     * @param referenceParameters
     */
    public void setReferenceParameters(Map referenceParameters) {
        this.referenceParameters = referenceParameters;
    }

}
