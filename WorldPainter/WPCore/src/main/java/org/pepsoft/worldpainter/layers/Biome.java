/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Biome extends Layer {
    public Biome() {
        super("生物群系", "显示Minecraft会生成的生物群系", Layer.DataSize.BYTE, 70);
    }

    @Override
    public int getDefaultValue() {
        return 255;
    }
    
    public static final Biome INSTANCE = new Biome();
    
    private static final long serialVersionUID = -5510962172433402363L;
}