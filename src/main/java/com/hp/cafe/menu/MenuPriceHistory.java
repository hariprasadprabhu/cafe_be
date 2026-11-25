package com.hp.cafe.menu;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "menu_price_history")
public class MenuPriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Instant effectiveFrom;

    private Instant effectiveTo;

    protected MenuPriceHistory() {}

    public MenuPriceHistory(MenuItem menuItem, BigDecimal price) {
        this.menuItem = menuItem;
        this.price = price;
        this.effectiveFrom = Instant.now();
    }

    public Long getId() { return id; }
    public MenuItem getMenuItem() { return menuItem; }
    public BigDecimal getPrice() { return price; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public Instant getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(Instant effectiveTo) { this.effectiveTo = effectiveTo; }
}
