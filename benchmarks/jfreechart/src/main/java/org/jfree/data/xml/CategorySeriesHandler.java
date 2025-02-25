

package org.jfree.data.xml;

import org.jfree.data.DefaultKeyedValues;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class CategorySeriesHandler extends DefaultHandler
        implements DatasetTags {

    
    private RootHandler root;

    
    private Comparable seriesKey;

    
    private DefaultKeyedValues values;

    
    public CategorySeriesHandler(RootHandler root) {
        this.root = root;
        this.values = new DefaultKeyedValues();
    }

    
    public void setSeriesKey(Comparable key) {
        this.seriesKey = key;
    }

    
    public void addItem(Comparable key, Number value) {
        this.values.addValue(key, value);
    }

    
    @Override
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) throws SAXException {

        if (qName.equals(SERIES_TAG)) {
            setSeriesKey(atts.getValue("name"));
            ItemHandler subhandler = new ItemHandler(this.root, this);
            this.root.pushSubHandler(subhandler);
        }
        else if (qName.equals(ITEM_TAG)) {
            ItemHandler subhandler = new ItemHandler(this.root, this);
            this.root.pushSubHandler(subhandler);
            subhandler.startElement(namespaceURI, localName, qName, atts);
        }

        else {
            throw new SAXException(
                "Expecting <Series> or <Item> tag...found " + qName
            );
        }
    }

    
    @Override
    public void endElement(String namespaceURI,
                           String localName,
                           String qName) {

        if (this.root instanceof CategoryDatasetHandler) {
            CategoryDatasetHandler handler = (CategoryDatasetHandler) this.root;

            for (Object o : this.values.getKeys()) {
                Comparable key = (Comparable) o;
                Number value = this.values.getValue(key);
                handler.addItem(this.seriesKey, key, value);
            }

            this.root.popSubHandler();
        }

    }

}
