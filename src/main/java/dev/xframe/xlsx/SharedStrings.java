package dev.xframe.xlsx;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class SharedStrings {
    
    private static final String XL_XML = "xl/sharedStrings.xml";

    private List<String> sharedStrings = new ArrayList<>();
    
    public String get(int index) {
        return sharedStrings.get(index);
    }
    
    SharedStrings readFrom(FileSet fs) throws Exception {
        XMLEventReader reader = fs.newEventReader(XL_XML);
        boolean flagFound = false;
        StringBuilder sharedValue = null;
        while (reader.hasNext()) {
            XMLEvent event = (XMLEvent) reader.next();
            if (event.isStartElement()) {
                StartElement startElement = (StartElement) event;
                if (startElement.getName().getLocalPart().equalsIgnoreCase("si")) {
                    //start element of shared string item
                    flagFound = true;
                    sharedValue = new StringBuilder();
                }
            } else if (event.isCharacters() && flagFound) {
                //chars of the shared string item
                sharedValue.append(event.asCharacters().getData());
            } else if (event.isEndElement()) {
                EndElement endElement = (EndElement) event;
                if (endElement.getName().getLocalPart().equalsIgnoreCase("si")) {
                    //end element of shared string item
                    flagFound = false;
                    sharedStrings.add(sharedValue.toString());
                }
            }
        }
        reader.close();
        return this;
    }
    
}
