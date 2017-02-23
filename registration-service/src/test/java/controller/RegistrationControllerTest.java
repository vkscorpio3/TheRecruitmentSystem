package controller;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import registrationapp.RegistrationServiceApplication;
import registrationapp.dao.MysqlUserServiceDao;
import registrationapp.domain.UserManager;
import registrationapp.dto.UserCredentialsDTO;
import registrationapp.entity.CredentialEntity;
import registrationapp.entity.PersonEntity;
import registrationapp.entity.RoleEntity;
import registrationapp.httpResponse.RegistrationResponse;
import registrationapp.inputForm.RegistrationForm;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

/**
 * This class tests the methods of the RegistrationController.
 *
 */

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = RegistrationServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationControllerTest
{

    /**
     * Sets up the test by
     * TODO: Finish this comment
     */
    @BeforeClass
    public static void setup()
    {
        System.setProperty("spring.cloud.config.enabled", "false");
    }

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private UserManager userManager;


    /**
     * Tests the response when sending a HTTP Post with a valid form to the REST API.
     * No exceptions is encountered in the test.
     */
    @Test
    public void registerWithValidRegistrationFormTest()
    {
        doNothing().when(userManager).register(any(RegistrationForm.class));

        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setFirstname("Test");
        registrationForm.setLastname("Testsson");
        registrationForm.setDateOfBirth(new Date(1994, 2, 20));
        registrationForm.setEmail("test@example.com");
        registrationForm.setUsername("test");
        registrationForm.setPassword("password123");

        RegistrationResponse registrationResponse = testRestTemplate.postForObject("/register", registrationForm,
                RegistrationResponse.class);
        assertEquals(HttpStatus.CREATED, registrationResponse.getStatus());
    }

    /**
     * Tests the response when sending a HTTP Post with a non-valid form to the REST API.
     * This tests both the validation of the form and repsonse that is sent.
     */
    @Test
    public void registerWithNonValidRegistrationFormTest()
    {
        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setFirstname("T");
        registrationForm.setLastname("Te");
        registrationForm.setDateOfBirth(null);
        registrationForm.setEmail("testexamplecom");
        registrationForm.setUsername("t");
        registrationForm.setPassword("pwd");

        RegistrationResponse registrationResponse = testRestTemplate.postForObject("/register", registrationForm
                , RegistrationResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, registrationResponse.getStatus());
        assertEquals(6, registrationResponse.getErrorList().size());
    }


    /**
     * Tests if a user can be found on id correctly
     */
    @Test
    public void findUserByIdTest()
    {
        given(userManager.getUserById(5, "sv"))
                .willReturn(new PersonEntity("Test", "Testsson", new Date(1994, 3, 20),
                        "albin@example.com", new RoleEntity("Testrole")));
        PersonEntity personEntity = testRestTemplate.getForObject("/sv/get/by/5", PersonEntity.class);
        assertEquals("Test", personEntity.getFirstName());
    }

    /**
     * Tests if a user can be validated in a correct way
     */
    @Test
    public void validateUserByIdTest()
    {
        given(userManager.validate(5))
                .willReturn(true);

        Boolean bool = testRestTemplate.getForObject("/sv/validate/5", Boolean.class);
        assertEquals(true, bool);
    }

    /**
     * Tests if userids can be found by name in a correct way
     */
    @Test
    public void getUserIdsByNameTest()
    {
        ArrayList<Integer> intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        given(userManager.getUserIdsByName("albin"))
                .willReturn(intList);
        Collection<Integer> users = testRestTemplate.getForObject("/sv/get/users/by/name/albin", Collection.class);
        assertEquals(3, users.size());

    }
    /*
    @Test
    public void getUserAndCredentialsByUsernameTest()
    {
        given(userManager.getUserAndCredentialsByUsername("sv", "testuser"))
                .willReturn(new UserCredentialsDTO(new PersonEntity("test", "testsson", new Date(1994, 3, 20)
                        , "albin@example.com", new RoleEntity("Testroll"))
                        , new CredentialEntity(5, "testuser", "testpassword")));

        UserCredentialsDTO userCredentialsDTO = testRestTemplate.getForObject("/sv/get/usercredentials/by/testuser", UserCredentialsDTO.class);

        assertEquals("test", userCredentialsDTO.getPersonEntity().getFirstName());
        assertEquals(5, (long) userCredentialsDTO.getCredentialEntity().getPersonId());
    }
    */

    /**
     * Tests the response when sending a HTTP Post with a valid form to the REST API.
     * A RuntimeException is encountered in the test.
     */
    @Test
    public void registerWithUncheckedExceptionTest()
    {
        doThrow(new RuntimeException("Unchecked exception")).when(userManager).register(any(RegistrationForm.class));

        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setFirstname("Test");
        registrationForm.setLastname("Testsson");
        registrationForm.setDateOfBirth(new Date(1994, 2, 20));
        registrationForm.setEmail("test@example.com");
        registrationForm.setUsername("test");
        registrationForm.setPassword("password123");

        RegistrationResponse registrationResponse = testRestTemplate.postForObject("/register", registrationForm
                , RegistrationResponse.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, registrationResponse.getStatus());
    }


}
