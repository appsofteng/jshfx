package dev.jshfx.util.chart;

import java.util.AbstractMap.SimpleEntry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class Series<X, Y> {

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


    public void iterate(BiConsumer<X, Y> consumer) {

        xStream.map(x -> new SimpleEntry<>(x, yFunction.apply(x))).takeWhile(e -> e.getValue() != null)
                .forEach(e -> consumer.accept(e.getKey(), e.getValue()));
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
