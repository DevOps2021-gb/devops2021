package benchmarking;

import persistence.FollowerRepository;
import persistence.MessageRepository;
import persistence.UserRepository;

import java.util.List;
import java.util.Random;

public class DBBenchmarkableFunctions {
    private static Random rand = new java.security.SecureRandom();
    private static int getRandomIndex(int count){
        return CreateAndFillTestDB.getRandomIndex(count);
    }
    private static int getRandomID(int count){
        return CreateAndFillTestDB.getRandomID(count);
    }

    public static int runGetUserId(int sizeUsers, String[] usernames){
        return UserRepository.getUserId(usernames[rand.nextInt(sizeUsers)]).get();
    }
    public static int runGetUser(int sizeUsers, String[] usernames){
        return UserRepository.getUser(usernames[rand.nextInt(sizeUsers)]).get().id;
    }
    public static int runGetUserById(int sizeUsers){
        return UserRepository.getUserById(rand.nextInt(sizeUsers)+1).get().id;
    }
    public static int runCountUsers(){
        return Math.toIntExact(UserRepository.countUsers().get());
    }
    public static int runCountMessages(){
        return Math.toIntExact(MessageRepository.countMessages().get());
    }
    public static int runCountFollowers(){
        return Math.toIntExact(FollowerRepository.countFollowers().get());
    }

    public static int runPublicTimeline(){
        return MessageRepository.publicTimeline().get().size();
    }
    public static int runTweetsByUsername(int sizeUsers, String[] usernames){
        return MessageRepository.getTweetsByUsername(usernames[getRandomIndex(sizeUsers)]).get().size();
    }
    public static int runPersonalTweetsById(int sizeUsers){
        return MessageRepository.getPersonalTweetsById(getRandomID(sizeUsers)).get().size();
    }
}
