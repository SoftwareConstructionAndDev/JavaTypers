import DTO.FileDTO;
import DTO.FilePageDTO;

import java.sql.*;
import java.util.*;

public class MockDatabase {

    private static final Map<Integer, FileDTO> files = new HashMap<>();
    private static final Map<Integer, FilePageDTO> filePages = new HashMap<>();
    private static int fileIdCounter = 1;
    private static int pageIdCounter = 1;

    public static Connection getMockConnection() {
        return new MockConnection();
    }

    static class MockConnection extends Connection {
        @Override
        public PreparedStatement prepareStatement(String sql) {
            if (sql.contains("INSERT INTO files") || sql.contains("INSERT INTO pages")) {
                return new MockInsertPreparedStatement();
            } else if (sql.contains("SELECT * FROM files") || sql.contains("SELECT * FROM pages")) {
                return new MockSelectPreparedStatement();
            } else if (sql.contains("DELETE FROM files") || sql.contains("DELETE FROM pages")) {
                return new MockDeletePreparedStatement();
            } else if (sql.contains("UPDATE files") || sql.contains("UPDATE pages")) {
                return new MockUpdatePreparedStatement();
            }
            return null;
        }

        @Override
        public Statement createStatement() {
            return null; // Not implemented for brevity
        }

        @Override
        public void close() {
            // No operation needed for the mock
        }

        // Other methods can throw UnsupportedOperationException for brevity
    }

    static class MockInsertPreparedStatement extends PreparedStatement {
        private String sql;
        private String fileName;
        private String content;
        private int fileId;

        @Override
        public void setString(int parameterIndex, String value) {
            if (parameterIndex == 1) {
                this.fileName = value;
            } else if (parameterIndex == 2) {
                this.content = value;
            }
        }

        @Override
        public void setInt(int parameterIndex, int value) {
            if (parameterIndex == 1) {
                this.fileId = value;
            }
        }

        @Override
        public int executeUpdate() {
            if (sql.contains("INSERT INTO files")) {
                FileDTO file = new FileDTO(fileIdCounter++, fileName, content, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null);
                files.put(file.getId(), file);
            } else if (sql.contains("INSERT INTO pages")) {
                FilePageDTO page = new FilePageDTO(pageIdCounter++, fileId, 1, content);
                filePages.put(page.getPageId(), page);
            }
            return 1; // Simulate one row affected
        }

        // Other methods can remain empty for brevity
    }

    static class MockSelectPreparedStatement extends PreparedStatement {
        private String fileName;
        private int fileId;

        @Override
        public void setString(int parameterIndex, String value) {
            if (parameterIndex == 1) {
                this.fileName = value;
            }
        }

        @Override
        public void setInt(int parameterIndex, int value) {
            if (parameterIndex == 1) {
                this.fileId = value;
            }
        }

        @Override
        public ResultSet executeQuery() {
            return new MockResultSet(fileId, fileName);
        }

        // Other methods can remain empty for brevity
    }

    static class MockDeletePreparedStatement extends PreparedStatement {
        private String fileName;

        @Override
        public void setString(int parameterIndex, String value) {
            if (parameterIndex == 1) {
                this.fileName = value;
            }
        }

        @Override
        public int executeUpdate() {
            Optional<Integer> fileId = files.entrySet().stream()
                    .filter(entry -> entry.getValue().getFileName().equals(fileName))
                    .map(Map.Entry::getKey)
                    .findFirst();
            fileId.ifPresent(files::remove);
            return fileId.isPresent() ? 1 : 0;
        }

        // Other methods can remain empty for brevity
    }

    static class MockUpdatePreparedStatement extends PreparedStatement {
        private int fileId;
        private String newContent;

        @Override
        public void setString(int parameterIndex, String value) {
            if (parameterIndex == 1) {
                this.newContent = value;
            }
        }

        @Override
        public void setInt(int parameterIndex, int value) {
            if (parameterIndex == 3) {
                this.fileId = value;
            }
        }

        @Override
        public int executeUpdate() {
            FileDTO file = files.get(fileId);
            if (file != null) {
                file.setContent(newContent);
                file.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                return 1;
            }
            return 0; // File not found
        }

        // Other methods can remain empty for brevity
    }

    static class MockResultSet extends ResultSet {
        private final Iterator<Map.Entry<Integer, FileDTO>> fileIterator;
        private Map.Entry<Integer, FileDTO> currentEntry;

        public MockResultSet(int fileId, String fileName) {
            if (fileName != null) {
                this.fileIterator = files.entrySet().stream()
                        .filter(entry -> entry.getValue().getFileName().equals(fileName))
                        .iterator();
            } else if (fileId > 0) {
                this.fileIterator = files.entrySet().stream()
                        .filter(entry -> entry.getKey() == fileId)
                        .iterator();
            } else {
                this.fileIterator = files.entrySet().iterator();
            }
        }

        @Override
        public boolean next() {
            if (fileIterator.hasNext()) {
                currentEntry = fileIterator.next();
                return true;
            }
            return false;
        }

        @Override
        public String getString(String columnLabel) {
            if (currentEntry != null) {
                FileDTO file = currentEntry.getValue();
                switch (columnLabel) {
                    case "file_name":
                        return file.getFileName();
                    case "file_content":
                        return file.getContent();
                }
            }
            return null;
        }

        @Override
        public int getInt(String columnLabel) {
            if (currentEntry != null) {
                FileDTO file = currentEntry.getValue();
                switch (columnLabel) {
                    case "id":
                        return file.getId();
                }
            }
            return 0;
        }

        // Other methods can remain empty for brevity
    }
}
