package benchmarking;

import com.dieselpoint.norm.Database;
import persistence.DB;
import persistence.FollowerRepository;
import persistence.MessageRepository;
import persistence.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreateAndFillTestDB {
    private static Random rand = new java.security.SecureRandom();

    public static void instantiateDB(){
        DB.removeInstance();
        DB.setDATABASE("benchmarkMinitwit");
        if (System.getProperty("DB_TEST_CONNECTION_STRING") != null) {
            DB.setCONNECTIONSTRING(System.getProperty("DB_TEST_CONNECTION_STRING"));
            DB.setUSER(System.getProperty("DB_USER"));
            DB.setPW(System.getProperty("DB_PASSWORD"));
        }
    }
    public static void addUsers(int count, String[] users) {
        for(int i=0; i<count; i++) {
            var email       = generateRandomString(14);
            var password1   = generateRandomString(14);
            var rs = UserRepository.addUser(users[i], email, password1);
            if(!rs.isSuccess()) {
                i--;
                continue;
            }
            if(i % 1000 == 0) {
                System.out.println(count - i);
            }
        }
    }
    public static void addFollowers(int count, String[] userNames) {
        int countUsers = userNames.length;
        for(int i=0; i<count; i++) {
            var rs = FollowerRepository.followUser(getRandomID(countUsers), userNames[getRandomIndex(countUsers)]);
            if(!rs.isSuccess()) {
                i--;
                continue;
            }
            if(i % 1000 == 0) {
                System.out.println(count - i);
            }
        }
    }

    public static void addMessages(int count, int countUsers) {
        for(int i=0; i<count; i++) {
            var text = generateRandomString(30);
            var rs = MessageRepository.addMessage(text, getRandomIndex(countUsers));
            if(!rs.isSuccess()) {
                i--;
                continue;
            }
            if(i % 1000 == 0) {
                System.out.println(count - i);
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
        return users;
    }
    public static int getRandomID(int count){
        return rand.nextInt(count)+1;
    }

    public static int getRandomIndex(int count){
        return rand.nextInt(count);
    }

}
