package Test;

import DTO.FileDTO;
import DTO.FilePageDTO;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DummyDatabase {

    // Singleton instance
    private static DummyDatabase instance;

    // Simulated tables
    private final Map<Integer, FileDTO> filesTable = new HashMap<>(); // file_id -> FileDTO
    private final Map<Integer, List<FilePageDTO>> pagesTable = new HashMap<>(); // file_id -> List<FilePageDTO>

    // Auto-increment ID generators
    private final AtomicInteger fileIdGenerator = new AtomicInteger(1);
    private final AtomicInteger pageIdGenerator = new AtomicInteger(1);

    // Singleton pattern to ensure one instance
    private DummyDatabase() {}

    public static synchronized DummyDatabase getInstance() {
        if (instance == null) {
            instance = new DummyDatabase();
        }
        return instance;
    }

    // ========== FILE TABLE OPERATIONS ==========

    // Insert a new file
    public FileDTO insertFile(String fileName, String content, String hashValue) {
        int fileId = fileIdGenerator.getAndIncrement();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        FileDTO file = new FileDTO(fileId, fileName, content, currentTime, null, hashValue);
        filesTable.put(fileId, file);
        return file;
    }

    // Get a file by ID
    public FileDTO getFileById(int fileId) {
        return filesTable.get(fileId);
    }

    // Get a file by name
    public FileDTO getFileByName(String fileName) {
        return filesTable.values().stream()
                .filter(file -> file.getFileName().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    // Update a file
    public boolean updateFile(int fileId, String content, String hashValue) {
        FileDTO file = filesTable.get(fileId);
        if (file != null) {
            file.setContent(content);
            file.setHashValue(hashValue);
            file.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            return true;
        }
        return false;
    }

    // Delete a file
    public boolean deleteFile(int fileId) {
        if (filesTable.containsKey(fileId)) {
            filesTable.remove(fileId);
            pagesTable.remove(fileId); // Remove associated pages
            return true;
        }
        return false;
    }

    // List all files
    public List<FileDTO> getAllFiles() {
        return new ArrayList<>(filesTable.values());
    }

    // ========== PAGE TABLE OPERATIONS ==========

    // Insert a new page
    public FilePageDTO insertPage(int fileId, int pageNumber, String content) {
        int pageId = pageIdGenerator.getAndIncrement();
        FilePageDTO page = new FilePageDTO(pageId, fileId, pageNumber, content);
        pagesTable.computeIfAbsent(fileId, k -> new ArrayList<>()).add(page);
        return page;
    }

    // Get all pages for a file
    public List<FilePageDTO> getPagesByFileId(int fileId) {
        return pagesTable.getOrDefault(fileId, Collections.emptyList());
    }

    // Get a specific page by file ID and page number
    public FilePageDTO getPageByFileIdAndPageNumber(int fileId, int pageNumber) {
        return pagesTable.getOrDefault(fileId, Collections.emptyList())
                .stream()
                .filter(page -> page.getPageNumber() == pageNumber)
                .findFirst()
                .orElse(null);
    }

    // Delete all pages for a file
    public boolean deletePagesByFileId(int fileId) {
        return pagesTable.remove(fileId) != null;
    }

    // List all pages
    public List<FilePageDTO> getAllPages() {
        List<FilePageDTO> allPages = new ArrayList<>();
        pagesTable.values().forEach(allPages::addAll);
        return allPages;
    }

    // ========== UTILITY METHODS ==========

    // Clear the database (useful for test setup/teardown)
    public void clearDatabase() {
        filesTable.clear();
        pagesTable.clear();
        fileIdGenerator.set(1);
        pageIdGenerator.set(1);
    }
}