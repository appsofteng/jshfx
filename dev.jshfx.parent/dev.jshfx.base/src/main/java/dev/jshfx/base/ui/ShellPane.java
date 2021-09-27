package dev.jshfx.base.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.fxmisc.richtext.LineNumberFactory;

import dev.jshfx.base.jshell.Completion;
import dev.jshfx.base.jshell.Session;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.fxmisc.richtext.CodeAreaWrappers;
import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import dev.jshfx.j.util.json.JsonUtils;
import dev.jshfx.jfx.concurrent.CTask;
import dev.jshfx.jfx.concurrent.TaskQueuer;
import dev.jshfx.jfx.scene.control.SplitConsoleView;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.layout.StackPane;

public class ShellPane extends StackPane {

	private SplitConsoleView consoleView;
	private Completion completion;
	private Session session;
	private TaskQueuer taskQueuer = new TaskQueuer();

	public ShellPane() {
		List<String> history = JsonUtils.get().fromJson(FileManager.HISTORY_FILE, List.class, List.of());
		consoleView = new SplitConsoleView(history, List.of("block-delimiter-match"));
		getProperties().put(getClass(), consoleView.getInputArea());
		session = new Session(consoleView, taskQueuer);
		completion = new Completion(session);

		getChildren().add(consoleView);
		
		consoleView.getInputArea().setParagraphGraphicFactory(LineNumberFactory.get(consoleView.getInputArea()));
		
		CodeAreaWrappers.get(consoleView.getInputArea(), "java").style()
				.highlighting(consoleView.getConsoleModel().getReadFromPipe())
				.completion(this::codeCompletion, completion::loadDocumentation).indentation();

		CodeAreaWrappers.get(consoleView.getOutputArea(), "java").style();

		setBehavior();
	}

	private void setBehavior() {
		
        sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
                session.setIO();
            }
        });

		consoleView.getConsoleModel().getInputToOutput().addListener((Change<? extends TextStyleSpans> c) -> {

			while (c.next()) {

				if (c.wasAdded()) {
					List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());
					for (TextStyleSpans span : added) {
						session.processBatch(span.getText());
					}
				}
			}
		});

		consoleView.getHistory().addListener((Change<? extends String> c) -> {

			while (c.next()) {

				if (c.wasAdded() || c.wasRemoved()) {
					List<? extends String> history = new ArrayList<>(consoleView.getHistory());
					JsonUtils.get().toJson(history, FileManager.HISTORY_FILE);
				}
			}
		});
	}

	private void codeCompletion(Consumer<Collection<CompletionItem>> behavior) {

		CTask<Collection<CompletionItem>> task = CTask
				.create(() -> completion.getCompletionItems(consoleView.getInputArea())).onSucceeded(behavior);

		taskQueuer.add(Session.PRIVILEDGED_TASK_QUEUE, task);
	}

	public ReadOnlyBooleanProperty closedProperty() {
		return session.closedProperty();
	}

	public void dispose() {
		var task = CTask.create(() -> session.close()).onFinished(t -> consoleView.dispose());

		taskQueuer.add(Session.PRIVILEDGED_TASK_QUEUE, task);
	}
}
