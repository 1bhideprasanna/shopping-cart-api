package in.geomitra.cart.repository.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import in.geomitra.cart.domain.Cart;
import in.geomitra.cart.domain.CartItem;
import in.geomitra.cart.domain.CartMapper;
import in.geomitra.cart.domain.CartRepository;
import in.geomitra.cart.domain.CartServiceImpl;
import in.geomitra.cart.domain.CreateCartItemRequest;
import in.geomitra.cart.domain.CreateCartRequest;
import in.geomitra.cart.domain.CreateCartResponse;
import in.geomitra.cart.domain.FindCartPage;
import in.geomitra.cart.domain.UpdateCartRequest;
import in.geomitra.cart.domain.UpdateCartResponse;
import in.geomitra.cart.error.InternalServerErrorException;



@RunWith(MockitoJUnitRunner.class)
public class ServiceLevelTest {
	
	@Mock
	private CartRepository cartRepository;
	
	@InjectMocks
	private CartServiceImpl cartService;
	
	
	private CreateCartRequest createCartRequest;
	private CreateCartResponse createCartResponse;
	private CreateCartItemRequest cartItem;
	private Cart cart;
	private CartItem item;
	
	
	@Before
	public void init() {
		
		Set<CartItem> itemList = new HashSet<>();
		item = CartItem.builder()
				.id(UUID.randomUUID())
				.name("Watch")
				.price(6000.00)
				.quantity(1)
				.build();
		itemList.add(item);
		
		cart = Cart.builder()
				.id(UUID.randomUUID())
				.name("Prasanna")
				.cartItems(itemList)
				.totalAmt(6000.00)
				.build();
		
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

		createCartResponse = new CreateCartResponse(UUID.randomUUID(), 61500.0, "Cart Created");
	}
	
	@Test
	public void should_CalculateCorrectTotal_When_ValidDataIsGiven() {
		
		Cart cart = CartMapper.toEntity(createCartRequest);
		when(cartRepository.saveAndFlush(Mockito.any(Cart.class))).thenReturn(cart);
		CreateCartResponse response = cartService.create(createCartRequest);
		
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(cart.getId());
		assertThat(response.getTotalAmt()).isEqualTo(createCartResponse.getTotalAmt());
	}
	
	@Test
	public void should_CreateCart_When_ValidFieldsAreGiven() {
		
		cart = CartMapper.toEntity(createCartRequest);
		when(cartRepository.saveAndFlush(Mockito.any(Cart.class))).thenReturn(cart);
		CreateCartResponse response = cartService.create(createCartRequest);
		
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(cart.getId());
		assertThat(response.getMessage()).isEqualToIgnoringCase("Cart Created.");
	}
	
	@Test
	public void should_UpdateCart_When_ValidFieldsAreGiven() {
		
		UUID id = UUID.randomUUID();
		UpdateCartRequest request = new UpdateCartRequest();
		request.setName("Prasanna");
		
		when(cartRepository.findById(id)).thenReturn(Optional.of(cart));
		when(cartRepository.saveAndFlush(cart)).thenReturn(cart);

		UpdateCartResponse response = cartService.update(request, id);
		assertThat(response.getId()).isEqualTo(cart.getId());
		assertThat(response.getTotalAmt()).isEqualTo(cart.getTotalAmt());
	}
	
	@Test
	public void should_ReturnCart_When_ValidIdIsGiven() {
	
		UUID id = UUID.randomUUID();
		when(cartRepository.findById(id)).thenReturn(Optional.of(cart));
		Optional<Cart> foundCart = cartService.find(id);
		assertThat(foundCart.get().getId()).isEqualTo(cart.getId());
		assertThat(foundCart.get().getName()).isEqualTo(cart.getName());
		assertThat(foundCart.get().getTotalAmt()).isEqualTo(cart.getTotalAmt());
	}
	
	@Test
    public void Should_DeleteItem_When_ValidIdIsGiven() {
		
        UUID id = UUID.randomUUID();
        cartService.delete(id);
        
        verify(cartRepository, times(1)).deleteById(id);
        verifyNoMoreInteractions(cartRepository);
    }
	
	@Test
	public void should_ReturnAllCarts_When_FindAllIsTriggered() {
		
		List<Cart> cartsList = new ArrayList<>();
		cartsList.add(cart);
		
		Pageable pageable = PageRequest.of(0, 20);
        Page<Cart> expectedPage = new PageImpl<>(cartsList, pageable, cartsList.size());
		
        when(cartRepository.findAll(Mockito.any(Pageable.class))).thenReturn(expectedPage);
        
		Page<FindCartPage> foundPage = cartService.findAll(pageable);
		assertThat(foundPage).isNotEmpty();
		assertThat(foundPage).isNotNull();
		assertThat(foundPage.getNumberOfElements()).isEqualTo(1);
        assertThat(foundPage.getTotalElements()).isEqualTo(1);
        assertThat(foundPage.get().findFirst().isPresent()).isTrue();
        FindCartPage cartPage = foundPage.get().findFirst().get();
        assertThat(cartPage.getId()).isEqualTo(cart.getId());
        assertThat(cartPage.getName()).isEqualTo(cart.getName());
        assertThat(cartPage.getTotalAmt()).isEqualTo(cart.getTotalAmt());
	}
	
	@Test
	public void should_ReturnNoCart_When_InvalidIdIsGiven() throws Exception {
		
		UUID id = UUID.randomUUID();
		
		when(cartRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());
		assertThatExceptionOfType(InternalServerErrorException.class).isThrownBy(()-> cartService.find(id));
	}
	
	@Test
	public void should_NotDeleteItem_When_InvalidIdIsGiven() {
		
		UUID id  = UUID.randomUUID();
		doThrow(EmptyResultDataAccessException.class).when(cartRepository).deleteById(id);
		assertThatExceptionOfType(InternalServerErrorException.class).isThrownBy(()-> cartService.delete(id));
		verify(cartRepository, times(1)).deleteById(id);
		verifyNoMoreInteractions(cartRepository);
	}
}
