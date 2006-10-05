package org.apache.axis2.schema.writer;

import org.apache.axis2.schema.*;
import org.apache.axis2.schema.i18n.SchemaCompilerMessages;
import org.apache.axis2.schema.typemap.JavaTypeMap;
import org.apache.axis2.schema.util.PrimitiveTypeFinder;
import org.apache.axis2.schema.util.SchemaPropertyLoader;
import org.apache.axis2.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

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

/**
 * Java Bean writer for the schema compiler.
 */
public class JavaBeanWriter implements BeanWriter {

    private static final Log log = LogFactory.getLog(JavaBeanWriter .class);

    public static final String WRAPPED_DATABINDING_CLASS_NAME = "WrappedDatabinder";

    private String javaBeanTemplateName = null;

    private boolean templateLoaded = false;

    private Templates templateCache;

    private List namesList;

    private static int count = 0;

    private boolean wrapClasses = false;

    private boolean writeClasses = false;

    private String packageName = null;

    private File rootDir;

    private Document globalWrappedDocument;

    private Map modelMap = new HashMap();

    private static final String DEFAULT_PACKAGE = "adb";

    private Map baseTypeMap = new JavaTypeMap().getTypeMap();

    private Map ns2packageNameMap = new HashMap();

    private boolean isHelperMode = false;

    /**
     * package for the mapping class
     */
    private String mappingClassPackage = null;

    public static final String EXTENSION_MAPPER_CLASSNAME = "ExtensionMapper";

    /**
     * Default constructor
     */
    public JavaBeanWriter() {
    }

    /**
     * This returns a map of Qnames vs DOMDocument models. One can use this
     * method to obtain the raw DOMmodels used to write the classes. This has no
     * meaning when the classes are supposed to be wrapped so the
     *
     * @return Returns Map.
     * @throws SchemaCompilationException
     * @see BeanWriter#getModelMap()
     */
    public Map getModelMap() {
        return modelMap;
    }

    public void init(CompilerOptions options) throws SchemaCompilationException {
        try {
            initWithFile(options.getOutputLocation());
            packageName = options.getPackageName();
            writeClasses = options.isWriteOutput();
            if (!writeClasses) {
                wrapClasses = false;
            } else {
                wrapClasses = options.isWrapClasses();
            }

            // if the wrap mode is set then create a global document to keep the
            // wrapped
            // element models
            if (options.isWrapClasses()) {
                globalWrappedDocument = XSLTUtils.getDocument();
                Element rootElement = XSLTUtils.getElement(
                        globalWrappedDocument, "beans");
                globalWrappedDocument.appendChild(rootElement);
                XSLTUtils.addAttribute(globalWrappedDocument, "name",
                        WRAPPED_DATABINDING_CLASS_NAME, rootElement);
                String tempPackageName;

                if (packageName != null && packageName.endsWith(".")) {
                    tempPackageName = this.packageName.substring(0,
                            this.packageName.lastIndexOf("."));
                } else {
                    tempPackageName = DEFAULT_PACKAGE;
                }

                XSLTUtils.addAttribute(globalWrappedDocument, "package",
                        tempPackageName, rootElement);
            }

            // add the ns mappings
            this.ns2packageNameMap = options.getNs2PackageMap();
            //set helper mode
            this.isHelperMode = options.isHelperMode();
            //set mapper class package if present
            if (options.isMapperClassPackagePresent()) {
                this.mappingClassPackage = options.getMapperClassPackage();
            }

        } catch (IOException e) {
            throw new SchemaCompilationException(e);
        } catch (ParserConfigurationException e) {
            throw new SchemaCompilationException(e); // todo need to put
            // correct error
            // messages
        }
    }

