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
        //DB.setDATABASE("testMinitwit");
        Queries.initDb();
        var session = DB.dropDATABASE().get();


        session.beginTransaction();
        User user = new User("Mukesh","Google", "qq");
        session.save(user);
        User user2 = new User("Mukesh","Google", "qq");
        session.save(user2);

        Follower folower = new Follower(user,user2);
        session.save(folower);
        session.save(new Follower(user2,user2));
        session.save(new Follower(user,user));

        System.out.println(user);
        System.out.println(user2);
        System.out.println(folower);

        User rUser = (User) session.get(User.class, 2);

        List<Follower> rFolower = session.createQuery("from Follower f where f.who.id>:whoId").setInteger("whoId", 0).list();
        for (var fol : rFolower) {
            System.out.println(fol);
        }
        System.out.println(rUser);

        session.getTransaction().commit();
        session.close();
        DB.close();
    }

    static Result<User> register(String username, String password, String password2, String email){
        if (password2==null) password2 = password;
        if (email==null)     email = username + "@example.com";
        return Queries.register(username, email, password, password2);
    }

}