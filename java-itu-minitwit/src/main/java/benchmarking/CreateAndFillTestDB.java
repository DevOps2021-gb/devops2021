package benchmarking;

import repository.*;

import java.util.Random;

import static repository.DB.setDatabaseParameters;

public class CreateAndFillTestDB implements ICreateAndFillTestDB{

    private final IUserRepository userRepository;
    private final IFollowerRepository followerRepository;
    private final IMessageRepository messageRepository;

    public CreateAndFillTestDB(IUserRepository _userRepository, IFollowerRepository _followerRepository, IMessageRepository _messageRepository){
        userRepository = _userRepository;
        followerRepository = _followerRepository;
        messageRepository = _messageRepository;
    }
    private static Random rand = new java.security.SecureRandom();

    public void instantiateDB(){
        DB.removeInstance();
        DB.setDatabase("benchmarkMinitwit");
        if (System.getProperty("DB_TEST_CONNECTION_STRING") != null) {
            setDatabaseParameters(System.getProperty("DB_TEST_CONNECTION_STRING"), System.getProperty("DB_USER"), System.getProperty("DB_PASSWORD"));
        }
    }
    public void addUsers(String[] users) {
        for (String user : users) {
            var email       = generateRandomString(14);
            var password1   = generateRandomString(14);
            var rs = userRepository.addUser(user, email, password1);
            while (!rs.isSuccess()) {
                rs = userRepository.addUser(user, email, password1);
            }
        }
    }
    public void addFollowers(int count, String[] userNames) {
        int countUsers = userNames.length;
        for(int i =0; i<count; i++) {
            var rs = followerRepository.followUser(getRandomID(countUsers), userNames[getRandomIndex(countUsers)]);
            while (!rs.isSuccess()) {
                rs = followerRepository.followUser(getRandomID(countUsers), userNames[getRandomIndex(countUsers)]);
            }
        }
    }

    public void addMessages(int count, int countUsers) {
        for(int i =0; i<count; i++) {
            var text = generateRandomString(30);
            var rs = messageRepository.addMessage(text, getRandomIndex(countUsers));
            while (!rs.isSuccess()) {
                rs = messageRepository.addMessage(text, getRandomIndex(countUsers));
            }
        }
    }

    private String generateRandomString(int length) {
        String seedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890 ";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < length) {
            sb.append(seedChars.charAt(rand.nextInt(seedChars.length())));
            i++;
        }
        return sb.toString();
    }
    public String[] genUsernames(int count) {
        String[] users = new String[count];
        for(int i=0; i<count; i++) {
            users[i] = "paul"+i;
        }
        return users;
    }

    private int getRandomID(int count){
        return rand.nextInt(count)+1;
    }

    private int getRandomIndex(int count) {
        return rand.nextInt(count);
    }
}