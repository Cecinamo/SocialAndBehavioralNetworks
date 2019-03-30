
package Indices;

import java.io.FileWriter;
import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cecin
 */
public class Writer {
    final private FileWriter writer;
    public Writer(String fileName) throws IOException {

        writer = new FileWriter(fileName, false);
    }
    public void add(String line) throws IOException {
        writer.write(line);
    }
    
    public void close() throws IOException {
        writer.close();
    }
    
}
