package com.hp.cafe.menu;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_options")
public class MenuOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private MenuOptionGroup group;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceDelta = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    protected MenuOption() {}

    public MenuOption(String name, BigDecimal priceDelta, boolean available, Integer sortOrder) {
        this.name = name;
        this.priceDelta = priceDelta;
        this.available = available;
        this.sortOrder = sortOrder;
    }

    public void setGroup(MenuOptionGroup group) { this.group = group; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPriceDelta() { return priceDelta; }
    public boolean isAvailable() { return available; }
    public Integer getSortOrder() { return sortOrder; }
}
