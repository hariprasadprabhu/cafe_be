package com.hp.cafe.menu;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String detail;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String emoji = "☕";

    private String badge;

    @Column(nullable = false)
    private boolean available = true;

    @ManyToMany
    @JoinTable(name = "menu_item_categories",
        joinColumns = @JoinColumn(name = "menu_item_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id"))
    @OrderBy("sortOrder ASC, name ASC")
    private List<MenuCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MenuOptionGroup> optionGroups = new ArrayList<>();

    protected MenuItem() {}

    public MenuItem(String name, String detail, BigDecimal price, String emoji, String badge, boolean available) {
        this.name = name;
        this.detail = detail;
        this.price = price;
        this.emoji = emoji;
        this.badge = badge;
        this.available = available;
    }

    public void setPrice(BigDecimal price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }
    public void addCategory(MenuCategory category) { categories.add(category); }
    public void addOptionGroup(MenuOptionGroup group) { group.setMenuItem(this); optionGroups.add(group); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDetail() { return detail; }
    public BigDecimal getPrice() { return price; }
    public String getEmoji() { return emoji; }
    public String getBadge() { return badge; }
    public boolean isAvailable() { return available; }
    public List<MenuCategory> getCategories() { return categories; }
    public List<MenuOptionGroup> getOptionGroups() { return optionGroups; }
}
