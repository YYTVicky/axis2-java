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


package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AxisServiceGroup extends AxisDescription {

    private Log log = LogFactory.getLog(getClass());

    // to store module ref at deploy time parsing
    private ArrayList modulesList = new ArrayList();

    // to store service Group engagedModules name
    private ArrayList engagedModules;

    // to store modeule configuration info
    private HashMap moduleConfigmap;

    // class loader
    private ClassLoader serviceGroupClassLoader;

    // to keep name of the service group
    private String serviceGroupName;

    /**
     * Field services
     */
//    private HashMap services;
    public AxisServiceGroup() {
//        services = new HashMap();
        moduleConfigmap = new HashMap();
        engagedModules = new ArrayList();
    }

    public AxisServiceGroup(AxisConfiguration axisDescription) {
        this();
        setParent(axisDescription);
    }

    /**
     * Adds module configuration , if there is moduleConfig tag in service.
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        if (moduleConfigmap == null) {
            moduleConfigmap = new HashMap();
        }

        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
    }

    public void addModuleref(QName moduleref) {
        modulesList.add(moduleref);
    }

    public void addService(AxisService service) throws AxisFault {
        service.setParent(this);

        AxisConfiguration axisConfig = (AxisConfiguration) getParent();

        if (axisConfig != null) {
            Iterator modules = getEngagedModules().iterator();

            while (modules.hasNext()) {
                QName moduleName = (QName) modules.next();
                AxisModule axisModule = axisConfig.getModule(moduleName);

                if (axisModule != null) {
                    Module moduleImpl = axisModule.getModule();
                    if (moduleImpl != null) {
                        // notyfying module for service engagement
                        moduleImpl.engageNotify(service);
                    }
                    service.engageModule(axisModule, axisConfig);
                } else {
                    throw new AxisFault(Messages.getMessage(
                            "modulenotavailble", moduleName.getLocalPart()));
                }
            }
        }

        service.setLastupdate();
        addChild(service);
    }

    public void addToengagedModules(QName moduleName) {
        engagedModules.add(moduleName);
    }

    public void engageModule(AxisModule module) throws AxisFault {
        QName moduleName = module.getName();
        for (Iterator iterator = engagedModules.iterator(); iterator.hasNext();) {
            QName modu = (QName) iterator.next();

            if (modu.getLocalPart().equals(moduleName.getLocalPart())) {
                log.debug(Messages.getMessage(
                        "modulealredyengagedtoservicegroup", moduleName.getLocalPart()));
                throw new AxisFault(Messages.getMessage(
                        "modulealredyengagedtoservicegroup", moduleName.getLocalPart()));
            }
        }

        Iterator srevice = getServices();
        while (srevice.hasNext()) {
            // engaging each service
            AxisService axisService = (AxisService) srevice.next();
            try {
                axisService.engageModule(module, (AxisConfiguration) getParent());
            } catch (AxisFault axisFault) {
                log.info(axisFault.getMessage());
            }
        }
        addToengagedModules(moduleName);
    }

    public void removeService(String name) throws AxisFault {
        AxisService service = getService(name);

        if (service != null) {
            ((AxisConfiguration) getParent()).notifyObservers(AxisEvent.SERVICE_DEPLOY, service);
        }

//        services.remove(name);
        removeChild(name);
    }

    public AxisConfiguration getAxisDescription() {
        return (AxisConfiguration) getParent();
    }

    public ArrayList getEngagedModules() {
        return engagedModules;
    }

    public ModuleConfiguration getModuleConfig(QName moduleName) {
        return (ModuleConfiguration) moduleConfigmap.get(moduleName);
    }

    public ArrayList getModuleRefs() {
        return modulesList;
    }

    public AxisService getService(String name) throws AxisFault {
//        return (AxisService) services.get(name);
        return (AxisService) getChild(name);
    }

    public ClassLoader getServiceGroupClassLoader() {
        return serviceGroupClassLoader;
    }

    public String getServiceGroupName() {
        return serviceGroupName;
    }

    public Iterator getServices() {
//        return services.values().iterator();
        return getChildren();
    }

    public void setAxisDescription(AxisConfiguration axisDescription) {
        setParent(axisDescription);
    }

    public void setServiceGroupClassLoader(ClassLoader serviceGroupClassLoader) {
        this.serviceGroupClassLoader = serviceGroupClassLoader;
    }

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    public Object getKey() {
        return getServiceGroupName();
    }
}
