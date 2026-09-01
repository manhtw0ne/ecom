package com.manh.ecom_be.services.product;

import com.manh.ecom_be.dtos.ProductDTO;
import com.manh.ecom_be.dtos.ProductImageDTO;
import com.manh.ecom_be.exceptions.DataNotFoundException;
import com.manh.ecom_be.exceptions.InvalidParamException;
import com.manh.ecom_be.models.*;
import com.manh.ecom_be.repositories.*;
import com.manh.ecom_be.responses.product.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private FavoriteRepository favoriteRepository;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;
    private ProductDTO testProductDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder().id(1L).name("Electronics").build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(100.0f)
                .thumbnail("test.jpg")
                .description("Test description")
                .category(testCategory)
                .comments(new ArrayList<>())
                .favorites(new ArrayList<>())
                .productImages(new ArrayList<>())
                .build();

        testProductDTO = ProductDTO.builder()
                .name("Test Product")
                .price(100.0f)
                .thumbnail("test.jpg")
                .description("Test description")
                .categoryId(1L)
                .build();

        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .build();
    }

    // ─────────────── CREATE ───────────────

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("should create product when category exists")
        void createProduct_validDTO_shouldReturnProduct() throws Exception {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            Product result = productService.createProduct(testProductDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getPrice()).isEqualTo(100.0f);
            assertThat(result.getCategory()).isEqualTo(testCategory);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw DataNotFoundException when category does not exist")
        void createProduct_invalidCategory_shouldThrowDataNotFound() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.createProduct(testProductDTO))
                    .isInstanceOf(DataNotFoundException.class)
                    .hasMessageContaining("Category not found");
        }
    }

    // ─────────────── READ ───────────────

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("should return product when it exists")
        void getProductById_existingId_shouldReturnProduct() throws Exception {
            when(productRepository.getDetailProduct(1L)).thenReturn(Optional.of(testProduct));
            when(productImageRepository.findByProductId(1L))
                    .thenReturn(List.of(ProductImage.builder().imageUrl("test.jpg").build()));

            Product result = productService.getProductById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("should throw DataNotFoundException when product does not exist")
        void getProductById_nonExistingId_shouldThrowDataNotFound() {
            when(productRepository.getDetailProduct(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(DataNotFoundException.class)
                    .hasMessageContaining("Cannot find product");
        }
    }

    @Test
    @DisplayName("getAllProducts should return paginated results")
    void getAllProducts_shouldReturnPage() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageRequest, 1);

        when(productRepository.searchProducts(eq(0L), eq("test"), eq(pageRequest)))
                .thenReturn(productPage);

        Page<ProductResponse> result = productService.getAllProducts("test", 0L, pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Product");
    }

    // ─────────────── UPDATE ───────────────

    @Test
    @DisplayName("updateProduct should update fields when product exists")
    void updateProduct_validDTO_shouldUpdateFields() throws Exception {
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .price(200.0f)
                .description("Updated description")
                .thumbnail("updated.jpg")
                .categoryId(1L)
                .build();

        when(productRepository.getDetailProduct(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findByProductId(1L)).thenReturn(Collections.emptyList());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getPrice()).isEqualTo(200.0f);
        assertThat(result.getDescription()).isEqualTo("Updated description");
    }

    // ─────────────── DELETE ───────────────

    @Test
    @DisplayName("deleteProduct should call repository delete when product exists")
    void deleteProduct_existingId_shouldCallDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(1L);

        verify(productRepository).delete(testProduct);
    }

    // ─────────────── PRODUCT IMAGE ───────────────

    @Test
    @DisplayName("createProductImage should throw when exceeds max images")
    void createProductImage_exceedsMax_shouldThrowInvalidParam() {
        List<ProductImage> existingImages = new ArrayList<>();
        for (int i = 0; i < ProductImage.MAXIMUM_IMAGES_PER_PRODUCT; i++) {
            existingImages.add(ProductImage.builder().id((long) i).imageUrl("img" + i + ".jpg").build());
        }

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findByProductId(1L)).thenReturn(existingImages);

        ProductImageDTO imageDTO = ProductImageDTO.builder().imageUrl("new-image.jpg").build();

        assertThatThrownBy(() -> productService.createProductImage(1L, imageDTO))
                .isInstanceOf(InvalidParamException.class)
                .hasMessageContaining("Number of images must be <=");
    }

    // ─────────────── LIKE / UNLIKE ───────────────

    @Nested
    @DisplayName("Like & Unlike")
    class LikeUnlike {

        @Test
        @DisplayName("likeProduct should save favorite when not already liked")
        void likeProduct_validIds_shouldSaveFavorite() throws Exception {
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsById(1L)).thenReturn(true);
            when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(favoriteRepository.save(any(Favorite.class))).thenReturn(Favorite.builder().build());

            Product result = productService.likeProduct(1L, 1L);

            assertThat(result).isNotNull();
            verify(favoriteRepository).save(any(Favorite.class));
        }

        @Test
        @DisplayName("unlikeProduct should delete favorite when it exists")
        void unlikeProduct_existingFavorite_shouldDelete() throws Exception {
            Favorite existingFavorite = Favorite.builder()
                    .id(1L).user(testUser).product(testProduct).build();

            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsById(1L)).thenReturn(true);
            when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);
            when(favoriteRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(existingFavorite);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            Product result = productService.unlikeProduct(1L, 1L);

            assertThat(result).isNotNull();
            verify(favoriteRepository).delete(existingFavorite);
        }
    }
}
