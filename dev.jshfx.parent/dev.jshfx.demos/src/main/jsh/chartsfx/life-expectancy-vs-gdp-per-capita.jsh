import dev.jshfx.jfxext.scene.chart.Charts
import javafx.scene.chart.XYChart.Data
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import static java.lang.Double.parseDouble
import static java.util.stream.Collectors.collectingAndThen
import static java.util.stream.Collectors.toCollection
import static javafx.collections.FXCollections.observableArrayList

var lines = Files.lines(JSh.resolve("../../resources/ourworldindata/demography/life-expectancy-vs-gdp-per-capita.csv"))

var series = lines
    .map(line -> line.split(",", -1))
    .filter(values -> values[2].equals("2018"))
    .filter(values -> !values[3].isBlank() && !values[4].isBlank())
    .map(values -> { 
        var data = new Data<>(parseDouble(values[4]), parseDouble(values[3]));
        var label = new Label();
        label.setPrefSize(6,6);
        label.setTooltip(new Tooltip(values[0]));
        data.setNode(label);
        return data;
    }).collect(collectingAndThen(toCollection(() -> observableArrayList()), data -> new Series<>(data)))
    
var chart = Charts.getScatterChart(series)
chart.setTitle("Life Expectancy vs GDP per Capita")
chart.getXAxis().setLabel("GDP per Capita (USD)")
chart.getYAxis().setLabel("Life Expectancy")
chart.setLegendVisible(false)

JSh.show(chart)