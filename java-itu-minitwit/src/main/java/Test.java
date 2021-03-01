import java.util.Date;

import Model.User;
import org.hibernate.Session;

public class Test {
    public static void main(String[] args) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        session.beginTransaction();
        User user = new User();

        user.setId(1);
        user.setUsername("Mukesh");
        user.setEmail("Google");
        user.setPwHash("qq");

        session.save(user);
        session.getTransaction().commit();

    }

}