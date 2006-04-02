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


package org.apache.axis2.receivers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;

import java.lang.reflect.Method;

/**
 * This is a Simple java Provider.
 */
public class RawXMLINOutMessageReceiver extends AbstractInOutSyncMessageReceiver
        implements MessageReceiver {

    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(getClass());

    /**
     * Constructor RawXMLProvider
     */
    public RawXMLINOutMessageReceiver() {
    }

    public Method findOperation(AxisOperation op, Class ImplClass) {
        Method method = null;
        String methodName = op.getName().getLocalPart();
        Method[] methods = ImplClass.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                method = methods[i];

                break;
            }
        }

        return method;
    }

    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newmsgContext)
            throws AxisFault {
        try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            // find the WebService method
            Class ImplClass = obj.getClass();

            // Inject the Message Context if it is asked for
            DependencyManager.configureBusinessLogicProvider(obj, msgContext, newmsgContext);

            AxisOperation opDesc = msgContext.getOperationContext().getAxisOperation();
            Method method = findOperation(opDesc, ImplClass);

            if (method != null) {
                Class[]  parameters = method.getParameterTypes();
                Object[] args;

                if ((parameters == null) || (parameters.length == 0)) {
                    args = new Object[0];
                } else if (parameters.length == 1) {
                    OMElement omElement = msgContext.getEnvelope().getBody().getFirstElement();
                    args = new Object[]{omElement};
                } else {
                    throw new AxisFault(Messages.getMessage("rawXmlProivdeIsLimited"));
                }

                OMElement result = (OMElement) method.invoke(obj, args);
                AxisService service = msgContext.getAxisService();
                service.getTargetNamespace();
                result.declareNamespace(service.getTargetNamespace(),
                        service.getTargetNamespacePrefix());
                OMElement bodyContent;

                SOAPFactory fac = getSOAPFactory(msgContext);
                bodyContent = result;

                SOAPEnvelope envelope = fac.getDefaultEnvelope();

                if (bodyContent != null) {
                    envelope.getBody().addChild(bodyContent);
                }

                newmsgContext.setEnvelope(envelope);
            } else {
                throw new AxisFault(Messages.getMessage("methodNotImplemented",
                        opDesc.getName().toString()));
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}
