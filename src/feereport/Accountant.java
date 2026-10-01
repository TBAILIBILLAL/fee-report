package feereport;

/** One accountant account, created and managed by the admin. */
public class Accountant {

    private int id;
    private String name;
    private String username;
    private String email;
    private String contactNo;

    public Accountant(int id, String name, String username, String email, String contactNo) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.contactNo = contactNo;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNo() {
        return contactNo;
    }
}
