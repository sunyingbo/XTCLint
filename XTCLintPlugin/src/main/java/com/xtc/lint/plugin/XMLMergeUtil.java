package com.xtc.lint.plugin;


import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * XML 合并工具类
 * Created by chentong on 27/11/15.
 */
public class XMLMergeUtil {

    public static void merge(OutputStream outputStream, String expression,
                                 InputStream... inputStreams) throws Exception {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression compiledExpression = xpath
                .compile(expression);
        Document doc = merge(compiledExpression, inputStreams);

        print(doc, outputStream);

        for (InputStream inputStream : inputStreams) {
            IOUtils.closeQuietly(inputStream);
        }
        IOUtils.closeQuietly(outputStream);
    }

    public static Document merge(String expression,
                                  InputStream... inputStreams) throws Exception {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression compiledExpression = xpath
                .compile(expression);
        return merge(compiledExpression, inputStreams);
    }

    private static Document merge(XPathExpression expression,
                                  InputStream... inputStreams) throws Exception {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        docBuilderFactory
                .setIgnoringElementContentWhitespace(true);
        DocumentBuilder docBuilder = docBuilderFactory
                .newDocumentBuilder();
        Document base = docBuilder.parse(inputStreams[0]);

        Node results = (Node) expression.evaluate(base,
                                                  XPathConstants.NODE);
        if (results == null) {
            throw new IOException(inputStreams[0]
                                          + ": expression does not evaluate to node");
        }

        for (int i = 1; i < inputStreams.length; i++) {
            Document merge = docBuilder.parse(inputStreams[i]);
            Node nextResults = (Node) expression.evaluate(merge,
                                                          XPathConstants.NODE);
            while (nextResults.hasChildNodes()) {
                Node kid = nextResults.getFirstChild();
                nextResults.removeChild(kid);
                kid = base.importNode(kid, true);
                results.appendChild(kid);
            }
        }

        return base;
    }

    private static void print(Document doc, OutputStream targetFile) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory
                .newTransformer();
        DOMSource source = new DOMSource(doc);
        Result result = new StreamResult(targetFile);
        transformer.transform(source, result);
    }
}
