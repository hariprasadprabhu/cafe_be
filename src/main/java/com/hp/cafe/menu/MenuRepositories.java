package com.hp.cafe.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    Optional<MenuCategory> findByName(String name);
    List<MenuCategory> findAllByOrderBySortOrderAscNameAsc();
}

interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByAvailableTrueOrderByIdAsc();
    List<MenuItem> findAllByOrderByIdAsc();

    Optional<MenuItem> findById(Long id);
}

interface MenuPriceHistoryRepository extends JpaRepository<MenuPriceHistory, Long> {
    List<MenuPriceHistory> findByMenuItemIdOrderByEffectiveFromDesc(Long menuItemId);
}
