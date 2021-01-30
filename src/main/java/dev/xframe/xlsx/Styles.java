package dev.xframe.xlsx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class Styles {
    
    private static final String XL_FILE = "xl/styles.xml";
    
    private Map<String, String> numberFormats = new HashMap<String, String>();  //map of number formats
    private List<String> cellNumberFormats = new ArrayList<String>();           //list of cell number formats
    
    private String attrValue(Attribute attr) {
        return attr == null ? "null" : attr.getValue();
    }

    Styles readFrom(FileSet fs) throws Exception {
        XMLEventReader reader = fs.newEventReader(XL_FILE);
        boolean cellXfsFound = false;
        while (reader.hasNext()) {
            XMLEvent event = (XMLEvent) reader.next();
            if (event.isStartElement()) {
                StartElement startElement = (StartElement) event;
                if (startElement.getName().getLocalPart().equalsIgnoreCase("numFmt")) {
                    //start element of number format
                    Attribute numFmtId = startElement.getAttributeByName(new QName("numFmtId"));
                    Attribute formatCode = startElement.getAttributeByName(new QName("formatCode"));
                    numberFormats.put(numFmtId.getValue(), attrValue(formatCode));
                } else if (startElement.getName().getLocalPart().equalsIgnoreCase("cellXfs")) {
                    //start element of cell format setting
                    cellXfsFound = true;
                } else if (startElement.getName().getLocalPart().equalsIgnoreCase("xf") && cellXfsFound) {
                    //start element of format setting in cell format setting
                    Attribute numFmtId = startElement.getAttributeByName(new QName("numFmtId"));
                    cellNumberFormats.add(attrValue(numFmtId));
                }
            } else if (event.isEndElement()) {
                EndElement endElement = (EndElement) event;
                if (endElement.getName().getLocalPart().equalsIgnoreCase("cellXfs")) {
                    //end element of cell format setting
                    cellXfsFound = false;
                }
            }
        }
        reader.close();
        return this;
    }
    
}
