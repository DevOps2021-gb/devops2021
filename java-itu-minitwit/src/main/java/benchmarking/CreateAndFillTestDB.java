package benchmarking;

import persistence.DB;
import persistence.FollowerRepository;
import persistence.MessageRepository;
import persistence.UserRepository;

import java.util.Arrays;
import java.util.Random;

import static persistence.DB.setDatabaseParameters;

public class CreateAndFillTestDB {

    private CreateAndFillTestDB(){

    }
    private static Random rand = new java.security.SecureRandom();

    public static void instantiateDB(){
        DB.removeInstance();
        DB.setDatabase("benchmarkMinitwit");
        if (System.getProperty("DB_TEST_CONNECTION_STRING") != null) {
            setDatabaseParameters(System.getProperty("DB_TEST_CONNECTION_STRING"), System.getProperty("DB_USER"), System.getProperty("DB_PASSWORD"));
        }
    }
    public static void addUsers(String[] users) {
        for (String user : users) {
            var email       = generateRandomString(14);
            var password1   = generateRandomString(14);
            var rs = UserRepository.addUser(user, email, password1);
            while (!rs.isSuccess()) {
                rs = UserRepository.addUser(user, email, password1);
            }
        }
    }
    public static void addFollowers(int count, String[] userNames) {
        int countUsers = userNames.length;
        for(int i =0; i<count; i++) {
            var rs = FollowerRepository.followUser(getRandomID(countUsers), userNames[getRandomIndex(countUsers)]);
            while (!rs.isSuccess()) {
                rs = FollowerRepository.followUser(getRandomID(countUsers), userNames[getRandomIndex(countUsers)]);
            }
        }
    }

    public static void addMessages(int count, int countUsers) {
        for(int i =0; i<count; i++) {
            var text = generateRandomString(30);
            var rs = MessageRepository.addMessage(text, getRandomIndex(countUsers));
            while (!rs.isSuccess()) {
                rs = MessageRepository.addMessage(text, getRandomIndex(countUsers));
            }
        }
    }

    private static String generateRandomString(int length) {
        String seedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890 ";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < length) {
            sb.append(seedChars.charAt(rand.nextInt(seedChars.length())));
            i++;
        }
        return sb.toString();
    }
    public static String[] genUsernames(int count) {
        String[] users = new String[count];
        for(int i=0; i<count; i++) {
            users[i] = "paul"+i;
        }
        System.out.println(Arrays.toString(users));
        return users;
    }
    public static int getRandomID(int count){
        return rand.nextInt(count)+1;
    }

    public static int getRandomIndex(int count){
        return rand.nextInt(count);
    }

}
