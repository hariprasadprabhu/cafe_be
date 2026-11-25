package com.hp.cafe.menu;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_categories", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @ManyToMany(mappedBy = "categories")
    private List<MenuItem> items = new ArrayList<>();

    protected MenuCategory() {}

    public MenuCategory(String name, Integer sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getSortOrder() { return sortOrder; }
}
