package com.hp.cafe.menu;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/categories")
    public List<MenuCategoryResponse> categories() {
        return menuService.categories().stream()
            .map(category -> new MenuCategoryResponse(category.getId(), category.getName(), category.getSortOrder()))
            .toList();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuDtos.CategoryResponse createCategory(@RequestBody MenuDtos.CreateCategoryRequest request) {
        return menuService.createCategory(request);
    }

    @GetMapping("/items")
    public List<MenuDtos.ItemResponse> items() {
        return menuService.items();
    }

    @GetMapping("/items/all")
    public List<MenuDtos.ItemResponse> allItems() {
        return menuService.allItems();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuDtos.ItemResponse createItem(@RequestBody MenuDtos.CreateItemRequest request) {
        return menuService.createItem(request);
    }

    @GetMapping("/items/{id}")
    public MenuDtos.ItemResponse item(@PathVariable Long id) {
        return menuService.item(id);
    }

    @PatchMapping("/items/{id}/price")
    public MenuDtos.ItemResponse updatePrice(@PathVariable Long id,
                                             @RequestBody MenuDtos.UpdatePriceRequest request) {
        return menuService.updatePrice(id, request.price());
    }

    @PatchMapping("/items/{id}/availability")
    public MenuDtos.ItemResponse updateAvailability(@PathVariable Long id,
                                                     @RequestBody MenuDtos.UpdateAvailabilityRequest request) {
        return menuService.updateAvailability(id, request.available());
    }

    @GetMapping("/items/{id}/price-history")
    public List<MenuDtos.PriceHistoryResponse> priceHistory(@PathVariable Long id) {
        return menuService.priceHistory(id);
    }

    public record MenuCategoryResponse(Long id, String name, Integer sortOrder) {}
}
