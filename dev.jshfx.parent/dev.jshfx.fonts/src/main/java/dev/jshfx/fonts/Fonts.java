package dev.jshfx.fonts;

import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;

public class Fonts {

    public static final String FONT_AWESOME = "FontAwesome";
    public static final String FONT_AWESOME_5_FREE_REGULAR = "Font Awesome 5 Free Regular";
    public static final String FONT_AWESOME_5_FREE_SOLID = "Font Awesome 5 Free Solid";
    public static final String MATERIAL_ICONS = "Material Icons";
    public static final String OCTICONS = "octicons";

    private static final Map<String, String> URLS = Map.of(FONT_AWESOME_5_FREE_REGULAR,
            getUrl("/dev/jshfx/fonts/Font_Awesome_5_Free-Regular-400.otf"), FONT_AWESOME_5_FREE_SOLID,
            getUrl("/dev/jshfx/fonts/Font_Awesome_5_Free-Solid-900.otf"), MATERIAL_ICONS,
            getUrl("/dev/jshfx/fonts/MaterialIcons-Regular.ttf"), OCTICONS,
            getUrl("/dev/jshfx/fonts/octicons.ttf"));

    private static String getUrl(String path) {
        return Fonts.class.getResource(path).toExternalForm();
    }

    public static Map<String, String> getUrls() {

        return URLS;
    }

    public static Node getGraphic(String text, String family) {
        return getGraphic(text, family, 14, new Insets(0));
    }

    public static Node getGraphic(String text, String family, int size, Insets padding) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setPadding(padding);
        label.setStyle(String.format("-fx-font-family: \"%s\"; -fx-font-size: %d;", family, size));

