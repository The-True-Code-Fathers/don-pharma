package com.codefathers.repository.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Storage;

public interface StorageRepository {
    void save(Storage storage);

    void update(Storage storage);

    void delete(UUID id);

    Optional<Storage> findByProductSku(String productSku);

    List<Storage> listAll();
}
