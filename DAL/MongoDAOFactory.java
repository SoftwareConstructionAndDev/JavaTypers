package DAL;

public class MongoDAOFactory implements DAOFactory {
    @Override
    public MongoDAOFactory getFileDAO() {
        return new MongoDAOFactory();  // Use the correct implementation for MongoDB
    }
}
