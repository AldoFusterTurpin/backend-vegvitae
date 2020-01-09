package com.vegvitae.vegvitae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.hash.Hashing;
import com.vegvitae.vegvitae.model.Product;
import com.vegvitae.vegvitae.model.ProductAdditionalTypeEnum;
import com.vegvitae.vegvitae.model.ProductBaseTypeEnum;
import com.vegvitae.vegvitae.model.SupermarketEnum;
import com.vegvitae.vegvitae.model.User;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ProductTest extends AbstractTest {

  private final String uri = "/api/products";

  @Override
  @BeforeClass
  public void setUp() {
    super.setUp();
  }

  private List<Object> createSimpleUserAndToken()
      throws Exception {
    String localUri = "/api/users";

    List<String> socialMediaLinks = new ArrayList<String>();
    socialMediaLinks.add("www.youtube.com");
    socialMediaLinks.add("www.facebook.com");
    User user = new User("testProducts", "TEst1234", "testProducts@test.com", "I'm just a user test",
        socialMediaLinks);
    String inputJson = super.mapToJson(user);
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(localUri).contentType(
        MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();
    User userResponse = mapFromJson(mvcResult.getResponse().getContentAsString(), User.class);

    List<Object> ret = new ArrayList<>();
    ret.add(user);
    ret.add(userResponse.getToken());
    return ret;
  }

  private boolean isTheSameUser(User u1, User u2) {
    if (u1.getUsername().equals(u2.getUsername()) &&
        Hashing.sha256().hashString(u1.getPassword(), StandardCharsets.UTF_8).toString().equals(
            Hashing.sha256().hashString(u2.getPassword(), StandardCharsets.UTF_8).toString()) &&
        u1.getEmail().equals(u2.getEmail()) &&
        u1.getId().equals(u2.getId()) &&
        u1.getSocialMediaLinks().equals(u2.getSocialMediaLinks()) &&
        u1.getPersonalDescription().equals(u2.getPersonalDescription())) {
      return true;
    } else {
      return false;
    }
  }

  @Test
  public void createProductTest() throws Exception {
    long barcode = 365765L;
    String name = "Guacamole";
    ProductBaseTypeEnum baseType = ProductBaseTypeEnum.NOT_VEGGIE;
    Set<ProductAdditionalTypeEnum> additionalTypes = null;
    Set<SupermarketEnum> supermarketsAvailable = new HashSet<>(Arrays.asList(SupermarketEnum.MERCADONA, SupermarketEnum.ALDI));
    String shop = "My shop";
    String uploaderComment = "I love this Guacamole";
    Double approximatePrice = 3.75;
    List<Object> aux = createSimpleUserAndToken();

    User uploader = (User) aux.get(0);
    String token = (String) aux.get(1);

    Product product = new Product(barcode, name, baseType, additionalTypes, supermarketsAvailable, shop,
        uploader, uploaderComment, approximatePrice);

    String inputJson = super.mapToJson(product);

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).header("token", token).contentType(
        MediaType.APPLICATION_JSON).content(inputJson)).andReturn();

    assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());

    Product productResponse = mapFromJson(mvcResult.getResponse().getContentAsString(), Product.class);
    assertEquals(product.getBarcode(), productResponse.getBarcode());
    assertEquals(product.getName(), productResponse.getName());
    assertEquals(product.getBaseType(), productResponse.getBaseType());
    assertEquals(product.getAdditionalTypes(), productResponse.getAdditionalTypes());
    assertEquals(product.getSupermarketsAvailable(), productResponse.getSupermarketsAvailable());
    assertEquals(product.getShop(), productResponse.getShop());
    assertEquals(product.getApproximatePrice(), productResponse.getApproximatePrice());
    //assertTrue(isTheSameUser(product.getUploader(), productResponse.getUploader()));
  }
}
