package me.tomassetti.antlrplus.xml;

import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;
import me.tomassetti.antlrplus.metamodel.mapping.AntlrReflectionMapper;
import me.tomassetti.antlrplus.model.Element;
import me.tomassetti.antlrplus.model.OrderedElement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class XmlExporter {

    public enum PropertySetting {
        AS_NODE,
        AS_NODE_WITH_CDATA,
        AS_ATTRIBUTE
    }

    public static final String ROOT_ROLE = "root";

    private boolean useCDataByDefault = true;
    private boolean avoidRedundantType = false;

    public void setAvoidRedundantType(boolean avoidRedundantType) {
        this.avoidRedundantType = avoidRedundantType;
    }

    public void doNotPrintPositions() {
        addPropertyToNotPrint(AntlrReflectionMapper.START_LINE.getName());
        addPropertyToNotPrint(AntlrReflectionMapper.START_COLUMN.getName());
        addPropertyToNotPrint(AntlrReflectionMapper.END_LINE.getName());
        addPropertyToNotPrint(AntlrReflectionMapper.END_COLUMN.getName());
    }

    public void setUseCDataByDefault(boolean useCDataByDefault) {
        this.useCDataByDefault = useCDataByDefault;
    }

    private Map<Property, PropertySetting> propertySettings = new HashMap<>();

    private boolean printProperties = true;

    private Set<String> propertiesToNotPrint = new HashSet<>();

    public void addPropertyToNotPrint(String propertyName) {
        this.propertiesToNotPrint.add(propertyName);
    }

    public void setPrintProperties(boolean printProperties) {
        this.printProperties = printProperties;
    }

    public Node toXml(OrderedElement astNode, String role) {
        return toXml(astNode, createDocument(), role);
    }

    public void setPropertySetting(Property property, PropertySetting setting) {
        if (!property.isSingle() && setting==PropertySetting.AS_ATTRIBUTE) {
            throw new IllegalArgumentException("Single property cannot be configured to be printed as attributes");
        }
        propertySettings.put(property, setting);
    }

    private PropertySetting getPropertySetting(Property property) {
        if (propertySettings.containsKey(property)) {
            return propertySettings.get(property);
        } else {
            if (useCDataByDefault) {
                return PropertySetting.AS_NODE_WITH_CDATA;
            } else {
                return PropertySetting.AS_NODE;
            }
        }
    }

    private String propertyValueToString(Object propertyValue) {
        if (propertyValue instanceof ParseTree) {
            return ((ParseTree)propertyValue).getText();
        } else {
            return propertyValue.toString();
        }
    }

    private Node propertyValueNode(Object value, Document document, String role, boolean useCdata) {
        org.w3c.dom.Element node = document.createElement(role);
        if (useCdata) {
            node.appendChild(document.createCDATASection(propertyValueToString(value)));
        } else {
            node.appendChild(document.createTextNode(propertyValueToString(value)));
        }
        return node;
    }

    private Object getPropertyValue(OrderedElement astNode, OrderedElement.ValueReference valueReference) {
        Property property = valueReference.getFeature().asProperty();
        if (property.isSingle()) {
            return astNode.getSingleProperty(property).get();
        } else {
            return astNode.getMultipleProperty(property).get(valueReference.getIndex());
        }
    }

    private OrderedElement getRelationValue(OrderedElement astNode, OrderedElement.ValueReference valueReference) {
        Relation relation = valueReference.getFeature().asRelation();
        if (relation.isSingle()) {
            return (OrderedElement) astNode.getSingleRelation(relation).get();
        } else {
            return (OrderedElement) astNode.getMultipleRelation(relation).get(valueReference.getIndex());
        }
    }

    private Node toXml(OrderedElement astNode, Document document, String role) {
        org.w3c.dom.Element node = document.createElement(role);
        if (!avoidRedundantType || !role.toLowerCase().equals(astNode.type().getName().toLowerCase())) {
            node.setAttribute("type", astNode.type().getName());
        }
        astNode.getValuesOrder().forEach(valueReference -> {
            if (valueReference.getFeature().isProperty()) {
                if (printProperties && !propertiesToNotPrint.contains(valueReference.getFeature().getName())) {
                    PropertySetting ps = getPropertySetting(valueReference.getFeature().asProperty());
                    switch (ps) {
                        case AS_NODE:
                        case AS_NODE_WITH_CDATA:
                            node.appendChild(propertyValueNode(getPropertyValue(astNode, valueReference),
                                    document, valueReference.getFeature().getName(), ps == PropertySetting.AS_NODE_WITH_CDATA));
                            break;
                        case AS_ATTRIBUTE:
                            node.setAttribute(valueReference.getFeature().getName(),
                                    propertyValueToString(getPropertyValue(astNode, valueReference)));
                            break;
                        default:
                            throw new RuntimeException("Unknown value: " + ps);
                    }
                }
            } else {
                node.appendChild(toXml(getRelationValue(astNode, valueReference), document, valueReference.getFeature().getName()));
            }
        });
        return node;
    }

    public String toXmlString(OrderedElement astNode, String role) {
        Document document = createDocument();
        return serialize(toXml(astNode, document, role), document);
    }

    private String serialize(Node node, Document document) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            StringWriter writer = new StringWriter();
            document.appendChild(node);
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private Document createDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            return document;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void toXmlFile(OrderedElement astNode, File file, String role) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(toXmlString(astNode, role));
        }
    }

    public void toXmlFile(OrderedElement astNode, File file) throws FileNotFoundException {
        toXmlFile(astNode, file, ROOT_ROLE);
    }

}
