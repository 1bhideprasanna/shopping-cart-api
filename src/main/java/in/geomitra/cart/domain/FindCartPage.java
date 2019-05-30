package in.geomitra.cart.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class FindCartPage {

	private String name;

	private UUID id;
	
	private Set<CartItem> cartItem = new HashSet<>();
	
	private Double totalAmt;
}
