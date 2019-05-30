package in.geomitra.cart.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.geomitra.cart.util.ResponseUtil;


@RestController
public class CartResource {
	
	
	private CartService cartService;
	
	@Autowired
	public CartResource(CartService cartService) {
		this.cartService = cartService;
	}
	
	/*
	 * Creates a new cart
	 * when we hit this url
	 */
	@PostMapping("/carts")
	public ResponseEntity<CreateCartResponse> create(@Valid @RequestBody CreateCartRequest request) throws URISyntaxException {
		
		CreateCartResponse response = cartService.create(request);		
		return ResponseEntity.created(new URI("/cart/" + response.getId().toString())).body(response);
	}
	
	/*
	 * Update a cart 
	 * when we hit this url
	 */
	@PutMapping("/carts/{id}" )
	public ResponseEntity<UpdateCartResponse> update(@Valid @RequestBody UpdateCartRequest request, @PathVariable UUID id) throws URISyntaxException{
		
		UpdateCartResponse response = cartService.update(request, id);
		return ResponseEntity.ok().body(response);
	}
	
	@DeleteMapping("/carts/{id}")
	public ResponseEntity<Void> deleteCart(@PathVariable UUID id) {
		
		cartService.delete(id);
		return ResponseEntity.ok().build();
	}
	
	/*
	 * Get a cart by ID 
	 * when we hit this url
	 */	
	@GetMapping("/carts/{id}")
	public ResponseEntity<Cart> find(@PathVariable("id") UUID id) {
		
		Optional<Cart> cart = cartService.find(id);
		return ResponseUtil.wrapOrNotFound(cart);
	}
	
	
	/*
	 * Get all carts 
	 * when we hit this url
	 */
	@GetMapping("/carts")
	public ResponseEntity<List<FindCartPage>> findAll(Pageable pageable) {
		
		final Page<FindCartPage> page = cartService.findAll(pageable);
		
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/carts");
		
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}
}
