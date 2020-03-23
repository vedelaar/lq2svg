/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lq;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.zip.GZIPInputStream;
import com.google.gson.*;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;

/**
 *
 * @author derk
 */
import java.io.InputStreamReader;
public class Main {

    public JFrame frame;
    public JButton openbutton;
    public JButton savebutton;
    public JLabel label;
    public File selectedfile;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main x = new Main();
        x.doit();
    }
    
    public void doit() {
        this.frame = new JFrame("jq to svg");
        this.frame.setLayout(null);
        
        this.frame.setVisible(true);
        this.frame.setSize(500,200);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.openbutton = new JButton("Open jq file");
        this.openbutton.setBounds(10, 10, 140, 30);
        this.frame.add(openbutton);

        this.label = new JLabel("NO FILE OPENED");
        this.label.setBounds(10, 40, 500, 30);
        this.frame.add(label);
        
        
        this.savebutton = new JButton("save as svg");
        this.savebutton.setBounds(10, 70, 140, 30);
        this.savebutton.setEnabled(false);
        this.frame.add(savebutton);
        
        this.openbutton.addActionListener (new Open(this)); 
        this.savebutton.addActionListener (new Save(this)); 
        
        this.frame.setVisible(true);
    }
}

class Save implements ActionListener {
    private Main main;

    public Save(Main main) {
        this.main = main;
    }
    
    protected String getHeader()
    {
        String newline = System.getProperty("line.separator");
      
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + newline +
               "<svg" + newline +
               "   xmlns=\"http://www.w3.org/2000/svg\"" + newline +
               "   width=\"1000mm\"" + newline +
               "   height=\"600mm\"" + newline +
               "   viewBox=\"0 0 1000 600\"" + newline +
               "   version=\"1.1\" >" + newline +
               "  <g>" + newline;
    }

    protected String getFooter()
    {
        String newline = System.getProperty("line.separator");
        return "  </g>" + newline +
               "</svg>" + newline;
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        String newline = System.getProperty("line.separator");
        String ret = this.getHeader();
 
        try {
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(main.selectedfile.getPath()));
            
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject)jsonParser.parse(
            new InputStreamReader(gzis, "UTF-8"));
                        
            for (JsonElement ele : jsonObject.getAsJsonObject("canvasData").getAsJsonArray("objects")) {
                JsonObject obj = ele.getAsJsonObject();
                
                ret += "    <path" + newline
                    +  "       style=\"fill:none;fill-opacity:1;stroke:#000000;stroke-width:0.1;stroke-opacity:1\"" + newline
                    +  "       d=\"";

                
                for (JsonElement ele2 : obj.getAsJsonArray("path")) {
                    for (JsonElement ele3 : ele2.getAsJsonArray()) {
                        ret += ele3.getAsString()+" ";
                    }
                }
                
                ret += "\"" + newline +
                       "       id=\"" + obj.get("id").getAsString() + "\" />" + newline;
            }
            
            ret += this.getFooter();

            String path = main.selectedfile.getPath();
            String newfilename = "";
            int i = path.lastIndexOf('.');
            if (i > 0) {
                newfilename = path.substring(0,i);
            }
            newfilename += ".svg";

            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(newfilename))) {
                writer.write(ret);
            }
            
            JOptionPane.showMessageDialog(main.frame, "Saved as " + newfilename);
        } catch(IOException ex){
            System.out.println(ex.toString());
        }
    }
}

class Open implements ActionListener {

    private Main main;
    public Open(Main main) {
        this.main = main;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                String extension = "";
                String fileName = f.getName();
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i+1);
                }
                
                if (extension.equals("lq"))
                    return true;
                return false;
            }

            @Override
            public String getDescription() {
                return "lq files";
            }
        });
        int result = fc.showOpenDialog(this.main.frame);
        if (result == JFileChooser.APPROVE_OPTION) {
          this.main.label.setText("File " + fc.getSelectedFile().getName() + " selected.");
          this.main.savebutton.setEnabled(true);
          main.selectedfile = fc.getSelectedFile();
        }
    }
    
}
