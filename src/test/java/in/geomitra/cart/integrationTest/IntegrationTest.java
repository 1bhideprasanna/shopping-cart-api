
package in.geomitra.cart.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import in.geomitra.cart.ShoppingCartAppApplication;
import in.geomitra.cart.domain.Cart;
import in.geomitra.cart.domain.CartItem;
import in.geomitra.cart.domain.CartRepository;
import in.geomitra.cart.domain.CartResource;
import in.geomitra.cart.domain.CartServiceImpl;
import in.geomitra.cart.domain.CreateCartItemRequest;
import in.geomitra.cart.domain.CreateCartRequest;
import in.geomitra.cart.domain.UpdateCartRequest;
import in.geomitra.test.util.TestUtil;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShoppingCartAppApplication.class)
public class IntegrationTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTest.class);

	private MockMvc restMvc;
	
	@Autowired
	private CartServiceImpl cartService;
	
	@Autowired
	private CartRepository cartRepository;
	
	private Cart cart1;
	private Cart saveCart1;
	private Cart cart2;
	private Cart saveCart2;
	
	private CartItem cartItem1;
	private CartItem cartItem2;
	
	private CreateCartRequest cartRequest1;
	private UpdateCartRequest cartRequest2;
	private CreateCartRequest cartRequest3;
	private CreateCartItemRequest itemRequest; 
	
	@Before
	public void init() throws Exception{
		
		MockitoAnnotations.initMocks(this);
		
		CartResource cartResource = new CartResource(cartService);
		
		this.restMvc = MockMvcBuilders.standaloneSetup(cartResource).build();
		
		Set<CreateCartItemRequest> itemRequests = new HashSet<>();
		
		itemRequest = CreateCartItemRequest.builder()
					  .name("Pen")
					  .price(500.00)
					  .quantity(2)
					  .build();
		
		itemRequests.add(itemRequest);
		
		Set<CartItem> cartItems = new HashSet<CartItem>();
		
		
		
		
		cartItem1 = CartItem.builder()
					.id(UUID.randomUUID())
					.name("Watch")
					.price(30000.00)
					.quantity(2)
					.build();
		
		cartItems.add(cartItem1);
		
		cartItem2 = CartItem.builder()
				.id(UUID.randomUUID())
				.name("Laptop")
				.price(50000.00)
				.quantity(1)
				.build();
		
		cartItems.add(cartItem2);
		
		cart1 = Cart.builder()
				.id(UUID.randomUUID())
				.name("Harshad")
				.cartItems(cartItems)
				.totalAmt(110000.00)
				.build();
		
		cart2 = Cart.builder()
				.id(UUID.randomUUID())
				.name("Rahul")
				.cartItems(cartItems)
				.totalAmt(110000.00)
				.build();
		
		cartRequest1 = CreateCartRequest.builder()
					   	.cartItems(itemRequests)
					   	.name("Rahul")
						.build();
		
		cartRequest2 = UpdateCartRequest.builder()
					   .name("Shubham")
					   .build();
		
		cartRequest3 = CreateCartRequest.builder()
						.name("")
						.cartItems(itemRequests)
						.build();
		
		saveCart1 = cartRepository.saveAndFlush(cart1);
		saveCart2 = cartRepository.saveAndFlush(cart2);
		
		LOGGER.info("Cart1 :-- id: " + saveCart1.getId() + ", name: " + saveCart1.getName() + ", totalAmt: " + saveCart1.getTotalAmt());
		LOGGER.info("Cart2 :-- id: " + saveCart2.getId() + ", name: " + saveCart2.getName() + ", totalAmt: " + saveCart2.getTotalAmt());
	}
	
	@Test
	@Transactional
	public void should_CreateItem_When_CartFieldsAreValid() throws Exception{
		
		CreateCartRequest request = cartRequest1;
		MvcResult result = restMvc.perform(post("/carts")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(request)))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Cart created."))
                .andReturn();
		
		JSONObject responseJson = new JSONObject(result.getResponse().getContentAsString());
		String id = (String)responseJson.get("id");
		Double totalAmt = (Double)responseJson.getDouble("totalAmt");
		
		Cart cart = cartRepository.findById(UUID.fromString(id)).get();
		assertThat(cart.getId().toString()).isEqualTo(id.toString());
		assertThat(cart.getTotalAmt()).isEqualTo(totalAmt);
	}
	
	@Test
	@Transactional
	public void should_NotCreateItem_When_InvalidFieldsAreGiven() throws Exception {
		
		CreateCartRequest request = cartRequest3;
		
		restMvc.perform(post("/carts")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(request)))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	@Transactional
	public void should_UpdateCart_When_ValidFieldsAreGiven() throws Exception{
		
		UpdateCartRequest request = cartRequest2;
		MvcResult result = restMvc.perform(put("/carts/" + saveCart2.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(request)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.message").value("Cart Updated."))
				.andReturn();
		JSONObject responseJson = new JSONObject(result.getResponse().getContentAsString());
		String id = (String)responseJson.get("id");
		Double totalAmt = (Double)responseJson.getDouble("totalAmt");
		
		Cart cart = cartRepository.findById(UUID.fromString(id)).get();
		assertThat(cart.getId().toString()).isEqualTo(id.toString());
		assertThat(cart.getTotalAmt()).isEqualTo(totalAmt);
	}
	
	@Test
	@Transactional
	public void should_NotUpdateCart_When_InvalidFieldsAreGiven() throws Exception {
		
		UpdateCartRequest request = new UpdateCartRequest();
		request.setName("");
		restMvc.perform(put("/carts/" + saveCart2.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(request)))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	@Transactional
	public void should_DeleteCart_When_ValidIdISgiven() throws Exception{
		
		UUID id = saveCart1.getId();
		restMvc.perform(delete("/carts/" + id))
			   .andExpect(status().isOk());
		
		Optional<Cart> cart = cartRepository.findById(id);
		assertFalse(cart.isPresent());
	}
}
