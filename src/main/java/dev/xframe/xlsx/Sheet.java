package dev.xframe.xlsx;

import java.util.ArrayList;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

@SuppressWarnings("serial")
public class Sheet extends ArrayList<Row> {
    
	private static final String XL_FILE = "xl/worksheets/sheet%d.xml";
    
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
                add(new Row().readFrom(reader, workbook));
                continue;
            }
            //sheet start
            if(event.isStartElement() && event.asStartElement().getName().getLocalPart().equalsIgnoreCase("sheetData")) {
                isSheedStarted = true;
            }
            reader.next();//skip not [sheetData] element
        }
        reader.close();
        return this;
    }

}
