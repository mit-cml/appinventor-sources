// @author xcitizen.team@gmail.com (José Mª Martín)

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;

import java.io.File; 
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import java.io.BufferedReader;
import android.widget.EditText;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.annotations.SimpleProperty;
import java.util.zip.*;
import java.io.*;

import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.app.Activity;

@DesignerComponent(version = YaVersion.NOTIFIER_COMPONENT_VERSION,
    category = ComponentCategory.MISC,
    description = "I/O Files, Save, Read, Delete, Zip, Unzip ...",
    nonVisible = true,
    iconName = "images/files.png")
@SimpleObject
public final class Files extends AndroidNonvisibleComponent implements Component {
  private final Activity activity;
  private final Handler handler;
  private final TextView view;
  private static final int BUFFER_SIZE = 1024;
  private static final int BUFFER = 2048; 

/**
   * Creates a new Files component.
   *
   * @param container the enclosing component
   */
  public Files (ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    handler = new Handler();
    view = new TextView(container.$context()); // Añadido
  }


  

  
  /**
   * SaveSD
   * Guadar un texto en un fichero en la Tarjeta SD
   * creado por xcitizen.team@gmail.com
   */
 
  @SimpleFunction(description = "Guarda un fichero en la tarjeta SD en la ruta que le indiquemos")
  public void SaveSD(String Filename, String Filetext, String Directory,  final String TextMessage, boolean LaunchMessage) {
  	try {
			File dir = new File("/mnt/sdcard/"+Directory);
                        dir.mkdir();	
                        //File SD = Environment.getExternalStorageDirectory();
			File file = new File(dir.getAbsolutePath(), Filename);
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
			osw.write(Filetext);
			osw.flush();
			osw.close();
                        if (LaunchMessage) {
                        handler.post(new Runnable() {
                        public void run() {
                        Toast.makeText(activity, TextMessage, Toast.LENGTH_LONG).show();
                           }
                        });
			}
		} catch (IOException ioe) {
		}
	}


   /**
    * ReadFileSD
    * Recibe los datos de un fichero alojado en la tarjeta SD y los envia a la etiqueta ReadSD
    * creado por xcitizen.team@gmail.com
    */

    @SimpleFunction(description = "Lee un fichero en la SD")
    public void ReadFileSD(String Filename, String Directory) {
        //File tarjeta = Environment.getExternalStorageDirectory();
        try {
            File dir = new File("/mnt/sdcard/"+Directory);
            File file = new File(dir.getAbsolutePath(), Filename);
            FileInputStream fIn = new FileInputStream(file);
            InputStreamReader archivo = new InputStreamReader(fIn);
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String todo = "";
            while (linea != null) {
                todo = todo + linea + "";
                linea = br.readLine();
            }
            br.close();
            archivo.close();
            view.setText(todo);
    
 
            } catch (IOException ioe) { 
            }
            }
   
    /*
    * FileReadSD
    * Etiqueta donde se guardan los datos recibidos de ReadFileSD
    * creado por xcitizen.team@gmail.com
    */

    @SimpleProperty(description = "Etiqueta que contiene los datos leidos por ReadFileSD")
     public String FileReadSD() {
     return TextViewUtil.getText(view);
     }

    @SimpleProperty(description = "Para vaciar el contenido o añadir un contenido" + "independiente del que saque ReadFileSD")
     public void FileReadSD(String text) {
     TextViewUtil.setText(view, text);
     }
    
    /**
     * CreateDir
     * Funcion para crear directorios en la tarjeta SD
     * creado por xcitizen.team@gmail.com
     */
   
     @SimpleFunction(description = "Crea un directorio")
     public void CreateDir(String Directory) {
			File dir = new File("/mnt/sdcard/"+Directory);
                        dir.mkdir();	
                        }

  
     /**
      * DeleteDir & Subdirs & Files
      */
     
    @SimpleFunction
    public void Deletedir(String path) {  
    File file = new File(path);  
    
    if (!file.isDirectory()) {  
        file.delete();  
    }  
    deleteChildren(file);
    file.delete();  
    }  
  
    private boolean deleteChildren(File dir) {  
    File[] children = dir.listFiles();  
    boolean childrenDeleted = true;  
    for (int i = 0; children != null && i < children.length; i++) {  
        File child = children[i];  
        if (child.isDirectory()) {  
            childrenDeleted = this.deleteChildren(child) && childrenDeleted;  
        }  
        if (child.exists()) {  
            childrenDeleted = child.delete() && childrenDeleted;  
        }  
    }  
    return childrenDeleted;  
}  
    
    @SimpleFunction
    public void Deletefile(String Filename, String Directory) {  
    File dir = new File("/mnt/sdcard/"+Directory);
    File file = new File(dir.getAbsolutePath(), Filename);
    file.delete();
    }

