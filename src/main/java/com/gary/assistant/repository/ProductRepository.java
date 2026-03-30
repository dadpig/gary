package com.gary.assistant.repository;

import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByPlatformAndPlatformProductId(Platform platform, String platformProductId);

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE p.available = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByQuery(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE p.platform = :platform AND p.available = true")
    List<Product> findByPlatformAndAvailable(@Param("platform") Platform platform);

    List<Product> findByPlatformIn(List<Platform> platforms);
}
