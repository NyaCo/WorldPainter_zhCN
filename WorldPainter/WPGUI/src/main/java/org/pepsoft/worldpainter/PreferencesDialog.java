/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PreferencesDialog.java
 *
 * Created on Apr 26, 2012, 3:18:17 PM
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.GUIUtils;
import org.pepsoft.worldpainter.TileRenderer.LightOrigin;
import org.pepsoft.worldpainter.themes.SimpleTheme;
import org.pepsoft.worldpainter.themes.TerrainListCellRenderer;
import org.pepsoft.worldpainter.util.EnumListCellRenderer;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;

import static org.pepsoft.minecraft.Constants.DEFAULT_MAX_HEIGHT_2;
import static org.pepsoft.worldpainter.Constants.DIM_NORMAL;
import static org.pepsoft.worldpainter.Terrain.GRASS;

/**
 *
 * @author pepijn
 */
public class PreferencesDialog extends WorldPainterDialog {
    /** Creates new form PreferencesDialog */
    public PreferencesDialog(java.awt.Frame parent, ColourScheme colourScheme) {
        super(parent);
        this.colourScheme = colourScheme;
        
        initComponents();
        if (GUIUtils.UI_SCALE > 1) {
//            comboBoxLookAndFeel.setEnabled(false);
//            jLabel32.setText("<html><em>视觉主题不适用于高分辨显示</em></html>");
            jLabel32.setText("<html><em>高分辨显示建议使用System</em></html>");
        }
        
        comboBoxSurfaceMaterial.setModel(new DefaultComboBoxModel(Terrain.PICK_LIST));
        comboBoxSurfaceMaterial.setRenderer(new TerrainListCellRenderer(colourScheme));
        comboBoxMode.setModel(new DefaultComboBoxModel<>(GameType.values()));
        comboBoxMode.setRenderer(new EnumListCellRenderer());
        comboBoxWorldType.setModel(new DefaultComboBoxModel<>(Generator.values()));
        comboBoxWorldType.setRenderer(new EnumListCellRenderer());

        List<AccelerationType> accelTypes = AccelerationType.getForThisOS();
        radioButtonAccelDefault.setEnabled(accelTypes.contains(AccelerationType.DEFAULT));
        radioButtonAccelDirect3D.setEnabled(accelTypes.contains(AccelerationType.DIRECT3D));
        radioButtonAccelOpenGL.setEnabled(accelTypes.contains(AccelerationType.OPENGL));
        radioButtonAccelQuartz.setEnabled(accelTypes.contains(AccelerationType.QUARTZ));
        radioButtonAccelUnaccelerated.setEnabled(accelTypes.contains(AccelerationType.UNACCELERATED));
        radioButtonAccelXRender.setEnabled(accelTypes.contains(AccelerationType.XRENDER));

        loadSettings();
        
        rootPane.setDefaultButton(buttonOK);
        pack();
        setLocationRelativeTo(parent);
    }
    
    public void ok() {
        saveSettings();
        super.ok();
    }
    
