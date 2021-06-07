package dev.xframe.xlsx;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

//worksheets
public class Workbook {
    
    private static final String XL_FILE = "xl/workbook.xml";

    FileSet fs;
    SharedStrings ss;
    Styles styles;
    List<SheetRef> sheetRefs;
    
    Workbook readFrom(FileSet fileSet) {
        try {
            fs = fileSet;
            ss = new SharedStrings().readFrom(fs);
            styles = new Styles().readFrom(fs);
            sheetRefs = readWorkbook(fs);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private List<SheetRef> readWorkbook(FileSet fs) throws Exception {
        List<SheetRef> sheetRefs = new ArrayList<>();
        XMLEventReader reader = fs.newEventReader(XL_FILE);
        boolean isWorkbookStarted = false;
        while (reader.hasNext()) {
            XMLEvent event = reader.peek();
            if(event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase("sheets")) {  //sheet end
                break;
            }
            if(isWorkbookStarted && event.isStartElement()) {
                StartElement se = event.asStartElement();
                String rid = se.getAttributeByName(new QName(se.getNamespaceURI("r"), "id")).getValue();//r:id="rId1"
                if (rid != null && rid.length() >= 4) {
                    int index = Integer.parseInt(rid.substring(3));
                    String name = se.getAttributeByName(new QName("name")).getValue();
                    sheetRefs.add(new SheetRef(index, name));
                }
                reader.next();
                continue;
            }
            //sheet start
            if(event.isStartElement() && event.asStartElement().getName().getLocalPart().equalsIgnoreCase("sheets")) {
                isWorkbookStarted = true;
            }
            reader.next();//skip not [sheetData] element
        }
        return sheetRefs;
    }
    
    public List<SheetRef> sheetRefs() {
        return sheetRefs;
    }
    
    public Sheet getSheet(int index) {
        return getSheet(sheetRefs.stream().filter(ref->ref.index==index).findAny().get());
    }
    public Sheet getSheet(SheetRef ref) {
        try {
            return new Sheet(ref).readFrom(fs, this, ref.index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void close() {
    	try {
    		fs.close();
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    public static Workbook getWorkbook(String xlsx) {
    	return getWorkbook(FileSet.of(xlsx));
    }
    public static Workbook getWorkbook(File xlsx) {
    	return getWorkbook(FileSet.of(xlsx));
    }
    public static Workbook getWorkbook(InputStream input) {
    	return getWorkbook(FileSet.of(input));
    }
    public static Workbook getWorkbook(FileSet fs) {
		return new Workbook().readFrom(fs);
	}

}
