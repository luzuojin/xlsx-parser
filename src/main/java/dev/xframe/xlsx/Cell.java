package dev.xframe.xlsx;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
r:id
s:style
t:type ['s':sharedString, v:sharedStrings.index)

<['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::c r='A1' s='1' t='s'>
<['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::v>
{data}
</['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::v>
</['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::c>
 */
public class Cell {
    
    private String cellNum;
    private String style;
    private String value;
    private String type;
    
    private boolean isShared;

    public String getCellNum() {
		return cellNum;
	}
	public String getType() {
    	return type;
    }
    public String getStyle() {
        return style;
    }
    public String getValue() {
        return value;
    }
    @Override
    public String toString() {
        return value;
    }

    public Cell readFrom(XMLEventReader reader, Workbook workbook) throws XMLStreamException {
        do {
            XMLEvent event = reader.peek();
            if(event.isEndElement()) {
                reader.next();
                break;//结束Cell </['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::c>
            }
            if(this.cellNum != null) {//{value}
                String val = readValue(reader);
                this.value = isShared ? workbook.ss.get(Integer.parseInt(val)) : val;
                continue;
            }
            if (event.isStartElement()) {//开始cell <['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::c r='A1' s='1' t='s'>
                StartElement startElement = (StartElement) event;
                if (startElement.getName().getLocalPart().equalsIgnoreCase("c")) {
                    this.cellNum = attrValue(startElement.getAttributeByName(new QName("r")));
                    this.style = attrValue(startElement.getAttributeByName(new QName("s")));
                    this.type = attrValue(startElement.getAttributeByName(new QName("t")));
                    this.isShared = "s".equalsIgnoreCase(type);
                }
            }
            reader.next();
        } while (reader.hasNext());
        return this;
    }

    private String readValue(XMLEventReader reader) {
        StringBuilder value = new StringBuilder();
        do {
            XMLEvent event = (XMLEvent) reader.next();
            if(event.isEndElement()) {
                break;//结束value </['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::v>
            }
            if(event.isCharacters()) {//{data}
                value.append(event.asCharacters().getData());
            }
            if (event.isStartElement()) {//开始value <['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::v>
                StartElement startElement = (StartElement) event;
                if (startElement.getName().getLocalPart().equalsIgnoreCase("v")//value
                || startElement.getName().getLocalPart().equalsIgnoreCase("is"))//inline string
                {
                  //do anything?
                }
            }
        } while (reader.hasNext());
        return value.toString();
    }

    private String attrValue(Attribute attr) {
        return attr == null ? null : attr.getValue();
    }

}