    private void loadSettings() {
        Configuration config = Configuration.getInstance();
        if (Main.privateContext == null) {
            checkBoxPing.setSelected(false);
            checkBoxPing.setEnabled(false);
            pingNotSet = true;
        } else if (config.getPingAllowed() != null) {
            checkBoxPing.setSelected(config.getPingAllowed());
        } else {
            checkBoxPing.setSelected(false);
            pingNotSet = true;
        }
        if ((Main.privateContext == null)
                || "true".equals(System.getProperty("org.pepsoft.worldpainter.devMode"))
                || "true".equals(System.getProperty("org.pepsoft.worldpainter.disableUpdateCheck"))) {
            checkBoxCheckForUpdates.setSelected(false);
            checkBoxCheckForUpdates.setEnabled(false);
        } else {
            checkBoxCheckForUpdates.setSelected(config.isCheckForUpdates());
        }
        if ("true".equals(System.getProperty("org.pepsoft.worldpainter.disableUndo"))) {
            checkBoxUndo.setSelected(false);
            checkBoxUndo.setEnabled(false);
            spinnerUndoLevels.setEnabled(false);
        } else {
            checkBoxUndo.setSelected(config.isUndoEnabled());
            spinnerUndoLevels.setValue(config.getUndoLevels());
        }
        
        checkBoxGrid.setSelected(config.isDefaultGridEnabled());
        spinnerGrid.setValue(config.getDefaultGridSize());
        checkBoxContours.setSelected(config.isDefaultContoursEnabled());
        spinnerContours.setValue(config.getDefaultContourSeparation());
        checkBoxViewDistance.setSelected(config.isDefaultViewDistanceEnabled());
        checkBoxWalkingDistance.setSelected(config.isDefaultWalkingDistanceEnabled());
        comboBoxLightDirection.setSelectedItem(config.getDefaultLightOrigin());
        checkBoxCircular.setSelected(config.isDefaultCircularWorld());
        spinnerBrushSize.setValue(config.getMaximumBrushSize());
        
        spinnerWidth.setValue(config.getDefaultWidth() * 128);
        spinnerHeight.setValue(config.getDefaultHeight() * 128);
        comboBoxHeight.setSelectedItem(Integer.toString(config.getDefaultMaxHeight()));
        if (config.isHilly()) {
            radioButtonHilly.setSelected(true);
        } else {
            radioButtonFlat.setSelected(true);
            spinnerRange.setEnabled(false);
            spinnerScale.setEnabled(false);
        }
        spinnerRange.setValue((int) (config.getDefaultRange() + 0.5f));
        spinnerScale.setValue((int) (config.getDefaultScale() * 100 + 0.5f));
        spinnerGroundLevel.setValue(config.getLevel());
        spinnerWaterLevel.setValue(config.getWaterLevel());
        checkBoxLava.setSelected(config.isLava());
        checkBoxBeaches.setSelected(config.isBeaches());
        comboBoxSurfaceMaterial.setSelectedItem(config.getSurface());
        spinnerWorldBackups.setValue(config.getWorldFileBackups());
        textFieldFont.setText(config.getFont());
        spinnerFontSize.setValue(config.getFontSize());
        spinnerBrushRow.setValue(config.getBrushRow());
        spinnerBrushColumn.setValue(config.getBrushColumn());
        checkBoxExtendedBlockIds.setSelected(config.isDefaultExtendedBlockIds());
        
        // Export settings
        checkBoxChestOfGoodies.setSelected(config.isDefaultCreateGoodiesChest());
        comboBoxWorldType.setSelectedIndex(config.getDefaultGenerator().ordinal());
        generatorOptions = config.getDefaultGeneratorOptions();
        checkBoxStructures.setSelected(config.isDefaultMapFeatures());
        comboBoxMode.setSelectedItem(config.getDefaultGameType());
        checkBoxCheats.setSelected(config.isDefaultAllowCheats());

        previousExp = (int) Math.round(Math.log(config.getDefaultMaxHeight()) / Math.log(2.0));

//        if (GUIUtils.UI_SCALE == 1) {
            comboBoxLookAndFeel.setSelectedIndex(config.getLookAndFeel() != null ? config.getLookAndFeel().ordinal() : 0);
//        }
        
        switch (config.getAccelerationType()) {
            case DEFAULT:
                radioButtonAccelDefault.setSelected(true);
                break;
            case DIRECT3D:
                radioButtonAccelDirect3D.setSelected(true);
                break;
            case OPENGL:
                radioButtonAccelOpenGL.setSelected(true);
                break;
            case QUARTZ:
                radioButtonAccelQuartz.setSelected(true);
                break;
            case UNACCELERATED:
                radioButtonAccelUnaccelerated.setSelected(true);
                break;
            case XRENDER:
                radioButtonAccelXRender.setSelected(true);
                break;
        }
        
        switch (config.getOverlayType()) {
            case OPTIMISE_ON_LOAD:
                radioButtonOverlayOptimiseOnLoad.setSelected(true);
                break;
            case SCALE_ON_LOAD:
                radioButtonOverlayScaleOnLoad.setSelected(true);
                break;
            case SCALE_ON_PAINT:
                radioButtonOverlayScaleOnPaint.setSelected(true);
                break;
        }
        
        checkBoxAutoSave.setSelected(config.isAutosaveEnabled());
        spinnerAutoSaveGuardTime.setValue(config.getAutosaveDelay() / 1000);
        spinnerAutoSaveInterval.setValue(config.getAutosaveInterval() / 1000);
        
        setControlStates();
    }
    
    private void saveSettings() {
        Configuration config = Configuration.getInstance();
        if (! pingNotSet) {
            config.setPingAllowed(checkBoxPing.isSelected());
        }
        if ((! "true".equals(System.getProperty("org.pepsoft.worldpainter.devMode")))
                && (! "true".equals(System.getProperty("org.pepsoft.worldpainter.disableUpdateCheck")))) {
            config.setCheckForUpdates(checkBoxCheckForUpdates.isSelected());
        }
        if (! "true".equals(System.getProperty("org.pepsoft.worldpainter.disableUndo"))) {
            config.setUndoEnabled(checkBoxUndo.isSelected());
            config.setUndoLevels(((Number) spinnerUndoLevels.getValue()).intValue());
        }
        config.setDefaultGridEnabled(checkBoxGrid.isSelected());
        config.setDefaultGridSize((Integer) spinnerGrid.getValue());
        config.setDefaultContoursEnabled(checkBoxContours.isSelected());
        config.setDefaultContourSeparation((Integer) spinnerContours.getValue());
        config.setDefaultViewDistanceEnabled(checkBoxViewDistance.isSelected());
        config.setDefaultWalkingDistanceEnabled(checkBoxWalkingDistance.isSelected());
        config.setDefaultLightOrigin((LightOrigin) comboBoxLightDirection.getSelectedItem());
        config.setDefaultWidth(((Integer) spinnerWidth.getValue()) / 128);
        // Set defaultCircularWorld *before* defaultHeight, otherwise defaultHeight might not take
        config.setDefaultCircularWorld(checkBoxCircular.isSelected());
        config.setDefaultHeight(((Integer) spinnerHeight.getValue()) / 128);
        config.setDefaultMaxHeight(Integer.parseInt((String) comboBoxHeight.getSelectedItem()));
        config.setHilly(radioButtonHilly.isSelected());
        config.setDefaultRange(((Number) spinnerRange.getValue()).floatValue());
        config.setDefaultScale((Integer) spinnerScale.getValue() / 100.0);
        config.setLevel((Integer) spinnerGroundLevel.getValue());
        config.setWaterLevel((Integer) spinnerWaterLevel.getValue());
        config.setLava(checkBoxLava.isSelected());
        config.setBeaches(checkBoxBeaches.isSelected());
        config.setSurface((Terrain) comboBoxSurfaceMaterial.getSelectedItem());
        config.setWorldFileBackups((Integer) spinnerWorldBackups.getValue());
        config.setFont((String) textFieldFont.getText());
        config.setFontSize((Integer) spinnerFontSize.getValue());
        config.setBrushRow((Integer) spinnerBrushRow.getValue());
        config.setBrushColumn((Integer) spinnerBrushColumn.getValue());
        config.setMaximumBrushSize((Integer) spinnerBrushSize.getValue());
        config.setDefaultExtendedBlockIds(checkBoxExtendedBlockIds.isSelected());
        
        // Export settings
        config.setDefaultCreateGoodiesChest(checkBoxChestOfGoodies.isSelected());
        config.setDefaultGenerator(Generator.values()[comboBoxWorldType.getSelectedIndex()]);
        config.setDefaultGeneratorOptions(generatorOptions);
        config.setDefaultMapFeatures(checkBoxStructures.isSelected());
        config.setDefaultGameType((GameType) comboBoxMode.getSelectedItem());
        config.setDefaultAllowCheats(checkBoxCheats.isSelected());

//        if (GUIUtils.UI_SCALE == 1) {
            config.setLookAndFeel(Configuration.LookAndFeel.values()[comboBoxLookAndFeel.getSelectedIndex()]);
//        }
        
        if (radioButtonAccelDefault.isSelected()) {
            config.setAccelerationType(AccelerationType.DEFAULT);
        } else if (radioButtonAccelDirect3D.isSelected()) {
            config.setAccelerationType(AccelerationType.DIRECT3D);
        } else if (radioButtonAccelOpenGL.isSelected()) {
            config.setAccelerationType(AccelerationType.OPENGL);
        } else if (radioButtonAccelQuartz.isSelected()) {
            config.setAccelerationType(AccelerationType.QUARTZ);
        } else if (radioButtonAccelUnaccelerated.isSelected()) {
            config.setAccelerationType(AccelerationType.UNACCELERATED);
        } else if (radioButtonAccelXRender.isSelected()) {
            config.setAccelerationType(AccelerationType.XRENDER);
        }
        
        if (radioButtonOverlayOptimiseOnLoad.isSelected()) {
            config.setOverlayType(Configuration.OverlayType.OPTIMISE_ON_LOAD);
        } else if (radioButtonOverlayScaleOnLoad.isSelected()) {
            config.setOverlayType(Configuration.OverlayType.SCALE_ON_LOAD);
        } else if (radioButtonOverlayScaleOnPaint.isSelected()) {
            config.setOverlayType(Configuration.OverlayType.SCALE_ON_PAINT);
        }
        
        config.setAutosaveEnabled(checkBoxAutoSave.isSelected());
        config.setAutosaveDelay(((Integer) spinnerAutoSaveGuardTime.getValue()) * 1000);
        config.setAutosaveInterval(((Integer) spinnerAutoSaveInterval.getValue()) * 1000);
        
        try {
            config.save();
        } catch (IOException e) {
            ErrorDialog errorDialog = new ErrorDialog(this);
            errorDialog.setException(e);
            errorDialog.setVisible(true);
        }
    }
    
