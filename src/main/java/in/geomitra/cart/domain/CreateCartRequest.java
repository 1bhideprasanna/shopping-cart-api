package in.geomitra.cart.domain;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCartRequest {
	
	@Builder.Default
	@NotEmpty
	private Set<CreateCartItemRequest> cartItems = new HashSet<>();
	
	@NotBlank
	private String name;
}
