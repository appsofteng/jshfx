module dev.jshfx.util {
    requires javafx.controls;
    requires javafx.graphics;
    
    exports dev.jshfx.util.chart;
    exports dev.jshfx.util.jsh;
    exports dev.jshfx.util.sys to dev.jshfx.base;
    
    uses dev.jshfx.util.sys.JShService;
}