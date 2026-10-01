package feereport;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Checks the database layer (table creation, logins, accountants, students, due fee)
 * against a temporary database file. Run it with test.bat; it needs no test framework.
 * The program exits with code 1 if any check fails.
 */
public class DataLayerTest {

    private static int failures = 0;

    public static void main(String[] args) throws Exception {
        // must be set before the Database class is first used
        File db = File.createTempFile("feereport-test", ".db");
        db.deleteOnExit();
        System.setProperty("feereport.db", db.getAbsolutePath());

        Database.initialize();
        Database.initialize(); // a second start must not create a second admin

        testAdminLogin();
        testAccountants();
        testStudents();
        testDeletingAccountantKeepsStudents();

        System.out.println(failures == 0 ? "ALL CHECKS PASSED" : failures + " CHECK(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void testAdminLogin() throws SQLException {
        AdminDao dao = new AdminDao();
        check("default admin can log in", "Administrator".equals(dao.login("admin", "admin123")));
        check("wrong admin password is refused", dao.login("admin", "wrong") == null);
        check("unknown admin is refused", dao.login("nobody", "admin123") == null);

        try (Connection con = Database.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*), MAX(password) FROM admin")) {
            rs.next();
            check("there is exactly one admin", rs.getInt(1) == 1);
            check("the admin password is stored as a hash", !rs.getString(2).contains("admin123"));
        }
    }

    private static void testAccountants() throws SQLException {
        AccountantDao dao = new AccountantDao();
        dao.insert(new Accountant(0, "Li Wei", "liwei", "li.wei@example.edu", "028-6183-0101"), "liwei2026");
        dao.insert(new Accountant(0, "Amina Yusuf", "amina", "amina@example.edu", "028-6183-0102"), "amina2026");

        List<Accountant> all = dao.findAll();
        check("two accountants are stored", all.size() == 2);

        Accountant li = dao.login("liwei", "liwei2026");
        check("accountant can log in", li != null && "Li Wei".equals(li.getName()));
        check("wrong accountant password is refused", dao.login("liwei", "nope") == null);
        check("a taken username is detected", dao.usernameTaken("amina", 0));
        check("an accountant's own username is not reported as taken",
                !dao.usernameTaken("amina", all.get(1).getId()));

        boolean refused = false;
        try {
            dao.insert(new Accountant(0, "Other", "liwei", "other@example.edu", "1234567"), "secret12");
        } catch (SQLException e) {
            refused = true;
        }
        check("the database refuses a duplicate username", refused);

        Accountant amina = all.get(1);
        dao.update(new Accountant(amina.getId(), "Amina Yusuf", "amina", "amina@example.edu", "028-0000-0000"), null);
        Accountant updated = dao.login("amina", "amina2026");
        check("an update without a new password keeps the old one", updated != null);
        check("an update changes the details", updated != null && "028-0000-0000".equals(updated.getContactNo()));

        dao.update(new Accountant(amina.getId(), "Amina Yusuf", "amina", "amina@example.edu", "028-0000-0000"), "newpass1");
        check("an update with a new password changes it",
                dao.login("amina", "newpass1") != null && dao.login("amina", "amina2026") == null);
    }

    private static void testStudents() throws SQLException {
        int accountantId = new AccountantDao().login("liwei", "liwei2026").getId();
        StudentDao dao = new StudentDao();
        dao.insert(new Student(0, "Kwame Mensah", "kwame@example.com", "Software Engineering",
                24000, 24000, "12 Jianshe Road", "Chengdu", "138-0013-8001"), accountantId);
        dao.insert(new Student(0, "Fatima Noor", "fatima@example.com", "Computer Science",
                26000, 13000, "4 Campus Road", "Chengdu", "138-0013-8002"), accountantId);
        dao.insert(new Student(0, "Chen Jing", "", "Data Science",
                28000, 0, "", "", "138-0013-8003"), accountantId);

        check("three students are stored", dao.find("", false).size() == 3);
        check("search by part of the name", dao.find("chen", false).size() == 1);
        check("search by course", dao.find("Science", false).size() == 2);
        check("search by roll number", dao.find("2", false).size() == 1
                && "Fatima Noor".equals(dao.find("2", false).get(0).getName()));
        check("a search without a match returns nothing", dao.find("zzzz", false).isEmpty());

        List<Student> due = dao.find("", true);
        double totalDue = 0;
        for (Student s : due) {
            totalDue += s.getDue();
        }
        check("only students with a due fee are in the due list", due.size() == 2);
        check("the total due fee is 41000", Math.abs(totalDue - 41000) < 0.001);

        Student fatima = dao.find("Fatima", false).get(0);
        dao.update(new Student(fatima.getRollNo(), fatima.getName(), fatima.getEmail(), fatima.getCourse(),
                26000, 26000, fatima.getAddress(), fatima.getCity(), fatima.getContactNo()));
        check("paying the full fee removes the student from the due list", dao.find("Fatima", true).isEmpty());

        dao.delete(fatima.getRollNo());
        check("a deleted student is gone", dao.find("Fatima", false).isEmpty());
    }

    private static void testDeletingAccountantKeepsStudents() throws SQLException {
        AccountantDao accountants = new AccountantDao();
        StudentDao students = new StudentDao();
        int before = students.find("", false).size();

        accountants.delete(accountants.login("liwei", "liwei2026").getId());
        check("a deleted accountant cannot log in", accountants.login("liwei", "liwei2026") == null);
        check("the students of a deleted accountant are kept", students.find("", false).size() == before);
    }

    private static void check(String what, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + what);
        if (!ok) {
            failures++;
        }
    }
}