    private void setControlStates() {
        spinnerUndoLevels.setEnabled(checkBoxUndo.isSelected());
        boolean hilly = radioButtonHilly.isSelected();
        spinnerRange.setEnabled(hilly);
        spinnerScale.setEnabled(hilly);
        spinnerHeight.setEnabled(! checkBoxCircular.isSelected());
        buttonModePreset.setEnabled(comboBoxWorldType.getSelectedIndex() == 1);
        boolean autosaveEnabled = checkBoxAutoSave.isSelected();
        boolean autosaveInhibited = Configuration.getInstance().isAutosaveInhibited();
        checkBoxAutoSave.setEnabled(! autosaveInhibited);
        spinnerAutoSaveGuardTime.setEnabled(autosaveEnabled && (! autosaveInhibited));
        spinnerAutoSaveInterval.setEnabled(autosaveEnabled && (! autosaveInhibited));
    }
    
    private void editTerrainAndLayerSettings() {
        Configuration config = Configuration.getInstance();
        Dimension defaultSettings = config.getDefaultTerrainAndLayerSettings();
        DimensionPropertiesDialog dialog = new DimensionPropertiesDialog(this, defaultSettings, colourScheme, true);
        dialog.setVisible(true);
        TileFactory tileFactory = defaultSettings.getTileFactory();
        if ((tileFactory instanceof HeightMapTileFactory)
                && (((HeightMapTileFactory) tileFactory).getTheme() instanceof SimpleTheme)) {
            HeightMapTileFactory heightMapTileFactory = (HeightMapTileFactory) tileFactory;
            SimpleTheme theme = (SimpleTheme) ((HeightMapTileFactory) tileFactory).getTheme();
            checkBoxBeaches.setSelected(theme.isBeaches());
            int waterLevel = heightMapTileFactory.getWaterHeight();
            spinnerWaterLevel.setValue(waterLevel);
            defaultSettings.setBorderLevel(heightMapTileFactory.getWaterHeight());
            SortedMap<Integer, Terrain> terrainRanges = theme.getTerrainRanges();
            comboBoxSurfaceMaterial.setSelectedItem(terrainRanges.get(terrainRanges.headMap(waterLevel + 3).lastKey()));
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jRadioButton3 = new javax.swing.JRadioButton();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        checkBoxPing = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        checkBoxCheckForUpdates = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        spinnerWorldBackups = new javax.swing.JSpinner();
        jLabel30 = new javax.swing.JLabel();
        comboBoxLookAndFeel = new javax.swing.JComboBox();
        jLabel32 = new javax.swing.JLabel();
        checkBoxAutoSave = new javax.swing.JCheckBox();
        jLabel45 = new javax.swing.JLabel();
        spinnerAutoSaveGuardTime = new javax.swing.JSpinner();
        jLabel46 = new javax.swing.JLabel();
        spinnerAutoSaveInterval = new javax.swing.JSpinner();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        checkBoxUndo = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        spinnerUndoLevels = new javax.swing.JSpinner();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        checkBoxGrid = new javax.swing.JCheckBox();
        checkBoxContours = new javax.swing.JCheckBox();
        checkBoxViewDistance = new javax.swing.JCheckBox();
        jLabel22 = new javax.swing.JLabel();
        comboBoxLightDirection = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        spinnerGrid = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        spinnerContours = new javax.swing.JSpinner();
        checkBoxWalkingDistance = new javax.swing.JCheckBox();
        jLabel26 = new javax.swing.JLabel();
        spinnerBrushSize = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        spinnerWidth = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        spinnerHeight = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        comboBoxHeight = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        radioButtonHilly = new javax.swing.JRadioButton();
        jLabel23 = new javax.swing.JLabel();
        spinnerRange = new javax.swing.JSpinner();
        jLabel24 = new javax.swing.JLabel();
        spinnerScale = new javax.swing.JSpinner();
        jLabel25 = new javax.swing.JLabel();
        radioButtonFlat = new javax.swing.JRadioButton();
        checkBoxCircular = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        spinnerGroundLevel = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        spinnerWaterLevel = new javax.swing.JSpinner();
        checkBoxLava = new javax.swing.JCheckBox();
        checkBoxBeaches = new javax.swing.JCheckBox();
        jLabel16 = new javax.swing.JLabel();
        comboBoxSurfaceMaterial = new javax.swing.JComboBox();
        checkBoxExtendedBlockIds = new javax.swing.JCheckBox();
        buttonReset = new javax.swing.JButton();
        labelTerrainAndLayerSettings = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        checkBoxChestOfGoodies = new javax.swing.JCheckBox();
        jLabel28 = new javax.swing.JLabel();
        comboBoxWorldType = new javax.swing.JComboBox<>();
        buttonModePreset = new javax.swing.JButton();
        checkBoxStructures = new javax.swing.JCheckBox();
        jLabel29 = new javax.swing.JLabel();
        comboBoxMode = new javax.swing.JComboBox<>();
        checkBoxCheats = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        radioButtonAccelDefault = new javax.swing.JRadioButton();
        radioButtonAccelDirect3D = new javax.swing.JRadioButton();
        radioButtonAccelOpenGL = new javax.swing.JRadioButton();
        radioButtonAccelQuartz = new javax.swing.JRadioButton();
        radioButtonAccelXRender = new javax.swing.JRadioButton();
        radioButtonAccelUnaccelerated = new javax.swing.JRadioButton();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        radioButtonOverlayScaleOnLoad = new javax.swing.JRadioButton();
        radioButtonOverlayOptimiseOnLoad = new javax.swing.JRadioButton();
        radioButtonOverlayScaleOnPaint = new javax.swing.JRadioButton();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        spinnerFontSize = new javax.swing.JSpinner();
        jLabel50 = new javax.swing.JLabel();
        textFieldFont = new javax.swing.JTextField();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        spinnerBrushRow = new javax.swing.JSpinner();
        spinnerBrushColumn = new javax.swing.JSpinner();
        jLabel53 = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();

        jRadioButton3.setText("jRadioButton3");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("参数设置");

        checkBoxPing.setSelected(true);
        checkBoxPing.setText("向开发者发送使用信息");
        checkBoxPing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPingActionPerformed(evt);
            }
        });

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel3.setText("这些信息不包括有关个人身份的信息，");

        jLabel4.setFont(jLabel4.getFont().deriveFont((jLabel4.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel4.setText("也永远不会出售或提供给第三方组织。");

        checkBoxCheckForUpdates.setSelected(true);
        checkBoxCheckForUpdates.setText("在启动时检查更新");

        jLabel20.setText("保存自动备份的数量：");

        spinnerWorldBackups.setModel(new javax.swing.SpinnerNumberModel(3, 0, null, 1));

        jLabel30.setText("视觉主题：");

        comboBoxLookAndFeel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "System", "Metal", "Nimbus", "Dark Metal", "Dark Nimbus", "Pgs", "Acryl", "Aero", "Aluminium", "Bernstein", "Fast", "Graphite", "HiFi", "Luna", "McWin", "Mint", "Noire", "Smart" }));

        jLabel32.setText("<html><em>重启软件后生效</em></html>");

        checkBoxAutoSave.setSelected(true);
        checkBoxAutoSave.setText("启用自动保存");
        checkBoxAutoSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxAutoSaveActionPerformed(evt);
            }
        });

        jLabel45.setText("保护时间：");

        spinnerAutoSaveGuardTime.setModel(new javax.swing.SpinnerNumberModel(10, 1, 999, 1));

        jLabel46.setText("自动保存间隔：");

        spinnerAutoSaveInterval.setModel(new javax.swing.SpinnerNumberModel(300, 1, 9999, 1));

        jLabel47.setText("秒");

        jLabel48.setText("秒");

        checkBoxUndo.setSelected(true);
        checkBoxUndo.setText("启用撤销操作");
        checkBoxUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxUndoActionPerformed(evt);
            }
        });

        jLabel5.setLabelFor(spinnerUndoLevels);
        jLabel5.setText("最大撤销次数：");

        spinnerUndoLevels.setModel(new javax.swing.SpinnerNumberModel(25, 1, 999, 1));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxPing)
                    .addComponent(checkBoxCheckForUpdates)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerWorldBackups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboBoxLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(checkBoxAutoSave)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel45)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerAutoSaveGuardTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel47))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel46)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerAutoSaveInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel48))))
                    .addComponent(checkBoxUndo)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerUndoLevels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxPing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(checkBoxCheckForUpdates)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(spinnerWorldBackups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(checkBoxAutoSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(spinnerAutoSaveGuardTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel47))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(spinnerAutoSaveInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel48))
                .addGap(18, 18, 18)
                .addComponent(checkBoxUndo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(spinnerUndoLevels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(comboBoxLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("常规", jPanel1);

        jLabel1.setText("在这个界面设置默认参数：");

        jLabel18.setFont(jLabel18.getFont().deriveFont((jLabel18.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel18.setText("（注意：这些设置将在你下次读取或创建世界时才会生效。）");

        jLabel6.setFont(jLabel6.getFont().deriveFont((jLabel6.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel6.setText("默认视图设置：");

        checkBoxGrid.setText("显示网格");

        checkBoxContours.setSelected(true);
        checkBoxContours.setText("显示等高线");

        checkBoxViewDistance.setText("显示视距");

        jLabel22.setText("光照方位：");

        comboBoxLightDirection.setModel(new DefaultComboBoxModel(LightOrigin.values()));
        comboBoxLightDirection.setRenderer(new EnumListCellRenderer());

        jLabel7.setLabelFor(spinnerGrid);
        jLabel7.setText("网格大小：");

        spinnerGrid.setModel(new javax.swing.SpinnerNumberModel(128, 2, 999, 1));

        jLabel8.setText("等高线间距：");

        spinnerContours.setModel(new javax.swing.SpinnerNumberModel(10, 2, 999, 1));

        checkBoxWalkingDistance.setText("显示步行距离");

        jLabel26.setText("笔刷最大尺寸：");

        spinnerBrushSize.setModel(new javax.swing.SpinnerNumberModel(300, 100, null, 10));

        jLabel21.setText(" ");

        jLabel27.setFont(jLabel27.getFont().deriveFont((jLabel27.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel27.setText("警告：过大的笔刷可能会造成电脑卡顿！");

        jLabel9.setFont(jLabel9.getFont().deriveFont((jLabel9.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel9.setText("默认世界设置：");

        jLabel10.setLabelFor(spinnerWidth);
        jLabel10.setText("世界尺寸：");

        spinnerWidth.setModel(new javax.swing.SpinnerNumberModel(640, 128, null, 128));
        spinnerWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWidthStateChanged(evt);
            }
        });

        jLabel11.setText("x");

        spinnerHeight.setModel(new javax.swing.SpinnerNumberModel(640, 128, null, 128));
        spinnerHeight.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerHeightStateChanged(evt);
            }
        });

        jLabel19.setText("块");

        jLabel12.setLabelFor(comboBoxHeight);
        jLabel12.setText("高度上限：");

        comboBoxHeight.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "32", "64", "128", "256", "512", "1024", "2048" }));
        comboBoxHeight.setSelectedIndex(3);
        comboBoxHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxHeightActionPerformed(evt);
            }
        });

        jLabel13.setText("地形：");

        buttonGroup1.add(radioButtonHilly);
        radioButtonHilly.setSelected(true);
        radioButtonHilly.setText("丘陵");
        radioButtonHilly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonHillyActionPerformed(evt);
            }
        });

        jLabel23.setText("（高度：");

        spinnerRange.setModel(new javax.swing.SpinnerNumberModel(20, 1, 255, 1));

        jLabel24.setText("尺寸：");

        spinnerScale.setModel(new javax.swing.SpinnerNumberModel(100, 1, 999, 1));

        jLabel25.setText("%）");

        buttonGroup1.add(radioButtonFlat);
        radioButtonFlat.setText("超平坦");
        radioButtonFlat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFlatActionPerformed(evt);
            }
        });

        checkBoxCircular.setText("圆形世界");
        checkBoxCircular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCircularActionPerformed(evt);
            }
        });

        jLabel14.setLabelFor(spinnerGroundLevel);
        jLabel14.setText("世界高度：");

        spinnerGroundLevel.setModel(new javax.swing.SpinnerNumberModel(58, 1, 255, 1));

        jLabel15.setText("水平面：");

        spinnerWaterLevel.setModel(new javax.swing.SpinnerNumberModel(62, 0, 255, 1));
        spinnerWaterLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWaterLevelStateChanged(evt);
            }
        });

        checkBoxLava.setText("用熔岩代替水");

        checkBoxBeaches.setSelected(true);
        checkBoxBeaches.setText("生成沙滩");
        checkBoxBeaches.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxBeachesActionPerformed(evt);
            }
        });

        jLabel16.setText("地表方块：");

        comboBoxSurfaceMaterial.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoxSurfaceMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxSurfaceMaterialActionPerformed(evt);
            }
        });

        checkBoxExtendedBlockIds.setText("扩展方块ID");

        buttonReset.setText("恢复默认...");
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });

        labelTerrainAndLayerSettings.setForeground(java.awt.Color.blue);
        labelTerrainAndLayerSettings.setText("<html><u>配置默认边界、地表和覆盖层</u></html>");
        labelTerrainAndLayerSettings.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelTerrainAndLayerSettings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelTerrainAndLayerSettingsMouseClicked(evt);
            }
        });

        jLabel17.setFont(jLabel17.getFont().deriveFont((jLabel17.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel17.setText("默认导出设置：");

        checkBoxChestOfGoodies.setText("生成奖励箱");

        jLabel28.setText("世界类型：");

        comboBoxWorldType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxWorldTypeActionPerformed(evt);
            }
        });

        buttonModePreset.setText("...");
        buttonModePreset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonModePresetActionPerformed(evt);
            }
        });

        checkBoxStructures.setText("生成建筑");

        jLabel29.setText("游戏模式：");

        comboBoxMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxModeActionPerformed(evt);
            }
        });

        checkBoxCheats.setText("允许作弊");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3)
                    .addComponent(jSeparator2)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(labelTerrainAndLayerSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonReset))
                    .addComponent(jSeparator5)
                    .addComponent(jLabel1)
                    .addComponent(jLabel18)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(checkBoxChestOfGoodies)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxWorldType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonModePreset)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxStructures)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxCheats))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel19)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonHilly)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonFlat))
                    .addComponent(jLabel6)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(checkBoxGrid))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxContours)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerContours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxWalkingDistance)
                            .addComponent(checkBoxViewDistance))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel26)
                                .addGap(6, 6, 6)
                                .addComponent(spinnerBrushSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel21))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxLightDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel27)))
                    .addComponent(jLabel9)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(checkBoxCircular)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerGroundLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerWaterLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxLava)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxBeaches))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxSurfaceMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxExtendedBlockIds))
                    .addComponent(jLabel17))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxGrid)
                    .addComponent(checkBoxContours)
                    .addComponent(checkBoxViewDistance)
                    .addComponent(jLabel22)
                    .addComponent(comboBoxLightDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinnerGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(spinnerContours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxWalkingDistance)
                    .addComponent(jLabel21)
                    .addComponent(jLabel26)
                    .addComponent(spinnerBrushSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel27)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(spinnerWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(spinnerHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(comboBoxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(radioButtonHilly)
                    .addComponent(radioButtonFlat)
                    .addComponent(jLabel19)
                    .addComponent(jLabel23)
                    .addComponent(spinnerRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(spinnerGroundLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(spinnerWaterLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxLava)
                    .addComponent(checkBoxBeaches)
                    .addComponent(checkBoxCircular))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(comboBoxSurfaceMaterial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxExtendedBlockIds))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTerrainAndLayerSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonReset))
                .addGap(18, 18, 18)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxChestOfGoodies)
                    .addComponent(jLabel28)
                    .addComponent(comboBoxWorldType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonModePreset)
                    .addComponent(checkBoxStructures)
                    .addComponent(jLabel29)
                    .addComponent(comboBoxMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxCheats))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("默认", jPanel5);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("硬件加速"));

        jLabel31.setText("选择一个硬件加速模式。找到合适的模式以提升整体性能：");

        buttonGroup2.add(radioButtonAccelDefault);
        radioButtonAccelDefault.setText("默认");

        buttonGroup2.add(radioButtonAccelDirect3D);
        radioButtonAccelDirect3D.setText("Direct3D");

        buttonGroup2.add(radioButtonAccelOpenGL);
        radioButtonAccelOpenGL.setText("OpenGL");

        buttonGroup2.add(radioButtonAccelQuartz);
        radioButtonAccelQuartz.setText("Quartz");

        buttonGroup2.add(radioButtonAccelXRender);
        radioButtonAccelXRender.setText("XRender");

        buttonGroup2.add(radioButtonAccelUnaccelerated);
        radioButtonAccelUnaccelerated.setText("不加速");

        jLabel33.setText("不适用任何硬件加速");

        jLabel34.setText("使用XRender X11 extension（Linux）");

        jLabel36.setText("<html><em>重启软件后生效</em></html>");

        jLabel37.setText("使用OpenGL渲染系统");

        jLabel38.setText("使用Direct3D渲染系统（Windows）");

        jLabel39.setText("使用Java的默认渲染设置");

        jLabel35.setText("使用Apple的Quartz渲染系统（Mac OS X）");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31)
                    .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButtonAccelUnaccelerated)
                            .addComponent(radioButtonAccelDirect3D)
                            .addComponent(radioButtonAccelDefault)
                            .addComponent(radioButtonAccelXRender)
                            .addComponent(radioButtonAccelQuartz)
                            .addComponent(radioButtonAccelOpenGL))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel34)
                            .addComponent(jLabel33)
                            .addComponent(jLabel35)
                            .addComponent(jLabel37)
                            .addComponent(jLabel38)
                            .addComponent(jLabel39))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelDefault)
                    .addComponent(jLabel39))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelDirect3D)
                    .addComponent(jLabel38))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelOpenGL)
                    .addComponent(jLabel37))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelQuartz)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelXRender)
                    .addComponent(jLabel34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonAccelUnaccelerated)
                    .addComponent(jLabel33))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("图形缩放和绘制"));

        jLabel40.setText("选择一种图形缩放和绘制模式。找到合适的模式以提升图形性能：");

        jLabel41.setText("<html><em>重启软件后生效</em></html>");

        buttonGroup3.add(radioButtonOverlayScaleOnLoad);
        radioButtonOverlayScaleOnLoad.setText("读取时缩放");

        buttonGroup3.add(radioButtonOverlayOptimiseOnLoad);
        radioButtonOverlayOptimiseOnLoad.setText("读取时优化，绘制时缩放");

        buttonGroup3.add(radioButtonOverlayScaleOnPaint);
        radioButtonOverlayScaleOnPaint.setText("绘制时缩放");

        jLabel42.setText("在第一次读取时进行图形的优化，但只在程序绘制地图时进行缩放。占用较少的内存。");

        jLabel43.setText("在第一次读取时使用内存进行图形的缩放和优化。占用较多的内存。");

        jLabel44.setText("不进行图形优化，仅在程序绘制地图时进行缩放。占用最少的内存。");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel40)
                    .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButtonOverlayOptimiseOnLoad)
                            .addComponent(radioButtonOverlayScaleOnLoad)
                            .addComponent(radioButtonOverlayScaleOnPaint))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel44)
                            .addComponent(jLabel43)
                            .addComponent(jLabel42))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonOverlayScaleOnLoad)
                    .addComponent(jLabel43))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonOverlayOptimiseOnLoad)
                    .addComponent(jLabel42))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonOverlayScaleOnPaint)
                    .addComponent(jLabel44))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("性能", jPanel2);

        jLabel2.setText("软件字体：");

        jLabel49.setText("软件字号：");

        spinnerFontSize.setModel(new javax.swing.SpinnerNumberModel(12, 10, 20, 1));

        jLabel50.setFont(new java.awt.Font("宋体", 2, 12)); // NOI18N
        jLabel50.setText("<html>注意：使用英文字体会导致中文显示为“□□□”，<br>\n过大的字号会导致窗口拥挤和文字显示不全等问题。<br>\n重启软件后生效</html>");

        textFieldFont.setText("微软雅黑");

        jLabel51.setText("笔刷行数：");

        jLabel52.setText("笔刷列数：");

        spinnerBrushRow.setModel(new javax.swing.SpinnerNumberModel(0, 0, 9, 1));

        spinnerBrushColumn.setModel(new javax.swing.SpinnerNumberModel(3, 0, 9, 1));

        jLabel53.setFont(new java.awt.Font("宋体", 2, 12)); // NOI18N
        jLabel53.setText("<html>注意：必须设置其中一个为非0，另一个为0！<br>\n例①：将行数设置为2，列数设置为0，笔刷将会横向显示为2排。<br>\n例②：将行数设置为0，列数设置为3，笔刷将会纵向显示为3列。<br>\n重启软件后生效</html>");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldFont, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel49)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel51)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerBrushRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel52)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerBrushColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textFieldFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel49)
                    .addComponent(spinnerFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel51)
                    .addComponent(spinnerBrushRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel52)
                    .addComponent(spinnerBrushColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(178, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("其他", jPanel6);

        buttonCancel.setText("取消");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("确定");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void checkBoxUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxUndoActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxUndoActionPerformed

    private void labelTerrainAndLayerSettingsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelTerrainAndLayerSettingsMouseClicked
        editTerrainAndLayerSettings();
    }//GEN-LAST:event_labelTerrainAndLayerSettingsMouseClicked

    private void comboBoxHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxHeightActionPerformed
        int maxHeight = Integer.parseInt((String) comboBoxHeight.getSelectedItem());
        int exp = (int) (Math.log(maxHeight) / Math.log(2));
        if (exp != previousExp) {
            previousExp = exp;
            
            int terrainLevel = (Integer) spinnerGroundLevel.getValue();
            int waterLevel = (Integer) spinnerWaterLevel.getValue();
            if (terrainLevel >= maxHeight) {
                spinnerGroundLevel.setValue(maxHeight - 1);
            }
            if (waterLevel >= maxHeight) {
                spinnerWaterLevel.setValue(maxHeight - 1);
            }
            ((SpinnerNumberModel) spinnerGroundLevel.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerWaterLevel.getModel()).setMaximum(maxHeight - 1);
            
            int range = (Integer) spinnerRange.getValue();
            if (range >= maxHeight) {
                spinnerRange.setValue(maxHeight - 1);
            }
            ((SpinnerNumberModel) spinnerRange.getModel()).setMaximum(maxHeight - 1);
                        
            setControlStates();
        }
    }//GEN-LAST:event_comboBoxHeightActionPerformed

    private void spinnerWaterLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWaterLevelStateChanged
        int waterLevel = ((Number) spinnerWaterLevel.getValue()).intValue();
        Dimension defaults = Configuration.getInstance().getDefaultTerrainAndLayerSettings();
        defaults.setBorderLevel(waterLevel);
        TileFactory tileFactory = defaults.getTileFactory();
        if (tileFactory instanceof HeightMapTileFactory) {
            ((HeightMapTileFactory) tileFactory).setWaterHeight(waterLevel);
        }
    }//GEN-LAST:event_spinnerWaterLevelStateChanged

    private void checkBoxBeachesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxBeachesActionPerformed
        TileFactory tileFactory = Configuration.getInstance().getDefaultTerrainAndLayerSettings().getTileFactory();
        if (tileFactory instanceof HeightMapTileFactory) {
            ((HeightMapTileFactory) tileFactory).setBeaches(checkBoxBeaches.isSelected());
        }
    }//GEN-LAST:event_checkBoxBeachesActionPerformed

    private void comboBoxSurfaceMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxSurfaceMaterialActionPerformed
        // Update the terrain ranges map to conform to the surface material
        // setting
        Configuration config = Configuration.getInstance();
        Dimension defaultSettings = config.getDefaultTerrainAndLayerSettings();
        TileFactory tileFactory = defaultSettings.getTileFactory();
        if ((tileFactory instanceof HeightMapTileFactory)
                && (((HeightMapTileFactory) tileFactory).getTheme() instanceof SimpleTheme)) {
            SortedMap<Integer, Terrain> defaultTerrainRanges = ((SimpleTheme) ((HeightMapTileFactory) tileFactory).getTheme()).getTerrainRanges();
            // Find what is probably meant to be the surface material. With the
            // default settings this should be -1, but if someone configured a
            // default underwater material, try not to change that
            int waterLevel = (Integer) spinnerWaterLevel.getValue();
            int surfaceLevel = defaultTerrainRanges.headMap(waterLevel + 3).lastKey();
            defaultTerrainRanges.put(surfaceLevel, (Terrain) comboBoxSurfaceMaterial.getSelectedItem());
        }
    }//GEN-LAST:event_comboBoxSurfaceMaterialActionPerformed

    private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
        if (JOptionPane.showConfirmDialog(this, "你确定要重设所有的默认世界设置吗？\n（包括边界，地表，覆盖层设置）", "确认恢复默认设置", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            spinnerWidth.setValue(640);
            spinnerHeight.setValue(640);
            comboBoxHeight.setSelectedIndex(3);
            radioButtonHilly.setSelected(true);
            spinnerGroundLevel.setValue(58);
            spinnerWaterLevel.setValue(62);
            checkBoxLava.setSelected(false);
            checkBoxBeaches.setSelected(true);
            comboBoxSurfaceMaterial.setSelectedItem(GRASS);
            Configuration.getInstance().setDefaultTerrainAndLayerSettings(new World2(DefaultPlugin.JAVA_ANVIL, World2.DEFAULT_OCEAN_SEED, TileFactoryFactory.createNoiseTileFactory(new Random().nextLong(), GRASS, DEFAULT_MAX_HEIGHT_2, 58, 62, false, true, 20, 1.0), DEFAULT_MAX_HEIGHT_2).getDimension(DIM_NORMAL));
        }
    }//GEN-LAST:event_buttonResetActionPerformed

    private void spinnerWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWidthStateChanged
        int value = (Integer) spinnerWidth.getValue();
        value = Math.round(value / 128f) * 128;
        if (value < 128) {
            value = 128;
        }
        spinnerWidth.setValue(value);
    }//GEN-LAST:event_spinnerWidthStateChanged

    private void spinnerHeightStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerHeightStateChanged
        int value = (Integer) spinnerHeight.getValue();
        value = Math.round(value / 128f) * 128;
        if (value < 128) {
            value = 128;
        }
        spinnerHeight.setValue(value);
    }//GEN-LAST:event_spinnerHeightStateChanged

    private void radioButtonHillyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonHillyActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonHillyActionPerformed

    private void radioButtonFlatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFlatActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonFlatActionPerformed

    private void checkBoxCircularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCircularActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxCircularActionPerformed

    private void buttonModePresetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonModePresetActionPerformed
        String editedGeneratorOptions = JOptionPane.showInputDialog(this, "Edit the Superflat mode preset:", generatorOptions);
        if (editedGeneratorOptions != null) {
            generatorOptions = editedGeneratorOptions;
        }
    }//GEN-LAST:event_buttonModePresetActionPerformed

    private void comboBoxWorldTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxWorldTypeActionPerformed
        setControlStates();
    }//GEN-LAST:event_comboBoxWorldTypeActionPerformed

    private void comboBoxModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxModeActionPerformed
        if (comboBoxMode.getSelectedItem() == GameType.CREATIVE) {
            checkBoxCheats.setSelected(true);
        }
    }//GEN-LAST:event_comboBoxModeActionPerformed

    private void checkBoxPingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPingActionPerformed
        pingNotSet = false;
    }//GEN-LAST:event_checkBoxPingActionPerformed

    private void checkBoxAutoSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxAutoSaveActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxAutoSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton buttonModePreset;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonReset;
    private javax.swing.JCheckBox checkBoxAutoSave;
    private javax.swing.JCheckBox checkBoxBeaches;
    private javax.swing.JCheckBox checkBoxCheats;
    private javax.swing.JCheckBox checkBoxCheckForUpdates;
    private javax.swing.JCheckBox checkBoxChestOfGoodies;
    private javax.swing.JCheckBox checkBoxCircular;
    private javax.swing.JCheckBox checkBoxContours;
    private javax.swing.JCheckBox checkBoxExtendedBlockIds;
    private javax.swing.JCheckBox checkBoxGrid;
    private javax.swing.JCheckBox checkBoxLava;
    private javax.swing.JCheckBox checkBoxPing;
    private javax.swing.JCheckBox checkBoxStructures;
    private javax.swing.JCheckBox checkBoxUndo;
    private javax.swing.JCheckBox checkBoxViewDistance;
    private javax.swing.JCheckBox checkBoxWalkingDistance;
    private javax.swing.JComboBox comboBoxHeight;
    private javax.swing.JComboBox comboBoxLightDirection;
    private javax.swing.JComboBox comboBoxLookAndFeel;
    private javax.swing.JComboBox<GameType> comboBoxMode;
    private javax.swing.JComboBox comboBoxSurfaceMaterial;
    private javax.swing.JComboBox<Generator> comboBoxWorldType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelTerrainAndLayerSettings;
    private javax.swing.JRadioButton radioButtonAccelDefault;
    private javax.swing.JRadioButton radioButtonAccelDirect3D;
    private javax.swing.JRadioButton radioButtonAccelOpenGL;
    private javax.swing.JRadioButton radioButtonAccelQuartz;
    private javax.swing.JRadioButton radioButtonAccelUnaccelerated;
    private javax.swing.JRadioButton radioButtonAccelXRender;
    private javax.swing.JRadioButton radioButtonFlat;
    private javax.swing.JRadioButton radioButtonHilly;
    private javax.swing.JRadioButton radioButtonOverlayOptimiseOnLoad;
    private javax.swing.JRadioButton radioButtonOverlayScaleOnLoad;
    private javax.swing.JRadioButton radioButtonOverlayScaleOnPaint;
    private javax.swing.JSpinner spinnerAutoSaveGuardTime;
    private javax.swing.JSpinner spinnerAutoSaveInterval;
    private javax.swing.JSpinner spinnerBrushColumn;
    private javax.swing.JSpinner spinnerBrushRow;
    private javax.swing.JSpinner spinnerBrushSize;
    private javax.swing.JSpinner spinnerContours;
    private javax.swing.JSpinner spinnerFontSize;
    private javax.swing.JSpinner spinnerGrid;
    private javax.swing.JSpinner spinnerGroundLevel;
    private javax.swing.JSpinner spinnerHeight;
    private javax.swing.JSpinner spinnerRange;
    private javax.swing.JSpinner spinnerScale;
    private javax.swing.JSpinner spinnerUndoLevels;
    private javax.swing.JSpinner spinnerWaterLevel;
    private javax.swing.JSpinner spinnerWidth;
    private javax.swing.JSpinner spinnerWorldBackups;
    private javax.swing.JTextField textFieldFont;
    // End of variables declaration//GEN-END:variables

    private final ColourScheme colourScheme;
    private boolean pingNotSet;
    private int previousExp;
    private String generatorOptions;
    
    private static final long serialVersionUID = 1L;
}