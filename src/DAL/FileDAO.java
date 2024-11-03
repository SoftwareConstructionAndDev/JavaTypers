package DAL;

//DAL/FileDAO.java


import DTO.FileDTO;
import java.sql.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

 // Database connection details
 private static final String URL = "jdbc:mysql://localhost:3306/texteditor";  // Your database URL
 private static final String USER = "root";  // Your MySQL username
 private static final String PASSWORD = "";  // Your MySQL password
private static final String DEFAULT_OWNER = null;

 // Establishing a connection to the database
 public Connection getConnection() throws SQLException {
     return DriverManager.getConnection(URL, USER, PASSWORD);
 }

 // Calculate the hash of the file content using SHA-256 for integrity checks
 private String calculateHash(String content) throws NoSuchAlgorithmException {
     MessageDigest digest = MessageDigest.getInstance("SHA-256");
     byte[] hash = digest.digest(content.getBytes());
     StringBuilder hexString = new StringBuilder();
     for (byte b : hash) {
         hexString.append(String.format("%02x", b));
     }
     return hexString.toString();
 }

 // Create a new file in the database with owner "imama"
 public boolean createFile(String fileName, String content, String owner) throws SQLException, NoSuchAlgorithmException {
     String sql = "INSERT INTO files (file_name, file_content, owner, hash_value) VALUES (?, ?, ?, ?)";
     try (Connection conn = getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         pstmt.setString(1, fileName);           // Set file name
         pstmt.setString(2, content);            // Set file content
         pstmt.setString(3, owner);              // Owner is "imama"
         pstmt.setString(4, calculateHash(content));  // Set SHA-256 hash of content
         return pstmt.executeUpdate() > 0;       // Return true if the insert was successful
     }
 }

 // Read a file's details from the database using the file name
 public FileDTO readFile(String fileName) throws SQLException {
     String sql = "SELECT * FROM files WHERE file_name = ?";
     try (Connection conn = getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         pstmt.setString(1, fileName);   // Set file name as parameter
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
             return new FileDTO(
                 rs.getString("file_name"),        // File name
                 rs.getString("file_content"),     // File content
                 rs.getString("owner"),            // Owner (default: "imama")
                 rs.getTimestamp("created_at"),    // Created timestamp
                 rs.getTimestamp("updated_at")     // Updated timestamp
             );
         }
     }
     return null;  // Return null if the file is not found
 }

 // Update an existing file's content and hash based on the file name and owner
 public boolean updateFile(String fileName, String content, String owner) throws SQLException, NoSuchAlgorithmException {
     String sql = "UPDATE files SET file_content = ?, hash_value = ? WHERE file_name = ? AND owner = ?";
     try (Connection conn = getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         pstmt.setString(1, content);            // Set updated content
         pstmt.setString(2, calculateHash(content));  // Set updated hash
         pstmt.setString(3, fileName);           // Set file name
         pstmt.setString(4, owner);              // Owner is "imama"
         return pstmt.executeUpdate() > 0;       // Return true if the update was successful
     }
 }

 // Delete a file from the database based on the file name and owner
//DAL/FileDAO.java

//In FileDAO.java
public boolean deleteFile(String fileName, String owner) throws SQLException {
  String sql = "DELETE FROM files WHERE file_name = ? AND owner = ?";
  try (Connection conn = getConnection();
       PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, fileName);  // Set the file name in the SQL query
      pstmt.setString(2, owner);  // Set the owner ("imama") in the SQL query
      return pstmt.executeUpdate() > 0;  // Return true if a row was deleted
  }
}



 // Retrieve a list of all file names in the database
 public List<String> listAllFiles() throws SQLException {
     List<String> fileNames = new ArrayList<>();
     String sql = "SELECT file_name FROM files";
     try (Connection conn = getConnection();
          Statement stmt = conn.createStatement();
          ResultSet rs = stmt.executeQuery(sql)) {
         while (rs.next()) {
             fileNames.add(rs.getString("file_name"));  // Add each file name to the list
         }
     }
     return fileNames;  // Return the list of file names
 }
//DAL/FileDAO.java

//DAL/FileDAO.java

public boolean importFile(Path filePath) throws IOException, SQLException, NoSuchAlgorithmException {
  try {
      // Read file content as bytes and convert to string
      byte[] fileBytes = Files.readAllBytes(filePath);
      String content = new String(fileBytes);

      // Generate hash from file content
      String hashValue = calculateHash(content);

      // Check if file with this hash already exists
      if (isFileExists(hashValue)) {
          System.out.println("File already exists in the database.");
          return false;
      }

      // Insert the new file into the database with default owner "imama"
      String sql = "INSERT INTO files (file_name, file_content, owner, hash_value) VALUES (?, ?, ?, ?)";
      try (Connection conn = getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setString(1, filePath.getFileName().toString());  // File name
          pstmt.setString(2, content);  // File content
          pstmt.setString(3, "imama");  // Default owner "imama"
          pstmt.setString(4, hashValue);  // File hash value
          return pstmt.executeUpdate() > 0;  // Return true if insertion was successful
      }
  } catch (SQLException | IOException e) {
      throw e;  // Rethrow exception to the calling method
  }
}

//Check if a file with the same hash already exists
public boolean isFileExists(String hashValue) throws SQLException {
  String sql = "SELECT COUNT(*) FROM files WHERE hash_value = ?";
  try (Connection conn = getConnection();
       PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, hashValue);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      return rs.getInt(1) > 0;
  }
}

}

