package in.geomitra.cart.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import in.geomitra.cart.error.InternalServerErrorException;

@Service
public class CartServiceImpl implements CartService {
	
	
	
	  private CartRepository cartRepository;
	  
	  @Autowired 
	  public CartServiceImpl(CartRepository cartRepository) {
		  this.cartRepository = cartRepository; 
	  }
	  
	  @Override 
	  public CreateCartResponse create(CreateCartRequest request) {
	  
		  Cart cart = CartMapper.toEntity(request);	
		  Cart savedCart = cartRepository.saveAndFlush(cart);
		  CreateCartResponse response = CreateCartResponse.builder()
				  						.id(savedCart.getId()) 
				  						.message("Cart created.")
				  						.totalAmt(savedCart.getTotalAmt()) 
				  						.build();
		  return response; 
	  }
	 
	

	@Override
	public UpdateCartResponse update(UpdateCartRequest request, UUID id) {
						
		Cart cart = cartRepository.findById(id).get();
		if(cart.getId() != null) {
			cart.setName(request.getName());
			Cart saveCart = cartRepository.saveAndFlush(cart);
			
			UpdateCartResponse response = UpdateCartResponse.builder()
											.id(saveCart.getId())
											.totalAmt(saveCart.getTotalAmt())
											.message("Cart Updated.")
											.build();
			return response;
		}
		else {
			throw new InternalError("Cart not present by given ID!");
		}
		
		
	}
	
	@Override
	public Optional<Cart> find(UUID id) {
		
		Optional<Cart> opCart = cartRepository.findById(id);
		opCart.orElseThrow(() -> new InternalServerErrorException("Cart not found error"));
		return opCart;
	}
	
	@Override
	public Page<FindCartPage> findAll(Pageable pageable){
		
		Page<Cart> cartPage = cartRepository.findAll(pageable);
		
		return cartPage.map(Cart ->{
		FindCartPage pageCart = new FindCartPage();
		pageCart.setCartItem(Cart.getCartItems());
		pageCart.setName(Cart.getName());
		pageCart.setTotalAmt(Cart.getTotalAmt());
		pageCart.setId(Cart.getId());
		return pageCart;
		});
	}

	@Override
	public void delete(UUID id){
		try {
			cartRepository.deleteById(id);
		}catch(EmptyResultDataAccessException ex) {
			throw new InternalServerErrorException("Error: Item not found");
		}
	}
}
