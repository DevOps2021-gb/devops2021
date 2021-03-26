package benchmarking;

import persistence.FollowerRepository;
import persistence.MessageRepository;
import persistence.UserRepository;

import java.util.List;
import java.util.Random;

public class DBBenchmarkableFunctions {
    private static int getRandomIndex(Random rand, int count){
        return CreateAndFillTestDB.getRandomIndex(rand, count);
    }
    private static int getRandomID(Random rand, int count){
        return CreateAndFillTestDB.getRandomID(rand, count);
    }

    public static int runGetUserId(Random rand, int sizeUsers, String[] usernames){
        return UserRepository.getUserId(usernames[rand.nextInt(sizeUsers)]).get();
    }
    public static int runGetUser(Random rand, int sizeUsers, String[] usernames){
        return UserRepository.getUser(usernames[rand.nextInt(sizeUsers)]).get().id;
    }
    public static int runGetUserById(Random rand, int sizeUsers){
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
    public static int runTweetsByUsername(Random rand, int sizeUsers, String[] usernames){
        return MessageRepository.getTweetsByUsername(usernames[getRandomIndex(rand, sizeUsers)]).get().size();
    }
    public static int runPersonalTweetsById(Random rand, int sizeUsers){
        return MessageRepository.getPersonalTweetsById(getRandomID(rand, sizeUsers)).get().size();
    }
}
