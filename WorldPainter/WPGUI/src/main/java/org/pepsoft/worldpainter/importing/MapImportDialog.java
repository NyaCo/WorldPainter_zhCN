/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.importing;

import org.pepsoft.minecraft.ChunkStore;
import org.pepsoft.minecraft.Level;
import org.pepsoft.minecraft.MinecraftCoords;
import org.pepsoft.util.FileUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.plugins.BlockBasedPlatformProvider;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.util.MinecraftUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static org.pepsoft.minecraft.Constants.VERSION_ANVIL;
import static org.pepsoft.minecraft.Constants.VERSION_MCREGION;
import static org.pepsoft.worldpainter.Constants.*;

/**
 *
 * @author SchmitzP
 */
public class MapImportDialog extends WorldPainterDialog {
    /**
     * Creates new form MapImportDialog
     */
    public MapImportDialog(App app) {
        super(app);
        this.app = app;
        
        initComponents();
        
        resetStats();

        fieldFilename.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkSelection();
                setControlStates();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkSelection();
                setControlStates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkSelection();
                setControlStates();
            }
        });
        
        getRootPane().setDefaultButton(buttonOK);
        
        setLocationRelativeTo(app);
    }

    public World2 getImportedWorld() {
        return importedWorld;
    }

    private void checkSelection() {
        String fileStr = fieldFilename.getText();
        if (fileStr.endsWith("level.dat")) {
            File file = new File(fileStr);
            if (file.isFile() && (! file.equals(previouslySelectedFile))) {
                previouslySelectedFile = file;
                analyseMap();
            }
        }
    }
    
    private void analyseMap() {
        mapInfo = null;
        resetStats();
        
        File levelDatFile = new File(fieldFilename.getText());
        final File worldDir = levelDatFile.getParentFile();

        // Check if it's a valid level.dat file before we commit
        int version;
        try {
            Level testLevel = Level.load(levelDatFile);
            version = testLevel.getVersion();
        } catch (IOException e) {
            logger.error("IOException while analysing map " + levelDatFile, e);
            JOptionPane.showMessageDialog(MapImportDialog.this, strings.getString("selected.file.is.not.a.valid.level.dat.file"), strings.getString("invalid.file"), ERROR_MESSAGE);
            return;
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException while analysing map " + levelDatFile, e);
            JOptionPane.showMessageDialog(MapImportDialog.this, strings.getString("selected.file.is.not.a.valid.level.dat.file"), strings.getString("invalid.file"), ERROR_MESSAGE);
            return;
        } catch (NullPointerException e) {
            logger.error("NullPointerException while analysing map " + levelDatFile, e);
            JOptionPane.showMessageDialog(MapImportDialog.this, strings.getString("selected.file.is.not.a.valid.level.dat.file"), strings.getString("invalid.file"), ERROR_MESSAGE);
            return;
        }

        // Other sanity checks
        if ((version != VERSION_MCREGION) && (version != VERSION_ANVIL)) {
            logger.error("Unsupported Minecraft version while analysing map " + levelDatFile);
            JOptionPane.showMessageDialog(MapImportDialog.this, strings.getString("unsupported.minecraft.version"), strings.getString("unsupported.version"), ERROR_MESSAGE);
            return;
        }

        // Determine the platform
        Platform platform = PlatformManager.getInstance().identifyMap(worldDir);
        // TODO handle non-block based platform provider matching more gracefully
        BlockBasedPlatformProvider platformProvider = (BlockBasedPlatformProvider) PlatformManager.getInstance().getPlatformProvider(platform);
        if (platform == null) {
            logger.error("Could not determine platform for " + levelDatFile);
            JOptionPane.showMessageDialog(MapImportDialog.this, "Could not determine map format for " + levelDatFile, "Unidentified Map Format", ERROR_MESSAGE);
            return;
        }

        // Sanity checks for the surface dimension
        Set<Integer> dimensions = stream(platformProvider.getDimensions(platform, worldDir)).boxed().collect(toSet());
        if (! dimensions.contains(DIM_NORMAL)) {
            logger.error("Map has no surface dimension: " + levelDatFile);
            JOptionPane.showMessageDialog(MapImportDialog.this, "This map has no surface dimension; this is not supported by WorldPainter", "Missing Surface Dimension", ERROR_MESSAGE);
            return;
        }

        // Check for Nether and End
        final boolean netherPresent = dimensions.contains(DIM_NETHER), endPresent = dimensions.contains(DIM_END);
        checkBoxImportNether.setEnabled(netherPresent);
        checkBoxImportNether.setSelected(netherPresent);
        checkBoxImportEnd.setEnabled(endPresent);
        checkBoxImportEnd.setSelected(endPresent);

        mapInfo = ProgressDialog.executeTask(this, new ProgressTask<MapInfo>() {
            @Override
            public String getName() {
                return "分析地图中...";
            }
            
            @Override
            public MapInfo execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                final MapInfo stats = new MapInfo();

                // TODO do this for the other dimensions as well
                final List<Integer> xValues = new ArrayList<>(), zValues = new ArrayList<>();
                final ChunkStore chunkStore = platformProvider.getChunkStore(platform, worldDir, DIM_NORMAL);
                final Set<MinecraftCoords> allChunkCoords = chunkStore.getChunkCoords();
                stats.chunkCount = allChunkCoords.size();
                for (MinecraftCoords chunkCoords: allChunkCoords) {
                    // TODO update the progress receiver
                    if (chunkCoords.x < stats.lowestChunkX) {
                        stats.lowestChunkX = chunkCoords.x;
                    }
                    if (chunkCoords.x > stats.highestChunkX) {
                        stats.highestChunkX = chunkCoords.x;
                    }
                    if (chunkCoords.z < stats.lowestChunkZ) {
                        stats.lowestChunkZ = chunkCoords.z;
                    }
                    if (chunkCoords.z > stats.highestChunkZ) {
                        stats.highestChunkZ = chunkCoords.z;
                    }
                    xValues.add(chunkCoords.x);
                    zValues.add(chunkCoords.z);
                }

                Collections.sort(xValues);
                int p1 = xValues.size() / 4;
                float q1 = xValues.get(p1) * 0.75f + xValues.get(p1 + 1) * 0.25f;
                int p2 = xValues.size() / 2;
                float q2 = (xValues.get(p2) + xValues.get(p2 + 1)) / 2f;
                int p3 = xValues.size() * 3 / 4;
                float q3 = xValues.get(p3) * 0.25f + xValues.get(p3 + 1) * 0.75f;
                float iqr = q3 - q1;
                int lowerLimit = (int) (q2 - iqr * 1.5f);
                int upperLimit = (int) (q2 + iqr * 1.5f);
                for (MinecraftCoords chunkCoords: allChunkCoords) {
                    if ((chunkCoords.x < lowerLimit) || (chunkCoords.x > upperLimit)) {
                        stats.outlyingChunks.add(chunkCoords);
                    }
                }

                Collections.sort(zValues);
                p1 = zValues.size() / 4;
                q1 = zValues.get(p1) * 0.75f + zValues.get(p1 + 1) * 0.25f;
                p2 = zValues.size() / 2;
                q2 = (zValues.get(p2) + zValues.get(p2 + 1)) / 2f;
                p3 = zValues.size() * 3 / 4;
                q3 = zValues.get(p3) * 0.25f + zValues.get(p3 + 1) * 0.75f;
                iqr = q3 - q1;
                lowerLimit = (int) (q2 - iqr * 1.5f);
                upperLimit = (int) (q2 + iqr * 1.5f);
                for (MinecraftCoords chunkCoords: allChunkCoords) {
                    if ((chunkCoords.z < lowerLimit) || (chunkCoords.z > upperLimit)) {
                        stats.outlyingChunks.add(chunkCoords);
                    }
                }
                
                if (! stats.outlyingChunks.isEmpty()) {
                    allChunkCoords.stream().filter(chunk -> !stats.outlyingChunks.contains(chunk)).forEach(chunk -> {
                        if (chunk.x < stats.lowestChunkXNoOutliers) {
                            stats.lowestChunkXNoOutliers = chunk.x;
                        }
                        if (chunk.x > stats.highestChunkXNoOutliers) {
                            stats.highestChunkXNoOutliers = chunk.x;
                        }
                        if (chunk.z < stats.lowestChunkZNoOutliers) {
                            stats.lowestChunkZNoOutliers = chunk.z;
                        }
                        if (chunk.z > stats.highestChunkZNoOutliers) {
                            stats.highestChunkZNoOutliers = chunk.z;
                        }
                    });
                } else {
                    stats.lowestChunkXNoOutliers = stats.lowestChunkX;
                    stats.highestChunkXNoOutliers = stats.highestChunkX;
                    stats.lowestChunkZNoOutliers = stats.lowestChunkZ;
                    stats.highestChunkZNoOutliers = stats.highestChunkZ;
                }
                
                progressReceiver.setProgress(1.0f);
                return stats;
            }
        });
        if ((mapInfo != null) && (mapInfo.chunkCount > 0)) {
            mapInfo.platform = platform;
            labelPlatform.setText(platform.displayName);
            int width = mapInfo.highestChunkXNoOutliers - mapInfo.lowestChunkXNoOutliers + 1;
            int length = mapInfo.highestChunkZNoOutliers - mapInfo.lowestChunkZNoOutliers + 1;
            int area = (mapInfo.chunkCount - mapInfo.outlyingChunks.size());
            labelWidth.setText(FORMATTER.format(width * 16) + " 块（从 " + FORMATTER.format(mapInfo.lowestChunkXNoOutliers << 4) + " 到 " + FORMATTER.format((mapInfo.highestChunkXNoOutliers << 4) + 15) + "；" + FORMATTER.format(width) + " 区块）");
            labelLength.setText(FORMATTER.format(length * 16) + " 块（从 " + FORMATTER.format(mapInfo.lowestChunkZNoOutliers << 4) + " 到 " + FORMATTER.format((mapInfo.highestChunkZNoOutliers << 4) + 15) + "；" + FORMATTER.format(length) + " 区块）");
            labelArea.setText(FORMATTER.format(area * 256L) + " 块（" + FORMATTER.format(area) + " 区块）");
            if (! mapInfo.outlyingChunks.isEmpty()) {
                // There are outlying chunks
                int widthWithOutliers = mapInfo.highestChunkX - mapInfo.lowestChunkX + 1;
                int lengthWithOutliers = mapInfo.highestChunkZ - mapInfo.lowestChunkZ + 1;
                int areaOfOutliers = mapInfo.outlyingChunks.size();
                labelOutliers1.setVisible(true);
                labelOutliers2.setVisible(true);
                labelWidthWithOutliers.setText(FORMATTER.format(widthWithOutliers * 16) + " 块（" + FORMATTER.format(widthWithOutliers) + " 区块）");
                labelWidthWithOutliers.setVisible(true);
                labelOutliers3.setVisible(true);
                labelLengthWithOutliers.setText(FORMATTER.format(lengthWithOutliers * 16) + " 块（" + FORMATTER.format(lengthWithOutliers) + " 区块）");
                labelLengthWithOutliers.setVisible(true);
                labelOutliers4.setVisible(true);
                labelAreaOutliers.setText(FORMATTER.format(areaOfOutliers * 256L) + " 块（" + FORMATTER.format(areaOfOutliers) + " 区块）");
                labelAreaOutliers.setVisible(true);
                checkBoxImportOutliers.setVisible(true);
                // The dialog may need to become bigger:
                pack();
            }
        }
    }
    
    private void setControlStates() {
        String fileStr = fieldFilename.getText().trim();
        File file = (! fileStr.isEmpty()) ? new File(fileStr) : null;
        if ((mapInfo == null) || (mapInfo.chunkCount == 0) || (file == null) || (! file.isFile())) {
            buttonOK.setEnabled(false);
        } else {
            buttonOK.setEnabled(true);
        }
    }
    
    private void resetStats() {
        labelWidth.setText("0 块（从 ? 到 ?; 0 区块）");
        labelLength.setText("0 块（从 ? 到 ?; 0 区块）");
        labelArea.setText("0 块（0 区块）");

        labelOutliers1.setVisible(false);
        labelOutliers2.setVisible(false);
        labelWidthWithOutliers.setVisible(false);
        labelOutliers3.setVisible(false);
        labelLengthWithOutliers.setVisible(false);
        labelOutliers4.setVisible(false);
        labelAreaOutliers.setVisible(false);
        checkBoxImportOutliers.setSelected(false);
        checkBoxImportOutliers.setVisible(false);
    }
    
    private void selectFile() {
        File mySavesDir = Configuration.getInstance().getSavesDirectory();
        if ((mySavesDir == null) && (MinecraftUtil.findMinecraftDir() != null)) {
            mySavesDir = new File(MinecraftUtil.findMinecraftDir(), "saves");
        }
        File selectedFile = FileUtils.selectFileForOpen(this, "Select Minecraft map level.dat file", mySavesDir, new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().equalsIgnoreCase("level.dat");
            }

            @Override
            public String getDescription() {
                return strings.getString("minecraft.level.dat.file");
            }
        });
        if (selectedFile != null) {
            fieldFilename.setText(selectedFile.getAbsolutePath());
        }        
    }
    
    private void importWorld() {
        final File levelDatFile = new File(fieldFilename.getText());
        final Set<MinecraftCoords> chunksToSkip = checkBoxImportOutliers.isSelected() ? null : mapInfo.outlyingChunks;
        final MapImporter.ReadOnlyOption readOnlyOption;
        if (radioButtonReadOnlyAll.isSelected()) {
            readOnlyOption = MapImporter.ReadOnlyOption.ALL;
        } else if (radioButtonReadOnlyManMade.isSelected()) {
            readOnlyOption = MapImporter.ReadOnlyOption.MAN_MADE;
        } else if (radioButtonReadOnlyManMadeAboveGround.isSelected()) {
            readOnlyOption = MapImporter.ReadOnlyOption.MAN_MADE_ABOVE_GROUND;
        } else {
            readOnlyOption = MapImporter.ReadOnlyOption.NONE;
        }
        app.clearWorld();
        importedWorld = ProgressDialog.executeTask(this, new ProgressTask<World2>() {
            @Override
            public String getName() {
                return strings.getString("importing.world");
            }

            @Override
            public World2 execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                try {
                    Level level = Level.load(levelDatFile);
                    int maxHeight = level.getMaxHeight();
                    int waterLevel;
                    if (level.getVersion() == VERSION_MCREGION) {
                        waterLevel = maxHeight / 2 - 2;
                    } else {
                        waterLevel = 62;
                    }
                    int terrainLevel = waterLevel - 4;
                    TileFactory tileFactory = TileFactoryFactory.createNoiseTileFactory(0, Terrain.GRASS, maxHeight, terrainLevel, waterLevel, false, true, 20, 1.0);
                    Set<Integer> dimensionsToImport = new HashSet<>(3);
                    dimensionsToImport.add(DIM_NORMAL);
                    if (checkBoxImportNether.isSelected()) {
                        dimensionsToImport.add(Constants.DIM_NETHER);
                    }
                    if (checkBoxImportEnd.isSelected()) {
                        dimensionsToImport.add(Constants.DIM_END);
                    }
                    final MapImporter importer = new JavaMapImporter(mapInfo.platform, tileFactory, levelDatFile, false, chunksToSkip, readOnlyOption, dimensionsToImport);
                    World2 world = importer.doImport(progressReceiver);
                    if (importer.getWarnings() != null) {
                        try {
                            SwingUtilities.invokeAndWait(() -> {
                                Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");
                                Toolkit.getDefaultToolkit().beep();
                                int selectedOption = JOptionPane.showOptionDialog(MapImportDialog.this, strings.getString("the.import.process.generated.warnings"), strings.getString("import.warnings"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, warningIcon, new Object[] {strings.getString("review.warnings"), strings.getString("ok")}, null);
                                if (selectedOption == 0) {
                                    ImportWarningsDialog warningsDialog = new ImportWarningsDialog(MapImportDialog.this, strings.getString("import.warnings"));
                                    warningsDialog.setWarnings(importer.getWarnings());
                                    warningsDialog.setVisible(true);
                                }
                            });
                        } catch (InterruptedException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return world;
                } catch (IOException e) {
                    throw new RuntimeException("I/O error while importing world", e);
                }
            }

        });
        if (importedWorld == null) {
            // The import was cancelled
            cancel();
            return;
        }
        
        Configuration config = Configuration.getInstance();
        config.setSavesDirectory(levelDatFile.getParentFile().getParentFile());
        ok();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        fieldFilename = new javax.swing.JTextField();
        buttonSelectFile = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        labelWidth = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        labelLength = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        labelArea = new javax.swing.JLabel();
        labelOutliers3 = new javax.swing.JLabel();
        labelOutliers2 = new javax.swing.JLabel();
        labelOutliers1 = new javax.swing.JLabel();
        labelWidthWithOutliers = new javax.swing.JLabel();
        labelLengthWithOutliers = new javax.swing.JLabel();
        labelOutliers4 = new javax.swing.JLabel();
        labelAreaOutliers = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        checkBoxImportOutliers = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        radioButtonReadOnlyNone = new javax.swing.JRadioButton();
        radioButtonReadOnlyManMade = new javax.swing.JRadioButton();
        radioButtonReadOnlyAll = new javax.swing.JRadioButton();
        radioButtonReadOnlyManMadeAboveGround = new javax.swing.JRadioButton();
        checkBoxImportSurface = new javax.swing.JCheckBox();
        checkBoxImportNether = new javax.swing.JCheckBox();
        checkBoxImportEnd = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        labelPlatform = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("导入现有Minecraft地图");

        jLabel1.setText("从现存的Minecraft地图存档文件夹中导入level.dat文件：");

        buttonSelectFile.setText("...");
        buttonSelectFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectFileActionPerformed(evt);
            }
        });

        jLabel2.setText("地表数据：");

        jLabel3.setText("宽度：");

        labelWidth.setText("0");

        jLabel4.setText("长度：");

        labelLength.setText("0");

        jLabel7.setText("面积：");

        labelArea.setText("0");

        labelOutliers3.setText("边远区块长度：");

        labelOutliers2.setText("边远区块宽度：");

        labelOutliers1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelOutliers1.setText("这张地图有脱离整体的边远区块！");

        labelWidthWithOutliers.setText("0");

        labelLengthWithOutliers.setText("0");

        labelOutliers4.setText("边远区块面积：");

        labelAreaOutliers.setText("0");

        buttonCancel.setText("取消");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("确定");
        buttonOK.setEnabled(false);
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        checkBoxImportOutliers.setText("将边远区块包括在导入范围中");

        jLabel5.setText("选项：");

        buttonGroup1.add(radioButtonReadOnlyNone);
        radioButtonReadOnlyNone.setText("不设置任何一个区块为只读模式");
        radioButtonReadOnlyNone.setToolTipText("如果你确定你的存档没有任何重要建筑，可以选择此选项");

        buttonGroup1.add(radioButtonReadOnlyManMade);
        radioButtonReadOnlyManMade.setText("设置一切有人造方块的区块为只读模式");
        radioButtonReadOnlyManMade.setToolTipText("对于保护建筑来说比较安全的模式，当然存档还是要备份好");

        buttonGroup1.add(radioButtonReadOnlyAll);
        radioButtonReadOnlyAll.setText("设置所有区块为只读模式");
        radioButtonReadOnlyAll.setToolTipText("保护建筑最安全的方法，只能修改存档中没有被探索过的地区");

        buttonGroup1.add(radioButtonReadOnlyManMadeAboveGround);
        radioButtonReadOnlyManMadeAboveGround.setSelected(true);
        radioButtonReadOnlyManMadeAboveGround.setText("设置地表上有人造方块的区块为只读模式");
        radioButtonReadOnlyManMadeAboveGround.setToolTipText("如果你确定你的建筑没有地下部分或者地下部分不重要的话");

        checkBoxImportSurface.setSelected(true);
        checkBoxImportSurface.setText("导入主世界");
        checkBoxImportSurface.setEnabled(false);

        checkBoxImportNether.setText("导入下界");
        checkBoxImportNether.setEnabled(false);

        checkBoxImportEnd.setText("导入末地");
        checkBoxImportEnd.setEnabled(false);

        jLabel6.setText("地图版本：");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fieldFilename)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectFile))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButtonReadOnlyManMadeAboveGround, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radioButtonReadOnlyAll)
                            .addComponent(radioButtonReadOnlyManMade)
                            .addComponent(radioButtonReadOnlyNone)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelLength))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelWidth))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelArea)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelOutliers4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelAreaOutliers))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelOutliers2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelWidthWithOutliers))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelOutliers3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelLengthWithOutliers))
                                    .addComponent(checkBoxImportOutliers)))
                            .addComponent(jLabel5)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(labelOutliers1))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkBoxImportSurface)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxImportNether)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxImportEnd))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelPlatform)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectFile))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(labelPlatform))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(labelOutliers1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(labelWidth)
                    .addComponent(labelOutliers2)
                    .addComponent(labelWidthWithOutliers))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(labelLength)
                    .addComponent(labelOutliers3)
                    .addComponent(labelLengthWithOutliers))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(labelArea)
                    .addComponent(labelOutliers4)
                    .addComponent(labelAreaOutliers))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxImportOutliers)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxImportSurface)
                    .addComponent(checkBoxImportNether)
                    .addComponent(checkBoxImportEnd))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonReadOnlyNone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonReadOnlyManMadeAboveGround, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonReadOnlyManMade)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonReadOnlyAll)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSelectFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectFileActionPerformed
        selectFile();
    }//GEN-LAST:event_buttonSelectFileActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        importWorld();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonSelectFile;
    private javax.swing.JCheckBox checkBoxImportEnd;
    private javax.swing.JCheckBox checkBoxImportNether;
    private javax.swing.JCheckBox checkBoxImportOutliers;
    private javax.swing.JCheckBox checkBoxImportSurface;
    private javax.swing.JTextField fieldFilename;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel labelArea;
    private javax.swing.JLabel labelAreaOutliers;
    private javax.swing.JLabel labelLength;
    private javax.swing.JLabel labelLengthWithOutliers;
    private javax.swing.JLabel labelOutliers1;
    private javax.swing.JLabel labelOutliers2;
    private javax.swing.JLabel labelOutliers3;
    private javax.swing.JLabel labelOutliers4;
    private javax.swing.JLabel labelPlatform;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JLabel labelWidthWithOutliers;
    private javax.swing.JRadioButton radioButtonReadOnlyAll;
    private javax.swing.JRadioButton radioButtonReadOnlyManMade;
    private javax.swing.JRadioButton radioButtonReadOnlyManMadeAboveGround;
    private javax.swing.JRadioButton radioButtonReadOnlyNone;
    // End of variables declaration//GEN-END:variables

    private final App app;
    private File previouslySelectedFile;
    private MapInfo mapInfo;
    private World2 importedWorld;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MapImportDialog.class);
    private static final ResourceBundle strings = ResourceBundle.getBundle("org.pepsoft.worldpainter.resources.strings"); // NOI18N
    private static final NumberFormat FORMATTER = NumberFormat.getIntegerInstance();
    private static final long serialVersionUID = 1L;
    
    static class MapInfo {
        Platform platform;
        int lowestChunkX = Integer.MAX_VALUE, lowestChunkZ = Integer.MAX_VALUE, highestChunkX = Integer.MIN_VALUE, highestChunkZ = Integer.MIN_VALUE;
        int lowestChunkXNoOutliers = Integer.MAX_VALUE, lowestChunkZNoOutliers = Integer.MAX_VALUE, highestChunkXNoOutliers = Integer.MIN_VALUE, highestChunkZNoOutliers = Integer.MIN_VALUE;
        int chunkCount;
        final Set<MinecraftCoords> outlyingChunks = new HashSet<>();
        String errorMessage;
    }
}