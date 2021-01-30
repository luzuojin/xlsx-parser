package dev.xframe.xlsx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

public interface FileSet {

    InputStream get(String xlRes) throws IOException;
    
    void close() throws IOException;
    
    default XMLEventReader newEventReader(String xlRes) throws Exception {
        return XMLInputFactory.newInstance().createXMLEventReader(get(xlRes));
    }

    static FileSet of(String res) {
    	File file = new File(res);
    	return file.exists() ? 
    			of(file) : 
    				of(getResourceAsStream(res));
    }
    static FileSet of(File file) {
    	try {
    		return new SysFileSet(FileSystems.newFileSystem(file.toPath(), null));
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	}
    }
    static FileSet of(InputStream input) {
    	try{
    		return new StreamFileSet(input);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	}
    }
	static InputStream getResourceAsStream(String res) {
		InputStream in = FileSet.class.getClassLoader().getResourceAsStream(res);
		if(in == null) {
			in = ClassLoader.getSystemResourceAsStream(res);
		}
		return in;
	}
	static class SysFileSet implements FileSet {
    	final FileSystem fs;
    	SysFileSet(FileSystem fs) {
    		this.fs = fs;
		}
		public InputStream get(String xlRes) throws IOException {
			return Files.newInputStream(fs.getPath(xlRes));
		}
		public void close() throws IOException {
			fs.close();
		}
    }
    
    static class StreamFileSet implements FileSet {
    	InputStream input;
    	StreamFileSet(InputStream input) throws IOException {
    		cache(input);
		}
		void cache(InputStream input) throws IOException {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			while(input.available() > 0) {
				output.write(input.read());
			}
			input.close();
			this.input = new ByteArrayInputStream(output.toByteArray());
		}
		@Override
		public InputStream get(String xlRes) throws IOException {
			input.reset();
			ZipInputStream zip = new ZipInputStream(input);
			ZipEntry entry;
			while((entry = zip.getNextEntry()) != null) {
				if(xlRes.equals(entry.getName())) {
					return zip;
				}
			}
			throw new FileNotFoundException(xlRes);
		}
		@Override
		public void close() throws IOException {
			input.close();
		}
    }
}
