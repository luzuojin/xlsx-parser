package dev.xframe.xlsx;

import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 <['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::row spans='1:20' r='2'>
 {...cell}
 </['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::row>
 */
@SuppressWarnings("serial")
public class Row extends ArrayList<Cell> {
	
	private int rowNum = -1;
    
    public int getRowNum() {
		return rowNum;
	}

    Row readFrom(XMLEventReader reader, Workbook workbook) throws XMLStreamException {
        do {
            XMLEvent event = reader.peek();
            if(event.isEndElement()) {
                reader.next();
                break;//结束row </['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::row>
            }
            if(this.rowNum != -1) {//{...Cell}
            	add(new Cell().readFrom(reader, workbook));
                continue;
            }
            if (event.isStartElement()) {//开始row <['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::row spans='1:20' r='2'>
                StartElement startElement = event.asStartElement();
                if (startElement.getName().getLocalPart().equalsIgnoreCase("row")) {
                    this.rowNum = Integer.parseInt(startElement.getAttributeByName(new QName("r")).getValue());
                }
            }
            reader.next();
        } while (reader.hasNext());
        return this;
    }
    
}
