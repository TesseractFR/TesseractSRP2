package onl.tesseract.srp.common.adapter.userside.menu;

import onl.tesseract.srp.common.domain.model.ItemTag;

public enum CustomMenuButton {
    MENU_MENU_BUTTON("menu_menu_button"),
    MENU_RETURN_BUTTON("menu_return_button"),
    MENU_UP_ARROW_BUTTON("menu_up_arrow_button"),
    MENU_DOWN_ARROW_BUTTON("menu_down_arrow_button"),
    MENU_RIGHT_ARROW_BUTTON("menu_right_arrow_button"),
    MENU_LEFT_ARROW_BUTTON("menu_left_arrow_button"),
    MENU_BACK_ARROW_BUTTON("menu_back_arrow_button"),
    MENU_CLOSE2_BUTTON("menu_close2_button"),
    MENU_QUANTITY_BUTTON("menu_quantity_button"),
    MENU_MIN_BUTTON("menu_min_button"),
    MENU_MAX_BUTTON("menu_max_button"),
    MENU_MINUS_1_BUTTON("menu_minus_1_button"),
    MENU_MINUS_5_BUTTON("menu_minus_5_button"),
    MENU_PLUS_1_BUTTON("menu_plus_1_button"),
    MENU_PLUS_5_BUTTON("menu_plus_5_button"),
    MENU_RECIPE_BOOK_BUTTON("menu_recipe_book_button"),
    MENU_STRUCTURE_INFO_BUTTON("menu_structure_info_button"),
    MENU_INFORMATION_BUTTON("menu_information_button")
    ;
    private final static String TESSERACT_BASE = "tesseract:";
    private final ItemTag itemTag;

    CustomMenuButton(String itemTag) {
        this.itemTag = new ItemTag(TESSERACT_BASE + itemTag);
    }

    public ItemTag getItemTag() {
        return itemTag;
    }
}