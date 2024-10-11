

//BL/FileBL.java
//package BL;

//import DAL.FileDAO;
//import DTO.FileDTO;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

public class FileBL {
 private FileDAO fileDAO;

 // Default owner of all files is "imama"
 private static final String DEFAULT_OWNER = "imama";

 // Constructor initializes the FileDAO object
 public FileBL() {
     fileDAO = new FileDAO();
 }

 
//BL/FileBL.java

//Business logic to import a single file
public boolean importFile(Path filePath) throws IOException, SQLException, NoSuchAlgorithmException {
  return fileDAO.importFile(filePath);
}

}

