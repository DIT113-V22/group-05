package safetyfirst.androidapp.safetyfirstcontroller.Model;

import static org.junit.Assert.*;
import safetyfirst.androidapp.safetyfirstcontroller.Model.EmergencyContact;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.regex.Pattern;

public class EmergencyContactTest {

    EmergencyContact testContact;


    @Before
    public void setUp(){
        int id = 1;
        String firstName = "John";
        String lastName = "John";
        long phoneNumber = 1321231313;
        String email = "john.doe@gmail.com";
        testContact = new EmergencyContact(id, firstName, lastName, phoneNumber, email);

    }

    //Testing for correct email input.
    @Test
    public void testUsingSimpleRegex() {

        String regexPattern = "^(.+)@(\\S+)$";
        assertTrue(EmergencyContact.patternMatches(testContact.getEmail(), regexPattern));
    }

}