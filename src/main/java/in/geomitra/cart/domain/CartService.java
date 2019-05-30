package in.geomitra.cart.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface CartService {
	
	public CreateCartResponse create(CreateCartRequest request);

	public UpdateCartResponse update(UpdateCartRequest request, UUID id);
	
	public Optional<Cart> find(UUID id);
	
	public Page<FindCartPage> findAll(Pageable pageable);
	
	public void delete(UUID id);
}
