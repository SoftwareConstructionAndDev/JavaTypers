package DAL;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import com.qcri.farasa.pos.Sentence;

import DTO.FileDTO;
import DTO.FilePageDTO;

public interface I_DAO {

	// Retrieve file ID by name
	int getFileIdByName(String fileName) throws SQLException;

	FileDTO readFile(int fileId) throws SQLException;

	// In FileDAO.java
	boolean deleteFile(String fileName) throws SQLException;

	// File Import Methods
	boolean importFile(Path filePath) throws IOException, SQLException, NoSuchAlgorithmException;

	boolean importMultipleFiles(List<Path> filePaths) throws IOException, SQLException, NoSuchAlgorithmException;

	// List all files
	List<FileDTO> listAllFiles() throws SQLException;

	// Transliteration
	String transliterateToEnglish(String arabicText);

	List<FileDTO> searchFiles(String searchTerm) throws SQLException;

	boolean transliterateAndUpdateFile(String fileName) throws SQLException, NoSuchAlgorithmException;

	// Update file with transliterated content
	boolean updateFileWithTransliteration(int fileId) throws SQLException, NoSuchAlgorithmException;

	int saveFileAndReturnId(String fileName, String content) throws SQLException;

	//fetching file content for a given file name
	List<String> getAllFiles() throws Exception;

	String getFileContent(String fileName) throws Exception;
	///===============================P A G I N A T I O N ===============================================

	int saveFilePage(FilePageDTO pageDTO) throws SQLException;

	void savePage(FilePageDTO pageDTO) throws SQLException;

	List<FilePageDTO> getPagesByDocumentId(int documentId) throws SQLException;

	List<FilePageDTO> getPagesByDocumentName(String documentName) throws SQLException;

	// Pagination Methods
	List<FilePageDTO> paginateContent(int fileId, String content) throws SQLException;

	FilePageDTO getPage(int fileId, int pageNumber) throws SQLException;

	List<FilePageDTO> getAllPages(int fileId) throws SQLException;

	boolean fileHashExists(String hashValue) throws SQLException;

	boolean createFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException;

	String generateUniqueFileName(String fileName) throws SQLException;

	boolean fileExists(String fileName) throws SQLException;

	boolean updateFile(int fileId, String content) throws SQLException, NoSuchAlgorithmException;

	//POS Tagging 
	Sentence getPOSTags(String text)
			throws IOException, FileNotFoundException, UnsupportedEncodingException, InterruptedException;

}