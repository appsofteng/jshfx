package dev.jshfx.jx.tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Lexer {

    private Pattern pattern;
    private List<String> groups;
    private String openTokenPattern;
    private Map<String, String> openCloseTypes = new HashMap<>();
    private Map<String, String> closeOpenTypes = new HashMap<>();
    private List<Token> tokens = new ArrayList<>();
    private Map<String, Deque<Token>> tokenStack = new HashMap<>();
    private List<Token> tokensOnCaretPosition = new ArrayList<>();
    private IntegerProperty length = new SimpleIntegerProperty();

    private Lexer(String regex, List<String> groups, String openingTokenPattern) {
        this.pattern = Pattern.compile(regex);
        this.groups = groups;
        this.openTokenPattern = openingTokenPattern;
        extractOpenCloseTokens();
    }

    private void extractOpenCloseTokens() {

        String open = null;
        for (var group : groups) {
            if (Token.isOpen(group)) {
                open = group;
            } else if (Token.isClose(group)) {
                openCloseTypes.put(open, group);
                closeOpenTypes.put(group, open);
            }
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public int getLength() {
        return length.get();
    }
    
    public ReadOnlyIntegerProperty lengthProperty() {
        return length;
    }
    
    public List<Token> getTokensOnCaretPosition() {
        return tokensOnCaretPosition;
    }

    public static Lexer get(String fileName) {
        return get(fileName, "");
    }

    public static Lexer get(String fileName, String language) {
        Lexer lexer = null;

        InputStream is = Lexer.class.getResourceAsStream(fileName + ".txt");

        if (is == null) {
            is = Lexer.class.getResourceAsStream(language + ".txt");

        }

        if (is == null) {
            return null;
        }

        try (var bis = new BufferedReader(new InputStreamReader(is))) {
            List<String> groups = Arrays.asList(bis.readLine().split(","));
            String openingTokenPattern = bis.readLine();
            String regex = bis.lines().collect(Collectors.joining());
            lexer = new Lexer(regex, groups, openingTokenPattern);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return lexer;
    }

    public List<Token> tokenize(String input) {

        return tokenize(input, 0);
    }

    public List<Token> tokenize(String input, int caretPosition) {
        List<Token> tokens = new ArrayList<>();

        tokenize(input, caretPosition, (le, t) -> tokens.add(t));

        return tokens;
    }

    public int tokenize(String input, int caretPosition, BiConsumer<Integer, Token> consumer) {
        Matcher matcher = pattern.matcher(input);
        int lastEnd = 0;
        tokens.clear();
        tokenStack.clear();
        tokensOnCaretPosition.clear();
        int index = 0;

        while (matcher.find()) {
            String type = groups.stream().filter(g -> matcher.group(g) != null).findFirst().orElse("");
            Token token = new Token(index++, matcher.start(), matcher.end(), type, matcher.group());

            if (token.isOnCaretPosition(caretPosition)) {
                tokensOnCaretPosition.add(token);
            }

            updateStack(token);
            tokens.add(token);
            consumer.accept(lastEnd, token);
            lastEnd = matcher.end();
        }

        length.set(input.length());
        
        return lastEnd;
    }

    private void updateStack(Token token) {
        String closeType = openCloseTypes.get(token.getType());
        if (closeType != null) {
            tokenStack.computeIfAbsent(closeType, k -> new ArrayDeque<>()).push(token);
        } else {
            String openType = closeOpenTypes.get(token.getType());
            if (openType != null) {
                var stack = tokenStack.get(token.getType());
                if (stack != null) {
                    var opposite = stack.pollFirst();
                    if (opposite != null) {
                        token.setOppositeToken(opposite);
                        opposite.setOppositeToken(token);
                    }
                }
            }
        }
    }

    public String getOpenTokenPattern() {
        return openTokenPattern;
    }

    public List<Token> getTokensNextToCarretPosition(int caretPosition) {

        if (tokens.isEmpty() || caretPosition < tokens.get(0).getStart()
                || caretPosition > tokens.get(tokens.size() - 1).getEnd()) {
            return List.of();
        }

        List<Token> result = new ArrayList<>();

        Token token = null;
        int index = tokens.size() / 2;
        int start = 0;
        int end = tokens.size();
        Set<Integer> indices = new HashSet<>();

        while (index >= 0 && index < tokens.size() && !indices.contains(index)) {
            token = tokens.get(index);
            indices.add(index);

            if (caretPosition < token.getStart()) {
                end = index;
                index = (start + index) / 2;
            } else if (caretPosition > token.getEnd()) {
                start = index;
                index = (end + index) / 2;
            } else {

                if (caretPosition == token.getStart()) {
                    if (index > 0 && tokens.get(index - 1).getEnd() == caretPosition) {
                        result.add(tokens.get(index - 1));
                    }
                    result.add(token);
                } else if (caretPosition == token.getEnd()) {
                    result.add(token);
                    if (index < tokens.size() - 1 && tokens.get(index + 1).getStart() == caretPosition) {
                        result.add(tokens.get(index + 1));
                    }
                }
                break;
            }

            token = null;
        }

        return result;
    }

    public Optional<Token> getTokenOnCarretPosition(int caretPosition) {

        Optional<Token> result = Optional.empty();

        if (tokens.isEmpty() || caretPosition < tokens.get(0).getStart()
                || caretPosition > tokens.get(tokens.size() - 1).getEnd()) {
            return result;
        }

        Token token = null;
        int index = tokens.size() / 2;
        int start = 0;
        int end = tokens.size();
        Set<Integer> indices = new HashSet<>();

        while (index >= 0 && index < tokens.size() && !indices.contains(index)) {
            token = tokens.get(index);
            indices.add(index);

            if (caretPosition < token.getStart()) {
                end = index;
                index = (start + index) / 2;
            } else if (caretPosition > token.getEnd()) {
                start = index;
                index = (end + index) / 2;
            } else {

                result = Optional.of(token);
                break;
            }

            token = null;
        }

        return result;
    }
}
