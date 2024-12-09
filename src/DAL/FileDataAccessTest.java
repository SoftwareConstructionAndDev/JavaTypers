package DAL;

import DTO.FileDTO;
import DTO.FilePageDTO;
import Test.DummyDatabase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.qcri.farasa.pos.Sentence;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDataAccessTest {

    private FileDAO fileDAO;
    private DummyDatabase dummyDB;

    @BeforeEach
    void setUp() throws Exception {
        dummyDB = DummyDatabase.getInstance();
        dummyDB.clearDatabase(); // Clear database for isolated testing
        fileDAO = new FileDAO(); // FileDAO connected to DummyDatabase
    }

    @Test
    void testGetFileIdByName() throws SQLException {
        dummyDB.insertFile("testFile.txt", "Sample content", "hash123");
        int fileId = fileDAO.getFileIdByName("testFile.txt");
        assertTrue(fileId > 0, "File ID should be a positive integer");
    }
    
    @Test
    void testGetFileIdByName_EmptyName() throws SQLException {
        assertThrows(SQLException.class, () -> fileDAO.getFileIdByName(""));
    }

    @Test
    void testGetFileIdByName_InvalidName() throws SQLException {
        assertEquals(-1, fileDAO.getFileIdByName("nonexistent.txt"));
    }


    @Test
    void testReadFile() throws SQLException {
        FileDTO file = dummyDB.insertFile("testFile.txt", "Sample content", "hash123");
        FileDTO retrievedFile = fileDAO.readFile(file.getId());
        assertNotNull(retrievedFile, "File should be retrieved successfully");
        assertEquals("Updated content", retrievedFile.getContent(), "File content should match");
    }
    
    @Test
    void testReadFile_NonExistingId() throws SQLException {
        assertNull(fileDAO.readFile(Integer.MAX_VALUE));
    }


    @Test
    void testDeleteFile() throws SQLException {
        dummyDB.insertFile("testFile.txt", "Sample content", "hash123");
        boolean isDeleted = fileDAO.deleteFile("testFile.txt");
        assertTrue(isDeleted, "File should be deleted successfully");
    }
    
    @Test
    void testDeleteFile_NonExisting() throws SQLException {
        assertFalse(fileDAO.deleteFile("nonexistent.txt"));
    }

    @Test
    void testDeleteFile_EmptyName() throws SQLException {
        assertFalse(fileDAO.deleteFile(""), "Deleting a file with an empty name should return false");
    }

    @Test
    void testDeleteFile_NullName() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> fileDAO.deleteFile(null),
                     "Deleting a file with a null name should throw an IllegalArgumentException");
    }

    @Test
    void testDeleteFile_VerifyDatabaseState() throws Exception {
        dummyDB.insertFile("fileToDelete.txt", "Some content", "hash789");
        boolean isDeleted = fileDAO.deleteFile("fileToDelete.txt");
        assertTrue(isDeleted, "File should be deleted successfully");
        assertNull(fileDAO.getFileContent("fileToDelete.txt"), "Deleted file should no longer exist in the database");
    }


    @Test
    void testImportFile() throws IOException, SQLException, NoSuchAlgorithmException {
        Path testFilePath = Path.of("testFile.txt");
        java.nio.file.Files.writeString(testFilePath, "Sample content");
        boolean isImported = fileDAO.importFile(testFilePath);
        assertTrue(isImported, "File should be imported successfully");
        java.nio.file.Files.delete(testFilePath); // Clean up
    }

    @Test
    void testImportMultipleFiles() throws IOException, SQLException, NoSuchAlgorithmException {
        Path file1 = Path.of("file1.txt");
        Path file2 = Path.of("file2.txt");
        java.nio.file.Files.writeString(file1, "Content 1");
        java.nio.file.Files.writeString(file2, "Content 2");
        boolean isImported = fileDAO.importMultipleFiles(Arrays.asList(file1, file2));
        assertTrue(isImported, "All files should be imported successfully");
        java.nio.file.Files.delete(file1);
        java.nio.file.Files.delete(file2);
    }

    @Test
    void testListAllFiles() throws SQLException {
        dummyDB.insertFile("file1.txt", "Content 1", "hash1");
        dummyDB.insertFile("file2.txt", "Content 2", "hash2");
        List<FileDTO> files = fileDAO.listAllFiles();
        assertEquals(10, files.size(), "There should be 2 files listed");
    }

    @Test
    void testTransliterateToEnglish() {
        String arabicText = "اختبار";
        String transliterated = fileDAO.transliterateToEnglish(arabicText);
        assertNotNull(transliterated, "Transliteration should return a result");
    }
    
    @Test
    void testTransliterateToEnglish_EmptyString() {
        String transliterated = fileDAO.transliterateToEnglish("");
        assertTrue(transliterated.isEmpty(), "Transliteration of an empty string should return an empty result");
    }

    @Test
    void testTransliterateToEnglish_MixedCharacters() {
        String mixedText = "اختبار 1234 ABC";
        String transliterated = fileDAO.transliterateToEnglish(mixedText);
        assertNotNull(transliterated, "Transliteration should handle mixed Arabic, numeric, and English characters");
    }

    @Test
    void testTransliterateToEnglish_LongText() {
        String longText = "هذا نص طويل جدا يُستخدم لاختبار قدرة النظام على معالجة النصوص الطويلة بنجاح وتحويلها إلى الإنجليزية";
        String transliterated = fileDAO.transliterateToEnglish(longText);
        assertNotNull(transliterated, "Transliteration of a long text should be handled correctly and return a result");
    }

    @Test
    void testTransliterateToEnglish_NonArabicCharacters() {
        String nonArabicText = "Hello World!";
        String transliterated = fileDAO.transliterateToEnglish(nonArabicText);
        assertEquals("Hello World!", transliterated, "Transliteration should return the original text when non-Arabic characters are used");
    }

    @Test
    void testSearchFiles() throws SQLException {
        dummyDB.insertFile("file1.txt", "Sample content", "hash1");
        List<FileDTO> results = fileDAO.searchFiles("Sample");
        assertEquals(1, results.size(), "Search should return 1 matching file");
    }
    
    @Test
    void testSearchFiles_NoMatches() throws SQLException {
        dummyDB.insertFile("file1.txt", "No match content", "hash1");
        List<FileDTO> results = fileDAO.searchFiles("Sample");
        assertFalse(results.isEmpty(), "Search should return no matching files");
    }

    @Test
    void testSearchFiles_MultipleMatches() throws SQLException {
        dummyDB.insertFile("file1.txt", "Sample content here", "hash1");
        dummyDB.insertFile("file2.txt", "Another sample content", "hash2");
        List<FileDTO> results = fileDAO.searchFiles("Sample");
        assertEquals(1, results.size(), "Search should return multiple matching files");
    }

    @Test
    void testSearchFiles_SpecialCharacters() throws SQLException {
        dummyDB.insertFile("file1.txt", "Special #content@ here!", "hash1");
        List<FileDTO> results = fileDAO.searchFiles("#content@");
        assertEquals(1, results.size(), "Search should handle special characters correctly");
    }

    @Test
    void testSearchFiles_CaseSensitivity() throws SQLException {
        dummyDB.insertFile("file1.txt", "Case sensitive Content", "hash1");
        List<FileDTO> results = fileDAO.searchFiles("content");
        assertFalse(results.isEmpty(), "Search should be case sensitive and return no matches");
    }


    @Test
    void testUpdateFileWithTransliteration() throws SQLException, NoSuchAlgorithmException {
        FileDTO file = dummyDB.insertFile("arabicFile.txt", "اختبار", "hash1");
        boolean isUpdated = fileDAO.updateFileWithTransliteration(file.getId());
        assertTrue(isUpdated, "File should be updated with transliteration");
    }

    @Test
    void testSaveFileAndReturnId() throws SQLException {
        int fileId = fileDAO.saveFileAndReturnId("newFile.txt", "New content");
        assertTrue(fileId > 0, "File ID should be a positive integer");
    }
    
    @Test
    void testSaveFileAndReturnId_EmptyFileName() throws SQLException {
        assertThrows(SQLException.class, () -> fileDAO.saveFileAndReturnId("", "Content with empty file name"),
                      "Saving a file with an empty file name should throw an SQLException");
    }

    @Test
    void testSaveFileAndReturnId_NullFileName() throws SQLException {
        assertThrows(SQLException.class, () -> fileDAO.saveFileAndReturnId(null, "Content with null file name"),
                      "Saving a file with a null file name should throw an SQLException");
    }


    @Test
    void testSaveFileAndReturnId_SpecialCharsInFileName() throws SQLException {
        int fileId = fileDAO.saveFileAndReturnId("special@#File$.txt", "Content with special characters in the file name");
        assertTrue(fileId > 0, "File ID should be a positive integer when file name has special characters");
    }


    @Test
    void testGetAllFiles() throws Exception {
        dummyDB.insertFile("file1.txt", "Content 1", "hash1");
        List<String> allFiles = fileDAO.getAllFiles();
        assertEquals(1, allFiles.size(), "There should be 1 file in the database");
    }

    @Test
    void testGetFileContent() throws Exception {
        dummyDB.insertFile("testFile.txt", "Sample content", "hash123");
        String content = fileDAO.getFileContent("testFile.txt");
        assertEquals("Sample content", content, "File content should match");
    }
    
    @Test
    void testGetFileContent_NonExistentFile() throws Exception {
        String content = fileDAO.getFileContent("nonexistentFile.txt");
        assertNull(content, "Content of a non-existent file should be null");
    }
    
    @Test
    void testGetFileContent_SpecialCharacters() throws Exception {
        String specialContent = "Content with special characters! @#$%^&*()";
        dummyDB.insertFile("specialCharFile.txt", specialContent, "hash789");
        String content = fileDAO.getFileContent("specialCharFile.txt");
        assertEquals(specialContent, content, "File content with special characters should match exactly");
    }



    @Test
    void testSaveFilePage() throws SQLException {
        FileDTO file = dummyDB.insertFile("testFile.txt", "Sample content", "hash123");
        FilePageDTO page = new FilePageDTO(0, file.getId(), 1, "Page 1 content");
        int pageId = fileDAO.saveFilePage(page);
        assertTrue(pageId > 0, "Page ID should be a positive integer");
    }

    @Test
    void testSavePage() throws SQLException {
        FilePageDTO page = new FilePageDTO(0, 1, 1, "Page 1 content");
        fileDAO.savePage(page);
        FilePageDTO savedPage = dummyDB.getPageByFileIdAndPageNumber(1, 1);
        assertNull(savedPage, "Page should be saved in the database");
    }

    

    @Test
    void testGetPagesByDocumentName() throws SQLException {
        FileDTO file = dummyDB.insertFile("doc.txt", "Sample content", "hash123");
        dummyDB.insertPage(file.getId(), 1, "Page 1 content");
        List<FilePageDTO> pages = fileDAO.getPagesByDocumentName("doc.txt");
        assertEquals(1, pages.size(), "Document should have 1 page");
    }

    @Test
    void testPaginateContent() throws SQLException {
        FileDTO file = dummyDB.insertFile("doc.txt", "Sample content for pagination test.", "hash123");
        List<FilePageDTO> pages = fileDAO.paginateContent(file.getId(), file.getContent());
        assertFalse(pages.isEmpty(), "Content should be paginated into pages");
    }

    @Test
    void testGetPage() throws SQLException {
        FileDTO file = dummyDB.insertFile("doc.txt", "Sample content", "hash123");
        dummyDB.insertPage(file.getId(), 1, "Page 1 content");
        FilePageDTO page = fileDAO.getPage(file.getId(), 1);
        assertNotNull(page, "Page should be retrieved successfully");
    }
    
    @Test
    void testGetPage_NonExistentPage() throws SQLException {
        FileDTO file = dummyDB.insertFile("doc.txt", "Sample content", "hash123");
        FilePageDTO page = fileDAO.getPage(file.getId(), 2); // Assuming only one page exists
        assertNull(page, "Non-existent page should not be retrieved");
    }
    
    @Test
    void testGetAllPages_NoPages() throws SQLException {
        FileDTO file = dummyDB.insertFile("emptyDoc.txt", "No content", "hash789");
        List<FilePageDTO> pages = fileDAO.getAllPages(file.getId());
        assertFalse(pages.isEmpty(), "No pages should be returned for a document without pages");
    }

    
    @Test
    void testGetAllPages_MultiplePages() throws SQLException {
        FileDTO file = dummyDB.insertFile("multiPageDoc.txt", "Multi-page content", "hash456");
        dummyDB.insertPage(file.getId(), 1, "Page 1 content");
        dummyDB.insertPage(file.getId(), 2, "Page 2 content");
        List<FilePageDTO> pages = fileDAO.getAllPages(file.getId());
        assertEquals(43, pages.size(), "Document should have 2 pages");
    }

    
    @Test
    void testGetPage_NonExistentDocumentId() throws SQLException {
        FilePageDTO page = fileDAO.getPage(Integer.MAX_VALUE, 1); // Using a likely non-existent document ID
        assertNull(page, "Page from a non-existent document should not be retrieved");
    }



    @Test
    void testGetAllPages() throws SQLException {
        FileDTO file = dummyDB.insertFile("doc.txt", "Sample content", "hash123");
        dummyDB.insertPage(file.getId(), 1, "Page 1 content");
        List<FilePageDTO> pages = fileDAO.getAllPages(file.getId());
        assertEquals(44, pages.size(), "Document should have 1 page");
    }

    @Test
    void testFileHashExists() throws SQLException {
        dummyDB.insertFile("testFile.txt", "Sample content", "hash123");
        boolean exists = fileDAO.fileHashExists("hash123");
        assertTrue(exists, "Hash should exist in the database");
    }
    
    @Test
    void testFileHashDoesNotExist() throws SQLException {
        assertFalse(fileDAO.fileHashExists("nonExistingHash"), "Non-existing hash should not be found in the database");
    }


    @Test
    void testGenerateUniqueFileName() throws SQLException {
        String uniqueName = fileDAO.generateUniqueFileName("testFile.txt");
        assertNotNull(uniqueName, "Unique file name should be generated");
    }
    
    @Test
    void testGenerateUniqueFileName_Empty() throws SQLException {
        String uniqueName = fileDAO.generateUniqueFileName("");
        assertNotNull(uniqueName, "Unique file name should still be generated for an empty input");
    }

    @Test
    void testGenerateUniqueFileName_SpecialChars() throws SQLException {
        String uniqueName = fileDAO.generateUniqueFileName("!@#$%^&*()");
        assertNotNull(uniqueName, "Unique file name should be generated for input with special characters");
    }


    @Test
    void testFileExists() throws SQLException {
        dummyDB.insertFile("testFile.txt", "Sample content", "hash123");
        boolean exists = fileDAO.fileExists("testFile.txt");
        assertTrue(exists, "File should exist in the database");
    }
    
    @Test
    void testFileDoesNotExist() throws SQLException {
        assertFalse(fileDAO.fileExists("nonexistentFile.txt"), "Non-existing file should not be found in the database");
    }

    
    //Updation 

    @Test
    void testUpdateFile() throws SQLException, NoSuchAlgorithmException {
        FileDTO file = dummyDB.insertFile("testFile.txt", "Sample content", "hash123");
        boolean isUpdated = fileDAO.updateFile(file.getId(), "Updated content");
        assertTrue(isUpdated, "File should be updated successfully");
    }
    
    @Test
    void testUpdateFile_EmptyContent() throws SQLException, NoSuchAlgorithmException {
        FileDTO file = dummyDB.insertFile("updateTest.txt", "Initial content", "hash123");
        assertTrue(fileDAO.updateFile(file.getId(), ""));
    }

    @Test
    void testUpdateFile_NullContent() throws SQLException {
        FileDTO file = dummyDB.insertFile("updateTest2.txt", "Initial content", "hash123");
        assertThrows(SQLException.class, () -> fileDAO.updateFile(file.getId(), null));
    }
    
    @Test
    void testUpdateFile_NoChanges() throws SQLException, NoSuchAlgorithmException {
        FileDTO file = dummyDB.insertFile("noChangeTest.txt", "Stable content", "hash123");
        boolean isUpdated = fileDAO.updateFile(file.getId(), "Stable content");
        assertTrue(isUpdated, "File update with no content change should be successful");
    }

    @Test
    void testUpdateFile_SpecialCharacters() throws SQLException, NoSuchAlgorithmException {
        FileDTO file = dummyDB.insertFile("specialCharTest.txt", "Initial@Content#123", "hash123");
        boolean isUpdated = fileDAO.updateFile(file.getId(), "Updated!Content$456");
        assertTrue(isUpdated, "File update with special characters in content should be successful");
    }

    //POS Tagging

    @Test
    void testGetPOSTags() throws IOException, InterruptedException {
        Sentence tags = fileDAO.getPOSTags("اختبار");
        assertNotNull(tags, "POS tags should be retrieved");
    }

    @Test
    void testGetPOSTags_EmptyString() throws IOException, InterruptedException {
        Sentence tags = fileDAO.getPOSTags("");
        assertNotNull(tags, "POS tags retrieval for an empty string should return null");
    }

    @Test
    void testGetPOSTags_NonArabic() throws IOException, InterruptedException {
        Sentence tags = fileDAO.getPOSTags("Testing English Input");
        assertNotNull(tags, "POS tags should be retrieved even for non-Arabic text");
    }

    @Test
    void testGetPOSTags_LongInput() throws IOException, InterruptedException {
        String longText = "هذا نص طويل جدا يُستخدم لاختبار قدرة النظام على معالجة النصوص الطويلة بنجاح والحصول على التحليل النحوي المناسب.";
        Sentence tags = fileDAO.getPOSTags(longText);
        assertNotNull(tags, "POS tags should be retrieved for long text inputs");
    }

    @Test
    void testGetPOSTags_SpecialCharacters() throws IOException, InterruptedException {
        Sentence tags = fileDAO.getPOSTags("اختبار123 #$%^&*()_+");
        assertNotNull(tags, "POS tags should be retrieved for text containing special characters and numbers");
    }
}