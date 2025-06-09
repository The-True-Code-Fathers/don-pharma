package com.codefathers.repository;

import java.util.List;
import java.util.Optional;

import com.codefathers.model.entity.Storage;

public interface StorageRepository {
    void save(Storage storage);
    void update(Storage storage);
    Optional<Storage> findByProductSku(String productSku);
    List<Storage> getAllStorages();
}
