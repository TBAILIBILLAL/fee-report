package feereport;

/** One student record with the course fee and the amount paid so far. */
public class Student {

    private int rollNo;
    private String name;
    private String email;
    private String course;
    private double fee;
    private double paid;
    private String address;
    private String city;
    private String contactNo;

    public Student(int rollNo, String name, String email, String course, double fee, double paid,
            String address, String city, String contactNo) {
        this.rollNo = rollNo;
        this.name = name;
        this.email = email;
        this.course = course;
        this.fee = fee;
        this.paid = paid;
        this.address = address;
        this.city = city;
        this.contactNo = contactNo;
    }

    public int getRollNo() {
        return rollNo;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCourse() {
        return course;
    }

    public double getFee() {
        return fee;
    }

    public double getPaid() {
        return paid;
    }

    /** The due fee is never stored; it is always fee minus paid. */
    public double getDue() {
        return fee - paid;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getContactNo() {
        return contactNo;
    }
}
