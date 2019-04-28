/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author SchmitzP
 */
public class Annotations extends Layer {
    private Annotations() {
        super("org.pepsoft.Annotations", "标记层", "有各种颜色的标记层，可以用来选择操作的有效范围", DataSize.NIBBLE, 65);
    }
    
    public static final Annotations INSTANCE = new Annotations();
    
    private static final long serialVersionUID = 1L;
}