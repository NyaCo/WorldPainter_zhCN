/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Level;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.biomeschemes.*;
import org.pepsoft.worldpainter.util.MinecraftUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.pepsoft.worldpainter.Constants.*;

/**
 *
 * @author pepijn
 */
public class BiomesViewerFrame extends JFrame {
    public BiomesViewerFrame(long seed, int preferredBiomeAlgorithm, ColourScheme colourScheme, SeedListener seedListener) throws HeadlessException {
        this(seed, null, preferredBiomeAlgorithm, colourScheme, seedListener);
    }
    
    public BiomesViewerFrame(long seed, final Point marker, int preferredBiomeAlgorithm, ColourScheme colourScheme, SeedListener seedListener) throws HeadlessException {
        super("WorldPainter - 生物群系视图");
        this.colourScheme = colourScheme;
        this.seedListener = seedListener;
        standAloneMode = App.getInstanceIfExists() == null;
        imageViewer = new BiomesViewer(standAloneMode, Math.max(Runtime.getRuntime().availableProcessors() - 1, 1), true);
        if (marker != null) {
            imageViewer.setMarkerCoords(marker);
            imageViewer.moveToMarker();
        }
        imageViewer.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            int zoom = imageViewer.getZoom();
            if (rotation < 0) {
                zoom = Math.min(zoom + -rotation, 0);
            } else {
                zoom = Math.max(zoom - rotation, -4);
            }
            imageViewer.setZoom(zoom);
        });
        
        if (! standAloneMode) {
            Controller controller = new Controller();
            imageViewer.addMouseListener(controller);
            imageViewer.addMouseMotionListener(controller);
        }
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().add(imageViewer, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new JLabel("生物群系类型："));
        List<Integer> availableAlgorithms = BiomeSchemeManager.getAvailableBiomeAlgorithms();
        //noinspection unchecked // NetBeans visual designer
        schemeChooser = new JComboBox(availableAlgorithms.toArray());
        schemeChooser.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Integer) {
                    switch ((Integer) value) {
                        case BIOME_ALGORITHM_1_1:
                            setText("Minecraft 1.1");
                            break;
                        case BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT:
                            setText("Minecraft 1.2 - 1.6 默认生物群系");
                            break;
                        case BIOME_ALGORITHM_1_3_LARGE:
                            setText("Minecraft 1.3 - 1.6 巨型生物群系");
                            break;
                        case BIOME_ALGORITHM_1_7_DEFAULT:
                            setText("Minecraft 1.7 - 1.12 默认生物群系");
                            break;
                        case BIOME_ALGORITHM_1_7_LARGE:
                            setText("Minecraft 1.7 - 1.12 巨型生物群系");
                            break;
                    }
                }
                return this;
            }
        });
        seedSpinner = new JSpinner(new SpinnerNumberModel(Long.valueOf(seed), Long.valueOf(Long.MIN_VALUE), Long.valueOf(Long.MAX_VALUE), Long.valueOf(1L)));
        if (availableAlgorithms.contains(preferredBiomeAlgorithm)) {
            schemeChooser.setSelectedItem(preferredBiomeAlgorithm);
        } else {
            schemeChooser.setSelectedIndex(0);
        }
        BiomeScheme biomeScheme = BiomeSchemeManager.getNewBiomeScheme((Integer) schemeChooser.getSelectedItem());
        if (biomeScheme != null) {
            this.biomeScheme = biomeScheme;
            this.biomeScheme.setSeed(seed);
            imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, BiomesViewerFrame.this.colourScheme, imageViewer.getZoom(), false));
        }
        schemeChooser.addItemListener(e -> {
            BiomeScheme biomeScheme1 = BiomeSchemeManager.getNewBiomeScheme((Integer) schemeChooser.getSelectedItem());
            if (biomeScheme1 != null) {
                BiomesViewerFrame.this.biomeScheme = biomeScheme1;
                BiomesViewerFrame.this.biomeScheme.setSeed(((Number) seedSpinner.getValue()).longValue());
                imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, BiomesViewerFrame.this.colourScheme, imageViewer.getZoom(), false));
            }
        });
        toolBar.add(schemeChooser);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(new JLabel("种子："));
        seedSpinner.setEditor(new JSpinner.NumberEditor(seedSpinner, "0"));
        seedSpinner.addChangeListener(e -> {
            BiomesViewerFrame.this.biomeScheme.setSeed(((Number) seedSpinner.getValue()).longValue());
            imageViewer.setTileProvider(new BiomesTileProvider(BiomesViewerFrame.this.biomeScheme, BiomesViewerFrame.this.colourScheme, imageViewer.getZoom(), false));
        });
        toolBar.add(seedSpinner);
        toolBar.add(Box.createHorizontalGlue());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        
        toolBar = new JToolBar();
        JButton button = new JButton("-");
        button.addActionListener(e -> {
            int zoom = imageViewer.getZoom();
            zoom = Math.max(zoom - 1, -4);
            imageViewer.setZoom(zoom);
        });
        toolBar.add(button);
        
        button = new JButton("+");
        button.addActionListener(e -> {
            int zoom = imageViewer.getZoom();
            zoom = Math.min(zoom + 1, 0);
            imageViewer.setZoom(zoom);
        });
        toolBar.add(button);
        
        toolBar.add(Box.createHorizontalStrut(5));
        createWorldButton = new JButton("创建世界");
        if (! standAloneMode) {
            createWorldButton.setToolTipText("用选中的区块创建一个新的WorldPainter世界");
            createWorldButton.addActionListener(e -> createWorld());
        }
        createWorldButton.setEnabled(false);
        toolBar.add(createWorldButton);
        
        toolBar.add(Box.createHorizontalStrut(5));
        button = new JButton("重置");
        button.addActionListener(e -> {
            imageViewer.setZoom(-2);
            if (marker != null) {
                imageViewer.moveToMarker();
            } else {
                imageViewer.moveToOrigin();
            }
        });
        toolBar.add(button);
        
        if (seedListener != null) {
            toolBar.add(Box.createHorizontalStrut(5));
            button = new JButton("将种子复制到世界");
            button.setToolTipText("将种子复制到正在编辑的世界");
            button.addActionListener(e -> BiomesViewerFrame.this.seedListener.setSeed(((Number) seedSpinner.getValue()).longValue(), ((schemeChooser.getSelectedIndex() == 1) || (schemeChooser.getSelectedIndex() == 3)) ? Generator.LARGE_BIOMES : Generator.DEFAULT));
            toolBar.add(button);
        }
        
        toolBar.add(Box.createHorizontalStrut(5));
        button = new JButton("生存模式预览");
        button.setToolTipText("用当前种子和位置创建一个Minecraft生存模式存档");
        button.addActionListener(event -> playHere(false));
        toolBar.add(button);

        toolBar.add(Box.createHorizontalStrut(5));
        button = new JButton("创造模式预览");
        button.setToolTipText("用当前种子和位置创建一个Minecraft创造模式存档");
        button.addActionListener(event -> playHere(true));
        toolBar.add(button);

        toolBar.add(Box.createHorizontalGlue());
        getContentPane().add(toolBar, BorderLayout.SOUTH);

        setSize(800, 600);
    }

    public void destroy() {
        imageViewer.removeAllTileProviders();
    }

    private void playHere(boolean creativeMode) {
        String name = JOptionPane.showInputDialog(BiomesViewerFrame.this, "Type a name for the map:", "Map Name", JOptionPane.QUESTION_MESSAGE);
        if ((name == null) || (name.trim().length() == 0)) {
            return;
        }
        name = name.trim();
        File savesDir;
        boolean minecraftDirUsed = false;
        File minecraftDir = MinecraftUtil.findMinecraftDir();
        if (minecraftDir != null) {
            savesDir = new File(minecraftDir, "saves");
            minecraftDirUsed = true;
        } else {
            savesDir = DesktopUtils.getDocumentsFolder();
        }
        File worldDir = new File(savesDir, name);
        int ordinal = 1;
        while (worldDir.isDirectory()) {
            worldDir = new File(savesDir, name + ordinal);
            ordinal++;
        }
        if (! worldDir.mkdirs()) {
            throw new RuntimeException("Could not create " + worldDir);
        }
        BiomeScheme biomeScheme1 = BiomesViewerFrame.this.biomeScheme;
        Level level = new Level(Constants.DEFAULT_MAX_HEIGHT_1, (biomeScheme1 instanceof Minecraft1_1BiomeScheme) ? DefaultPlugin.JAVA_MCREGION : DefaultPlugin.JAVA_ANVIL);
        if (! (biomeScheme1 instanceof Minecraft1_1BiomeScheme)) {
            level.setGenerator(((biomeScheme1 instanceof Minecraft1_3LargeBiomeScheme) || (biomeScheme1 instanceof Minecraft1_7LargeBiomeScheme) || (biomeScheme1 instanceof Minecraft1_8LargeBiomeScheme) || (biomeScheme1 instanceof Minecraft1_12LargeBiomeScheme)) ? Generator.LARGE_BIOMES : Generator.DEFAULT);
        }
        if (creativeMode) {
            level.setGameType(Constants.GAME_TYPE_CREATIVE);
        } else {
            level.setGameType(Constants.GAME_TYPE_SURVIVAL);
        }
        level.setMapFeatures(true);
        level.setName(name);
        level.setSeed(((Number) seedSpinner.getValue()).longValue());
        Point worldCoords = imageViewer.getViewLocation();
        level.setSpawnX(worldCoords.x);
        level.setSpawnZ(worldCoords.y);
        level.setSpawnY(64);
        try {
            level.save(worldDir);
        } catch (IOException e) {
            throw new RuntimeException("I/O error writing level.dat file", e);
        }
        if (minecraftDirUsed) {
            JOptionPane.showMessageDialog(BiomesViewerFrame.this, "地图已保存！你可以在Minecraft单人游戏中找到这个地图。");
        } else {
            JOptionPane.showMessageDialog(BiomesViewerFrame.this, "地图保存为" + worldDir + "。\n如要运行这个地图，请将其移动到Minecraft存档文件夹。");
        }
    }

    private void createWorld() {
        App app = App.getInstanceIfExists();
        if (! app.saveIfNecessary()) {
            return;
        }
        final NewWorldDialog dialog = new NewWorldDialog(
            app,
            "Generated World",
            ((Number) seedSpinner.getValue()).longValue(),
            ((Integer) schemeChooser.getSelectedItem() == BIOME_ALGORITHM_1_1) ? DefaultPlugin.JAVA_MCREGION : DefaultPlugin.JAVA_ANVIL,
            DIM_NORMAL,
            Configuration.getInstance().getDefaultMaxHeight(),
            imageViewer.getSelectedTiles());
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            app.clearWorld();
            if (! dialog.checkMemoryRequirements(this)) {
                return;
            }
            World2 newWorld = ProgressDialog.executeTask(this, new ProgressTask<World2>() {
                @Override
                public String getName() {
                    return "正在创建新世界";
                }
                
                @Override
                public World2 execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                    return dialog.getSelectedWorld(progressReceiver);
                }
            });
            if (newWorld != null) {
                switch ((Integer) schemeChooser.getSelectedItem()) {
                    case BIOME_ALGORITHM_1_1:
                    case BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT:
                    case BIOME_ALGORITHM_1_7_DEFAULT:
                        newWorld.setGenerator(Generator.DEFAULT);
                        break;
                    case BIOME_ALGORITHM_1_3_LARGE:
                    case BIOME_ALGORITHM_1_7_LARGE:
                        newWorld.setGenerator(Generator.LARGE_BIOMES);
                        break;
                }
                app.setWorld(newWorld, true);
            }
        }
    }
    
    private void setControlStates() {
        createWorldButton.setEnabled((! standAloneMode) && (! imageViewer.getSelectedTiles().isEmpty()));
    }

    private final WPTileSelectionViewer imageViewer;
    private final SeedListener seedListener;
    private final JButton createWorldButton;
    private final JSpinner seedSpinner;
    private final JComboBox schemeChooser;
    private final ColourScheme colourScheme;
    private final boolean standAloneMode;
    private BiomeScheme biomeScheme;
    
    private static final long serialVersionUID = 1L;
    
    public interface SeedListener {
        void setSeed(long seed, Generator generator);
    }
    
    class Controller implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            Point tileLocation = getTileLocation(e.getX(), e.getY());
            if (imageViewer.isSelectedTile(tileLocation)) {
                imageViewer.removeSelectedTile(tileLocation);
            } else {
                imageViewer.addSelectedTile(tileLocation);
            }
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
            setControlStates();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            selecting = true;
            selectionCorner1 = getTileLocation(e.getX(), e.getY());
            selectionCorner2 = null;
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            if ((selectionCorner1 != null) && (selectionCorner2 != null)) {
                int tileX1 = Math.min(selectionCorner1.x, selectionCorner2.x);
                int tileX2 = Math.max(selectionCorner1.x, selectionCorner2.x);
                int tileY1 = Math.min(selectionCorner1.y, selectionCorner2.y);
                int tileY2 = Math.max(selectionCorner1.y, selectionCorner2.y);
                for (int x = tileX1; x <= tileX2; x++) {
                    for (int y = tileY1; y <= tileY2; y++) {
                        Point tileLocation = new Point(x, y);
                        if (imageViewer.isSelectedTile(tileLocation)) {
                            imageViewer.removeSelectedTile(tileLocation);
                        } else {
                            imageViewer.addSelectedTile(tileLocation);
                        }
                    }
                }
                setControlStates();
            }
            imageViewer.setSelectedRectangleCorner1(null);
            imageViewer.setSelectedRectangleCorner2(null);
            selecting = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(null);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            imageViewer.setHighlightedTileLocation(getTileLocation(e.getX(), e.getY()));
            if (selecting) {
                selectionCorner2 = getTileLocation(e.getX(), e.getY());
                imageViewer.setSelectedRectangleCorner1(selectionCorner1);
                imageViewer.setSelectedRectangleCorner2(selectionCorner2);
            }
        }

        private Point getTileLocation(int x, int y) {
            Point coords = imageViewer.viewToWorld(x, y);
            return new Point(coords.x >> TILE_SIZE_BITS, coords.y >> TILE_SIZE_BITS);
        }

        private boolean selecting;
        private Point selectionCorner1, selectionCorner2;
    }
}