module dev.jshfx.access {
    requires javafx.controls;
    requires javafx.graphics;
    
    exports dev.jshfx.access.chart;
    exports dev.jshfx.access.jsh;
    exports dev.jshfx.access.sys to dev.jshfx.base;
    
    uses dev.jshfx.access.sys.JShService;
}