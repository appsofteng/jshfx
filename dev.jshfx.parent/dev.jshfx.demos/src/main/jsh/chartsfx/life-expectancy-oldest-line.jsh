import dev.jshfx.access.chart.Charts
import javafx.scene.chart.XYChart.Data
import javafx.scene.chart.XYChart.Series
import static java.lang.Double.parseDouble
import static java.lang.Integer.parseInt
import static java.util.Comparator.comparingInt
import static java.util.stream.Collectors.groupingBy
import static java.util.stream.Collectors.toCollection
import static javafx.collections.FXCollections.observableArrayList

var lines = Files.lines(JSh.getCurDir().resolve("../../resources/ourworldindata/demography/life-expectancy.csv"))
var series = lines
    .skip(1)
    .map(line -> line.split(","))
    .collect(groupingBy(values -> values[0]))
    .entrySet().stream().min(comparingInt(e -> parseInt(e.getValue().get(0)[2])))
    .map(e -> new Series<>(e.getKey(), e.getValue().stream().map(values -> new Data<>(parseInt(values[2]), parseDouble(values[3])))
    .collect(toCollection(() -> observableArrayList())))).get()
    
var chart = Charts.getLineChart(series)
chart.setTitle("Life Expectancy")
chart.getXAxis().setLabel("Year")
chart.getYAxis().setLabel("Age")

JSh.show(chart)
