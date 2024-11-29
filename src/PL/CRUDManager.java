package PL;

import BL.FileBL;
import DTO.FileDTO;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

public class CRUDManager {
    private final FileBL fileBL;

    public CRUDManager() throws Exception {
        fileBL = new FileBL();
    }

    public List<FileDTO> loadFilesFromDB() throws SQLException {
        return fileBL.listAllFiles();
    }

    public boolean saveFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
        return fileBL.createFile(fileName, content);
    }

    public boolean updateFile(String fileName, String content) throws SQLException, NoSuchAlgorithmException {
        return fileBL.updateFile(fileName, content);
    }

    public boolean deleteFile(String fileName) throws SQLException {
        return fileBL.deleteFile(fileName);
    }

    public FileDTO readFile(String fileName) throws SQLException {
        return fileBL.readFile(fileName);
    }

    public List<FileDTO> searchFilesByContent(String query) throws SQLException {
        return fileBL.searchFilesByContent(query);
    }
}
