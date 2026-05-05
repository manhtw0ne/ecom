package com.manh.ecom_be.services.product;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.manh.ecom_be.dtos.ProductDTO;
import com.manh.ecom_be.dtos.ProductImageDTO;
import com.manh.ecom_be.models.Category;
import com.manh.ecom_be.models.Product;
import com.manh.ecom_be.models.ProductImage;
import com.manh.ecom_be.repositories.CategoryRepository;
import com.manh.ecom_be.repositories.ProductImageRepository;
import com.manh.ecom_be.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService implements InterfaceProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final InterfaceProductRedisService productRedisService;

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
