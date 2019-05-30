package in.geomitra.cart.domain;

import java.util.HashSet;

public class CartMapper {

	public static Cart toEntity(CreateCartRequest request) {
		
		Cart cart = Cart.builder()
					.id(null)
					.name(request.getName())
					.cartItems(new HashSet<CartItem>())
					.build();
		
		double total = 0;
		for(CreateCartItemRequest item : request.getCartItems()) {
			CartItem cartItem = new CartItem();
			cartItem.setId(null);
			cartItem.setName(item.getName());
			cartItem.setPrice(item.getPrice());
			cartItem.setQuantity(item.getQuantity());
			cartItem.setCart(cart);
			cart.getCartItems().add(cartItem);
			
			total += item.getPrice() * item.getQuantity();
		}
		cart.setTotalAmt(total);
		return cart;
	}
}
