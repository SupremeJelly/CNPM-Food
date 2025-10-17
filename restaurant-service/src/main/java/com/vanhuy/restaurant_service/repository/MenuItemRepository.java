package com.vanhuy.restaurant_service.repository;

import com.vanhuy.restaurant_service.model.MenuItem;
import com.vanhuy.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    List<MenuItem> findByRestaurant(Restaurant restaurant);

    @Modifying
    @Transactional
    @Query("update MenuItem m set m.stock = m.stock - :qty where m.itemId = :id and m.stock >= :qty")
    int decreaseStock(@Param("id") Integer id, @Param("qty") Integer qty);

    @Modifying
    @Transactional
    @Query("update MenuItem m set m.stock = m.stock + :qty where m.itemId = :id")
    int increaseStock(@Param("id") Integer id, @Param("qty") Integer qty);
}
