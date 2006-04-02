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

package org.apache.axis2.mtom;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.attachments.ByteArrayDataSource;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMText;
import org.apache.ws.commons.om.impl.llom.OMTextImpl;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

public class EchoRawMTOMToBase64Test extends TestCase {
    private EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis2/services/EchoXMLService/echoMTOMtoBase64");

    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("EchoXMLService");

    private QName operationName = new QName("echoMTOMtoBase64");

    private AxisService service;

    OMText expectedTextData;

    private boolean finish = false;

    public EchoRawMTOMToBase64Test() {
        super(EchoRawMTOMToBase64Test.class.getName());
    }

    public EchoRawMTOMToBase64Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }

    private OMElement createPayload() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement rpcWrapEle = fac.createOMElement("echoMTOMtoBase64", omNs);
        OMElement data = fac.createOMElement("data", omNs);
        byte[] byteArray = new byte[]{13, 56, 65, 32, 12, 12, 7, -3, -2, -1,
                98};
        DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(byteArray));
        expectedTextData = new OMTextImpl(dataHandler, true, fac);
        data.addChild(expectedTextData);
        rpcWrapEle.addChild(data);
        return rpcWrapEle;
    }

    public void testEchoXMLASync() throws Exception {
        OMElement payload = createPayload();
        Options clientOptions = new Options();
        clientOptions.setTo(targetEPR);
        clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);


        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                SOAPEnvelope envelope = result.getResponseEnvelope();

                OMElement data = (OMElement) envelope.getBody().getFirstElement().getFirstOMChild();
                compareWithCreatedOMText(data.getText());
                finish = true;
            }

            public void onError(Exception e) {
                log.info(e.getMessage());
                finish = true;
            }
        };

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo",null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(clientOptions);

        sender.sendReceiveNonBlocking(payload, callback);

        int index = 0;
        while (!finish) {
            Thread.sleep(1000);
            index++;
            if (index > 10) {
                throw new AxisFault(
                        "Server was shutdown as the async response take too long to complete");
            }
        }
    }

    public void testEchoXMLSync() throws Exception {
        for (int i = 0; i < 10; i++) {
            OMElement payload = createPayload();

            Options clientOptions = new Options();
            clientOptions.setTo(targetEPR);
            clientOptions.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,null);
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(clientOptions);

            OMElement result = sender.sendReceive(payload);

            OMElement data = (OMElement) result.getFirstOMChild();
            compareWithCreatedOMText(data.getText());
            log.info("" + i);
            UtilServer.unDeployClientService();
        }
    }

    private void compareWithCreatedOMText(String actualText) {
        String originalTextValue = expectedTextData.getText();
        TestCase.assertEquals(actualText, originalTextValue);
    }

}