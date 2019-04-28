package org.pepsoft.worldpainter.layers;

/**
 * Created by Pepijn on 15-1-2017.
 */
public class Caves extends Layer {
    private Caves() {
        super("org.pepsoft.Caves", "洞穴", "Generate underground tunnel-like caves of varying size", DataSize.NIBBLE, 23);
    }

    public static final Caves INSTANCE = new Caves();

    private static final long serialVersionUID = 1L;
}
