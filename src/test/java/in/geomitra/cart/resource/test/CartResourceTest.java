package in.geomitra.cart.resource.test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import in.geomitra.cart.domain.Cart;
import in.geomitra.cart.domain.CartItem;
import in.geomitra.cart.domain.CartResource;
import in.geomitra.cart.domain.CartServiceImpl;
import in.geomitra.cart.domain.CreateCartItemRequest;
import in.geomitra.cart.domain.CreateCartRequest;
import in.geomitra.cart.domain.CreateCartResponse;
import in.geomitra.cart.domain.FindCartPage;
import in.geomitra.cart.domain.UpdateCartRequest;
import in.geomitra.cart.domain.UpdateCartResponse;
import in.geomitra.cart.error.InternalServerErrorException;
import in.geomitra.test.util.TestUtil;


@RunWith(SpringRunner.class)
@WebMvcTest(value = CartResource.class)
public class CartResourceTest {
	
	@Autowired
	private MockMvc restMvc;
	
	@MockBean
	private CartServiceImpl cartService;
	
	private CreateCartRequest createCartRequest;
	private CreateCartResponse createCartResponse;
	private CreateCartItemRequest cartItem;
	
	
	@Before  
	public void initEach() {
		
		Set<CreateCartItemRequest> listCartItems = new HashSet<CreateCartItemRequest>();
		cartItem = CreateCartItemRequest.builder()
				   .name("Mobile")
				   .price(60000.00)	
				   .quantity(1)
				   .build();
		listCartItems.add(cartItem);
		
		
		  cartItem = CreateCartItemRequest.builder()
				  	.name("HeadPhones")
				  	.price(1500.00)
				  	.quantity(1)
				  	.build(); 
		  listCartItems.add(cartItem);
		 
		
		createCartRequest = CreateCartRequest.builder()
							.name("Prasanna Bhide")			
							.cartItems(listCartItems)
							.build();
		createCartResponse = new CreateCartResponse(UUID.randomUUID(),7500.00,"Cart Created");
	}
	
	@Test
	public void should_CreateCart_When_ValidFieldsAreGiven() throws Exception{
		
		when(cartService.create(Mockito.any(CreateCartRequest.class))).thenReturn(createCartResponse);
		restMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(createCartRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createCartResponse.getId().toString()))
                .andExpect(jsonPath("$.message").value(createCartResponse.getMessage()));
	}
	
	@Test
	public void should_UpdateCart_When_ValidFieldsAreGiven() throws Exception {
		
		UUID id = UUID.randomUUID();
		UpdateCartRequest updateCartRequest = UpdateCartRequest.builder()
				  							  .name("Prasanna")
				  							  .build();
		
		UpdateCartResponse updateCartResponse = UpdateCartResponse.builder()
							.id(id)
							.totalAmt(7500.0)
							.message("Cart Updated")
							.build();
		
		when(cartService.update(Mockito.any(UpdateCartRequest.class),Mockito.any(UUID.class))).thenReturn(updateCartResponse);
		
		restMvc.perform(put("/carts/" + id)
			   .contentType(MediaType.APPLICATION_JSON_UTF8)
			   .content(TestUtil.convertObjectToJsonBytes(updateCartRequest)))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.id").value(updateCartResponse.getId().toString()))
			   .andExpect(jsonPath("$.message").value(updateCartResponse.getMessage()));
	}
	
	@SuppressWarnings("unused")
	@Test
	public void should_NotUpdateCart_When_InvalidFieldsAreGiven() throws Exception {
		
		UUID id  = UUID.randomUUID();
		UpdateCartRequest updateCartRequest = UpdateCartRequest.builder()
				  .name("")
				  .build();
		
		restMvc.perform(put("/carts/" + id)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	
	@Test
	public void should_ReturnCart_When_ValidIdisGiven() throws Exception {
		
		UUID id = UUID.randomUUID();
		Set<CartItem> itemSet = new HashSet<>();
		CartItem item = CartItem.builder()
						.id(UUID.randomUUID())
						.name("Watch")
						.price(1200.0)
						.quantity(1)
						.build();
		itemSet.add(item);
		Cart cart = new Cart();
		cart.setName("Prasanna");
		cart.setTotalAmt(1200.0);
		cart.setId(id);
		cart.setCartItems(itemSet);
		
		when(cartService.find(id)).thenReturn(Optional.of(cart));
		
		restMvc.perform(get("/carts/" + id))
				.andExpect(status().isOk())
        		.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.id").value(cart.getId().toString()))
                .andExpect(jsonPath("$.name").value(cart.getName()))
                .andExpect(jsonPath("$.totalAmt").value(cart.getTotalAmt()))
                .andReturn();
	}
	  
	@Test
	public void should_ReturnAllCarts_When_UrlIsHit() throws Exception {

		Set<CartItem> item = new HashSet<>();

		CartItem cartItems = CartItem.builder()
				.id(UUID.randomUUID())
				.name("Laptop")
				.price(60000.0)
				.quantity(1)
				.build();
	
		item.add(cartItems);

		FindCartPage pageCart = new FindCartPage();
		pageCart.setId(UUID.randomUUID());
		pageCart.setName("Rahul");
		pageCart.setCartItem(item);
		pageCart.setTotalAmt(60000.00);
		List<FindCartPage> page = new ArrayList<>();
		page.add(pageCart);
		Page<FindCartPage> expectedPage = new PageImpl<>(page); 
		
		when(cartService.findAll(Mockito.any(Pageable.class))).thenReturn(expectedPage);
				
		restMvc.perform(get("/carts"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));			   
	}
	
	@Test
	public void should_DeleteCart_When_ValidIdIsGiven() throws Exception {

		UUID id = UUID.randomUUID();	
		
		restMvc.perform(delete("/carts/" + id)
			   .contentType(MediaType.APPLICATION_JSON_UTF8))
			   .andExpect(status().isOk());
		verify(cartService, times(1)).delete(id);
	}
	
	@Test
	@Ignore
	public void should_ThrowBadRequestToFind_When_InvalidIdIsGiven() throws Exception {
		
		UUID id = UUID.randomUUID();
		doThrow(InternalServerErrorException.class).when(cartService).find(id);
		restMvc.perform(get("/carts/" + id))
			   .andExpect(status().isInternalServerError());
	}
	
	@Test
	@Ignore
	public void should_ThrowBadRequestToDelete_When_InvalidIdIsGiven() throws Exception{
		
		UUID id = UUID.randomUUID();
		doThrow(InternalServerErrorException.class).when(cartService).delete(id);
		restMvc.perform(delete("/carts/"  + id))
				.andExpect(status().isInternalServerError());
	}
}
