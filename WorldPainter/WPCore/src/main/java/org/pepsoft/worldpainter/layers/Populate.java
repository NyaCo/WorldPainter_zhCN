/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

/**
 *
 * @author pepijn
 */
public class Populate extends Layer {
    public Populate() {
        super("默认资源填充", "让Minecraft生成天然的植被，雪，资源（煤，矿物之类），水以及熔岩湖", Layer.DataSize.BIT_PER_CHUNK, 0);
    }

    public static final Populate INSTANCE = new Populate();

    private static final long serialVersionUID = 2011040701L;
}