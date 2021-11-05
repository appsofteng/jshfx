package dev.jshfx.cfx.glyphfont;

import org.controlsfx.glyphfont.Glyph;

public class StyleGlyph extends Glyph {

    public StyleGlyph(String fontFamily, Object icon) {
        super(fontFamily, icon);
        updateStyle();
    }    
    
    public StyleGlyph(String fontFamily, Object icon, double fontSize) {
        super(fontFamily, icon);
        setFontSize(fontSize);
        updateStyle();
    } 
    
    private void updateStyle() {
        setStyle(String.format("-fx-font-family: \"%s\"; -fx-font-size: %s;", getFontFamily(), getFontSize())); 
    }
    
    @Override
    public Glyph duplicate() {
        
        var glyph = super.duplicate();
        glyph.setStyle(getStyle());
        
        return glyph;
    }
}
