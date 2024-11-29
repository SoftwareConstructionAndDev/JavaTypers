package DTO;

import java.sql.Timestamp;

public class FileDTO {

    private int id;                  // Unique identifier for the file
    private String fileName;          // Name of the file
    private String content;           // Content of the file
    private Timestamp createdAt;      // Timestamp of file creation
    private Timestamp updatedAt;      // Timestamp of last update
    private String hashValue;         // Hash value for integrity check

    // Constructor with all fields
    public FileDTO(int id, String fileName, String content, Timestamp createdAt, Timestamp updatedAt, String hashValue) {
        this.id = id;
        this.fileName = fileName;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hashValue = hashValue;
    }

    // Getters and setters for all fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getHashValue() { return hashValue; }
    public void setHashValue(String hashValue) { this.hashValue = hashValue; }

    // Override toString for easy debugging
    @Override
    public String toString() {
        return "FileDTO{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", hashValue='" + hashValue + '\'' +
                '}';
    }
}
