package DAL;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import DAL.*;
import DTO.FileDTO;

public interface DAOFactory {
	    IFileDAO getFileDAO();
}