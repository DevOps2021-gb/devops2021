package benchmarking;

public interface IDBBenchmarkableFunctions {
    int runGetUserId(int sizeUsers, String[] usernames);
    int runGetUser(int sizeUsers, String[] usernames);
    int runGetUserById(int sizeUsers);
    int runCountUsers();
    int runCountMessages();
    int runCountFollowers();
    int runPublicTimeline();
    int runTweetsByUsername(int sizeUsers, String[] usernames);
    int runPersonalTweetsById(int sizeUsers);
}
