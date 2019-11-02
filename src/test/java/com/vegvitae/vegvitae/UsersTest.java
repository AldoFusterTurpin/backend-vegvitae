package com.vegvitae.vegvitae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.hash.Hashing;
import com.vegvitae.vegvitae.exceptions.ExceptionMessages;
import com.vegvitae.vegvitae.model.User;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UsersTest extends AbstractTest {

  private final String uri = "/api/users";

  @Override
  @BeforeClass
  public void setUp() {
    super.setUp();
  }

  @Test
  public void createUserTest() throws Exception {
    List<String> socialMediaLinks = new ArrayList<String>();
    socialMediaLinks.add("www.youtube.com");
    socialMediaLinks.add("www.facebook.com");
    User user = new User("test", "TEst1234", "test@test.com", "I'm just a user test",
        socialMediaLinks);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus());
    User userResponse = mapFromJson(mvcResult.getResponse().getContentAsString(), User.class);
    assertEquals(user.getUsername(), userResponse.getUsername());
    assertEquals(Hashing.sha256().hashString(user.getPassword(), StandardCharsets.UTF_8)
        .toString(), userResponse.getPassword());
    assertEquals(user.getEmail(), userResponse.getEmail());
    assertTrue(userResponse.getId() > 0);
    assertTrue(userResponse.getSocialMediaLinks().size() == 2);
    assertEquals("www.youtube.com", userResponse.getSocialMediaLinks().get(0));
    assertEquals("www.facebook.com", userResponse.getSocialMediaLinks().get(1));
    assertEquals(user.getPersonalDescription(), userResponse.getPersonalDescription());
  }

  @Test(dependsOnMethods = "createUserTest")
  public void duplicateUsernameTest() throws Exception {
    User user = new User("test", "TEst1234", "testNew@test.com", "I'm just a user test", null);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    assertEquals(ExceptionMessages.USERNAME_IN_USE.getErrorMessage(),
        mvcResult.getResponse().getErrorMessage());
  }

  @Test(dependsOnMethods = "createUserTest")
  public void duplicateEmailTest() throws Exception {
    User user = new User("test1234", "TEst1234", "test@test.com", "I'm just a user test", null);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    assertEquals(ExceptionMessages.EMAIL_IN_USE.getErrorMessage(),
        mvcResult.getResponse().getErrorMessage());
  }

  @Test
  public void getUsersTest() throws Exception {
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE)).andReturn();
    assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    /*
    For now, it will just check that it's an OK response, as i don't know how to get the unique user that it's returing
    as I can't map to object the Resources<Resource<User>>
     */
  }

  @Test(dependsOnMethods = {"duplicateEmailTest", "duplicateUsernameTest", "loginTest"})
  public void editUserTest() throws Exception {
    List<String> socialMediaLinks = new ArrayList<String>();
    socialMediaLinks.add("www.twitter.com");
    User user = new User("testEdit", "TEst1234Edit", "testEdit@testEdit.com",
        "I'm just a user test edit", socialMediaLinks);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put(uri + "/1").contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    User userResponse = mapFromJson(mvcResult.getResponse().getContentAsString(), User.class);
    assertEquals(user.getUsername(), userResponse.getUsername());
    assertEquals(Hashing.sha256().hashString(user.getPassword(), StandardCharsets.UTF_8)
        .toString(), userResponse.getPassword());
    assertEquals(user.getEmail(), userResponse.getEmail());
    assertTrue(userResponse.getId() > 0);
    assertTrue(userResponse.getSocialMediaLinks().size() == 1);
    assertEquals("www.twitter.com", userResponse.getSocialMediaLinks().get(0));
    assertEquals(user.getPersonalDescription(), userResponse.getPersonalDescription());
  }

  @Test(dependsOnMethods = "editUserTest")
  public void deleteUserTest() throws Exception {
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.delete(uri + "/1")).andReturn();
    assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
  }

  @Test
  public void deleteNonExistingUserTest() {
    boolean notfound = false;
    try {
      MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.delete(uri + "/69")).andReturn();
    } catch (Exception e) {
      //I catch the exception to avoid to propage it(weird behavior)
      notfound = true;
    }
    assertTrue(notfound);
  }

  @Test
  public void createUserWithoutUsernameTest() throws Exception {
    User user = new User(null, "testEdit", "testEdit@testEdit.com",
        "I'm just a user test edit", null);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
  }

  @Test
  public void createUserWithoutPasswordTest() throws Exception {
    User user = new User("test", null, "testEdit@testEdit.com",
        "I'm just a user test edit", null);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
  }

  @Test
  public void createUserWithoutEmailTest() throws Exception {
    User user = new User("test", "test1234ABC", null,
        "I'm just a user test edit", null);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
  }

  @Test
  public void createUserWithInvalidPasswordTest() throws Exception {
    User user = new User("InvalidPasswordUser", "xd", "InvalidPasswordUser@test.com",
        "I'm just a user test edit", null);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    assertEquals(ExceptionMessages.INVALID_PASSWORD.getErrorMessage(),
        mvcResult.getResponse().getErrorMessage());
  }

  @Test
  public void createUsertWithLongDescriptionTest() throws Exception {
    String init = "hello";
    for (int i = 0; i < 10; ++i) {
      init += init;
    }
    User user = new User("DescTooLong", "TEst1234", "longdesc@test.com",
        init, null);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    assertEquals(ExceptionMessages.INVALID_DESCRIPTION.getErrorMessage(),
        mvcResult.getResponse().getErrorMessage());
  }

  @Test
  public void createUserWithMoreThan4Links() throws Exception {
    List<String> socialMediaLinks = new ArrayList<>();
    socialMediaLinks.add("FirstLink");
    socialMediaLinks.add("SecondLink");
    socialMediaLinks.add("ThirdLink");
    socialMediaLinks.add("FourthLink");
    socialMediaLinks.add("FifthLink");
    socialMediaLinks.add("SixthLink");
    User user = new User("TooMuchLinks", "TEst1234", "toomuchlinks@test.com",
        "I'm just a user test edit", socialMediaLinks);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    assertEquals(ExceptionMessages.INVALID_SOCIAL_MEDIA_LINKS_LENGTH.getErrorMessage(),
        mvcResult.getResponse().getErrorMessage());
  }

  @Test(dependsOnMethods = "createUserTest")
  public void loginTest() throws Exception {
    String username = "test";
    String password = "TEst1234";
    JSONObject inputJson = new JSONObject();
    inputJson.put("username", username);
    inputJson.put("password", password);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri + "/login").contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson.toString())).andReturn();
    assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
  }

  @Test
  public void loginIncorrectTest() throws Exception {
    String username = "notExists";
    String password = "notExists";
    JSONObject inputJson = new JSONObject();
    inputJson.put("username", username);
    inputJson.put("password", password);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri + "/login").contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson.toString())).andReturn();
    assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    assertEquals(ExceptionMessages.INVALID_CREDENTIALS.getErrorMessage(),
        mvcResult.getResponse().getErrorMessage());
  }

  @Test
  public void loginWithoutUsernameTest() throws Exception {
    String password = "notExists";
    JSONObject inputJson = new JSONObject();
    inputJson.put("password", password);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri + "/login").contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson.toString())).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    assertEquals(ExceptionMessages.NULL_USERNAME.getErrorMessage(),
        mvcResult.getResponse().getErrorMessage());
  }

  @Test
  public void loginWithoutPasswordTest() throws Exception {
    String username = "notExists";
    JSONObject inputJson = new JSONObject();
    inputJson.put("username", username);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri + "/login").contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson.toString())).andReturn();
    assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    assertEquals(ExceptionMessages.NULL_PASSWORD.getErrorMessage(),
        mvcResult.getResponse().getErrorMessage());
  }

}
