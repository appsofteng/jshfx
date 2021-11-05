package dev.jshfx.cfx.glyphfont;

import org.controlsfx.glyphfont.Glyph;

public class StyleGlyph extends Glyph {

    public StyleGlyph(String fontFamily, Object icon) {
        super(fontFamily, icon);
        setStyle(String.format("-fx-font-family: \"%s\";", fontFamily));
    }    
    
    @Override
    public Glyph duplicate() {
        
        var glyph = super.duplicate();
        glyph.setStyle(getStyle());
        
        return glyph;
    }
}
