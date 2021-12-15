package dev.jshfx.base.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

public class ConsoleModel {

    private static final int LIMIT = 80000;
    public static final String NORMAL_STYLE = "jsh-console-normal";
    public static final String COMMENT_STYLE = "jsh-console-comment";
    public static final String HELP_STYLE = "jsh-console-help";
    public static final String ERROR_STYLE = "jsh-console-error";
    private StringBuilder textBuilder = new StringBuilder();
    private StyleSpans<Collection<String>> styleSpans;
    private ObjectProperty<TextStyleSpans> output = new SimpleObjectProperty<>(new TextStyleSpans(""));
    private PipedOutputStream outPipe = new PipedOutputStream();
    private PrintStream outToInStream = new PrintStream(outPipe, true);
    private InputStream in;
    private PrintStream out = new PrintStream(new ConsoleOutputStream(NORMAL_STYLE), true);
    private PrintStream err = new PrintStream(new ConsoleOutputStream(ERROR_STYLE), true);
    private AtomicBoolean readFromPipe = new AtomicBoolean();
    private AtomicBoolean setOutput = new AtomicBoolean();
    private ScheduledService<Void> bufferService;
    private ReentrantLock lock = new ReentrantLock();

    public ConsoleModel() {

        try {
            in = new ConsoleInputStream(outPipe);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        spansBuilder.add(Collections.emptyList(), 0);
        styleSpans = spansBuilder.create();

        bufferService = new ScheduledService<>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        if (setOutput.get()) {
                            lock.lock();
                            try {
                                StyleSpansBuilder<Collection<String>> styleSpansBuilder = new StyleSpansBuilder<>();
                                styleSpansBuilder.addAll(styleSpans);
                                output.set(new TextStyleSpans(textBuilder.toString(), styleSpansBuilder.create()));
                            } finally {
                                setOutput.set(false);
                                lock.unlock();
                            }
                        }
                        return null;
                    }
                };
            }
        };

        bufferService.setPeriod(Duration.seconds(1));
        bufferService.start();
    }

    public boolean isReadFromPipe() {
        return readFromPipe.get();
    }

    public AtomicBoolean getReadFromPipe() {
        return readFromPipe;
    }

    public boolean addInput(TextStyleSpans span) {
        boolean evalSnippet = true;

        if (isReadFromPipe()) {
            // Used when System.in.read waits for input string.
            outToInStream.print(span.getText());
            evalSnippet = false;
        }

        addOutput(span);

        return evalSnippet;
    }

    public TextStyleSpans getOutput() {
        return output.get();
    }

    public ReadOnlyObjectProperty<TextStyleSpans> outputProperty() {
        return output;
    }

    public InputStream getIn() {
        return in;
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return err;
    }

    public OutputStream getOut(String style) {
        return new ConsoleOutputStream(style);
    }

    public void addNewLineOutput(TextStyleSpans textStyleSpans) {
        lock.lock();
        try {

            if (textStyleSpans.getText().isEmpty()) {
                return;
            }

            if (!textBuilder.isEmpty() && textBuilder.charAt(textBuilder.length() - 1) != '\n') {
                addOutput(new TextStyleSpans("\n"));
            }

            if (!textStyleSpans.getText().isBlank()) {
                addOutput(textStyleSpans);
            }
        } finally {
            lock.unlock();
        }
    }

    public void addOutput(TextStyleSpans textStyleSpans) {
        lock.lock();
        try {
            textBuilder.append(textStyleSpans.getText());
            styleSpans = styleSpans.concat(textStyleSpans.getStyleSpans());

            int oldLength = textBuilder.length();

            if (oldLength > LIMIT) {
                int index = textBuilder.lastIndexOf("\n", LIMIT);
                if (index > -1) {
                    textBuilder.delete(0, index + 1);
                } else {
                    textBuilder.delete(0, textBuilder.length() - LIMIT);
                    index = textBuilder.indexOf("\n");
                    if (index > -1) {
                        textBuilder.delete(0, index + 1);
                    }
                }

                int newLength = textBuilder.length();
                int from = oldLength - newLength;
                var subView = styleSpans.subView(from, styleSpans.length());
                StyleSpansBuilder<Collection<String>> styleSpansBuilder = new StyleSpansBuilder<>();
                styleSpansBuilder.addAll(subView);
                styleSpans = styleSpansBuilder.create();
            }

            setOutput.set(true);
        } finally {
            lock.unlock();
        }
    }

    public void clear() {

        lock.lock();
        try {
            textBuilder.setLength(0);
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            spansBuilder.add(Collections.emptyList(), 0);
            styleSpans = spansBuilder.create();
            output.set(new TextStyleSpans(textBuilder.toString(), styleSpans));
            setOutput.set(false);
        } finally {
            lock.unlock();
        }
    }

    public void dispose() {
        bufferService.cancel();
    }

    private class ConsoleOutputStream extends ByteArrayOutputStream {

        private String style;

        public ConsoleOutputStream(String style) {
            this.style = style;
        }

        @Override
        public synchronized void flush() throws IOException {
            String string = toString();

            if (string.isEmpty()) {
                return;
            }

            // Windows OS
            string = string.replace("\r", "");

            TextStyleSpans textStyleSpans = new TextStyleSpans(string, style);
            addOutput(textStyleSpans);
            reset();
        }
    }

    private class ConsoleInputStream extends PipedInputStream {

        public ConsoleInputStream(PipedOutputStream outPipe) throws IOException {
            super(outPipe);
        }

        @Override
        public int read() throws IOException {
            readFromPipe.set(true);
            int o = super.read();
            readFromPipe.set(false);
            return o;
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            int o = super.read(b, off, len);
            readFromPipe.set(false);
            return o;
        }
    }
}
