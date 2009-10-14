/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Dmitry Abramov
 * Created: 22.12.2008 18:20:37
 * $Id$
 */
package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.bali.util.ReflectionHelper;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.gui.xml.layout.LayoutLoaderConfig;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.dom4j.Element;

import java.util.List;

public class AbstractFieldLoader extends AbstractDatasourceComponentLoader {
    protected LayoutLoaderConfig config;
    protected ComponentsFactory factory;

    private Log log = LogFactory.getLog(AbstractFieldLoader.class);

    public AbstractFieldLoader(Context context, LayoutLoaderConfig config, ComponentsFactory factory) {
        super(context);
        this.config = config;
        this.factory = factory;
    }

    public Component loadComponent(ComponentsFactory factory, Element element, Component parent) throws InstantiationException, IllegalAccessException {
        final Field component = factory.createComponent(element.getName());

        assignXmlDescriptor(component, element);
        loadId(component, element);
        loadDatasource(component, element);

        loadVisible(component, element);
        loadEditable(component, element);

        loadStyleName(component, element);

        loadCaption(component, element);

        loadValidators(component, element);
        loadRequired(component, element);

        loadHeight(component, element);
        loadWidth(component, element);

        loadExpandable(component, element);

        addAssignWindowTask(component);

        return component;
    }

    protected void loadRequired(Field component, Element element) {
        final String required = element.attributeValue("required");
        if (!StringUtils.isEmpty(required)) {
            component.setRequired(BooleanUtils.toBoolean(required));
            String msg = element.attributeValue("requiredMessage");
            if (msg != null)
                component.setRequiredMessage(loadResourceString(msg));
        }
    }

    protected void loadValidators(Field component, Element element) {
        @SuppressWarnings({"unchecked"})
        final List<Element> validatorElements = element.elements("validator");

        for (Element validatorElement : validatorElements) {
            final String className = validatorElement.attributeValue("class");
            final Class<Field.Validator> aClass = ReflectionHelper.getClass(className);

            Field.Validator validator = null;

            if (!StringUtils.isBlank(getMessagesPack()))
                try {
                    validator = ReflectionHelper.newInstance(aClass, validatorElement, getMessagesPack());
                } catch (NoSuchMethodException e) {
                    //
                }
            if (validator == null) {
                try {
                    validator = ReflectionHelper.newInstance(aClass, validatorElement);
                } catch (NoSuchMethodException e) {
                    try {
                        validator = ReflectionHelper.newInstance(aClass);
                    } catch (NoSuchMethodException e1) {
                        //
                    }
                }
            }
            if (validator == null) {
                log.warn("Validator class " + aClass + " has no supported constructors");
                return;
            }

            component.addValidator(validator);
        }
    }

}