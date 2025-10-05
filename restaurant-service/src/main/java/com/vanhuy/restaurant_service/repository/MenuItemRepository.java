package com.vanhuy.restaurant_service.repository;

import com.vanhuy.restaurant_service.model.MenuItem;
import com.vanhuy.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface MenuItemRepository extends JpaRepository<MenuItem , Integer> {
    List<MenuItem> findByRestaurant(Restaurant restaurant);

    @Modifying
    @Query("UPDATE MenuItem m SET m.stock = m.stock - :qty WHERE m.itemId = :id AND m.stock >= :qty")
    int decrementStockIfAvailable(@Param("id") Integer id, @Param("qty") Integer qty);

    @Modifying
    @Query("UPDATE MenuItem m SET m.stock = m.stock + :qty WHERE m.itemId = :id")
    int incrementStock(@Param("id") Integer id, @Param("qty") Integer qty);
}