    /**
     * @param element
     * @param typeMap
     * @param metainf
     * @return Returns String.
     * @throws SchemaCompilationException
     */
    public String write(XmlSchemaElement element, Map typeMap,
                        BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {

        try {
            QName qName = element.getQName();

            return process(qName, metainf, typeMap, true);
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }

    }

    /**
     * @param complexType
     * @param typeMap
     * @param metainf
     * @param fullyQualifiedClassName the name returned by makeFullyQualifiedClassName() or null if
     *                                it wasn't called
     * @throws org.apache.axis2.schema.SchemaCompilationException
     *
     * @see BeanWriter#write(org.apache.ws.commons.schema.XmlSchemaComplexType,
     *      java.util.Map, org.apache.axis2.schema.BeanWriterMetaInfoHolder)
     */
    public String write(XmlSchemaComplexType complexType, Map typeMap,
                        BeanWriterMetaInfoHolder metainf)
            throws SchemaCompilationException {

        try {
            // determine the package for this type.
            QName qName = complexType.getQName();
            return process(qName, metainf, typeMap, false);

        } catch (SchemaCompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }

    }

    /**
     * @throws Exception
     * @see BeanWriter#writeBatch()
     */
    public void writeBatch() throws SchemaCompilationException {
        try {
            if (wrapClasses) {
                String tempPackage;
                if (packageName == null) {
                    tempPackage = DEFAULT_PACKAGE;
                } else {
                    tempPackage = packageName;
                }
                File out = createOutFile(tempPackage,
                        WRAPPED_DATABINDING_CLASS_NAME);
                // parse with the template and create the files

                parse(globalWrappedDocument, out);
            }
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     * @param simpleType
     * @param typeMap
     * @param metainf
     * @return Returns String.
     * @throws SchemaCompilationException
     * @see BeanWriter#write(org.apache.ws.commons.schema.XmlSchemaSimpleType,
     *      java.util.Map, org.apache.axis2.schema.BeanWriterMetaInfoHolder)
     */
    public String write(XmlSchemaSimpleType simpleType, Map typeMap,
                        BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {
        try {
            QName qName = simpleType.getQName();
            if (qName == null) {
                qName = (QName) simpleType.getMetaInfoMap().get(SchemaConstants.SchemaCompilerInfoHolder.FAKE_QNAME);
            }
            metainf.addtStatus(qName, SchemaConstants.SIMPLE_TYPE_OR_CONTENT);
            return process(qName, metainf, typeMap, true);
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     * @param rootDir
     * @throws IOException
     * @see BeanWriter#init(java.io.File)
     */
    private void initWithFile(File rootDir) throws IOException {
        if (rootDir == null) {
            this.rootDir = new File(".");
        } else if (!rootDir.isDirectory()) {
            throw new IOException(SchemaCompilerMessages
                    .getMessage("schema.rootnotfolderexception"));
        } else {
            this.rootDir = rootDir;
        }

        namesList = new ArrayList();
        javaBeanTemplateName = SchemaPropertyLoader.getBeanTemplate();
    }

    /**
     * Make the fully qualified class name for an element or named type
     *
     * @param qName the qualified Name for this element or type in the schema
     * @return the appropriate fully qualified class name to use in generated
     *         code
     */
    public String makeFullyQualifiedClassName(QName qName) {

        String namespaceURI = qName.getNamespaceURI();
        String basePackageName;

        String packageName = getPackage(namespaceURI);

        String originalName = qName.getLocalPart();
        String className = makeUniqueJavaClassName(this.namesList, originalName);

        String packagePrefix = null;

        String fullyqualifiedClassName;

        if (wrapClasses)
            packagePrefix = (this.packageName == null ? DEFAULT_PACKAGE + "."
                    : this.packageName)
                    + WRAPPED_DATABINDING_CLASS_NAME;
        else if (writeClasses)
            packagePrefix = packageName;
        if (packagePrefix != null)
            fullyqualifiedClassName = packagePrefix
                    + (packagePrefix.endsWith(".") ? "" : ".") + className;
        else
            fullyqualifiedClassName = className;
        // return the fully qualified class name
        return fullyqualifiedClassName;
    }

    private String getPackage(String namespaceURI) {
        String basePackageName;
        if (ns2packageNameMap.containsKey(namespaceURI)) {
            basePackageName = (String) ns2packageNameMap.get(namespaceURI);
        } else {
            basePackageName = URLProcessor.makePackageName(namespaceURI);
        }

        return this.packageName == null ? basePackageName
                : this.packageName + basePackageName;
    }

    /**
     * A util method that holds common code for the complete schema that the
     * generated XML complies to look under other/beanGenerationSchema.xsd
     *
     * @param qName
     * @param metainf
     * @param typeMap
     * @param isElement
     * @param fullyQualifiedClassName the name returned by makeFullyQualifiedClassName() or null if
     *                                it wasn't called
     * @return Returns String.
     * @throws Exception
     */
    private String process(QName qName, BeanWriterMetaInfoHolder metainf,
                           Map typeMap, boolean isElement)
            throws Exception {
        String fullyQualifiedClassName = metainf.getOwnClassName();
        if (fullyQualifiedClassName == null)
            fullyQualifiedClassName = makeFullyQualifiedClassName(qName);
        String className = fullyQualifiedClassName
                .substring(1 + fullyQualifiedClassName.lastIndexOf('.'));
        String basePackageName;
        if (fullyQualifiedClassName.lastIndexOf('.') == -1) {// no 'dots' so
            // the package
            // is not there
            basePackageName = "";
        } else {
            basePackageName = fullyQualifiedClassName.substring(0,
                    fullyQualifiedClassName.lastIndexOf('.'));
        }

        String originalName = qName == null ? "" : qName.getLocalPart();
        ArrayList propertyNames = new ArrayList();

        if (!templateLoaded) {
            loadTemplate();
        }

        // if wrapped then do not write the classes now but add the models to a
        // global document. However in order to write the
        // global class that is generated, one needs to call the writeBatch()
        // method
        if (wrapClasses) {
            globalWrappedDocument.getDocumentElement().appendChild(
                    getBeanElement(globalWrappedDocument, className,
                            originalName, basePackageName, qName, isElement,
                            metainf, propertyNames, typeMap));

        } else {
            // create the model
            Document model = XSLTUtils.getDocument();
            // make the XML
            model.appendChild(getBeanElement(model, className, originalName,
                    basePackageName, qName, isElement, metainf, propertyNames,
                    typeMap));

            if (writeClasses) {
                // create the file
                File out = createOutFile(basePackageName, className);
                // parse with the template and create the files

                if (isHelperMode) {

                    XSLTUtils.addAttribute(model, "helperMode", "yes", model.getDocumentElement());

                    // Generate bean classes
                    parse(model, out);

                    // Generating the helper classes
                    out = createOutFile(basePackageName, className + "Helper");
                    XSLTUtils.addAttribute(model, "helper", "yes", model
                            .getDocumentElement());
                    parse(model, out);

                } else {
                    //No helper mode - just generate the classes
                    parse(model, out);
                }
            }

            // add the model to the model map
            modelMap.put(new QName(qName.getNamespaceURI(), className), model);

        }

        // return the fully qualified class name
        return fullyQualifiedClassName;

    }

    /**
     * @param model
     * @param className
     * @param originalName
     * @param packageName
     * @param qName
     * @param isElement
     * @param metainf
     * @param propertyNames
     * @param typeMap
     * @return Returns Element.
     * @throws SchemaCompilationException
     */
    private Element getBeanElement(Document model,
                                   String className,
                                   String originalName,
                                   String packageName,
                                   QName qName,
                                   boolean isElement,
                                   BeanWriterMetaInfoHolder metainf,
                                   ArrayList propertyNames,
                                   Map typeMap)
            throws SchemaCompilationException {

        Element rootElt = XSLTUtils.getElement(model, "bean");
        XSLTUtils.addAttribute(model, "name", className, rootElt);
        XSLTUtils.addAttribute(model, "originalName", originalName, rootElt);
        XSLTUtils.addAttribute(model, "package", packageName, rootElt);
        XSLTUtils
                .addAttribute(model, "nsuri", qName.getNamespaceURI(), rootElt);
        XSLTUtils.addAttribute(model, "nsprefix", getPrefixForURI(qName
                .getNamespaceURI(), qName.getPrefix()), rootElt);

        if (!wrapClasses) {
            XSLTUtils.addAttribute(model, "unwrapped", "yes", rootElt);
        }

        if (!writeClasses) {
            XSLTUtils.addAttribute(model, "skip-write", "yes", rootElt);
        }

        if (!isElement) {
            XSLTUtils.addAttribute(model, "type", "yes", rootElt);
        }

        if (metainf.isAnonymous()) {
            XSLTUtils.addAttribute(model, "anon", "yes", rootElt);
        }

        if (metainf.isExtension()) {
            XSLTUtils.addAttribute(model, "extension", metainf
                    .getExtensionClassName(), rootElt);

        }
        if (metainf.isRestriction()) {
            XSLTUtils.addAttribute(model, "restriction", metainf
                    .getRestrictionClassName(), rootElt);

        }
        //add the mapper class name
        XSLTUtils.addAttribute(model, "mapperClass", getFullyQualifiedMapperClassName(), rootElt);

        if (metainf.isChoice()) {
            XSLTUtils.addAttribute(model, "choice", "yes", rootElt);
        }

        if (metainf.isSimple()) {
            XSLTUtils.addAttribute(model, "simple", "yes", rootElt);
        }

        if (metainf.isOrdered()) {
            XSLTUtils.addAttribute(model, "ordered", "yes", rootElt);
        }

        if (isElement && metainf.isNillable(qName)) {
            XSLTUtils.addAttribute(model, "nillable", "yes", rootElt);
        }

        // populate all the information
        populateInfo(metainf, model, rootElt, propertyNames, typeMap, false);

        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElt));
        ////////////////////////////////////////////////////////////

        return rootElt;
    }

    /**
     * @param metainf
     * @param model
     * @param rootElt
     * @param propertyNames
     * @param typeMap
     * @throws SchemaCompilationException
     */
    private void populateInfo(BeanWriterMetaInfoHolder metainf, Document model,
                              Element rootElt, ArrayList propertyNames, Map typeMap,
                              boolean isInherited) throws SchemaCompilationException {
        if (metainf.getParent() != null) {
            populateInfo(metainf.getParent(), model, rootElt, propertyNames,
                    typeMap, true);
        }
        addPropertyEntries(metainf, model, rootElt, propertyNames, typeMap,
                isInherited);

    }

    /**
     * @param metainf
     * @param model
     * @param rootElt
     * @param propertyNames
     * @param typeMap
     * @throws SchemaCompilationException
     */
    private void addPropertyEntries(BeanWriterMetaInfoHolder metainf,
                                    Document model, Element rootElt, ArrayList propertyNames,
                                    Map typeMap, boolean isInherited) throws SchemaCompilationException {
        // go in the loop and add the part elements
        QName[] qName;
        String javaClassNameForElement;
        ArrayList missingQNames = new ArrayList();
        ArrayList qNames = new ArrayList();

        BeanWriterMetaInfoHolder parentMetaInf = metainf.getParent();

        if (metainf.isOrdered()) {
            qName = metainf.getOrderedQNameArray();
        } else {
            qName = metainf.getQNameArray();
        }

        for (int i = 0; i < qName.length; i++) {
            qNames.add(qName[i]);
        }
        //adding missing QNames to the end, including elements & attributes.
        if (metainf.isRestriction()) {
            addMissingQNames(metainf, qNames, missingQNames);
        }
        QName name;

        for (int i = 0; i < qNames.size(); i++) {
            name = (QName) qNames.get(i);
            Element property = XSLTUtils.addChildElement(model, "property", rootElt);

            String xmlName = name.getLocalPart();
            XSLTUtils.addAttribute(model, "name", xmlName, property);
            XSLTUtils.addAttribute(model, "nsuri", name.getNamespaceURI(), property);
            String javaName = makeUniqueJavaClassName(propertyNames, xmlName);
            XSLTUtils.addAttribute(model, "javaname", javaName, property);

            if (parentMetaInf != null && metainf.isRestriction() && missingQNames.contains(name)) {
                javaClassNameForElement = parentMetaInf.getClassNameForQName(name);
            } else {
                javaClassNameForElement = metainf.getClassNameForQName(name);
            }

            if (javaClassNameForElement == null) {
                javaClassNameForElement = SchemaCompiler.DEFAULT_CLASS_NAME;
                log.warn(SchemaCompilerMessages
                        .getMessage("schema.typeMissing", name.toString()));
            }

            if (metainf.isRestriction() && typeChanged(name, missingQNames, metainf)) {
                XSLTUtils.addAttribute(model, "typeChanged", "yes", property);
                //XSLTUtils.addAttribute(model, "restricted", "yes", property);
            }

            XSLTUtils.addAttribute(model, "type", javaClassNameForElement, property);

            if (PrimitiveTypeFinder.isPrimitive(javaClassNameForElement)) {
                XSLTUtils.addAttribute(model, "primitive", "yes", property);
            }
            // add an attribute that says the type is default
            if (isDefault(javaClassNameForElement)) {
                XSLTUtils.addAttribute(model, "default", "yes", property);
            }

            if (typeMap.containsKey(metainf.getSchemaQNameForQName(name))) {
                XSLTUtils.addAttribute(model, "ours", "yes", property);
            }

            if (metainf.getAttributeStatusForQName(name)) {
                XSLTUtils.addAttribute(model, "attribute", "yes", property);
            }

            if (metainf.isNillable(name)) {
                XSLTUtils.addAttribute(model, "nillable", "yes", property);
            }

            if (metainf.getOptionalAttributeStatusForQName(name)) {
                XSLTUtils.addAttribute(model, "optional", "yes", property);
            }

            String shortTypeName;
            if (metainf.getSchemaQNameForQName(name) != null) {
                // see whether the QName is a basetype
                if (baseTypeMap.containsKey(metainf.getSchemaQNameForQName(name))) {
                    shortTypeName = metainf.getSchemaQNameForQName(name).getLocalPart();
                } else {
                    shortTypeName = getShortTypeName(javaClassNameForElement);
                }
            } else {
                shortTypeName = getShortTypeName(javaClassNameForElement);
            }
            XSLTUtils.addAttribute(model, "shorttypename", shortTypeName, property);

            if (metainf.isRestriction() && missingQNames.contains(name)) {
                //XSLTUtils.addAttribute(model, "restricted", "yes", property);
                XSLTUtils.addAttribute(model, "removed", "yes", property);
            }

            if (isInherited) {
                XSLTUtils.addAttribute(model, "inherited", "yes", property);
            }

            if (metainf.getAnyStatusForQName(name)) {
                XSLTUtils.addAttribute(model, "any", "yes", property);
            }

            if (metainf.getBinaryStatusForQName(name)) {
                XSLTUtils.addAttribute(model, "binary", "yes", property);
            }

            if (metainf.isSimple() || metainf.getSimpleStatusForQName(name)) {
                XSLTUtils.addAttribute(model, "simple", "yes", property);
            }

            // put the min occurs count irrespective of whether it's an array or
            // not
            long minOccurs = metainf.getMinOccurs(name);
            XSLTUtils.addAttribute(model, "minOccurs", minOccurs + "", property);

            //in the case the original element is an array but the derived one is not.
            if (parentMetaInf != null && metainf.isRestriction() && !missingQNames.contains(name) &&
                    (parentMetaInf.getArrayStatusForQName(name) && !metainf.getArrayStatusForQName(name))) {

                XSLTUtils.addAttribute(model, "rewrite", "yes", property);
                XSLTUtils.addAttribute(model, "occuranceChanged", "yes", property);
            } else if (metainf.isRestriction() && !missingQNames.contains(name) &&
                    (minOccursChanged(name, missingQNames, metainf) || maxOccursChanged(name, missingQNames, metainf))) {

                XSLTUtils.addAttribute(model, "restricted", "yes", property);
                XSLTUtils.addAttribute(model, "occuranceChanged", "yes", property);
            }

            if (metainf.getArrayStatusForQName(name)) {

                XSLTUtils.addAttribute(model, "array", "yes", property);

                int endIndex = javaClassNameForElement.indexOf("[");
                if (endIndex >= 0) {
                    XSLTUtils.addAttribute(model, "arrayBaseType",
                            javaClassNameForElement.substring(0, endIndex), property);
                } else {
                    XSLTUtils.addAttribute(model, "arrayBaseType",
                            javaClassNameForElement, property);
                }

                long maxOccurs = metainf.getMaxOccurs(name);
                if (maxOccurs == Long.MAX_VALUE) {
                    XSLTUtils.addAttribute(model, "unbound", "yes", property);
                } else {
                    XSLTUtils.addAttribute(model, "maxOccurs", maxOccurs + "", property);
                }
            }
            if (metainf.isRestrictionBaseType(name)) {
                XSLTUtils.addAttribute(model, "restrictionBaseType", "yes", property);
            }

            if (metainf.isExtensionBaseType(name)) {
                XSLTUtils.addAttribute(model, "extensionBaseType", "yes", property);
            }

            if (metainf.isRestrictionBaseType(name) && metainf.getLengthFacet() != -1) {
                XSLTUtils.addAttribute(model, "lenFacet", metainf.getLengthFacet() + "", property);
            }

            if (metainf.isRestrictionBaseType(name) && metainf.getMaxLengthFacet() != -1) {
                XSLTUtils.addAttribute(model, "maxLenFacet", metainf.getMaxLengthFacet() + "", property);
            }

            if (metainf.isRestrictionBaseType(name) && metainf.getMinLengthFacet() != -1) {
                XSLTUtils.addAttribute(model, "minLenFacet", metainf.getMinLengthFacet() + "", property);
            }

            if (metainf.isRestrictionBaseType(name) && metainf.getMaxExclusiveFacet() != null) {
                XSLTUtils.addAttribute(model, "maxExFacet", metainf.getMaxExclusiveFacet() + "", property);
            }

            if (metainf.isRestrictionBaseType(name) && metainf.getMinExclusiveFacet() != null) {
                XSLTUtils.addAttribute(model, "minExFacet", metainf.getMinExclusiveFacet() + "", property);
            }

            if (metainf.isRestrictionBaseType(name) && metainf.getMaxInclusiveFacet() != null) {
                XSLTUtils.addAttribute(model, "maxInFacet", metainf.getMaxInclusiveFacet() + "", property);
            }

            if (metainf.isRestrictionBaseType(name) && metainf.getMinInclusiveFacet() != null) {
                XSLTUtils.addAttribute(model, "minInFacet", metainf.getMinInclusiveFacet() + "", property);
            }

            if (!metainf.getEnumFacet().isEmpty()) {
                boolean validJava = true;    // Assume all enum values are valid ids

                Iterator iterator = metainf.getEnumFacet().iterator();
                // Walk the values looking for invalid ids
                while (iterator.hasNext()) {
                    String value = (String) iterator.next();
                    if (!JavaUtils.isJavaId(value)) {
                        validJava = false;
                    }
                }

                int id = 0;
                iterator = metainf.getEnumFacet().iterator();
                while (iterator.hasNext()) {
                    Element enumFacet = XSLTUtils.addChildElement(model, "enumFacet", property);
                    String attribValue = (String) iterator.next();
                    XSLTUtils.addAttribute(model, "value", attribValue, enumFacet);
                    if (validJava) {
                        XSLTUtils.addAttribute(model, "id", attribValue, enumFacet);
                    } else {
                        id++;
                        XSLTUtils.addAttribute(model, "id", "value" + id, enumFacet);
                    }
                }
            }

            if (metainf.isRestrictionBaseType(name) && metainf.getPatternFacet() != null) {
                XSLTUtils.addAttribute(model, "patternFacet", metainf.getPatternFacet(), property);
            }
        }
    }

    private void addMissingQNames(BeanWriterMetaInfoHolder metainf, ArrayList qName, ArrayList missingQNames) {

        QName[] qNames = null;
        QName[] pQNames = null;

        BeanWriterMetaInfoHolder parentMetaInf = metainf.getParent();

        if (metainf.isOrdered()) {
            qNames = metainf.getOrderedQNameArray();
        } else {
            qNames = metainf.getQNameArray();
        }

        if (parentMetaInf != null) {
            if (parentMetaInf.isOrdered()) {
                pQNames = parentMetaInf.getOrderedQNameArray();
            } else {
                pQNames = parentMetaInf.getQNameArray();
            }
        }


        for (int i = 0; pQNames != null && i < pQNames.length; i++) {
            if (qNameNotFound(pQNames[i], metainf)) {
                missingQNames.add(pQNames[i]);
            }
        }
        //adding missing QNames to the end of list.
        if (!missingQNames.isEmpty()) {
            for (int i = 0; i < missingQNames.size(); i++) {
                qName.add(missingQNames.get(i));
            }
        }

    }

    private boolean qNameNotFound(QName qname, BeanWriterMetaInfoHolder metainf) {

        boolean found = false;
        QName[] qNames;

        if (metainf.isOrdered()) {
            qNames = metainf.getOrderedQNameArray();
        } else {
            qNames = metainf.getQNameArray();
        }

        for (int j = 0; j < qNames.length; j++) {
            if (qname.getLocalPart().equals(qNames[j].getLocalPart())) {
                found = true;
            }
        }
        return !found;
    }

    private boolean typeChanged(QName qname, ArrayList missingQNames, BeanWriterMetaInfoHolder metainf) {

        boolean typeChanged = false;
        QName[] pQNames;

        BeanWriterMetaInfoHolder parentMetainf = metainf.getParent();

        if (parentMetainf != null && !missingQNames.contains(qname)) {

            if (parentMetainf.isOrdered()) {
                pQNames = parentMetainf.getOrderedQNameArray();
            } else {
                pQNames = parentMetainf.getQNameArray();
            }

            for (int j = 0; j < pQNames.length; j++) {
                if (qname.getLocalPart().equals(pQNames[j].getLocalPart())) {

                    String javaClassForParentElement = parentMetainf.getClassNameForQName(pQNames[j]);
                    String javaClassForElement = metainf.getClassNameForQName(qname);

                    if (!javaClassForParentElement.equals(javaClassForElement)) {
                        if (javaClassForParentElement.endsWith("[]")) {
                            if ((javaClassForParentElement.substring(0, javaClassForParentElement.indexOf('['))).equals(javaClassForElement)) {
                                continue;
                            }
                        } else if (javaClassForElement.endsWith("[]")) {
                            if ((javaClassForElement.substring(0, javaClassForElement.indexOf('['))).equals(javaClassForParentElement)) {
                                continue;
                            }
                        } else {
                            typeChanged = true;
                        }
                    }
                }
            }
        }
        return typeChanged;
    }

    private boolean minOccursChanged(QName qname, ArrayList missingQNames, BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {

        boolean minChanged = false;
        QName[] pQNames;

        BeanWriterMetaInfoHolder parentMetainf = metainf.getParent();

        if (parentMetainf != null && !missingQNames.contains(qname)) {

            if (parentMetainf.isOrdered()) {
                pQNames = parentMetainf.getOrderedQNameArray();
            } else {
                pQNames = parentMetainf.getQNameArray();
            }

            for (int j = 0; j < pQNames.length; j++) {
                if (qname.getLocalPart().equals(pQNames[j].getLocalPart())) {

                    if (metainf.getMinOccurs(qname) > parentMetainf.getMinOccurs(pQNames[j])) {
                        minChanged = true;
                    } else if (metainf.getMinOccurs(qname) < parentMetainf.getMinOccurs(pQNames[j])) {
                        throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("minOccurs Wrong!"));
                    }

                }
            }
        }
        return minChanged;
    }

    private boolean maxOccursChanged(QName qname, ArrayList missingQNames, BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {

        boolean maxChanged = false;
        QName[] pQNames;

        BeanWriterMetaInfoHolder parentMetainf = metainf.getParent();

        if (parentMetainf != null && !missingQNames.contains(qname)) {
            if (parentMetainf.isOrdered()) {
                pQNames = parentMetainf.getOrderedQNameArray();
            } else {
                pQNames = parentMetainf.getQNameArray();
            }

            for (int j = 0; j < pQNames.length; j++) {
                if (qname.getLocalPart().equals(pQNames[j].getLocalPart())) {

                    if (metainf.getMaxOccurs(qname) < parentMetainf.getMaxOccurs(pQNames[j])) {
                        maxChanged = true;
                    } else if (metainf.getMaxOccurs(qname) > parentMetainf.getMaxOccurs(pQNames[j])) {
                        throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("maxOccurs Wrong!"));
                    }
                }
            }
        }
        return maxChanged;
    }

    /**
     * Test whether the given class name matches the default
     *
     * @param javaClassNameForElement
     * @return
     */
    private boolean isDefault(String javaClassNameForElement) {
        return SchemaCompiler.DEFAULT_CLASS_NAME
                .equals(javaClassNameForElement)
                || SchemaCompiler.DEFAULT_CLASS_ARRAY_NAME
                .equals(javaClassNameForElement);
    }

    /**
     * Given the xml name, make a unique class name taking into account that
     * some file systems are case sensitive and some are not. -Consider the
     * Jax-WS spec for this
     *
     * @param listOfNames
     * @param xmlName
     * @return Returns String.
     */
    private String makeUniqueJavaClassName(List listOfNames, String xmlName) {
        String javaName;
        if (JavaUtils.isJavaKeyword(xmlName)) {
            javaName = JavaUtils.makeNonJavaKeyword(xmlName);
        } else {
            javaName = JavaUtils.capitalizeFirstChar(JavaUtils
                    .xmlNameToJava(xmlName));
        }

        while (listOfNames.contains(javaName.toLowerCase())) {
            javaName = javaName + count++;
        }

        listOfNames.add(javaName.toLowerCase());
        return javaName;
    }

    /**
     * A bit of code from the old code generator. We are better off using the
     * template engines and such stuff that's already there. But the class
     * writers are hard to be reused so some code needs to be repeated (atleast
     * a bit)
     */
    private void loadTemplate() throws SchemaCompilationException {

        // first get the language specific property map
        Class clazz = this.getClass();
        InputStream xslStream;
        String templateName = javaBeanTemplateName;
        if (templateName != null) {
            try {
                xslStream = clazz.getResourceAsStream(templateName);
                templateCache = TransformerFactory.newInstance().newTemplates(
                        new StreamSource(xslStream));
                templateLoaded = true;
            } catch (TransformerConfigurationException e) {
                throw new SchemaCompilationException(SchemaCompilerMessages
                        .getMessage("schema.templateLoadException"), e);
            }
        } else {
            throw new SchemaCompilationException(SchemaCompilerMessages
                    .getMessage("schema.templateNotFoundException"));
        }
    }

    /**
     * Creates the output file
     *
     * @param packageName
     * @param fileName
     * @throws Exception
     */
    private File createOutFile(String packageName, String fileName)
            throws Exception {
        return org.apache.axis2.util.FileWriter.createClassFile(this.rootDir,
                packageName, fileName, ".java");
    }

    /**
     * Writes the output file
     *
     * @param doc
     * @param outputFile
     * @throws Exception
     */
    private void parse(Document doc, File outputFile) throws Exception {
        OutputStream outStream = new FileOutputStream(outputFile);
        XSLTTemplateProcessor.parse(outStream, doc, this.templateCache
                .newTransformer());
        outStream.flush();
        outStream.close();

        PrettyPrinter.prettify(outputFile);
    }

    /**
     * Get a prefix for a namespace URI. This method will ALWAYS return a valid
     * prefix - if the given URI is already mapped in this serialization, we
     * return the previous prefix. If it is not mapped, we will add a new
     * mapping and return a generated prefix of the form "ns<num>".
     *
     * @param uri is the namespace uri
     * @return Returns prefix.
     */
    public String getPrefixForURI(String uri) {
        return getPrefixForURI(uri, null);
    }

    /**
     * Last used index suffix for "ns"
     */
    private int lastPrefixIndex = 1;

    /**
     * Map of namespaces URI to prefix(es)
     */
    HashMap mapURItoPrefix = new HashMap();

    HashMap mapPrefixtoURI = new HashMap();

    /**
     * Get a prefix for the given namespace URI. If one has already been defined
     * in this serialization, use that. Otherwise, map the passed default prefix
     * to the URI, and return that. If a null default prefix is passed, use one
     * of the form "ns<num>"
     */
    public String getPrefixForURI(String uri, String defaultPrefix) {
        if ((uri == null) || (uri.length() == 0))
            return null;
        String prefix = (String) mapURItoPrefix.get(uri);
        if (prefix == null) {
            if (defaultPrefix == null || defaultPrefix.length() == 0) {
                prefix = "ns" + lastPrefixIndex++;
                while (mapPrefixtoURI.get(prefix) != null) {
                    prefix = "ns" + lastPrefixIndex++;
                }
            } else {
                prefix = defaultPrefix;
            }
            mapPrefixtoURI.put(prefix, uri);
            mapURItoPrefix.put(uri, prefix);
        }
        return prefix;
    }

    private String getShortTypeName(String typeClassName) {
        if (typeClassName.endsWith("[]")) {
            typeClassName = typeClassName.substring(0, typeClassName
                    .lastIndexOf("["));
        }

        return typeClassName.substring(typeClassName.lastIndexOf(".") + 1,
                typeClassName.length());

    }

    /**
     * Get the mapper class name - there is going to be only one
     * mapper class for the whole
     *
     * @return
     */
    private String getFullyQualifiedMapperClassName() {
        if (wrapClasses || !writeClasses) {
            return EXTENSION_MAPPER_CLASSNAME;
        } else {
            return mappingClassPackage + "." + EXTENSION_MAPPER_CLASSNAME;
        }
    }

    /**
     * get the mapper class package name
     * May be ignored by the implementer
     *
     * @return
     */
    public String getExtensionMapperPackageName() {
        return mappingClassPackage;
    }

    /**
     * Sets the mapping class name of this writer. A mapping class
     * package set by the options may be overridden at the this point
     *
     * @param mapperPackageName
     */
    public void registerExtensionMapperPackageName(String mapperPackageName) {
        this.mappingClassPackage = mapperPackageName;
    }

    /**
     * Write the extension classes - this is needed to process
     * the hierarchy of classes
     *
     * @param metainfArray
     * @return
     */
    public void writeExtensionMapper(BeanWriterMetaInfoHolder[] metainfArray) throws SchemaCompilationException {
        //generate the element
        try {


            String mapperClassName = getFullyQualifiedMapperClassName();

            Document model = XSLTUtils.getDocument();
            Element rootElt = XSLTUtils.getElement(model, "mapper");
            String mapperName = mapperClassName.substring(mapperClassName.lastIndexOf(".") + 1);
            XSLTUtils.addAttribute(model, "name", mapperName, rootElt);
            String basePackageName = "";
            if (mapperClassName.indexOf(".") != -1) {
                basePackageName = mapperClassName.substring(0, mapperClassName.lastIndexOf("."));
                XSLTUtils.addAttribute(model, "package", basePackageName, rootElt);
            } else {
                XSLTUtils.addAttribute(model, "package", "", rootElt);
            }

            if (!wrapClasses) {
                XSLTUtils.addAttribute(model, "unwrapped", "yes", rootElt);
            }

            if (!writeClasses) {
                XSLTUtils.addAttribute(model, "skip-write", "yes", rootElt);
            }

            if (isHelperMode) {
                XSLTUtils.addAttribute(model, "helpermode", "yes", rootElt);
            }

            for (int i = 0; i < metainfArray.length; i++) {
                QName ownQname = metainfArray[i].getOwnQname();
                String className = metainfArray[i].getOwnClassName();
                //do  not add when the qname is not availble
                if (ownQname != null) {
                    Element typeChild = XSLTUtils.addChildElement(model, "type", rootElt);
                    XSLTUtils.addAttribute(model, "nsuri", ownQname.getNamespaceURI(), typeChild);
                    XSLTUtils.addAttribute(model, "classname", className == null ? "" : className, typeChild);
                    XSLTUtils.addAttribute(model, "shortname", ownQname == null ? "" :
                            ownQname.getLocalPart(), typeChild);
                }
            }

            model.appendChild(rootElt);

            if (!templateLoaded) {
                loadTemplate();
            }

            if (wrapClasses) {
                rootElt = (Element) globalWrappedDocument.importNode(rootElt, true);
                //add to the global wrapped document
                globalWrappedDocument.getDocumentElement().appendChild(rootElt);
            } else {
                if (writeClasses) {
                    // create the file
                    File out = createOutFile(basePackageName, mapperName);
                    // parse with the template and create the files
                    parse(model, out);

                }

                // add the model to the model map
                modelMap.put(new QName(mapperName), model);
            }

        } catch (ParserConfigurationException e) {
            throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("schema.docuement.error"), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SchemaCompilationException(e);
        }


    }

}


