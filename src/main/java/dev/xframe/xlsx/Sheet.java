package dev.xframe.xlsx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

public class Sheet implements Iterable<Row> {
    
    private static final String XL_FILE = "xl/worksheets/sheet%d.xml";
    
    private List<Row> rows = new ArrayList<>();

    public int rows() {
        return rows.size();
    }
    
    public List<Row> getRows() {
        return rows;
    }
    
    public Iterator<Row> iterator() {
        return rows.iterator();
    }

    Sheet readFrom(FileSet fs, Workbook workbook, int sheetIndex) throws Exception {
        XMLEventReader reader = fs.newEventReader(String.format(XL_FILE, sheetIndex));
        boolean isSheedStarted = false;
        while (reader.hasNext()) {
            XMLEvent event = reader.peek();
            if(isSheedStarted && event.isEndElement()) {  //sheet end
                reader.next();
                break;
            }
            if(isSheedStarted) {
                rows.add(new Row().readFrom(reader, workbook));
                continue;
            }
            //sheet start
            if(event.isStartElement() && event.asStartElement().getName().getLocalPart().equalsIgnoreCase("sheetData")) {
                isSheedStarted = true;
            }
            reader.next();
        }
        reader.close();
        return this;
    }

}
