package dev.jshfx.jfx.css;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.css.CssParser;
import javafx.css.Declaration;
import javafx.css.ParsedValue;
import javafx.css.Selector;
import javafx.css.Stylesheet;

public final class CssUtils {

    private static final Logger LOGGER = Logger.getLogger(CssUtils.class.getName());

    private CssUtils() {
    }

    public static <V> Optional<V> getPropertyValue(Path path, String selector, String property) {
        Optional<V> value = Optional.empty();

        if (Files.exists(path)) {

            try {
                value = getPropertyValue(path.toUri().toURL(), selector, property);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <V> Optional<V> getPropertyValue(URL url, String selector, String property) {
        Optional<V> value = Optional.empty();
        try {
            CssParser parser = new CssParser();
            Stylesheet stylesheet = parser.parse(url);
            Selector select = Selector.createSelector(selector);
            value = (Optional<V>) stylesheet.getRules().stream()
                    .filter(r -> r.getSelectors().stream().anyMatch(s -> s.equals(select)))
                    .flatMap(r -> r.getDeclarations().stream()).filter(d -> d.getProperty().equals(property))
                    .findFirst().map(d -> d.getParsedValue().getValue());

            if (value.isPresent()) {
                var v = value.get();

                while (v instanceof ParsedValue<?, ?> parsedValue) {
                    v = (V) parsedValue.getValue();
                }

                value = Optional.of(v);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        return value;
    }

    public static void setPropertyValue(Path path, String selector, String property, Object value) {
        try {
            String stylesheetStr = String.format("%s {%n    %s: %s;%n}", selector, property, value);

            if (Files.exists(path)) {
                CssParser parser = new CssParser();
                Stylesheet stylesheet = parser.parse(stylesheetStr);
                Declaration declaration = parser.parse(String.format("%s { %s: %s}", selector, property, value))
                        .getRules().get(0).getDeclarations().get(0);
                stylesheet = parser.parse(path.toUri().toURL());
                Selector select = Selector.createSelector(selector);

                var rules = stylesheet.getRules().stream()
                        .filter(r -> r.getSelectors().stream().anyMatch(s -> s.equals(select)))
                        .filter(r -> r.getDeclarations().stream().anyMatch(d -> d.getProperty().equals(property)))
                        .toList();

                rules.forEach(r -> {
                    r.getDeclarations().replaceAll(d -> d.getProperty().equals(property) ? declaration : d);
                });

                stylesheetStr = toString(stylesheet);
            }

            Files.writeString(path, stylesheetStr);

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private static String toString(Stylesheet stylesheet) {
        StringBuilder sbuf = new StringBuilder();

        for (var rule : stylesheet.getRules()) {
            sbuf.append(rule.getSelectors().stream().map(s -> s.toString().replace("*.", "."))
                    .collect(Collectors.joining(",")));
            sbuf.append(" {\n");
            sbuf.append(rule.getDeclarations().stream()
                    .map(d -> "    " + d.getProperty() + ": " + d.getParsedValue().getValue().toString()
                            .replaceFirst("(?s).*<value>([^<>]*)</value>.*", "$1"))
                    .collect(Collectors.joining(";\n", "", ";\n")));
            sbuf.append("}\n\n");
        }

        return sbuf.toString();
    }
}
