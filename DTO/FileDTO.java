package DTO;

//DTO/FileDTO.java


import java.sql.Timestamp;

public class FileDTO {
 private String fileName;
 private String content;
 private String owner;
 private Timestamp createdAt;
 private Timestamp updatedAt;

 // Constructor to initialize all fields
 public FileDTO(String fileName, String content, String owner, Timestamp createdAt, Timestamp updatedAt) {
     this.fileName = fileName;
     this.content = content;
     this.owner = owner;
     this.createdAt = createdAt;
     this.updatedAt = updatedAt;
 }

 // Getter for fileName
 public String getFileName() {
     return fileName;
 }

 // Setter for fileName
 public void setFileName(String fileName) {
     this.fileName = fileName;
 }

 // Getter for content
 public String getContent() {
     return content;
 }

 // Setter for content
 public void setContent(String content) {
     this.content = content;
 }

 // Getter for owner
 public String getOwner() {
     return owner;
 }

 // Setter for owner
 public void setOwner(String owner) {
     this.owner = owner;
 }

 // Getter for createdAt timestamp
 public Timestamp getCreatedAt() {
     return createdAt;
 }

 // Setter for createdAt timestamp
 public void setCreatedAt(Timestamp createdAt) {
     this.createdAt = createdAt;
 }

 // Getter for updatedAt timestamp
 public Timestamp getUpdatedAt() {
     return updatedAt;
 }

 // Setter for updatedAt timestamp
 public void setUpdatedAt(Timestamp updatedAt) {
     this.updatedAt = updatedAt;
 }

 // Method to display file details (optional, for debugging purposes)
 @Override
 public String toString() {
     return "FileDTO{" +
             "fileName='" + fileName + '\'' +
             ", content='" + content + '\'' +
             ", owner='" + owner + '\'' +
             ", createdAt=" + createdAt +
             ", updatedAt=" + updatedAt +
             '}';
 }
}
