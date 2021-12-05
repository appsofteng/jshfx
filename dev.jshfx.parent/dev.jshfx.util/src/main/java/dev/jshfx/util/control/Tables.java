package dev.jshfx.util.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;

public final class Tables {

    private Tables() {
    }

    @SuppressWarnings("rawtypes")
    public static TableView<Map> getTable(List<Object[]> data) {
        TableView<Map> tableView = new TableView<>();
        var columns = data.get(0);
        var values = data.get(1);

        for (int i = 0; i < columns.length; i++) {
        
            TableColumn<Map, Object> column = new TableColumn<>(columns[i].toString());
            column.setCellValueFactory(new MapValueFactory<>(columns[i].toString()));

            if (values[i] instanceof Number) {
                column.setStyle("-fx-alignment: CENTER-RIGHT;");
            }

            tableView.getColumns().add(column);
        }
        
        List<Map<String,Object>> maps = new ArrayList<>();
        
        for (int i = 1; i < data.size(); i++) {
            Map<String,Object> map = new TreeMap<>();
            
            for (int j = 0; j < columns.length; j++) {
                map.put(columns[j].toString(), data.get(i)[j]);
            }
            
            maps.add(map);
        }

        tableView.getItems().addAll(maps);

        return tableView;
    }
}
