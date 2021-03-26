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
    public static Database instantiateDB(){
        DB.removeInstance();
        DB.setDATABASE("testMinitwit");
        if (System.getProperty("DB_TEST_CONNECTION_STRING") != null) {
            DB.setCONNECTIONSTRING(System.getProperty("DB_TEST_CONNECTION_STRING"));
            DB.setUSER(System.getProperty("DB_USER"));
            DB.setPW(System.getProperty("DB_PASSWORD"));
        }
        DB.dropDB();
        return DB.connectDb().get();
    }
    public static List<String> addUsers(int count) {
        List<String> users = new ArrayList<>();
        while (users.size() < count) {
            var username = generateRandomString(14);
            var email = generateRandomString(14);
            var password1 = generateRandomString(14);
            var rs = UserRepository.addUser(username, email, password1);
            if(rs.isSuccess()) {
                users.add(username);
            }
            if(users.size() % 1000 == 0) {
                System.out.println(count - users.size());
            }
        }
        return users;
    }
    public static void addFollowers(int count, List<String> userNames) {
        Random rand = new Random();
        int countUsers = userNames.size();
        for(int i=0; i<count; i++) {
            var rs = FollowerRepository.followUser(getRandomID(rand, countUsers), userNames.get(getRandomIndex(rand, countUsers)));
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
        Random rand = new Random();
        for(int i=0; i<count; i++) {
            var text = generateRandomString(30);
            var rs = MessageRepository.addMessage(text, getRandomIndex(rand, countUsers));
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
        Random rand = new Random();
        while (i < length) {
            sb.append(seedChars.charAt(rand.nextInt(seedChars.length())));
            i++;
        }
        return sb.toString();
    }
    public static int getRandomID(Random rand, int count){
        return rand.nextInt(count)+1;
    }

    public static int getRandomIndex(Random rand, int count){
        return rand.nextInt(count);
    }
}
