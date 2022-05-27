package safetyfirst.androidapp.safetyfirstcontroller.Model;

import java.util.regex.Pattern;

public class EmergencyContact {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private long phoneNumber;


    public EmergencyContact(int id, String first_name, String last_name, long phone_number, String email) {
        this.id = id;
        this.firstName = first_name;
        this.lastName = last_name;
        this.email = email;
        this.phoneNumber = phone_number;
    }

    public EmergencyContact() {
    }

    @Override
    public String toString() {
        return
                "Name: " + firstName + " " + lastName + "\n" +
                "Email: " + email + "\n" +
                "Phone number: " + phoneNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirst_name() {
        return firstName;
    }

    public void setFirst_name(String first_name) {
        this.firstName = first_name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static boolean patternMatches(String emailAddress, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}
