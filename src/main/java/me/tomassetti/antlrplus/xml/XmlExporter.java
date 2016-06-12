package me.tomassetti.antlrplus.xml;

import me.tomassetti.antlrplus.model.Element;
import org.antlr.v4.runtime.ParserRuleContext;
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
import java.util.List;
import java.util.Optional;

public class XmlExporter {

    public static final String ROOT_ROLE = "root";

    private boolean printProperties = true;

    public void setPrintProperties(boolean printProperties) {
        this.printProperties = printProperties;
    }

    public Node toXml(Element astNode, String role) {
        return toXml(astNode, createDocument(), role);
    }

    private Node propertyValueNode(Object value, Document document, String role) {
        org.w3c.dom.Element node = document.createElement(role);
        node.appendChild(document.createCDATASection(value.toString()));
        return node;
    }

    private Node toXml(Element astNode, Document document, String role) {
        org.w3c.dom.Element node = document.createElement(role);
        node.setAttribute("type", astNode.type().getName());
        if (printProperties) {
            astNode.type().getProperties().forEach(p -> {
                if (p.isSingle()) {
                    Optional<Object> value = astNode.getSingleProperty(p);
                    if (value.isPresent()) {
                        node.appendChild(propertyValueNode(value.get(), document, p.getName()));
                    }
                } else {
                    for (Object value : astNode.getMultipleProperty(p)) {
                        node.appendChild(propertyValueNode(value, document, p.getName()));
                    }
                }
            });
        }
        astNode.type().getRelations().forEach(r -> {
            if (r.isSingle()) {
                Optional<Element> value = astNode.getSingleRelation(r);
                if (value.isPresent()) {
                    node.appendChild(toXml(value.get(), document, r.getName()));
                }
            } else {
                List<Element> values = astNode.getMultipleRelation(r);
                values.forEach(c -> node.appendChild(toXml(c, document, r.getName())));
            }
        });
        return node;
    }

    public String toXmlString(Element astNode, String role) {
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

    public void toXmlFile(Element astNode, File file, String role) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(toXmlString(astNode, role));
        }
    }

    public void toXmlFile(Element astNode, File file) throws FileNotFoundException {
        toXmlFile(astNode, file, ROOT_ROLE);
    }

}
