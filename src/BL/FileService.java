package BL;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import DTO.FileDTO;
import DTO.FilePageDTO;

public interface FileService {

	// Create a new file without pagination
	boolean createFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException;

	// Read file by name
	FileDTO readFile(String fileName) throws SQLException;

	// Read file by ID
	FileDTO readFile(int fileId) throws SQLException;

	// In FileBL.java
	boolean deleteFile(String fileName) throws SQLException;

	// Search for files by content
	List<FileDTO> searchFilesByContent(String searchTerm) throws SQLException;

	// Import a single file from the file system, creating a paginated structure
	boolean importFile(Path filePath) throws IOException, SQLException, NoSuchAlgorithmException;

	// Import multiple files from file paths
	boolean importMultipleFiles(List<Path> filePaths) throws IOException, SQLException, NoSuchAlgorithmException;

	// List all files from the database
	List<FileDTO> listAllFiles() throws SQLException;

	int saveFilePage(FilePageDTO pageDTO) throws SQLException;

	boolean fileHashExists(String content) throws SQLException, NoSuchAlgorithmException;
	// pagination ================================== P A G I N A T I O N ==========================

	List<FilePageDTO> getAllPagesForDocument(int documentId) throws SQLException;

	List<FilePageDTO> paginateAndSaveContent(int documentId, String content) throws SQLException;

	int getFileIdByName(String fileName) throws SQLException;

	FileDTO getFileByName(String fileName) throws SQLException ;

	// Retrieve a specific page of content from a file by page number
	FilePageDTO getFilePage(String fileName, int pageNumber) throws SQLException;

	// Retrieve all pages for a specific file
	List<FilePageDTO> getAllPagesForFile(String fileName) throws SQLException;

	String generateUniqueFileName(String fileName) throws SQLException;

	boolean updateFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException;

	// Create a new file with pagination
	boolean createPaginatedFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException;

	void saveFileAndPaginate(String fileName, String content) throws SQLException;

	String processTextForPOSTags(String text);

}