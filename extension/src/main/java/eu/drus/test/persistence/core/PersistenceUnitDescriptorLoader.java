package eu.drus.test.persistence.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.drus.test.persistence.JpaTestException;

public class PersistenceUnitDescriptorLoader {

    private DocumentBuilderFactory documentBuilderFactory;

    public List<PersistenceUnitDescriptor> loadPersistenceUnitDescriptors(final Map<String, Object> properties) {
        Enumeration<URL> resources = null;
        try {
            resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
        } catch (final IOException e) {
            throw new JpaTestException("Unexpected exception while looking for [/META-INF/persistence.xml]", e);
        }

        final List<PersistenceUnitDescriptor> units = new ArrayList<>();
        while (resources.hasMoreElements()) {
            final Document doc = loadDocument(resources.nextElement());
            final Element top = doc.getDocumentElement();

            final NodeList children = top.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    final Element element = (Element) children.item(i);
                    final String tag = element.getTagName();
                    if (tag.equals("persistence-unit")) {
                        units.add(new PersistenceUnitDescriptor(element, properties));
                    }
                }
            }
        }

        return units;

    }

    private Document loadDocument(final URL url) {
        final String resourceName = url.toExternalForm();
        try {
            final URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            try (InputStream in = conn.getInputStream()) {
                final InputSource is = new InputSource(in);
                try {
                    final DocumentBuilder documentBuilder = getDocumentBuilderFactory().newDocumentBuilder();
                    try {
                        return documentBuilder.parse(is);
                    } catch (final SAXException | IOException e) {
                        throw new JpaTestException("Unexpected error parsing [" + resourceName + "]", e);
                    }
                } catch (final ParserConfigurationException e) {
                    throw new JpaTestException("Unable to generate javax.xml.parsers.DocumentBuilder instance", e);
                }
            }
        } catch (final IOException e) {
            throw new JpaTestException("Unable to access [" + resourceName + "]", e);
        }
    }

    private DocumentBuilderFactory getDocumentBuilderFactory() {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
        }
        return documentBuilderFactory;
    }
}
