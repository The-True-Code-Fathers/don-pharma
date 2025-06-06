package com.codefathers.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.Storage;
import com.codefathers.repository.ProductRepositoryImpl;
import com.codefathers.repository.StorageRepositoryImpl;

public class StorageService {
    
    StorageRepositoryImpl storageRepository;
    ProductRepositoryImpl productRepository;

    public StorageService(StorageRepositoryImpl storageRepository, ProductRepositoryImpl productRepository) {
        this.storageRepository = storageRepository;
        this.productRepository = productRepository;
    }

    public void createStorage(String productSku, int productQuantity) {
        if (productQuantity <= 0) {
            throw new IllegalArgumentException("Product quantity needs to be higher than zero");
        }

        Product product = findProductBySku(productSku);

        Optional<Storage> optionalStorage = storageRepository.findByProductSku(product.getSku());
        if (optionalStorage.isPresent()) {
            Storage storage = optionalStorage.get();
            storage.setProductQuantity(storage.getProductQuantity() + productQuantity);
            storageRepository.update(storage);
        }
        if (!optionalStorage.isPresent()) {
            var storage = Storage.builder().product(product).productQuantity(productQuantity).productStorageCost(BigDecimal.ZERO).build();
            storageRepository.save(storage);
        }
    }

    public Product findProductBySku(String productSku) {
        return productRepository.findBySKU(productSku);
    }

    public int getQuantityStock(String productId) {
        Storage storage = storageRepository.findByProductSku(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return storage.getProductQuantity();
    }

}
