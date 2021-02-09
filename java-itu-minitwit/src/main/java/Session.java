import RoP.Result;

import java.sql.Connection;

public class Session {
    Result<Connection> connection;
    User user;

    public Session(Result<Connection> c, User u) {
        connection  = c;
        user        = u;
    }
}
