package BL;

import DAL.FileDAO;
import DAL.FileDataAccess;
import DTO.FileDTO;
import DTO.FilePageDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.qcri.farasa.pos.Clitic;
import com.qcri.farasa.pos.Sentence;

public class FileManager implements FileService {
	private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

    private final FileDataAccess fileDAO;
    
    private static final Map<Character, String> ARABIC_TO_ENGLISH_MAP = new HashMap<>();
	private static final int WORDS_PER_PAGE = 500;

   


    public FileManager() throws Exception{
        fileDAO = new FileDAO();
        LOGGER.log(Level.INFO, "FileManager initialized");
    }

   
    // Create a new file without pagination
    @Override
	public boolean createFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
    	LOGGER.log(Level.INFO, "Attempting to create file: {0}", fileName);
        if (fileName == null || fileName.trim().isEmpty() || content == null || content.trim().isEmpty()) {
        	
            throw new IllegalArgumentException("File name and content must be non-empty");
        }
        return fileDAO.createFile(fileName.trim(), content.trim());
    }

    // Read file by name
    @Override
	public FileDTO readFile(String fileName) throws SQLException {
    	LOGGER.log(Level.INFO, "Reading file by name: {0}", fileName);
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name must be non-empty");
        }
        return fileDAO.readFile(fileDAO.getFileIdByName(fileName));
    }

    // Read file by ID
    @Override
	public FileDTO readFile(int fileId) throws SQLException {
    	LOGGER.log(Level.INFO, "Reading file by ID: {0}", fileId);
        return fileDAO.readFile(fileId);
    }

    

    
    
 // In FileBL.java
    @Override
	public boolean deleteFile(String fileName) throws SQLException {
    	LOGGER.log(Level.INFO, "Deleting file: {0}", fileName);
        return fileDAO.deleteFile(fileName);  // Directly calls DAO's deleteFile method with fileName
    }


    // Search for files by content
    @Override
	public List<FileDTO> searchFilesByContent(String searchTerm) throws SQLException {
    	 LOGGER.log(Level.INFO, "Searching files by content");
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term must not be empty");
        }
        return fileDAO.searchFiles(searchTerm.trim());
    }

   

    // Import a single file from the file system, creating a paginated structure
    @Override
	public boolean importFile(Path filePath) throws IOException, SQLException, NoSuchAlgorithmException {
    	LOGGER.log(Level.INFO, "Importing file from path: {0}", filePath);
        return fileDAO.importFile(filePath);
    }

    // Import multiple files from file paths
    @Override
	public boolean importMultipleFiles(List<Path> filePaths) throws IOException, SQLException, NoSuchAlgorithmException {
    	LOGGER.log(Level.INFO, "Importing multiple files");
        return fileDAO.importMultipleFiles(filePaths);
    }

    // List all files from the database
    @Override
	public List<FileDTO> listAllFiles() throws SQLException {
    	LOGGER.log(Level.INFO, "Listing all files");
        return fileDAO.listAllFiles();
    }

 
    @Override
	public int saveFilePage(FilePageDTO pageDTO) throws SQLException {
    	LOGGER.log(Level.INFO, "Saving file page");
        return fileDAO.saveFilePage(pageDTO);
    }
    
    private String calculateHash(String content) throws NoSuchAlgorithmException {
    	LOGGER.log(Level.INFO, "Calculating hash for content");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
   
    @Override
	public boolean fileHashExists(String content) throws SQLException, NoSuchAlgorithmException {
    	LOGGER.log(Level.INFO, "Checking if file hash exists");
        String hashValue = calculateHash(content); // Ensure you have a hash function in `FileBL` for generating hashes
        return fileDAO.fileHashExists(hashValue);
    }
 // pagination ================================== P A G I N A T I O N ==========================
    
    
    @Override
	public List<FilePageDTO> getAllPagesForDocument(int documentId) throws SQLException {
    	LOGGER.log(Level.INFO, "Getting all pages for document ID: {0}", documentId);
        return fileDAO.getPagesByDocumentId(documentId);
    }

    @Override
	public List<FilePageDTO> paginateAndSaveContent(int documentId, String content) throws SQLException {
    	 LOGGER.log(Level.INFO, "Paginating and saving content for document ID: {0}", documentId);
        List<FilePageDTO> pages = new ArrayList<>();
        String[] words = content.split("\\s+");
        StringBuilder pageContent = new StringBuilder();
        int pageNumber = 1;

        for (int i = 0; i < words.length; i++) {
            pageContent.append(words[i]).append(" ");
            if ((i + 1) % WORDS_PER_PAGE == 0 || i == words.length - 1) {
                String pageText = pageContent.toString().trim();
                
                // Create page DTO
                FilePageDTO page = new FilePageDTO(0, documentId, pageNumber, pageText);
                
                // Save to database and get generated ID
                int generatedPageId = fileDAO.saveFilePage(page);
                page.setPageId(generatedPageId);
                
                pages.add(page);
                pageNumber++;
                pageContent.setLength(0);
            }
        }

        return pages;
    }

    @Override
	public int getFileIdByName(String fileName) throws SQLException {
    	LOGGER.log(Level.INFO, "Getting file ID by name: {0}", fileName);
        return fileDAO.getFileIdByName(fileName);
    }
    @Override
    public FileDTO getFileByName(String fileName) throws SQLException {
    	LOGGER.log(Level.INFO, "Getting file by name: {0}", fileName);
        if (fileName == null || fileName.trim().isEmpty()) {
        	LOGGER.log(Level.SEVERE, "File name cannot be empty");
            throw new IllegalArgumentException("File name must be non-empty");
        }
        // Get the file ID from the file name
        int fileId = fileDAO.getFileIdByName(fileName);
        // Retrieve the file using the file ID
        return fileDAO.readFile(fileId);
    }
    // Retrieve a specific page of content from a file by page number
    @Override
	public FilePageDTO getFilePage(String fileName, int pageNumber) throws SQLException {
    	LOGGER.log(Level.INFO, "Getting file page for file: {0}, page number: {1}", new Object[]{fileName, pageNumber});
        int fileId = fileDAO.getFileIdByName(fileName);
        if (fileId == -1) {
            throw new SQLException("File not found for name: " + fileName);
        }
        return fileDAO.getPage(fileId, pageNumber);
    }

    // Retrieve all pages for a specific file
    @Override
	public List<FilePageDTO> getAllPagesForFile(String fileName) throws SQLException {
    	LOGGER.log(Level.INFO, "Getting all pages for file: {0}", fileName);
        int fileId = fileDAO.getFileIdByName(fileName);
        if (fileId == -1) {
            throw new SQLException("File not found for name: " + fileName);
        }
        return fileDAO.getAllPages(fileId);
    }
    
    
    @Override
	public String generateUniqueFileName(String fileName) throws SQLException {
    	LOGGER.log(Level.INFO, "Generating unique file name for: {0}", fileName);
        return fileDAO.generateUniqueFileName(fileName); // Call the DAO's method
    }

    @Override
	public boolean updateFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
    	LOGGER.log(Level.INFO, "Updating file: {0}", fileName);
        if (fileName == null || fileName.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("File name and content must not be empty");
        }

        int fileId = fileDAO.getFileIdByName(fileName);
        if (fileId == -1) {
            throw new SQLException("File not found: " + fileName);
        }

        return fileDAO.updateFile(fileId, content);
    }

    // Create a new file with pagination
    @Override
	public boolean createPaginatedFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
    	 LOGGER.log(Level.INFO, "Creating paginated file: {0}", fileName);
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
    @Override
	public void saveFileAndPaginate(String fileName, String content) throws SQLException {
        // Save the file first and get its ID
    	 LOGGER.log(Level.INFO, "Saving file and paginating: {0}", fileName);
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
    
    
  //POS Tagging

    // POS Tag Mappings
    private static final HashMap<String, String> TAG_MEANINGS = new HashMap<>();

    static {
        TAG_MEANINGS.put("PREP", "Preposition");
        TAG_MEANINGS.put("NOUN-FS", "Noun (Feminine Singular)");
        TAG_MEANINGS.put("DET", "Determiner");
        TAG_MEANINGS.put("CONJ", "Conjunction");
        TAG_MEANINGS.put("ADJ-FS", "Adjective (Feminine Singular)");
        TAG_MEANINGS.put("NOUN-MS", "Noun (Masculine Singular)");
        TAG_MEANINGS.put("PRON", "Pronoun");
        TAG_MEANINGS.put("NSUFF", "Noun Suffix");
        TAG_MEANINGS.put("PUNC", "Punctuation");
        TAG_MEANINGS.put("VERB", "Verb");
        TAG_MEANINGS.put("NOUN-MP", "Noun (Masculine Plural)");
        TAG_MEANINGS.put("NOUN-FP", "Noun (Feminine Plural)");
        TAG_MEANINGS.put("ADJ-MS", "Adjective (Masculine Singular)");
        TAG_MEANINGS.put("E", "End Marker");
        TAG_MEANINGS.put("S", "Start Marker");
        // Add more mappings as needed
    }
    @Override
	public String processTextForPOSTags(String text) {
    	LOGGER.log(Level.INFO, "Processing text for POS tags");
        try {
            Sentence sentence = fileDAO.getPOSTags(text);
            StringBuilder result = new StringBuilder();
            for (Clitic clitic : sentence.clitics) {
                String word = clitic.surface;
                String tag = clitic.guessPOS;
                String description = TAG_MEANINGS.getOrDefault(tag, tag); // Get the description or use the tag itself
                result.append(word)
                      .append(" (")
                      .append(description)
                      .append(") ")
                      .append((clitic.genderNumber.isEmpty() ? "" : "-" + clitic.genderNumber))
                      .append("\n");
            }
            return result.toString().trim();
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE, "Error processing text for POS tags", e);
            return "Error processing text: " + e.getMessage();
        }
    }
    

}
