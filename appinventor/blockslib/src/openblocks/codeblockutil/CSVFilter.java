package openblocks.codeblockutil;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class CSVFilter extends FileFilter {
    public boolean accept(File file) {
    	if(file != null){
    		String filename = file.getName();
    		if (filename != null){
    			return filename.endsWith(".csv");
    		}
    	}
        return false;
    }
    public String getDescription() {
        return "*.csv";
    }
}
