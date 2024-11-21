package DTO;


public class FilePageDTO {

    private int pageId;         // Unique identifier for the page
    private int fileId;         // Foreign key referencing the file
    private int pageNumber;     // Page number within the file
    private String content;     // Content of the page

    // Constructor with all fields
    public FilePageDTO(int pageId, int fileId, int pageNumber, String content) {
        this.pageId = pageId;
        this.fileId = fileId;
        this.pageNumber = pageNumber;
        this.content = content;
    }

    // Getters and setters for all fields
    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }

    public int getFileId() { return fileId; }
    public void setFileId(int fileId) { this.fileId = fileId; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // Override toString for easy debugging
    @Override
    public String toString() {
        return "FilePageDTO{" +
                "pageId=" + pageId +
                ", fileId=" + fileId +
                ", pageNumber=" + pageNumber +
                ", content='" + content + '\'' +
                '}';
    }
}
