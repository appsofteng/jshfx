package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.jx.tools.JavaSourceResolver.HtmlDoc;
import dev.jshfx.jx.tools.GroupNames;
import dev.jshfx.jx.tools.Lexer;
import dev.jshfx.jx.tools.Token;
import picocli.AutoComplete;
import picocli.CommandLine;

class CommandCompletor extends Completor {

    private Token commandToken;
    
    CommandCompletor(CodeArea inputArea, Session session, Lexer lexer) {
        super(inputArea, session, lexer);
    }

    public void setCommandToken(Token commandToken) {
        this.commandToken = commandToken;
    }
    
    @Override
    public void getCompletionItems(boolean contains, Predicate<CompletionItem> items) {
        String parText = "";
        Token tokenOnCaret = null;
        List<String> arguments = new ArrayList<>();
        int relativeCaretPosition = 0;

        if (commandToken != null) {
            String input = commandToken.getValue();
            int originalRelativeCaretPosition = inputArea.getCaretPosition() - commandToken.getStart();
            relativeCaretPosition = originalRelativeCaretPosition;
            arguments = session.getCommandProcessor().getLexer().tokenize(input, relativeCaretPosition).stream()
                    .filter(t -> !t.getType().equals(GroupNames.COMMANDBREAK)).map(Token::getValue)
                    .collect(Collectors.toCollection(() -> new ArrayList<>()));
            tokenOnCaret = session.getCommandProcessor().getLexer().getTokensOnCaretPosition().stream()
                    .filter(t -> !t.getType().equals(GroupNames.COMMANDBREAK)).findFirst().orElse(null);

            if (contains) {
                parText = input;
                while (--relativeCaretPosition >= 0 && !Character.isWhitespace(parText.charAt(relativeCaretPosition))) {
                }
                relativeCaretPosition++;
                parText = parText.substring(relativeCaretPosition, originalRelativeCaretPosition);
            }
        }

        String filter = parText;

        int argIndex = arguments.size() - 1;

        int positionInArg = 0;

        if (tokenOnCaret != null) {
            argIndex = tokenOnCaret.getIndex();
            positionInArg = relativeCaretPosition - tokenOnCaret.getStart();
        } else {
            arguments.add("");
            argIndex++;
        }

        List<CharSequence> candidates = new ArrayList<>();
        String[] args = arguments.toArray(new String[0]);

        int anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args,
                argIndex, positionInArg, relativeCaretPosition, candidates);

        if (candidates.size() == 1 && candidates.get(0).length() == 0 && arguments.size() > 0) {

            candidates.clear();
            arguments.add("");
            argIndex++;
            positionInArg = 0;
            args = arguments.toArray(new String[0]);
            anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args,
                    argIndex, positionInArg, relativeCaretPosition, candidates);
            candidates = candidates.stream().map(c -> " " + c)
                    .collect(Collectors.toCollection(() -> new ArrayList<>()));
        }

        if (candidates.isEmpty() && args.length > 0) {
            args = new String[] { args[0], "" };
            argIndex = 1;
            positionInArg = 0;
            anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args,
                    argIndex, positionInArg, relativeCaretPosition, candidates);
        }

        String arg = args[argIndex];

        int absoluteAnchor = inputArea.getCaretPosition() - (relativeCaretPosition + filter.length() - anchor);

        for (CharSequence candidate : candidates) {

            if (candidate.length() == 0
                    || !filter.isEmpty() && !candidate.toString().toLowerCase().contains(filter.toLowerCase())) {
                continue;
            }

            String commandName = args[0];
            String name = arg.substring(0, positionInArg) + candidate;

            boolean processing = items.test(new CommandCompletionItem(inputArea, absoluteAnchor, candidate.toString(),
                    commandName, name, contains));

            if (!processing) {
                break;
            }
        }

        items.test(null);
    }

    @Override
    public HtmlDoc loadDocumentation(CompletionItem item) {
        HtmlDoc doc = null;
        CommandCompletionItem commandItem = (CommandCompletionItem) item;
        String help = "";
        CommandLine subcommand = session.getCommandProcessor().getCommandLine().getSubcommands()
                .get(commandItem.getName());

        if (subcommand != null) {
            help = "<pre>" + subcommand.getUsageMessage() + "</pre>";
        } else {
            help = FXResourceBundle.getBundle().getStringOrDefault(commandItem.getDocKey(),
                    FXResourceBundle.getBundle().getStringOrDefault(commandItem.getName(), ""));
        }

        if (!help.isEmpty()) {
            doc = new HtmlDoc(null, help, null, help);
        }

        return doc;
    }

    @Override
    public CompletionItem getCompletionItem(String reference, HtmlDoc data) {
        return null;
    }
}
