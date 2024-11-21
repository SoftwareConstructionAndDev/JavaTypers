package BL;

import DAL.FileDAO;
import DTO.FileDTO;
import DTO.FilePageDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBL {

    private final FileDAO fileDAO;
    private static final Map<Character, String> ARABIC_TO_ENGLISH_MAP = new HashMap<>();

   


    public FileBL() {
        fileDAO = new FileDAO();
    }

    // Create a new file with pagination
    public boolean createPaginatedFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
        if (fileName == null || fileName.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("File name and content must be non-empty");
        }

        if (fileDAO.createFile(fileName.trim(), content.trim())) {
            int fileId = fileDAO.getFileIdByName(fileName);
            fileDAO.paginateContent(fileId, content); // Paginate the content
            return true;
        }
        return false;
    }
    public void saveFileAndPaginate(String fileName, String content) throws SQLException {
        // Save the file first and get its ID
        int fileId = fileDAO.saveFileAndReturnId(fileName, content);

        // Split content into pages
        String[] words = content.split("\\s+");
        StringBuilder pageContent = new StringBuilder();
        int pageNumber = 1;

        for (int i = 0; i < words.length; i++) {
            pageContent.append(words[i]).append(" ");
            if ((i + 1) % 500 == 0 || i == words.length - 1) {
                // Save each page to the database
                FilePageDTO pageDTO = new FilePageDTO(0, fileId, pageNumber++, pageContent.toString().trim());
                fileDAO.savePage(pageDTO);
                pageContent.setLength(0); // Clear for the next page
            }
        }
    }

    // Create a new file without pagination
    public boolean createFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
        if (fileName == null || fileName.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("File name and content must be non-empty");
        }
        return fileDAO.createFile(fileName.trim(), content.trim());
    }

    // Read file by name
    public FileDTO readFile(String fileName) throws SQLException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name must be non-empty");
        }
        return fileDAO.readFile(fileDAO.getFileIdByName(fileName));
    }

    // Read file by ID
    public FileDTO readFile(int fileId) throws SQLException {
        return fileDAO.readFile(fileId);
    }

    // Update a file with pagination
    public boolean updateFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
        if (fileName == null || fileName.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("File name and content must be non-empty");
        }
        int fileId = fileDAO.getFileIdByName(fileName);
        if (fileDAO.updateFile(fileId, content)) {
            fileDAO.paginateContent(fileId, content); // Re-paginate updated content
            return true;
        }
        return false;
    }

    
    
 // In FileBL.java
    public boolean deleteFile(String fileName) throws SQLException {
        return fileDAO.deleteFile(fileName);  // Directly calls DAO's deleteFile method with fileName
    }


    // Search for files by content
    public List<FileDTO> searchFilesByContent(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term must not be empty");
        }
        return fileDAO.searchFiles(searchTerm.trim());
    }

    // Retrieve a specific page of content from a file by page number
    public FilePageDTO getFilePage(String fileName, int pageNumber) throws SQLException {
        int fileId = fileDAO.getFileIdByName(fileName);
        if (fileId == -1) {
            throw new SQLException("File not found for name: " + fileName);
        }
        return fileDAO.getPage(fileId, pageNumber);
    }

    // Retrieve all pages for a specific file
    public List<FilePageDTO> getAllPagesForFile(String fileName) throws SQLException {
        int fileId = fileDAO.getFileIdByName(fileName);
        if (fileId == -1) {
            throw new SQLException("File not found for name: " + fileName);
        }
        return fileDAO.getAllPages(fileId);
    }

    // Import a single file from the file system, creating a paginated structure
    public boolean importFile(Path filePath) throws IOException, SQLException, NoSuchAlgorithmException {
        return fileDAO.importFile(filePath);
    }

    // Import multiple files from file paths
    public boolean importMultipleFiles(List<Path> filePaths) throws IOException, SQLException, NoSuchAlgorithmException {
        return fileDAO.importMultipleFiles(filePaths);
    }

    // List all files from the database
    public List<FileDTO> listAllFiles() throws SQLException {
        return fileDAO.listAllFiles();
    }

 
    public boolean saveFilePage(FilePageDTO pageDTO) throws SQLException {
        return fileDAO.saveFilePage(pageDTO);
    }
    
    private String calculateHash(String content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
   
    public boolean fileHashExists(String content) throws SQLException, NoSuchAlgorithmException {
        String hashValue = calculateHash(content); // Ensure you have a hash function in `FileBL` for generating hashes
        return fileDAO.fileHashExists(hashValue);
    }
    public List<FilePageDTO> getAllPagesForDocument(int documentId) throws SQLException {
        return fileDAO.getPagesByDocumentId(documentId);
    }

    public List<FilePageDTO> paginateAndSaveContent(int documentId, String content) throws SQLException {
        return fileDAO.paginateContent(documentId, content);
    }

    public int getFileIdByName(String fileName) throws SQLException {
        return fileDAO.getFileIdByName(fileName);
    }

    

}
