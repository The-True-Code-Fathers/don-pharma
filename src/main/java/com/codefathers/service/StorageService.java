package com.codefathers.service;

import java.util.List;
import java.util.Optional;

import com.codefathers.model.dto.CreateStorageDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.Storage;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.repository.implementations.StorageRepositoryImpl;

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

        Optional<Storage> optionalStorage = storageRepository.findByProductSku(productSku);

        if (optionalStorage.isPresent()) {
            var storage = optionalStorage.get();
            storage.setProductQuantity(storage.getProductQuantity() + productQuantity);
            storageRepository.update(storage);
        }
        if (!optionalStorage.isPresent()) {
            var storage = Storage.builder()
                    .product(product)
                    .productQuantity(productQuantity)
                    .build();
            storageRepository.save(storage);
        }
    }

    public Product findProductBySku(String productSku) {
        return productRepository.findBySKU(productSku);
    }

    public int getQuantityStock(String productSku) {
        Storage storage = storageRepository.findByProductSku(productSku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return storage.getProductQuantity();
    }

    public List<CreateStorageDTO> getAllStorages() {
        return storageRepository.getAllStorages().stream().map(storage -> CreateStorageDTO.builder()
                .productQuantity(storage.getProductQuantity()).build()).toList();
    }

}
