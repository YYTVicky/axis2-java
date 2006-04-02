/**
 * CreateAccountRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

package org.apache.axis2.databinding;

/**
 * CreateAccountRequest bean class
 */

public class CreateAccountRequest implements
        org.apache.axis2.databinding.ADBBean {

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
            "http://www.wso2.com/types", "createAccountRequest", "ns1");

    /**
     * field for ClientInfo
     */
    protected ClientInfo localClientInfo;

    /**
     * Auto generated getter method
     * 
     * @return com.wso2.www.types.ClientInfo
     */
    public ClientInfo getClientInfo() {
        return localClientInfo;
    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            ClientInfo
     */
    public void setClientInfo(ClientInfo param) {

        this.localClientInfo = param;
    }

    /**
     * field for Password
     */
    protected java.lang.String localPassword;

    /**
     * Auto generated getter method
     * 
     * @return java.lang.String
     */
    public java.lang.String getPassword() {
        return localPassword;
    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            Password
     */
    public void setPassword(java.lang.String param) {

        this.localPassword = param;
    }

    /**
     * databinding method to get an XML representation of this object
     * 
     */
    public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName) {

        java.util.ArrayList elementList = new java.util.ArrayList();
        java.util.ArrayList attribList = new java.util.ArrayList();

        elementList.add(new javax.xml.namespace.QName(
                "http://www.wso2.com/types", "clientinfo"));
        elementList.add(localClientInfo);

        elementList.add(new javax.xml.namespace.QName("", "password"));
        elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
                .convertToString(localPassword));

        return org.apache.axis2.databinding.utils.ADBPullParser
                .createPullParser(qName, elementList.toArray(), attribList
                        .toArray());

    }

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {
        /**
         * static method to create the object
         */
        public static CreateAccountRequest parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
            CreateAccountRequest object = new CreateAccountRequest();
            try {
                int event = reader.getEventType();
                int count = 0;
                int argumentCount = 2;
                boolean done = false;
                // event better be a START_ELEMENT. if not we should go up to
                // the start element here
                while (!reader.isStartElement()) {
                    event = reader.next();
                }

                while (!done) {
                    if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event) {

                        if ("clientinfo".equals(reader.getLocalName())) {

                            object
                                    .setClientInfo(ClientInfo.Factory
                                            .parse(reader));
                            count++;

                        }

                        if ("password".equals(reader.getLocalName())) {

                            String content = reader.getElementText();
                            object
                                    .setPassword(org.apache.axis2.databinding.utils.ConverterUtil
                                            .convertTostring(content));
                            count++;

                        }

                    }

                    if (argumentCount == count) {
                        done = true;
                    }

                    if (!done) {
                        event = reader.next();
                    }

                }

            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }
    }// end of factory class

}
