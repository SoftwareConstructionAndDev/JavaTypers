public class FileDAOManualTestRunner {

    private static FileDAO fileDAO;

    public static void main(String[] args) throws Exception {
        setUp();

        // Run test cases
        testCreateFile();
        testReadFile();
        testDeleteFile();
        testListAllFiles();
        testUpdateFile();
        testSearchFiles();
        testPaginateContent();

        tearDown();
    }

    public static void setUp() {
        fileDAO = new FileDAO();
        MockDatabase.files.clear(); // Clear mock database before running tests
        MockDatabase.filePages.clear();
        System.out.println("Setup completed.");
    }

    public static void tearDown() {
        MockDatabase.files.clear(); // Clean up after tests
        MockDatabase.filePages.clear();
        System.out.println("Tests completed.");
    }

    public static void testCreateFile() throws Exception {
        String fileName = "testfile1.txt";
        String content = "This is a test file.";

        boolean result = fileDAO.createFile(fileName, content);
        if (result) {
            System.out.println("testCreateFile: Passed");
        } else {
            System.out.println("testCreateFile: Failed");
        }
    }

    public static void testReadFile() throws Exception {
        String fileName = "testfile2.txt";
        String content = "This is another test file.";

        fileDAO.createFile(fileName, content);
        int fileId = fileDAO.getFileIdByName(fileName);

        FileDTO file = fileDAO.readFile(fileId);
        if (file != null && file.getFileName().equals(fileName) && file.getContent().equals(content)) {
            System.out.println("testReadFile: Passed");
        } else {
            System.out.println("testReadFile: Failed");
        }
    }

    public static void testDeleteFile() throws Exception {
        String fileName = "testfile3.txt";
        String content = "This is a deletable file.";

        fileDAO.createFile(fileName, content);
        boolean deleteResult = fileDAO.deleteFile(fileName);
        int fileId = fileDAO.getFileIdByName(fileName);

        if (deleteResult && fileId == -1) {
            System.out.println("testDeleteFile: Passed");
        } else {
            System.out.println("testDeleteFile: Failed");
        }
    }

    public static void testListAllFiles() throws Exception {
        fileDAO.createFile("file1.txt", "Content 1");
        fileDAO.createFile("file2.txt", "Content 2");

        List<FileDTO> files = fileDAO.listAllFiles();
        if (files.size() == 2) {
            System.out.println("testListAllFiles: Passed");
        } else {
            System.out.println("testListAllFiles: Failed");
        }
    }

    public static void testUpdateFile() throws Exception {
        String fileName = "testfile4.txt";
        String content = "This is the original content.";
        String updatedContent = "This is the updated content.";

        fileDAO.createFile(fileName, content);
        int fileId = fileDAO.getFileIdByName(fileName);

        boolean updateResult = fileDAO.updateFile(fileId, updatedContent);
        FileDTO updatedFile = fileDAO.readFile(fileId);

        if (updateResult && updatedFile.getContent().equals(updatedContent)) {
            System.out.println("testUpdateFile: Passed");
        } else {
            System.out.println("testUpdateFile: Failed");
        }
    }

    public static void testSearchFiles() throws Exception {
        String fileName = "searchfile.txt";
        String content = "This is a searchable file.";

        fileDAO.createFile(fileName, content);

        List<FileDTO> results = fileDAO.searchFiles("searchable");
        if (results.size() > 0 && results.get(0).getFileName().equals(fileName)) {
            System.out.println("testSearchFiles: Passed");
        } else {
            System.out.println("testSearchFiles: Failed");
        }
    }

    public static void testPaginateContent() throws Exception {
        String fileName = "paginatefile.txt";
        String content = "This content will be split into pages.".repeat(200);

        int fileId = fileDAO.saveFileAndReturnId(fileName, content);
        List<FilePageDTO> pages = fileDAO.paginateContent(fileId, content);

        if (pages.size() > 1) {
            System.out.println("testPaginateContent: Passed");
        } else {
            System.out.println("testPaginateContent: Failed");
        }
    }
}
