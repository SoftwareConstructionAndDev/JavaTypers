//package DAL;

//DAL/FileDAO.java


//import DTO.FileDTO;
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

