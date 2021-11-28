package dev.jshfx.base.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public class ConsoleModel {

    private static final int LIMIT = 1500;
    public static final String NORMAL_STYLE = "jsh-console-normal";
    public static final String COMMENT_STYLE = "jsh-console-comment";
    public static final String HELP_STYLE = "jsh-console-help";
    public static final String ERROR_STYLE = "jsh-console-error";
    private ObservableList<TextStyleSpans> input = FXCollections.observableArrayList();
    private ObservableList<TextStyleSpans> inputToOutput = FXCollections.observableArrayList();
    private ObservableList<TextStyleSpans> output = FXCollections.observableArrayList();
    private PipedOutputStream outPipe = new PipedOutputStream();
    private PrintStream outToInStream = new PrintStream(outPipe, true);
    private InputStream in;
    private PrintStream out = new PrintStream(new ConsoleOutputStream(NORMAL_STYLE), true);
    private PrintStream err = new PrintStream(new ConsoleOutputStream(ERROR_STYLE), true);
    private AtomicBoolean readFromPipe = new AtomicBoolean();

    public ConsoleModel() {

        try {
            in = new ConsoleInputStream(outPipe);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setBehavior();
    }

    private void setBehavior() {
        input.addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());

                    if (isReadFromPipe()) {
                        added.stream().map(TextStyleSpans::getText).forEach(outToInStream::print);
                    } else {
                        add(inputToOutput, added, LIMIT);
                    }

                    add(output, added, LIMIT);
                }
            }
        });
    }

    public boolean isReadFromPipe() {
        return readFromPipe.get();
    }

    public AtomicBoolean getReadFromPipe() {
        return readFromPipe;
    }

    public void addInput(TextStyleSpans span) {
        add(input, span, LIMIT);
    }

    public ObservableList<TextStyleSpans> getInputToOutput() {
        return inputToOutput;
    }

    public ObservableList<TextStyleSpans> getOutput() {
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

    public synchronized void addNewLineOutput(TextStyleSpans textStyleSpans) {

        if (textStyleSpans.getText().isEmpty()) {
            return;
        }

        if (!output.isEmpty() && !output.get(output.size() - 1).getText().endsWith("\n")) {
            add(output, new TextStyleSpans("\n"), LIMIT);
        }

        if (!textStyleSpans.getText().isBlank()) {
            add(output, textStyleSpans, LIMIT);
        }
    }

    private <T> void add(ObservableList<T> list, T added, int limit) {
        add(list, List.of(added), limit);
    }
    
    private <T> void add(ObservableList<T> list, List<? extends T> added, int limit) {
        list.addAll(added);

        if (list.size() > limit) {
            list.remove(0, list.size() - limit);
        }
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

            addNewLineOutput(textStyleSpans);
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
