package dev.jshfx.util.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.jshfx.util.lang.InitArgument;

public class XYChart<X,Y> extends Chart {

    private boolean alternativeColumnFillVisible;
    private boolean alternativeRowFillVisible;   
    private Axis<X> xAxis;
    private Axis<Y> yAxis;
    private List<Series<X,Y>> data = new ArrayList<>();
    
    public XYChart(Axis<X> xAxis, Axis<Y> yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }
    
    @SuppressWarnings("unchecked")
    public XYChart(Stream<? extends Number> x, Stream<? extends Number> y) {
        xAxis = (Axis<X>) new NumberAxis();
        yAxis = (Axis<Y>) new NumberAxis();
        
        Series<? extends Number,? extends Number> s = new Series<>(x,y);
        data.add((Series<X, Y>) s);
    }
    
    @SuppressWarnings("unchecked")
    public XYChart(Stream<? extends Number> x, Function<? extends Number, ? extends Number> y) {
        xAxis = (Axis<X>) new NumberAxis();
        yAxis = (Axis<Y>) new NumberAxis();
        
        Series<Number,Number> s = new Series<>();
        s.setX((Stream<Number>) x);
        s.setY((Function<Number, Number>) y);
        
        data.add((Series<X, Y>) s);
    }
    
    public boolean isAlternativeColumnFillVisible() {
        return alternativeColumnFillVisible;
    }
    
    public void setAlternativeColumnFillVisible(boolean alternativeColumnFillVisible) {
        this.alternativeColumnFillVisible = alternativeColumnFillVisible;
    }
    
    public boolean isAlternativeRowFillVisible() {
        return alternativeRowFillVisible;
    }
    
    public void setAlternativeRowFillVisible(boolean alternativeRowFillVisible) {
        this.alternativeRowFillVisible = alternativeRowFillVisible;
    }
    
//    public boolean isAlternativeColumnFillVisible​() {
//        return alternativeColumnFillVisible​;
//    }
//    
//    public void setAlternativeColumnFillVisible​(boolean alternativeColumnFillVisible​) {
//        this.alternativeColumnFillVisible​ = alternativeColumnFillVisible​;
//    }
    
    @InitArgument(0)
    public Axis<X> getXAxis() {
        return xAxis;
    }
    
    @InitArgument(1)
    public Axis<Y> getYAxis() {
        return yAxis;
    }
    
    public List<Series<X,Y>> getData() {
        return data;
    }
    
    public static class Data<X,Y> {
        
        private X xValue;
        private Y yValue;
        
        public Data() {
        }
                     
        public Data(X xValue, Y yValue) {
            this.xValue = xValue;
            this.yValue = yValue;
        }

        public X getXValue() {
            return xValue;
        }
        
        public void setXValue(X xValue) {
            this.xValue = xValue;
        }
        
        public Y getYValue() {
            return yValue;
        }
        
        public void setYValue(Y yValue) {
            this.yValue = yValue;
        }
    }
    
    public static class Series<X, Y> {

        private Stream<X> xStream;
        private Function<X, Y> yFunction;
        
        public Series() {
        }
     
        public Series(Stream<X> xStream, Function<X, Y> yFunction) {
            this.xStream = xStream;
            this.yFunction = yFunction;
        }
        
        public Series(Stream<X> xStream, Stream<Y> yStream) {
            this.xStream = xStream;
            setY(yStream);
        }

        
        public List<Data<X,Y>> getData() {
            
            return xStream.map(x -> new Data<>(x, yFunction.apply(x))).takeWhile(e -> e.getYValue() != null)
                    .collect(Collectors.toList());
        }

        public void setX(Stream<X> stream) {
            this.xStream = stream;
        }
        
        public void setY(Function<X, Y> function) {
            this.yFunction = function;
        }    
        
        public void setY(Stream<Y> stream) {
            var iterator = stream.iterator();
            this.yFunction = x -> iterator.hasNext() ? iterator.next() : null;
        } 
    }
}
