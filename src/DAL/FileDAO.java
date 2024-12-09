package DAL;

import DTO.FileDTO;
import DTO.FilePageDTO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qcri.farasa.pos.Sentence;
import com.qcri.farasa.pos.FarasaPOSTagger;
import com.qcri.farasa.segmenter.Farasa;

public class FileDAO implements FileDataAccess {

    private static final String URL = "jdbc:mysql://localhost:3306/texteditor";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final int WORDS_PER_PAGE = 500;
    private static final Map<Character, String> ARABIC_TO_ENGLISH_MAP = new HashMap<>();
    private Farasa farasa;
    private FarasaPOSTagger farasaPOS;
    
    public FileDAO() throws Exception{
    	farasa = new Farasa();
        farasaPOS = new FarasaPOSTagger(farasa);
    }

    
    // Database connection method
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
  
    // Hashing method for duplicate prevention
    private String calculateHash(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    // Check for duplicate content
    private boolean isDuplicateContent(String hashValue) throws SQLException {
        String sql = "SELECT COUNT(*) FROM files WHERE hash_value = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashValue);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
 // Retrieve file ID by name
    @Override
	public int getFileIdByName(String fileName) throws SQLException {
        String sql = "SELECT id FROM files WHERE file_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }
 // Insert a page of content for a file
    private boolean insertFilePage(int fileId, int pageNumber, String content) throws SQLException {
        String sql = "INSERT INTO file_pages (file_id, page_number, page_content) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fileId);
            pstmt.setInt(2, pageNumber);
            pstmt.setString(3, content);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    

    @Override
	public FileDTO readFile(int fileId) throws SQLException {
        String sql = "SELECT * FROM files WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fileId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new FileDTO(
                    rs.getInt("id"),
                    rs.getString("file_name"),
                    rs.getString("file_content"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getString("hash_value")
                );
            }
        }
        return null;
    }

   

 // In FileDAO.java
    @Override
	public boolean deleteFile(String fileName) throws SQLException {
        String sql = "DELETE FROM files WHERE file_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            return pstmt.executeUpdate() > 0;
        }
    }


  

    // File Import Methods
    @Override
	public boolean importFile(Path filePath) throws IOException, SQLException, NoSuchAlgorithmException {
        String content = new String(Files.readAllBytes(filePath));
        String fileName = filePath.getFileName().toString();
        return createFile(fileName, content);
    }

    @Override
	public boolean importMultipleFiles(List<Path> filePaths) throws IOException, SQLException, NoSuchAlgorithmException {
        for (Path filePath : filePaths) {
            if (!importFile(filePath)) {
                return false;
            }
        }
        return true;
    }

    

    // List all files
    @Override
	public List<FileDTO> listAllFiles() throws SQLException {
        List<FileDTO> files = new ArrayList<>();
        String sql = "SELECT * FROM files";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                files.add(new FileDTO(
                    rs.getInt("id"),
                    rs.getString("file_name"),
                    rs.getString("file_content"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getString("hash_value")
                ));
            }
        }
        return files;
    }

