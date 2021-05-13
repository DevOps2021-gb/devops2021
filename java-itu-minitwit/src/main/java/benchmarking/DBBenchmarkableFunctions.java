package benchmarking;

import repository.*;

import java.util.Random;

public class DBBenchmarkableFunctions implements IDBBenchmarkableFunctions {

    private final IUserRepository userRepository;
    private final IMessageRepository messageRepository;
    private final IFollowerRepository followerRepository;

    public DBBenchmarkableFunctions(IUserRepository userRepository, IMessageRepository messageRepository, IFollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.followerRepository = followerRepository;
    }

    private static Random rand = new java.security.SecureRandom();

    public int runGetUserId(int sizeUsers, String[] usernames) {
        return userRepository.getUserId(usernames[getRandomIndex(sizeUsers)]).get();
    }
    public int runGetUser(int sizeUsers, String[] usernames) {
        return userRepository.getUser(usernames[getRandomIndex(sizeUsers)]).get().id;
    }
    public int runGetUserById(int sizeUsers) {
        return userRepository.getUserById(rand.nextInt(sizeUsers)+1).get().id;
    }
    public int runCountUsers() {
        return Math.toIntExact(userRepository.countUsers().get());
    }
    public int runCountMessages() {
        return Math.toIntExact(messageRepository.countMessages().get());
    }
    public int runCountFollowers() {
        return Math.toIntExact(followerRepository.countFollowers().get());
    }

    public int runPublicTimeline() {
        return messageRepository.publicTimeline().get().size();
    }
    public int runTweetsByUsername(int sizeUsers, String[] usernames) {
        return messageRepository.getTweetsByUsername(usernames[getRandomIndex(sizeUsers)]).get().size();
    }
    public int runPersonalTweetsById(int sizeUsers){
        return messageRepository.getPersonalTweetsById(getRandomID(sizeUsers)).get().size();
    }

    private int getRandomID(int count){
        return rand.nextInt(count)+1;
    }

    private int getRandomIndex(int count) {
        return rand.nextInt(count);
    }
}
