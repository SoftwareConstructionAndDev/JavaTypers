
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
//Business logic to create a file
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
  public List<FileDTO> searchFiles(String searchTerm) throws SQLException {
	    if (searchTerm == null || searchTerm.trim().isEmpty()) {
	        throw new IllegalArgumentException("Search term must not be empty");
	    }
	    return fileDAO.searchFiles(searchTerm.trim());
	}
 
	    // Method to transliterate Arabic text to a phonetic English equivalent
	    public String transliterateText(String arabicText) {
	        if (arabicText == null || arabicText.trim().isEmpty()) {
	            throw new IllegalArgumentException("Text for transliteration must not be empty");
	        }

	        Map<Character, String> transliterationMap = new HashMap<>();
	        transliterationMap.put('ا', "a");
	        transliterationMap.put('ب', "b");
	        transliterationMap.put('ت', "t");
	        transliterationMap.put('ث', "th");
	        transliterationMap.put('ج', "j");
	        transliterationMap.put('ح', "h");
	        transliterationMap.put('خ', "kh");
	        // Continue mapping Arabic characters to their phonetic English equivalents
	        // Add more mappings as needed

	        StringBuilder transliterated = new StringBuilder();
	        for (char c : arabicText.toCharArray()) {
	            transliterated.append(transliterationMap.getOrDefault(c, String.valueOf(c)));
	        }
	        return transliterated.toString();
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

public List<String> listAllFiles() throws SQLException {
    // Call the DAO layer to retrieve all file names
    return fileDAO.listAllFiles();
 }

}

