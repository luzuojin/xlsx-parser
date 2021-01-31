package dev.xframe.xlsx;

import java.util.Arrays;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 <row spans='1:20' r='1'>
  <c r='A1'>
  	<v></v>
  </c>
  <c r='B1'>
    <v></v>
  </c>
 <row>
 */
public class Row implements Iterable<Cell> {
	
	private int rowNum = -1;
	
	private int[] spans;
	
	private Cell[] cells;
    
    public int getRowNum() {
		return rowNum;
	}
    public int columns() {
    	return spans[1];
    }
    public Cell get(int index) {
    	return cells[index];
    }
    /**[A,ZZZ]*/
    public Cell getCell(String columnStr) {
    	return cells[Cell.columnStrToNum(columnStr)-1];
    }
    /**[1,)*/
    public Cell getCell(int columnNum) {
    	return cells[columnNum-1];
    }
    
	private void addCell(Cell cell) {
		cells[cell.getColumnNum() - 1] = cell;
	}
	private void fillBlanks() {
    	for (int i = 0; i < cells.length; i++) {
			if(cells[i] == null) {
				cells[i] = Cell.blank(rowNum, i+1);
			}
		}
	}
    Row readFrom(XMLEventReader reader, Workbook workbook) throws XMLStreamException {
        do {
            XMLEvent event = reader.peek();
            if(event.isEndElement()) {
                reader.next();
                fillBlanks();
                break;//结束row </['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::row>
            }
            if(this.rowNum != -1) {//{...Cell}
            	addCell(new Cell().readFrom(reader, workbook));
                continue;
            }
            if (event.isStartElement()) {//开始row <['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::row spans='1:20' r='2'>
                StartElement startElement = event.asStartElement();
                if (startElement.getName().getLocalPart().equalsIgnoreCase("row")) {
                    this.rowNum = Integer.parseInt(startElement.getAttributeByName(new QName("r")).getValue());
                    this.spans = splitSpans(startElement.getAttributeByName(new QName("spans")).getValue());
                    this.cells = new Cell[spans[1]];
                }
            }
            reader.next();
        } while (reader.hasNext());
        return this;
    }
	private int[] splitSpans(String value) {
		int[] r = new int[2];
		String[] s = value.split(":");
		r[0] = Integer.parseInt(s[0]);
		r[1] = Integer.parseInt(s[1]);
		return r;
	}
	public Iterator<Cell> iterator() {
		return Arrays.asList(cells).iterator();
	}
	@Override
	public String toString() {
		return Arrays.toString(cells);
	}
}
