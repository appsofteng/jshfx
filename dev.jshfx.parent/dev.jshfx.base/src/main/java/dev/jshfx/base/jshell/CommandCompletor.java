package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.jx.tools.JavaSourceResolver.HtmlDoc;
import dev.jshfx.jx.tools.Token;
import picocli.AutoComplete;
import picocli.CommandLine;

class CommandCompletor extends Completor {

    CommandCompletor(CodeArea inputArea, Session session) {
        super(inputArea, session);
    }

    @Override
    public void getCompletionItems(Predicate<CompletionItem> items) {
        var lineSpan = JShellUtils.getCurrentLineSpan(inputArea);
        String input = lineSpan.text();
        int caretPosition = lineSpan.caretPosition();
        List<String> arguments = session.getCommandProcessor().getLexer().tokenize(input, caretPosition).stream()
                .map(Token::getValue).collect(Collectors.toList());
        Token tokenOnCaret = session.getCommandProcessor().getLexer().getTokenOnCaretPosition();

        int argIndex = arguments.size() - 1;

        int positionInArg = 0;

        if (tokenOnCaret != null) {
            argIndex = tokenOnCaret.getIndex();
            positionInArg = caretPosition - tokenOnCaret.getStart();
        } else if (argIndex >= 0) {
            arguments.add("");
            argIndex++;
        }

        List<CharSequence> candidates = new ArrayList<>();
        String[] args = arguments.toArray(new String[0]);

        int anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args,
                argIndex, positionInArg, caretPosition, candidates);

        if (candidates.size() == 1 && candidates.get(0).length() == 0 && arguments.size() > 0) {

            candidates.clear();
            arguments.add("");
            argIndex++;
            positionInArg = 0;
            args = arguments.toArray(new String[0]);
            anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args,
                    argIndex, positionInArg, caretPosition, candidates);
            candidates = candidates.stream().map(c -> " " + c).collect(Collectors.toCollection(() -> new ArrayList<>()));
        }

        if (candidates.isEmpty() && args.length > 0) {
            args = new String[] { args[0], "" };
            argIndex = 1;
            positionInArg = 0;
            anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args,
                    argIndex, positionInArg, caretPosition, candidates);
        }

        String arg = args[argIndex];

        int absoluteAnchor = inputArea.getCaretPosition() - (caretPosition - anchor);

        for (CharSequence candidate : candidates) {

            if (candidate.length() == 0) {
                continue;
            }

            String commandName = args[0];
            String name = arg.substring(0, positionInArg) + candidate;

            boolean processing = items.test(new CommandCompletionItem(inputArea, absoluteAnchor, candidate.toString(), commandName, name));
            
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
