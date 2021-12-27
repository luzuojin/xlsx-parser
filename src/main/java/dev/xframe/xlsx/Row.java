package dev.xframe.xlsx;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

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
    private void fillData(List<Cell> tCells, int min, int max) {
        spans = new int[] {Math.min(min, max), max};
        cells = new Cell[max];
        tCells.forEach(this::addCell);
        IntStream.range(0, max) //补全blank
                .filter(idx -> cells[idx] == null)
                .forEach(idx -> cells[idx] = Cell.blank(rowNum, idx + 1));
    }

    Row readFrom(XMLEventReader reader, Workbook workbook) throws XMLStreamException {
        List<Cell> tCells = new ArrayList<>();
        int min = Integer.MAX_VALUE;
        int max = 0;
        do {
            XMLEvent event = reader.peek();
            if(event.isEndElement()) {
                reader.next();
                fillData(tCells, min, max);
                break;//结束row </['http://schemas.openxmlformats.org/spreadsheetml/2006/main']::row>
            }
            if(this.rowNum != -1) {//{...Cell}
            	Cell cell = new Cell().readFrom(reader, workbook);
            	min = Math.min(cell.getColumnNum(), min);
            	max = Math.max(cell.getColumnNum(), max);
                tCells.add(cell);
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

    public Iterator<Cell> iterator() {
		return Arrays.asList(cells).iterator();
	}
	@Override
	public String toString() {
		return Arrays.toString(cells);
	}
}