    // Transliteration
    @Override
	public String transliterateToEnglish(String arabicText) {
        StringBuilder transliterated = new StringBuilder();
        for (char c : arabicText.toCharArray()) {
            transliterated.append(ARABIC_TO_ENGLISH_MAP.getOrDefault(c, String.valueOf(c)));
        }
        return transliterated.toString();
    }
    @Override
	public List<FileDTO> searchFiles(String searchTerm) throws SQLException {
        List<FileDTO> results = new ArrayList<>();
        String sql = "SELECT * FROM files WHERE file_name LIKE ? OR file_content LIKE ?";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String query = "%" + searchTerm + "%";
            pstmt.setString(1, query);
            pstmt.setString(2, query);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                results.add(new FileDTO(
                    rs.getInt("id"),
                    rs.getString("file_name"),
                    rs.getString("file_content"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getString("hash_value")
                ));
            }
        }
        return results;
    }

   

    
    @Override
	public boolean transliterateAndUpdateFile(String fileName) throws SQLException, NoSuchAlgorithmException {
        int fileId = getFileIdByName(fileName);
        if (fileId == -1) {
            throw new SQLException("File not found with name: " + fileName);
        }
        return updateFileWithTransliteration(fileId);
    }
 // Update file with transliterated content
    @Override
	public boolean updateFileWithTransliteration(int fileId) throws SQLException, NoSuchAlgorithmException {
        FileDTO file = readFile(fileId);
        if (file == null) {
            throw new SQLException("File not found for ID: " + fileId);
        }
        String transliteratedContent = transliterateToEnglish(file.getContent());
        return updateFile(fileId, transliteratedContent);
    }
   

   
    @Override
	public int saveFileAndReturnId(String fileName, String content) throws SQLException {
        String sql = "INSERT INTO files (file_name, file_content) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, fileName);
            pstmt.setString(2, content);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Saving file failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated file ID
                } else {
                    throw new SQLException("Saving file failed, no ID obtained.");
                }
            }
        }
    }
   
     //fetching file content for a given file name
    @Override
	public List<String> getAllFiles() throws Exception {
        List<String> files = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT content FROM files");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                files.add(rs.getString("content"));
            }
        }
        return files;
    }

    @Override
	public String getFileContent(String fileName) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT content FROM files WHERE name = ?")) {
            stmt.setString(1, fileName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("content");
                }
            }
        }
        return null;
    }
 ///===============================P A G I N A T I O N ===============================================

    @Override
	public int saveFilePage(FilePageDTO pageDTO) throws SQLException {
        String sql = "INSERT INTO pages (file_id, page_number, content) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, pageDTO.getFileId());
            pstmt.setInt(2, pageDTO.getPageNumber());
            pstmt.setString(3, pageDTO.getContent());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Retrieve and return the auto-generated page ID
                        return generatedKeys.getInt(1);
                    }
                }
            }
            
            // If no ID was generated, throw an exception or return a default value
            throw new SQLException("Creating page failed, no ID obtained.");
        }
    }
    @Override
	public void savePage(FilePageDTO pageDTO) throws SQLException {
        String sql = "INSERT INTO pages (file_id, page_number, content) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pageDTO.getFileId());
            pstmt.setInt(2, pageDTO.getPageNumber());
            pstmt.setString(3, pageDTO.getContent());
            pstmt.executeUpdate();
        }
    }
    
    @Override
	public List<FilePageDTO> getPagesByDocumentId(int documentId) throws SQLException {
        List<FilePageDTO> pages = new ArrayList<>();
        String sql = "SELECT * FROM pages WHERE file_id = ? ORDER BY page_number ASC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, documentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                pages.add(new FilePageDTO(
                    rs.getInt("id"),
                    rs.getInt("file_id"),
                    rs.getInt("page_number"),
                    rs.getString("content")
                ));
            }
        }
        return pages;
    }
    @Override
	public List<FilePageDTO> getPagesByDocumentName(String documentName) throws SQLException {
        // Get the file ID for the given document name
        int documentId = getFileIdByName(documentName);

        if (documentId == -1) {
            throw new SQLException("Document not found: " + documentName);
        }

        // Retrieve pages for the document
        return getPagesByDocumentId(documentId);
    }
    
    // Pagination Methods
    @Override
	public List<FilePageDTO> paginateContent(int fileId, String content) throws SQLException {
        List<FilePageDTO> pages = new ArrayList<>();
        String[] words = content.split("\\s+");
        StringBuilder pageContent = new StringBuilder();
        int pageNumber = 1;
        for (int i = 0; i < words.length; i++) {
            pageContent.append(words[i]).append(" ");
            if ((i + 1) % WORDS_PER_PAGE == 0 || i == words.length - 1) {
                pages.add(new FilePageDTO(0, fileId, pageNumber++, pageContent.toString().trim()));
                pageContent.setLength(0);
            }
        }
        insertPages(pages);
        return pages;
    }

    private void insertPages(List<FilePageDTO> pages) throws SQLException {
        String sql = "INSERT INTO pages (file_id, page_number, content) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (FilePageDTO page : pages) {
                pstmt.setInt(1, page.getFileId());
                pstmt.setInt(2, page.getPageNumber());
                pstmt.setString(3, page.getContent());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    @Override
	public FilePageDTO getPage(int fileId, int pageNumber) throws SQLException {
        String sql = "SELECT * FROM pages WHERE file_id = ? AND page_number = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fileId);
            pstmt.setInt(2, pageNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new FilePageDTO(
                    rs.getInt("page_id"),
                    rs.getInt("file_id"),
                    rs.getInt("page_number"),
                    rs.getString("content")
                );
            }
        }
        return null;
    }

    @Override
	public List<FilePageDTO> getAllPages(int fileId) throws SQLException {
        List<FilePageDTO> pages = new ArrayList<>();
        String sql = "SELECT * FROM pages WHERE file_id = ? ORDER BY page_number ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fileId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pages.add(new FilePageDTO(
                    rs.getInt("page_id"),
                    rs.getInt("file_id"),
                    rs.getInt("page_number"),
                    rs.getString("content")
                ));
            }
        }
        return pages;
    }

/// =======================================
//    file setting no duplicate and file name change 
//    =======================================
    
    
    
   
    @Override
	public boolean fileHashExists(String hashValue) throws SQLException {
        String sql = "SELECT COUNT(*) FROM files WHERE hash_value = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashValue);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    @Override
	public boolean createFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
        // Calculate content hash
        String hashValue = calculateHash(content);

        // Check for duplicate content
        if (fileHashExists(hashValue)) {
            System.out.println("File with identical content already exists.");
            return false; // Prevent duplicate content
        }

        // Ensure unique file name
        fileName = generateUniqueFileName(fileName);

        // Insert the new file
        String sql = "INSERT INTO files (file_name, file_content, hash_value) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            pstmt.setString(2, content);
            pstmt.setString(3, hashValue);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
	public String generateUniqueFileName(String fileName) throws SQLException {
        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        int counter = 1;
        String newFileName = fileName;

        // Check if the file name exists and append an integer if necessary
        while (fileExists(newFileName)) {
            newFileName = baseName + counter + extension;
            counter++;
        }

        return newFileName;
    }

	
    @Override
	public boolean fileExists(String fileName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM files WHERE file_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0; // Return true if the file name exists
        }
    }
    @Override
	public boolean updateFile(int fileId, String content) throws SQLException, NoSuchAlgorithmException {
        String hashValue = calculateHash(content);

        if (fileHashExists(hashValue)) {
            throw new SQLException("Duplicate content detected. Cannot update the file.");
        }

        String sql = "UPDATE files SET file_content = ?, hash_value = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, content);
            pstmt.setString(2, hashValue);
            pstmt.setInt(3, fileId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
  //POS Tagging 
    @Override
	public Sentence getPOSTags(String text) throws IOException, FileNotFoundException, UnsupportedEncodingException, InterruptedException {
        System.out.println("Segmenting text...");
        ArrayList<String> segmentedText = farasa.segmentLine(text); // Segment the Arabic text
        System.out.println("Segmented text: " + segmentedText);
        try {
			return farasaPOS.tagLine(segmentedText);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Return tagged sentence
		return null;
    }


} 