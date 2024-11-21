package DAL;

import java.nio.file.Path;
import java.util.List;

import DTO.FileDTO;

public interface IFileDAO {
    boolean createFile(String fileName, String content, String owner);
    FileDTO readFile(String fileName);
    boolean updateFile(String fileName, String content, String owner);
    boolean deleteFile(String fileName, String owner);
    List<String> listAllFiles();
    boolean importFile(Path filePath);
    boolean isFileExists(String hashValue);
}
