package eu.drus.jpa.unit.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class PersistenceUnitDescriptorImpl implements PersistenceUnitDescriptor {

    private static final String ENTRY_PROPERTY = "property";
    private static final String ENTRY_VALUE = "value";
    private static final String ENTRY_PROPERTIES = "properties";
    private static final String ENTRY_PROVIDER = "provider";
    private static final String ENTRY_NAME = "name";
    private static final String ENTRY_CLASS = "class";
    private String unitName;
    private Map<String, Object> properties;
    private String providerClassName;
    private List<Class<?>> classList;

    public PersistenceUnitDescriptorImpl(final Element element, final Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
        classList = new ArrayList<>();
        parse(element);
    }

    private void parse(final Element persistenceUnitElement) {
        final String name = persistenceUnitElement.getAttribute(ENTRY_NAME);
        if (name != null && !name.isEmpty()) {
            unitName = name;
        }

        final NodeList children = persistenceUnitElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                parseChild(children, i);
            }
        }
    }

    private void parseChild(final NodeList children, final int i) {
        final Element element = (Element) children.item(i);
        final String tag = element.getTagName();
        if (tag.equals(ENTRY_PROVIDER)) {
            providerClassName = extractContent(element);
        } else if (tag.equals(ENTRY_PROPERTIES)) {
            parseProperties(element);
        } else if (tag.equals(ENTRY_CLASS)) {
            final String className = extractContent(element);
            try {
                classList.add(Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
            } catch (final ClassNotFoundException e) {
                throw new JpaUnitException("Could not find class: " + className, e);
            }
        }
    }

    private void parseProperties(final Element element) {
        final NodeList props = element.getChildNodes();
        for (int j = 0; j < props.getLength(); j++) {
            if (props.item(j).getNodeType() == Node.ELEMENT_NODE) {
                final Element propElement = (Element) props.item(j);
                if (!ENTRY_PROPERTY.equals(propElement.getTagName())) {
                    continue;
                }
                final String propName = propElement.getAttribute(ENTRY_NAME).trim();
                final String propValue = propElement.getAttribute(ENTRY_VALUE).trim();
                if (!properties.containsKey(propName)) {
                    properties.put(propName, propValue);
                }
            }
        }
    }

    private static String extractContent(final Element element) {
        final NodeList children = element.getChildNodes();
        final StringBuilder result = new StringBuilder("");
        for (int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                result.append(node.getNodeValue());
            }
        }
        return result.toString().trim();
    }

    @Override
    public String getUnitName() {
        return unitName;
    }

    @Override
    public String getProviderClassName() {
        return providerClassName;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public List<Class<?>> getClasses() {
        return classList;
    }
}
