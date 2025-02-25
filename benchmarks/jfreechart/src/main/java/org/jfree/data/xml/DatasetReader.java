

package org.jfree.data.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jfree.chart.internal.Args;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;


public class DatasetReader {

    
    static SAXParserFactory factory;

    
    public static SAXParserFactory getSAXParserFactory() {
    	if (factory == null) {
            SAXParserFactory f = SAXParserFactory.newInstance();
            try {
                f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                f.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory = f;
            } catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
    	}
        return factory;
    }
    
    
    public static void setSAXParserFactory(SAXParserFactory f) {
    	Args.nullNotPermitted(f, "f");
        factory = f;
    }

    
    public static PieDataset readPieDatasetFromXML(File file)
            throws IOException {
        InputStream in = new FileInputStream(file);
        return readPieDatasetFromXML(in);
    }

    
    public static PieDataset readPieDatasetFromXML(InputStream in)
             throws IOException {
        PieDataset result = null;
        try {
            SAXParser parser = getSAXParserFactory().newSAXParser();
            PieDatasetHandler handler = new PieDatasetHandler();
            parser.parse(in, handler);
            result = handler.getDataset();
        }
        catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    
    public static CategoryDataset readCategoryDatasetFromXML(File file)
            throws IOException {
        InputStream in = new FileInputStream(file);
        return readCategoryDatasetFromXML(in);
    }

    
    public static CategoryDataset readCategoryDatasetFromXML(InputStream in)
            throws IOException {
        CategoryDataset result = null;
        try {
            SAXParser parser = getSAXParserFactory().newSAXParser();
            CategoryDatasetHandler handler = new CategoryDatasetHandler();
            parser.parse(in, handler);
            result = handler.getDataset();
        }
        catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}