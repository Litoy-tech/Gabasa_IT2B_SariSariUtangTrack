package com.scc.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CsvExporter {

    private String pathway;

    public CsvExporter(String pathway){
        this.pathway = pathway;
    }

    public void writeline(List<String[]> rows) throws IOException{
        File file = new File(pathway);
        File folder = file.getParentFile();
        if(folder != null) folder.mkdirs();

        try(PrintWriter out = new PrintWriter(new FileWriter(file))){
            for(String[] row : rows){
                String[] safe = new String[row.length];
                for(int i = 0; i < row.length; i++){
                    safe[i] = escape(row[i]);
                }
                out.println(String.join(",", safe));
            }
        }
    }

    private String escape(String value){
        if(value == null) return "";
        if(value.contains(",") || value.contains("\"") || value.contains("\n")){
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
