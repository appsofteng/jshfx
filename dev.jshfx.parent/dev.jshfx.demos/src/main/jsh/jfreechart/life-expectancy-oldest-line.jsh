/resolve org.jfree:jfreechart-fx:1.0.1

import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.ChartFactory
import org.jfree.chart.fx.ChartViewer
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import static java.lang.Integer.parseInt
import static java.lang.Double.parseDouble
import static java.util.Comparator.comparingInt
import static java.util.stream.Collectors.groupingBy

var lines = Files.lines(JSh.resolve("../../resources/ourworldindata/demography/life-expectancy.csv"))
var dataset = lines
    .skip(1)
    .map(line -> line.split(","))
    .collect(groupingBy(values -> values[0]))
    .entrySet().stream().min(comparingInt(e -> parseInt(e.getValue().get(0)[2])))
    .map(e -> { 
        var series = new XYSeries(e.getKey());
        e.getValue().forEach(v -> series.add(parseDouble(v[2]), parseDouble(v[3])));
        return new XYSeriesCollection(series);
    }).get()

var chart = ChartFactory.createXYLineChart("Life Expectancy", "Year", "Age", dataset)
var yearAxis = (NumberAxis)chart.getXYPlot().getDomainAxis();
yearAxis.setNumberFormatOverride(new DecimalFormat("####"))
var viewer = new ChartViewer(chart)

JSh.show(viewer)
