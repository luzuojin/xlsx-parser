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

<c r='A1' s='1' t='s'>
  <v>
    {data}
  </v>
  or
  <is>
  	{data}
  </is>
</c>
 */
public class Cell {
	
    private int rowNum;
    private int columnNum;		//数字columnNum,从1开始(A->1)
    private String columnStr;	//字符'AA'
    private String cellNum;
    private String type;
    private String style;
    private String value;
    
    private boolean isShared;
    
    static Cell blank(int rowNum, int columnNum) {
    	Cell cell = new Cell();
    	cell.rowNum = rowNum;
    	cell.columnNum = columnNum;
    	cell.columnStr = columnNumToStr(columnNum);
		return cell;
	}
    static String columnNumToStr(int columnNum) {
    	StringBuilder cs = new StringBuilder();
    	do {
    		char c = (char) ('A' + (columnNum % 26 - 1));
			cs.append(Character.toString(c));
    		columnNum = columnNum / 26;
    	} while(columnNum > 0);
    	return cs.reverse().toString();
    }
    static int columnStrToNum(String columnStr) {
    	int columnNum = 0;
    	for (int i = 0; i < columnStr.length(); i++) {
			char t = columnStr.charAt(i);
			columnNum = columnNum * 26 + ((t - 'A') + 1);
		}
		return columnNum;
	}
    private void setCellNum(String cellNum) {
    	this.cellNum = cellNum;
    	int rowNum = 0;
    	int columnNum = 0;
    	String columnStr = "";
    	for (int i = 0; i < cellNum.length(); i++) {
			char t = cellNum.charAt(i);
			if(t < 'A') {//数字
				rowNum = rowNum * 10 + (t - '0');
			} else {
				columnStr += Character.toString(t);
				columnNum = columnNum * 26 + ((t - 'A') + 1);
			}
		}
    	this.rowNum = rowNum;
    	this.columnNum = columnNum;
    	this.columnStr = columnStr;
	}
    public int getRowNum() {
		return rowNum;
	}
    public int getColumnNum() {
    	return columnNum;
    }
	public String getColumnStr() {
		return columnStr;
	}
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

    Cell readFrom(XMLEventReader reader, Workbook workbook) throws XMLStreamException {
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
                    this.setCellNum(attrValue(startElement.getAttributeByName(new QName("r"))));
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
