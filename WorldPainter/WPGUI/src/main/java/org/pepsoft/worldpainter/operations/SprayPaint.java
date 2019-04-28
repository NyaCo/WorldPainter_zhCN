package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MapDragControl;
import org.pepsoft.worldpainter.RadiusControl;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.painting.DimensionPainter;
import org.pepsoft.worldpainter.painting.Paint;

/**
 * Created by pepijn on 14-5-15.
 */
public class SprayPaint extends AbstractPaintOperation {
    public SprayPaint(WorldPainterView view, RadiusControl radiusControl, MapDragControl mapDragControl) {
        super("喷枪", "将地表材质，覆盖层或者生物群系喷涂到世界上", view, radiusControl, mapDragControl, 100, "operation.sprayPaint");
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        painter.setUndo(inverse);
        Dimension dimension = getDimension();
        dimension.setEventsInhibited(true);
        try {
            painter.drawPoint(dimension, centreX, centreY, dynamicLevel);
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    @Override
    protected void paintChanged(Paint newPaint) {
        newPaint.setDither(true);
        painter.setPaint(newPaint);
    }

    private final DimensionPainter painter = new DimensionPainter();
}