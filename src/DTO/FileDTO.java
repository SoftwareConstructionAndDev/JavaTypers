package DTO;

import java.sql.Timestamp;

public class FileDTO {

    private int id;
    private String fileName;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String hashValue;

    public FileDTO(int id, String fileName, String content, Timestamp createdAt, Timestamp updatedAt, String hashValue) {
        this.id = id;
        this.fileName = fileName;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hashValue = hashValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

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
