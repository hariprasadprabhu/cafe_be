package com.hp.cafe.menu;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class MenuService {
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final MenuPriceHistoryRepository priceHistoryRepository;

    public MenuService(MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository,
                       MenuPriceHistoryRepository priceHistoryRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuCategory> categories() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc();
    }

    @Transactional
    public MenuDtos.CategoryResponse createCategory(MenuDtos.CreateCategoryRequest request) {
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Category name is required");
        if (categoryRepository.findByName(name).isPresent()) throw new IllegalArgumentException("Category already exists: " + name);
        int sortOrder = request.sortOrder() == null ? 999 : request.sortOrder();
        MenuCategory category = categoryRepository.save(new MenuCategory(name, sortOrder));
        return new MenuDtos.CategoryResponse(category.getId(), category.getName(), category.getSortOrder());
    }

    @Transactional(readOnly = true)
    public List<MenuDtos.ItemResponse> items() {
        return itemRepository.findByAvailableTrueOrderByIdAsc().stream().map(this::toItem).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuDtos.ItemResponse> allItems() {
        return itemRepository.findAllByOrderByIdAsc().stream().map(this::toItem).toList();
    }

    @Transactional
    public MenuDtos.ItemResponse createItem(MenuDtos.CreateItemRequest request) {
        if (request.name() == null || request.name().trim().isEmpty()) throw new IllegalArgumentException("Item name is required");
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be zero or greater");

        MenuItem item = new MenuItem(
            request.name().trim(),
            request.detail() == null ? "" : request.detail().trim(),
            request.price(),
            request.emoji() == null || request.emoji().trim().isEmpty() ? "☕" : request.emoji().trim(),
            request.badge(),
            true);

        if (request.categoryIds() != null) {
            for (Long categoryId : request.categoryIds()) {
                categoryRepository.findById(categoryId).ifPresent(item::addCategory);
            }
        }

        item = itemRepository.save(item);
        priceHistoryRepository.save(new MenuPriceHistory(item, item.getPrice()));
        return toItem(item);
    }

    @Transactional(readOnly = true)
    public MenuDtos.ItemResponse item(Long id) {
        return itemRepository.findById(id).map(this::toItem)
            .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));
    }

    @Transactional
    public MenuDtos.ItemResponse updatePrice(Long id, BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be zero or greater");
        }
        MenuItem item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));

        if (item.getPrice().compareTo(price) != 0) {
            priceHistoryRepository.findByMenuItemIdOrderByEffectiveFromDesc(id).stream()
                .filter(history -> history.getEffectiveTo() == null)
                .findFirst()
                .ifPresent(history -> history.setEffectiveTo(Instant.now()));
            item.setPrice(price);
            priceHistoryRepository.save(new MenuPriceHistory(item, price));
        }
        itemRepository.save(item);
        return toItem(item);
    }

    @Transactional
    public MenuDtos.ItemResponse updateAvailability(Long id, boolean available) {
        MenuItem item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));
        item.setAvailable(available);
        itemRepository.save(item);
        return toItem(item);
    }

    @Transactional(readOnly = true)
    public List<MenuDtos.PriceHistoryResponse> priceHistory(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new IllegalArgumentException("Menu item not found: " + id);
        }
        return priceHistoryRepository.findByMenuItemIdOrderByEffectiveFromDesc(id).stream()
            .map(history -> new MenuDtos.PriceHistoryResponse(history.getId(), history.getPrice(),
                history.getEffectiveFrom(), history.getEffectiveTo()))
            .toList();
    }

    private MenuDtos.ItemResponse toItem(MenuItem item) {
        var categories = item.getCategories().stream()
            .map(category -> new MenuDtos.CategoryResponse(category.getId(), category.getName(), category.getSortOrder()))
            .toList();
        var groups = item.getOptionGroups().stream()
            .map(group -> new MenuDtos.OptionGroupResponse(group.getId(), group.getName(), group.getSelectionType(),
                group.isRequired(), group.getSortOrder(), group.getOptions().stream()
                    .filter(MenuOption::isAvailable)
                    .map(option -> new MenuDtos.OptionResponse(option.getId(), option.getName(),
                        option.getPriceDelta(), option.isAvailable()))
                    .toList()))
            .toList();
        return new MenuDtos.ItemResponse(item.getId(), item.getName(), item.getDetail(), item.getPrice(),
            item.getEmoji(), item.getBadge(), item.isAvailable(), categories, groups);
    }
}
