import java.util.Date;
import java.util.List;

import Model.Follower;
import Model.Tweet;
import Model.User;
import RoP.Result;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.hql.internal.ast.tree.FromClause;

public class Test {
    public static void main(String[] args) {
        DB.setDATABASE("testMinitwit");
        Queries.initDb();
        DB.dropDATABASE();


        /*
        session.beginTransaction();
        User user = new User("Mukesh","Google", "qq");
        session.save(user);
        User user2 = new User("Mukesh","Google", "qq");
        session.save(user2);

        Follower folower = new Follower(0,1);
        session.save(folower);

        System.out.println(user.getId());
        System.out.println(user2.getId());
        System.out.println(folower.getId());

        User rUser = (User) session.get(User.class, 2);
        List<Follower> rFolower = session.createCriteria(Follower.class).add(Restrictions.eq("whoId", 0)).list();
        for (var fol : rFolower) {
            System.out.println(fol);
        }

        System.out.println(rUser);

        session.getTransaction().commit();

        session.close();

         */
        var error = register("user1", "q123", null, null);
        error = register("user1", "q123", null, null);

        var session = DB.connectDb().get();
        session.beginTransaction();
        User userT = new User("user3","email", Hashing.generatePasswordHash("password1"));
        session.save(userT);
        List<User> rUsers = session.createCriteria(User.class).list();
        List<User> rUsers2 = session.createSQLQuery("select * from User").list();
        for(var user: rUsers){
            System.out.println(user);
        }
        session.getTransaction().commit();
    }

    static Result<User> register(String username, String password, String password2, String email){
        if (password2==null) password2 = password;
        if (email==null)     email = username + "@example.com";
        return Queries.register(username, email, password, password2);
    }

}