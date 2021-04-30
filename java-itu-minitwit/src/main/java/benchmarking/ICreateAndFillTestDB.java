package benchmarking;

public interface ICreateAndFillTestDB {
    void instantiateDB();
    void addUsers(String[] users);
    void addFollowers(int count, String[] userNames);
    void addMessages(int count, int countUsers);
    String[] genUsernames(int count);
}
