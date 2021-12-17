import dev.jshfx.jfxext.scene.chart.Charts
import javafx.scene.chart.PieChart.Data
import static java.lang.Double.parseDouble
import static java.lang.Integer.parseInt
import static java.lang.Math.round
import static java.lang.String.format
import static java.util.Comparator.comparingDouble
import static java.util.stream.Collectors.collectingAndThen
import static java.util.stream.Collectors.groupingBy
import static java.util.stream.Collectors.maxBy

var lines = Files.lines(JSh.getCurDir().resolve("../../resources/ourworldindata/education/users-by-social-media-platform.csv"))

var map = lines
    .skip(1)
    .map(line -> line.split(","))
    .collect(groupingBy(values -> values[0], 
     collectingAndThen(maxBy(comparingDouble(values -> parseInt(values[2]))), o -> o.map(values -> new Data(format("%s (%s)", values[0], values[2]), parseDouble(values[3]))).get())))

var sum = map.values().stream().mapToDouble(data -> data.getPieValue()).sum()

var data = map.values().stream().map(d -> {
               d.setName(format("%s %.2f%%", d.getName(), round(d.getPieValue() / sum * 10_000)/10_000.0 * 100));
               return d;
           })
           .sorted(comparingDouble(d -> d.getPieValue()))
           .toList()

var chart = Charts.getPieChart(data)
chart.setTitle("Number of People Using Social Media Platforms")

JSh.show(chart)