   /*
    * ZIP crear zip y descomprimir zip
    */

   
        @SimpleFunction(description = "ZipFile el fichero a zipear test.txt, NameZip el nombre del fichero comprimido test.zip")
        public void ZipFile(String ZipFile, String NameZip) throws Exception {
		// objetos en memoria
		FileInputStream fis = null;
		FileOutputStream fos = null;
		ZipOutputStream zipos = null;
 
		// buffer
		byte[] buffer = new byte[BUFFER_SIZE];
		try {   

                        File dir = new File("/mnt/sdcard/"+ZipFile);
                        File file = new File(dir.getAbsolutePath());
                        File dir2 = new File("/mnt/sdcard/"+NameZip);
                        File file2 = new File(dir2.getAbsolutePath());
			// fichero a comprimir
			fis = new FileInputStream(file);
			// fichero contenedor del zip
			fos = new FileOutputStream(file2);
			// fichero comprimido
			zipos = new ZipOutputStream(fos);
			ZipEntry zipEntry = new ZipEntry(ZipFile);
			zipos.putNextEntry(zipEntry);
			int len = 0;
			// zippear
			while ((len = fis.read(buffer, 0, BUFFER_SIZE)) != -1)
				zipos.write(buffer, 0, len);
			// volcar la memoria al disco
			zipos.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			// cerramos los files
			zipos.close();
			fis.close();
			fos.close();
		} // end try
	} // end Zippear
 
        @SimpleFunction(description = "ZipFile fichero a descomprimir test.zip, FileName fichero que extraera text.txt")
	public void UnZip(String ZipFile, String FileName) throws Exception {
		BufferedOutputStream bos = null;
		FileInputStream fis = null;
		ZipInputStream zipis = null;
		FileOutputStream fos = null;
 
		try {
                        File dir = new File("/mnt/sdcard/"+ZipFile);
                        File file = new File(dir.getAbsolutePath());
                        File dir2 = new File("/mnt/sdcard/"+FileName);
                        File file2 = new File(dir2.getAbsolutePath());
			fis = new FileInputStream(file);
			zipis = new ZipInputStream(new BufferedInputStream(fis));
			if (zipis.getNextEntry() != null) {
				int len = 0;
				byte[] buffer = new byte[BUFFER_SIZE];
				fos = new FileOutputStream(file2);
				bos = new BufferedOutputStream(fos, BUFFER_SIZE);
 
				while  ((len = zipis.read(buffer, 0, BUFFER_SIZE)) != -1)
					bos.write(buffer, 0, len);
				bos.flush();
			} else {
				throw new Exception("El zip no contenia fichero alguno");
			} // end if
		} catch (Exception e) {
			throw e;
		} finally {
			bos.close();
			zipis.close();
			fos.close();
			fis.close();
		} // end try
	} // end UnZip

   @SimpleFunction
    public void Zip(String filename,String carpeta){
 
  try {
  File dir = new File("/mnt/sdcard/"+carpeta);
  File path = new File(dir.getAbsolutePath());
  
 
  //Nuestro InputStream
 
  BufferedInputStream origin = null;
 
  //Declaramos el FileOutputStream para guardar el archivo
 
  FileOutputStream dest = new FileOutputStream(filename);
 
  //Indicamos que será un archivo ZIP
 
  ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
 
  //Indicamos que el archivo tendrá compresión
 
  out.setMethod(ZipOutputStream.DEFLATED);
 
  //Indicamos que el archivo NO tendrá compresión
 
  //out.setMethod(ZipOutputStream.STORED);
 
  byte data[] = new byte[BUFFER];
 
  // Creamos la referencia de la carpeta a leer
 
  File f = new File(dir.getAbsolutePath());
 
  // Guarda los nombres de los archivos de la carpeta a leer
 
  String files[] = f.list();
 
 // Muestra el listado de archivos de la carpeta a leer
 
  for (int i=0; i<files.length; i++) {
 
  System.out.println("Agregando al ZIP: "+files[i]);
 
  //Creamos el objeto a guardar para cada uno de los elementos del listado
 
  FileInputStream fi = new FileInputStream(dir+files[i]);
 
  origin = new BufferedInputStream(fi, BUFFER);
 
  ZipEntry entry = new ZipEntry(files[i]);
 
  //Guardamos el objeto en el ZIP
 
  out.putNextEntry(entry);
 
  int count;
 
  //Escribimos el objeto en el ZIP
 
  while((count = origin.read(data, 0,BUFFER)) != -1) {
 
  out.write(data, 0, count);
 
  }
 
  origin.close();
 
  }
 
  out.close();
 
  } catch(Exception e) {
 
  e.printStackTrace();
 
  }
 

  }
  
}
