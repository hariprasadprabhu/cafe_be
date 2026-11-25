package com.hp.cafe.menu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class MenuSeeder implements CommandLineRunner {
    private final MenuCategoryRepository categories;
    private final MenuItemRepository items;
    private final MenuPriceHistoryRepository prices;

    public MenuSeeder(MenuCategoryRepository categories, MenuItemRepository items,
                      MenuPriceHistoryRepository prices) {
        this.categories = categories;
        this.items = items;
        this.prices = prices;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (items.count() > 0) return;

        seedCategory("New launches", 1);
        seedCategory("Bestsellers", 2);
        seedCategory("Hot drinks", 3);
        seedCategory("Cold drinks", 4);
        seedCategory("Food", 5);
        seedCategory("Beans", 6);
        seedCategory("High protein", 7);

        seedItem("Cloud Vanilla Latte", "Velvety vanilla cream", 245, "🥛", "NEW", true,
            new String[]{"New launches", "Hot drinks"});
        seedItem("Blueberry Iced Matcha", "Matcha · blueberry · oat", 265, "🫐", "NEW", true,
            new String[]{"New launches", "Cold drinks"});
        seedItem("Sea Salt Caramel", "Espresso · caramel · sea salt", 280, "🧊", "POPULAR", true,
            new String[]{"Bestsellers", "Cold drinks"});
        seedItem("Classic Cappuccino", "Rich espresso & foam", 210, "☕", null, true,
            new String[]{"Bestsellers", "Hot drinks"});
        seedItem("Hazelnut Cortado", "Nutty · balanced · smooth", 240, "☕", null, true,
            new String[]{"Hot drinks"});
        seedItem("Spanish Latte", "Sweet & creamy", 230, "🥛", null, true,
            new String[]{"Hot drinks"});
        seedItem("Iced Americano", "Bold double shot", 190, "🧊", null, true,
            new String[]{"Cold drinks"});
        seedItem("Pistachio Cold Brew", "Silky pistachio foam", 290, "🥤", "POPULAR", true,
            new String[]{"Cold drinks"});
        seedItem("Almond Croissant", "Baked fresh this morning", 180, "🥐", null, true,
            new String[]{"Food"});
        seedItem("Pesto Chicken Bagel", "Chicken · pesto · cheese", 320, "🥯", null, true,
            new String[]{"Food"});
        seedItem("House Espresso", "Chocolate · caramel · 250g", 550, "🫘", null, true,
            new String[]{"Beans"});
        seedItem("Morning Light", "Fruity · floral · 250g", 590, "🫘", null, true,
            new String[]{"Beans"});
        seedItem("Protein Mocha", "20g protein · cacao", 310, "🥤", "20G PROTEIN", true,
            new String[]{"High protein", "Cold drinks"});
        seedItem("Berry Protein Bowl", "Greek yogurt · granola", 350, "🍓", "25G PROTEIN", true,
            new String[]{"High protein", "Food"});
    }

    private void seedCategory(String name, int sortOrder) {
        categories.findByName(name).orElseGet(() -> categories.save(new MenuCategory(name, sortOrder)));
    }

    private void seedItem(String name, String detail, int price, String emoji, String badge,
                          boolean available, String[] categoryNames) {
        var item = new MenuItem(name, detail, BigDecimal.valueOf(price), emoji, badge, available);

        if (!name.matches(".*(Croissant|Bagel|Espresso|Morning Light|Protein Bowl).*")) {
            var milk = new MenuOptionGroup("Type of milk", "SINGLE", true, 1);
            milk.addOption(new MenuOption("Whole milk", BigDecimal.ZERO, true, 1));
            milk.addOption(new MenuOption("Oat milk", BigDecimal.valueOf(40), true, 2));
            milk.addOption(new MenuOption("Almond milk", BigDecimal.valueOf(50), true, 3));
            milk.addOption(new MenuOption("Soy milk", BigDecimal.valueOf(35), true, 4));
            item.addOptionGroup(milk);

            var flavour = new MenuOptionGroup("Flavour", "SINGLE", false, 2);
            flavour.addOption(new MenuOption("Vanilla", BigDecimal.valueOf(20), true, 1));
            flavour.addOption(new MenuOption("Caramel", BigDecimal.valueOf(20), true, 2));
            flavour.addOption(new MenuOption("Hazelnut", BigDecimal.valueOf(25), true, 3));
            flavour.addOption(new MenuOption("Mocha", BigDecimal.valueOf(25), true, 4));
            item.addOptionGroup(flavour);
        }

        var extras = new MenuOptionGroup("Extras", "MULTIPLE", false, 3);
        extras.addOption(new MenuOption("Extra shot", BigDecimal.valueOf(45), true, 1));
        extras.addOption(new MenuOption("Whipped cream", BigDecimal.valueOf(30), true, 2));
        extras.addOption(new MenuOption("Chia seeds", BigDecimal.valueOf(25), true, 3));
        item.addOptionGroup(extras);

        for (String categoryName : categoryNames) {
            categories.findByName(categoryName).ifPresent(item::addCategory);
        }

        item = items.save(item);
        prices.save(new MenuPriceHistory(item, item.getPrice()));
    }
}
