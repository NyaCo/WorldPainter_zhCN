/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Caverns extends Layer {
    private Caverns() {
        super("溶洞", "生成大小各异的地下溶洞", DataSize.NIBBLE, 20, 'c');
    }

    public static final Caverns INSTANCE = new Caverns();
    
    private static final long serialVersionUID = 2011040701L;
}