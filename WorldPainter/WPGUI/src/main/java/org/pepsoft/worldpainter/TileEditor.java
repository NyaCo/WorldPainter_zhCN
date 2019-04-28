/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TileEditor.java
 *
 * Created on Mar 21, 2012, 12:14:42 PM
 */
package org.pepsoft.worldpainter;

import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.history.HistoryEntry;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.NotPresent;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

/**
 *
 * @author pepijn
 */
public class TileEditor extends WorldPainterDialog implements TileSelector.Listener {
    /** Creates new form TileEditor */
    public TileEditor(java.awt.Frame parent, Dimension dimension, ColourScheme colourScheme, BiomeScheme biomeScheme, CustomBiomeManager customBiomeManager, Collection<Layer> hiddenLayers, boolean contourLines, int contourSeparation, TileRenderer.LightOrigin lightOrigin) {
        super(parent);
        this.dimension = dimension;
        initComponents();
        
        // Fix the incredibly ugly default font of the JTextPane
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument) jTextPane1.getDocument()).getStyleSheet().addRule(bodyRule);
        
        tileSelector1.setColourScheme(colourScheme);
        tileSelector1.setBiomeScheme(biomeScheme);
        tileSelector1.setHiddenLayers(hiddenLayers);
        tileSelector1.setContourLines(contourLines);
        tileSelector1.setContourSeparation(contourSeparation);
        tileSelector1.setLightOrigin(lightOrigin);
        tileSelector1.setDimension(dimension);
        tileSelector1.setCustomBiomeManager(customBiomeManager);
        tileSelector1.addListener(this);

        getRootPane().setDefaultButton(buttonClose);
        
        setLocationRelativeTo(parent);
    }
    
    // TileSelector.Listener
    
    @Override
    public void selectionChanged(TileSelector tileSelector, Set<Point> newSelection) {
        setControlStates();
    }
    
    private void setControlStates() {
        Set<Point> selectedTiles = tileSelector1.getSelectedTiles();
        boolean allowAddTiles, allowRemoveTiles;
        if (selectedTiles.isEmpty()) {
            allowAddTiles = allowRemoveTiles = false;
        } else {
            allowAddTiles = false;
            allowRemoveTiles = false;
            for (Point selectedTile: selectedTiles) {
                Tile existingTile = dimension.getTile(selectedTile);
                if (existingTile != null) {
                    allowRemoveTiles = true;
                    if (existingTile.hasLayer(NotPresent.INSTANCE)) {
                        allowAddTiles = true;
                    }
                } else {
                    allowAddTiles = true;
                }
            }
        }
        buttonAddTiles.setEnabled(allowAddTiles);
        buttonRemoveTiles.setEnabled(allowRemoveTiles);
    }
    
    private void addTiles() {
        Set<Point> selectedTiles = tileSelector1.getSelectedTiles();
        Set<Point> tilesToAdd = new HashSet<>(), tilesToExpand = new HashSet<>();
        int newLowestTileX = dimension.getLowestX(), newHighestTileX = dimension.getHighestX();
        int newLowestTileY = dimension.getLowestY(), newHighestTileY = dimension.getHighestY();
        for (Point selectedTile: selectedTiles) {
            Tile existingTile = dimension.getTile(selectedTile);
            if (existingTile == null) {
                tilesToAdd.add(selectedTile);
                if (selectedTile.x < newLowestTileX) {
                    newLowestTileX = selectedTile.x;
                }
                if (selectedTile.x > newHighestTileX) {
                    newHighestTileX = selectedTile.x;
                }
                if (selectedTile.y < newLowestTileY) {
                    newLowestTileY = selectedTile.y;
                }
                if (selectedTile.y > newHighestTileY) {
                    newHighestTileY = selectedTile.y;
                }
            } else if (existingTile.hasLayer(NotPresent.INSTANCE)) {
                tilesToExpand.add(selectedTile);
            }
        }
        if (tilesToAdd.isEmpty() && tilesToExpand.isEmpty()) {
            return;
        }
        
        // Try to guestimate whether there is enough memory to add the selected
        // number of tiles, and if not, ask the user whether they want to
        // continue at their own risk
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long memoryInUse = totalMemory - freeMemory;
        long availableMemory = maxMemory - memoryInUse;
        // Allow room for export
        availableMemory -= 250000000L;
        // Convert to KB
        availableMemory /= 1024;
        // Guestimate data and image size
        long totalEstimatedDataSize = tilesToAdd.size() * NewWorldDialog.ESTIMATED_TILE_DATA_SIZE;
        long totalEstimatedImageSize = (newHighestTileX - newLowestTileX + 1L) * TILE_SIZE * (newHighestTileY - newLowestTileY + 1) * TILE_SIZE * 4 / 1024;
        long currentImageSize = (dimension.getHighestX() - dimension.getLowestX() + 1L) * TILE_SIZE * (dimension.getHighestY() - dimension.getLowestY() + 1) * TILE_SIZE * 4 / 1024;
        long totalEstimatedSize = totalEstimatedDataSize + totalEstimatedImageSize - currentImageSize;
        if (totalEstimatedSize > availableMemory) {
            if (JOptionPane.showConfirmDialog(this, "可能没有足够的内存来添加" + tilesToAdd.size() + "个区块！\n此操作可能会导致失败或出现错误。\n请考虑减少区块添加数量，或安装更多内存。\n你确定要继续此操作吗？", "太多区块", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            String action = (tilesToAdd.isEmpty())
                ? ("扩大" + tilesToExpand.size() + "个区块吗")
                    : (tilesToExpand.isEmpty()
                        ? ("添加" + tilesToAdd.size() + "个新区块吗")
                        : ("添加" + tilesToAdd.size() + "个新区块及扩大" + tilesToExpand.size() + "个区块吗"));
            if (JOptionPane.showConfirmDialog(this, "你确定要" + action + "？\n注意：这个操作无法撤销！", "确认添加/扩大区块", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }            
        }
        
        dimension.setEventsInhibited(true);
        dimension.clearUndo();
        try {
            for (Point newTileCoords: tilesToAdd) {
                Tile newTile = dimension.getTileFactory().createTile(newTileCoords.x, newTileCoords.y);
                dimension.addTile(newTile);
            }
            for (Point expandTileCoords: tilesToExpand) {
                Tile tile = dimension.getTileForEditing(expandTileCoords);
                tile.clearLayerData(NotPresent.INSTANCE);
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
        dimension.armSavePoint();

        World2 world = dimension.getWorld();
        if (world != null) {
            world.addHistoryEntry(HistoryEntry.WORLD_TILES_ADDED, dimension.getName(), tilesToAdd.size());
        }

        tileSelector1.clearSelection();
        tileSelector1.refresh();
        setControlStates();
    }
    
    private void removeTiles() {
        Set<Point> selectedTiles = tileSelector1.getSelectedTiles();
        Set<Point> tilesToRemove = selectedTiles.stream().filter(selectedTile -> dimension.getTile(selectedTile) != null).collect(Collectors.toSet());
        if (tilesToRemove.isEmpty()) {
            return;
        }
        if (tilesToRemove.size() == dimension.getTileCount()) {
            JOptionPane.showMessageDialog(this, "<html>你不能移除这个维度<em>所有</em>区块！</html>", "已选择所有区块", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "你确定要移除" + tilesToRemove.size() + "个区块吗？\n注意：这个操作无法撤销！", "确认移除区块", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dimension.setEventsInhibited(true);
            dimension.clearUndo();
            try {
                tilesToRemove.forEach(dimension::removeTile);
            } finally {
                dimension.setEventsInhibited(false);
            }
            dimension.armSavePoint();
            World2 world = dimension.getWorld();
            if (world != null) {
                world.addHistoryEntry(HistoryEntry.WORLD_TILES_REMOVED, dimension.getName(), tilesToRemove.size());
            }
            tileSelector1.clearSelection();
            tileSelector1.refresh();
            setControlStates();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonClose = new javax.swing.JButton();
        buttonAddTiles = new javax.swing.JButton();
        buttonRemoveTiles = new javax.swing.JButton();
        tileSelector1 = new org.pepsoft.worldpainter.TileSelector();
        jTextPane1 = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("WorldPainter - 添加、扩大或移除区块");

        buttonClose.setText("关闭");
        buttonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCloseActionPerformed(evt);
            }
        });

        buttonAddTiles.setText("添加或扩大区块");
        buttonAddTiles.setEnabled(false);
        buttonAddTiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddTilesActionPerformed(evt);
            }
        });

        buttonRemoveTiles.setText("移除区块");
        buttonRemoveTiles.setEnabled(false);
        buttonRemoveTiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveTilesActionPerformed(evt);
            }
        });

        tileSelector1.setAllowNonExistentTileSelection(true);

        jTextPane1.setEditable(false);
        jTextPane1.setContentType("text/html"); // NOI18N
        jTextPane1.setText("WorldPainter以128*128为一个区块，在这个界面你可以添加、扩大或移除区块。<br><br>用鼠标左键选择区块，用鼠标中键或鼠标右键移动地图界面，然后选择下面的操作：<br><br><b>注意：</b> 这个操作会删除所有的撤销数据！");
        jTextPane1.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonClose)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonAddTiles)
                            .addComponent(buttonRemoveTiles))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tileSelector1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tileSelector1, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonClose))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonAddTiles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemoveTiles)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCloseActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCloseActionPerformed

    private void buttonAddTilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddTilesActionPerformed
        addTiles();
    }//GEN-LAST:event_buttonAddTilesActionPerformed

    private void buttonRemoveTilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveTilesActionPerformed
        removeTiles();
    }//GEN-LAST:event_buttonRemoveTilesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddTiles;
    private javax.swing.JButton buttonClose;
    private javax.swing.JButton buttonRemoveTiles;
    private javax.swing.JTextPane jTextPane1;
    private org.pepsoft.worldpainter.TileSelector tileSelector1;
    // End of variables declaration//GEN-END:variables

    private final Dimension dimension;
    
    private static final long serialVersionUID = 1L;
}