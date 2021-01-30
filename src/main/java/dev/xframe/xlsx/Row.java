package dev.xframe.xlsx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class Row implements Iterable<Cell> {
    
    private int rowNum = -1;
    
    private List<Cell> cells = new ArrayList<>();

    public int getRowNum() {
		return rowNum;
	}
	public List<Cell> getCells() {
		return cells;
	}

    public Row readFrom(XMLEventReader reader, Workbook workbook) throws XMLStreamException {
        do {
            XMLEvent event = reader.peek();
            if(event.isEndElement()) {
                reader.next();
                break;//结束row </['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::row>
            }
            if(this.rowNum != -1) {//{...Cell}
            	cells.add(new Cell().readFrom(reader, workbook));
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
    
    @Override
    public Iterator<Cell> iterator() {
        return cells.iterator();
    }
    
    @Override
    public String toString() {
        return cells.toString();
    }

}
