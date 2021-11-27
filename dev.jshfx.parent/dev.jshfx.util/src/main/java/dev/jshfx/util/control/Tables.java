package dev.jshfx.util.control;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;

public final class Tables {

    private List<TableView<?>> tableViews;

    private Tables(TableView<?>... tables) {
        this.tableViews = Arrays.asList(tables);
    }

    public List<TableView<?>> getTableViews() {
        return tableViews;
    }

    @SuppressWarnings("rawtypes")
    public static <T> TableView<Map> getTable(List<Map<String, T>> data) {
        TableView<Map> tableView = new TableView<>();
        var columns = data.get(0);

        columns.keySet().forEach(k -> {
            TableColumn<Map, T> column = new TableColumn<>(k);
            column.setCellValueFactory(new MapValueFactory<>(k));

            if (columns.get(k) instanceof Number) {
                column.setStyle("-fx-alignment: CENTER-RIGHT;");
            }

            tableView.getColumns().add(column);
        });

        tableView.getItems().addAll(data);

        return tableView;
    }

    public static Tables show(TableView<?>... tableViews) {

        for (int i = 0; i < tableViews.length; i++) {
            if (tableViews[i].getId() == null) {
                tableViews[i].setId("T" + i);
            }
        }

        Tables tables = new Tables(tableViews);

        return tables;
    }
}
