package com.manh.ecom_be.services.product.image;

import com.manh.ecom_be.models.Product;
import com.manh.ecom_be.models.ProductImage;
import com.manh.ecom_be.repositories.ProductImageRepository;
import com.manh.ecom_be.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ProductImageService implements InterfaceProductImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductImage deleteProductImage(Long id) {
        Optional<ProductImage> imageOptional = productImageRepository.findById(id);
        if (imageOptional.isEmpty()) {
            throw new RuntimeException("Image not found with id: " + id);
        }

        ProductImage image = imageOptional.get();
        Product product = image.getProduct();

        productImageRepository.deleteById(id);

        if (image.getImageUrl().equals(product.getThumbnail())) {
            List<ProductImage> remainingImages = productImageRepository
                    .findByProductId(product.getId());


            if (!remainingImages.isEmpty()) {
                product.setThumbnail(remainingImages.get(0).getImageUrl());
            } else {
                product.setThumbnail(null);
            }
            productRepository.save(product);
            }
        return image;
    }
}
