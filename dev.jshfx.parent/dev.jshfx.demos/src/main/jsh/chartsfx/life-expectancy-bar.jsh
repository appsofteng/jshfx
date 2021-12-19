import dev.jshfx.jfxext.scene.chart.Charts
import javafx.scene.chart.XYChart.Data
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.Label
import javafx.scene.text.Font
import static java.lang.Double.parseDouble
import static java.util.Comparator.comparingDouble
import static java.util.stream.Collectors.collectingAndThen
import static java.util.stream.Collectors.groupingBy
import static java.util.stream.Collectors.toCollection
import static javafx.collections.FXCollections.observableArrayList

var lines = Files.lines(JSh.resolve("../../resources/ourworldindata/demography/life-expectancy.csv"))

var series = lines
    .skip(1)
    .map(line -> line.split(","))
    .collect(groupingBy(values -> values[0]))
    .entrySet().stream().map(e -> {
        var data = new Data<>(parseDouble(e.getValue().get(e.getValue().size() - 1)[3]), e.getKey());
        data.setNode(new Label(data.getXValue().toString()));
        return data;
    }).sorted(comparingDouble(d -> d.getXValue()))
    .collect(collectingAndThen(toCollection(() -> observableArrayList()), data -> new Series<>(data)))

var chart = Charts.getBarChart(series)
chart.setLegendVisible(false)
chart.setTitle("Life Expectancy")
chart.getXAxis().setLabel("Age")
chart.getYAxis().setLabel("Country")
chart.getYAxis().setTickLabelFont(new Font(14))
chart.setPrefHeight(series.getData().size() * chart.getYAxis().getTickLabelFont().getSize() * 3)
JSh.show(chart)
