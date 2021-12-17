import dev.jshfx.jfxext.scene.chart.Charts
import javafx.scene.chart.XYChart.Data
import javafx.scene.chart.XYChart.Series
import static java.lang.Double.parseDouble
import static java.lang.Integer.parseInt
import static java.util.Comparator.comparingDouble
import static java.util.stream.Collectors.groupingBy
import static java.util.stream.Collectors.toCollection
import static javafx.collections.FXCollections.observableArrayList

var lines = Files.lines(JSh.getCurDir().resolve("../../resources/ourworldindata/demography/life-expectancy.csv"))

var countries = lines
    .skip(1)
    .map(line -> line.split(","))
    .collect(groupingBy(values -> values[0]))
    .entrySet().stream().sorted(comparingDouble(e ->  parseDouble(e.getValue().get(e.getValue().size() - 1)[3]))).toList()

var series = IntStream.of(0, countries.size() - 1).mapToObj(i -> new Series<>(countries.get(i).getKey(), countries.get(i).getValue().stream()
    .map(values -> new Data<>(parseInt(values[2]), parseDouble(values[3])))
    .collect(toCollection(() -> observableArrayList())))).toList()     

var chart = Charts.getLineChart(series)

chart.setTitle("Life Expectancy")
chart.getXAxis().setLabel("Year")
chart.getYAxis().setLabel("Age")
JSh.show(chart)
