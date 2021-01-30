package dev.xframe.xlsx;

import java.io.File;
import java.io.InputStream;

//worksheets
public class Workbook {

    FileSet fs;
    SharedStrings ss;
    Styles styles;
    
    Workbook readFrom(FileSet fileSet) {
        try {
            fs = fileSet;
            ss = new SharedStrings().readFrom(fs);
            styles = new Styles().readFrom(fs);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Sheet getSheet(int index) {
        try {
            return new Sheet().readFrom(fs, this, index);
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
