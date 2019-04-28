/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Frost extends Layer {
    private Frost() {
        super("积雪", "在地面覆盖一层雪并使水结冰", DataSize.BIT, 60, 'o');
    }

    public static final Frost INSTANCE = new Frost();
    
    private static final long serialVersionUID = 2011032901L;
}
