package in.geomitra.cart.domain;

import java.io.Serializable;
import java.util.UUID;

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
public class CreateCartResponse implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private UUID id;
	
	private Double totalAmt;
	
	private String message;
}
