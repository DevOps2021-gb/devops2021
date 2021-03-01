import java.util.Date;
import java.util.List;

import Model.Follower;
import Model.Tweet;
import Model.User;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.hql.internal.ast.tree.FromClause;

public class Test {
    public static void main(String[] args) {
        Session session = DB.connectDb().get();

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

    }

}