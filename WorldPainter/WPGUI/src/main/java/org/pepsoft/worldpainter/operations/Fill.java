package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.painting.DimensionPainter;
import org.pepsoft.worldpainter.painting.Paint;

import javax.swing.*;

/**
 * Created by pepijn on 14-5-15.
 */
public class Fill extends MouseOrTabletOperation implements PaintOperation {
    public Fill(WorldPainterView view) {
        super("油漆桶", "用任何覆盖层或地表填充一个区域", view, "operation.fill", "fill");
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        painter.setUndo(inverse);
        Dimension dimension = getDimension();
        dimension.setEventsInhibited(true);
        try {
            painter.fill(dimension, centreX, centreY, SwingUtilities.getWindowAncestor(getView()));
        } catch (IndexOutOfBoundsException e) {
            // This most likely indicates that the area being flooded was too
            // large
            dimension.undoChanges();
            JOptionPane.showMessageDialog(getView(), "要填充的区域过大，请用更小的区域重试", "区域过大", JOptionPane.ERROR_MESSAGE);
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    @Override
    public Paint getPaint() {
        return painter.getPaint();
    }

    @Override
    public void setPaint(Paint paint) {
        painter.setPaint(paint);
    }

    private final DimensionPainter painter = new DimensionPainter();
}