        return label;
    }

    public static class FontAwesome {

        public static final String ANCHOR = "\uf13d";
        public static final String ARROW_LEFT = "\uf060";
        public static final String ARROW_RIGHT = "\uf061";
        public static final String BOOKMARK = "\uf02e";
        public static final String BOOK_OPEN = "\uf518";
        public static final String CHEVRON_DOWN = "\uf078";
        public static final String CHEVRON_UP = "\uf077";
        public static final String CIRCLE = "\uf111";
        public static final String CLONE = "\uf24d";
        public static final String COMPRESS = "\uf066";
        public static final String CUBE = "\uf1b2";
        public static final String EXPAND = "\uf065";
        public static final String EXPAND_ARROWS_ALT = "\uf31e";
        public static final String EYE = "\uF06e";
        public static final String FILE = "\uf15b";
        public static final String FILE_ALT = "\uf15c";
        public static final String FILE_ARCHIVE = "\uf1c6";
        public static final String FILE_EXPORT = "\uf56e";
        public static final String FOLDER = "\uf07b";
        public static final String FOLDER_OPEN = "\uf07c";
        public static final String GLOBE = "\uf0ac";
        public static final String IMAGE = "\uf03e";
        public static final String LAYER_GROUP = "\uf5fd";
        public static final String LIST = "\uf03a";
        public static final String OBJECT_GROUP = "\uf247";
        public static final String PLAY = "\uf04b";
        public static final String PROJECT_DIAGRAM = "\uf542";
        public static final String REDO = "\uf01e";
        public static final String RUNNING = "\uf70c";
        public static final String SEARCH = "\uf002";
        public static final String SIGN_IN_ALT = "\uf2f6";
        public static final String SQUARE = "\uf0c8";
        public static final String TH_LARGE = "\uf009";
        public static final String TIMES = "\uf00d";
        public static final String TIMES_CIRCLE = "\uf057";
        public static final String UNDO = "\uf0e2";
        public static final String VIDEO = "\uf03d";

        public static Node getBookOpen() {
            return getGraphic(Fonts.FontAwesome.BOOK_OPEN, Fonts.FONT_AWESOME_5_FREE_SOLID);
        }

        public static Node getFolder() {
            return getGraphic(Fonts.FontAwesome.FOLDER, Fonts.FONT_AWESOME_5_FREE_REGULAR);
        }

        public static Node getProjectDiagram() {
            return getGraphic(Fonts.FontAwesome.PROJECT_DIAGRAM, Fonts.FONT_AWESOME_5_FREE_SOLID);
        }
    }

    public static class Material {
        public static final String ARROW_RIGHT_A = "\ue941";
        public static final String BLUR_CILCULAR = "\ue3a2";
        public static final String BLUR_LINEAR = "\ue3a3";
        public static final String BLUR_ON = "\ue3a5";
        public static final String BRIGHTNESS_1 = "\ue3a6";
        public static final String CLEAR_ALL = "\ue0b8";
        public static final String CROP_7_5 = "\ue3c0";
        public static final String CROP_PORTRAIT = "\ue3c5";
        public static final String CROP_ROTATE = "\ue437";
        public static final String EXIT_TO_APP = "\ue879";   
        public static final String EXPOSURE_ZERO = "\ue3cf";
        public static final String FOLDER = "\ue2c7";
        public static final String FONT_DOWNLOAD = "\ue167";
        public static final String GRADIENT = "\ue3e9";
        public static final String HIGHLIGHT = "\ue25f";
        public static final String IMPORT_CONTACTS = "\ue0e0";
        public static final String INPUT = "\ue890";        
        public static final String LIBRARY_BOOKS = "\ue02f";
        public static final String MENU = "\ue5d2";
        public static final String SAVE = "\ue161";
        public static final String SEND = "\ue163";
        public static final String TEXTURE = "\ue421";
        public static final String TRANSFORM = "\ue428";
        public static final String VIDEO_LABEL = "\ue071";
        public static final String VIEW_ARRAY = "\ue8ea";
        public static final String VIEW_COMPACT = "\ue42b";
        public static final String VIEW_WEEK = "\ue8f3";
        public static final String WALLPAPER = "\ue1bc";
        public static final String WB_INCANDESCENT = "\ue42e";
        public static final String WB_SUNNY = "\ue430";
        public static final String ZOOM_OUT_MAP = "\ue56b";

        public static Node getFolder() {
            return getGraphic(Fonts.Material.FOLDER, Fonts.MATERIAL_ICONS);
        }
    }

    public static class Octicons {
        public static final String SCREEN_FULL = "\uf066";
        public static final String SCREEN_NORMAL = "\uf067";
    }

    public static class Unicode {

        public static final String LARGE_CIRCLE = "\u25EF";
        public static final String CIRCLED_INFORMATION_SOURCE = "\ud83d\udec8";
        public static final String FLOPPY_DISK = "\uD83D\uDCBE";
        public static final String FULLWIDTH_PLUS_SIGN = "\uFF0B";
        public static final String NEGATIVE_SQUARED_CROSS_MARK = "\u274e";
        public static final String NORTH_EAST_AND_SOUTH_WEST_ARROW = "\u2922";
        public static final String NORTH_WEST_ARROW_TO_CORNER = "\u21f1";
        public static final String OPEN_FILE_FOLDER = "\uD83D\uDCC2";
        public static final String TWO_JOINED_SQUARES = "\u29c9";
        public static final String UPPER_RIGHT_DROP_SHADOWED_WHITE_SQUARE = "\u2750";
        public static final String WARNING_SIGN = "\u26a0";
        public static final String WHITE_CIRCLE = "\u25cb";
        public static final String WHITE_HORIZONTAL_ELLIPSE = "\u2b2d";
        public static final String WHITE_LARGE_SQUARE = "\u2b1c";
        public static final String WHITE_SQUARE_CONTAINING_BLACK_SQUARE = "\u25a3";
        public static final String WHITE_RECTANGLE = "\u25ad";
        public static final String WHITE_VERTICAL_RECTANGLE = "\u25af";
    }
}
