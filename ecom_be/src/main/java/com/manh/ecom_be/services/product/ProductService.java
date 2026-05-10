package com.manh.ecom_be.services.product;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.manh.ecom_be.dtos.ProductDTO;
import com.manh.ecom_be.dtos.ProductImageDTO;
import com.manh.ecom_be.models.*;
import com.manh.ecom_be.repositories.*;
import com.manh.ecom_be.responses.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements InterfaceProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final InterfaceProductRedisService productRedisService;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public Product createProduct(ProductDTO dto) throws DataNotFoundException {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Category not found: " + dto.getCategoryId()));

        return productRepository.save(Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .thumbnail(dto.getThumbnail())
                .description(dto.getDescription())
                .category(category)
                .build());
    }

    @Override
    public Product getProductById(long id) throws Exception {
        return productRepository.getDetailProduct(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found: " + id));
    }

    @Transactional
    public void likeProduct(long userId, Long productId) {
        if (!favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new DataNotFoundException("User not found"));
            Product product = getProductById(productId);
            favoriteRepository.save(Favorite.builder().user(user).product(product).build());
        }

    }

    @Transactional
    public void unlikeProduct(Long userId, Long productId) {
        Favorite fav = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (fav != null) {
            favoriteRepository.delete(fav);
        }
    }

    @Override
    public Page<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest)
        throws JsonProcessingException {
        List<ProductResponse> cached = productRedisService.getAllProducts(keyword, categoryId, pageRequest);
        if (cached != null && !cached.isEmpty()) {
            return new PageImpl<>(cached, pageRequest, cached.size());
        }

        Page<Product> page = productRepository.searchProducts(categoryId, keyword, pageRequest);

        List<ProductResponse> responses = page.getContent().stream()
                .map(ProductResponse::fromProduct).toList();
        productRedisService.saveAllProducts(responses, keyword, categoryId, pageRequest);
        return page.map(ProductResponse::fromProduct);
    }

    @Override
    @Transactional
    public Product updateProduct(long id, ProductDTO dto) throws Exception {
        Product product = getProductById(id);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Category not found"));

        if (dto.getName() != null && !dto.getName().isEmpty()) product.setName(dto.getName());
        product.setCategory(category);
        if (dto.getPrice() >= 0) product.setDescription(dto.getDescription());
        if (dto.getThumbnail() != null) product.setThumbnail(dto.getThumbnail());

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(long id) {productRepository.deleteById(id);}

    @Override
    public ProductImage createProductImage(Long productId, ProductImageDTO dto)
        throws Exception {
        Product product = getProductById(productId);

        if (productImageRepository.findByProductId(productId).size() >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new InvalidParamException("Maximum " + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT + " images");
        }

        return productImageRepository.save(ProductImage.builder()
                .product(product)
                .imageUrl(dto.getImageUrl())
                .build());
    }

}
