package BL;

//BL/FileBL.java
//package BL;

import DAL.FileDAO;
import DTO.FileDTO;

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
	 DAOFactory daoFactory = createDAOFactory();
     this.fileDAO = daoFactory.getFileDAO();
 }
 
 private static DAOFactory createDAOFactory() {
     String dbType = Config.getDBType();
     switch (dbType) {
         case "Mongo":
             return new MongoDAOFactory();
         case "MySQL":
         default:
             return new MySQLDAOFactory();
     }
 }

 // Business logic to create a file
 public boolean createFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
     // Validation: Check if the file name or content is empty
     if (fileName == null || fileName.trim().isEmpty() || content == null || content.trim().isEmpty()) {
         throw new IllegalArgumentException("File name and content must be non-empty");
     }
     // Call the DAO layer to create the file with the default owner
     return fileDAO.createFile(fileName.trim(), content.trim(), DEFAULT_OWNER);
 }

 // Business logic to read a file from the database
 public FileDTO readFile(String fileName) throws SQLException {
     // Validation: Check if the file name is empty
     if (fileName == null || fileName.trim().isEmpty()) {
         throw new IllegalArgumentException("File name must be non-empty");
     }
     // Call the DAO layer to read the file by its name
     return fileDAO.readFile(fileName.trim());
 }

 // Business logic to update an existing file
 public boolean updateFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
     // Validation: Check if the file name or content is empty
     if (fileName == null || fileName.trim().isEmpty() || content == null || content.trim().isEmpty()) {
         throw new IllegalArgumentException("File name and content must be non-empty");
     }
     // Call the DAO layer to update the file with the default owner
     return fileDAO.updateFile(fileName.trim(), content.trim(), DEFAULT_OWNER);
 }

 // Business logic to delete a file from the database
//BL/FileBL.java

//In FileBL.java
public boolean deleteFile(String fileName) throws SQLException {
  // Check if the file name is valid
  if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("File name must be non-empty");
  }
  // Call the DAO layer to delete the file and pass the default owner ("imama")
  return fileDAO.deleteFile(fileName.trim(), DEFAULT_OWNER);
}


//BL/FileBL.java

//Business logic to import a single file
public boolean importFile(Path filePath) throws IOException, SQLException, NoSuchAlgorithmException {
  return fileDAO.importFile(filePath);
}

 // Business logic to list all files from the database
 public List<String> listAllFiles() throws SQLException {
     // Call the DAO layer to retrieve all file names
     return fileDAO.listAllFiles();
 }
}
