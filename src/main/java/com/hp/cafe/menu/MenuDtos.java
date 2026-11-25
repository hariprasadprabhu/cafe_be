package com.hp.cafe.menu;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class MenuDtos {
    public record CategoryResponse(Long id, String name, Integer sortOrder) {}
    public record CreateCategoryRequest(String name, Integer sortOrder) {}
    public record CreateItemRequest(String name, String detail, BigDecimal price, String emoji,
                                    String badge, List<Long> categoryIds) {}
    public record OptionResponse(Long id, String name, BigDecimal priceDelta, boolean available) {}
    public record OptionGroupResponse(Long id, String name, String selectionType, boolean required,
                                      Integer sortOrder, List<OptionResponse> options) {}
    public record ItemResponse(Long id, String name, String detail, BigDecimal price, String emoji,
                               String badge, boolean available, List<CategoryResponse> categories,
                               List<OptionGroupResponse> optionGroups) {}
    public record PriceHistoryResponse(Long id, BigDecimal price, Instant effectiveFrom, Instant effectiveTo) {}
    public record UpdatePriceRequest(BigDecimal price) {}
    public record UpdateAvailabilityRequest(boolean available) {}
}
