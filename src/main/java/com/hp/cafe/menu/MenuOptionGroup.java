package com.hp.cafe.menu;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_option_groups")
public class MenuOptionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String selectionType = "SINGLE";

    @Column(nullable = false)
    private boolean required = false;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MenuOption> options = new ArrayList<>();

    protected MenuOptionGroup() {}

    public MenuOptionGroup(String name, String selectionType, boolean required, Integer sortOrder) {
        this.name = name;
        this.selectionType = selectionType;
        this.required = required;
        this.sortOrder = sortOrder;
    }

    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }
    public void addOption(MenuOption option) { option.setGroup(this); options.add(option); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSelectionType() { return selectionType; }
    public boolean isRequired() { return required; }
    public Integer getSortOrder() { return sortOrder; }
    public List<MenuOption> getOptions() { return options; }
}
