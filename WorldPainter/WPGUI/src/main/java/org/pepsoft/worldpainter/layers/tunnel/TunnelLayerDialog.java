/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.tunnel;

import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.MixedMaterialManager;
import org.pepsoft.worldpainter.NoiseSettings;
import org.pepsoft.worldpainter.layers.AbstractEditLayerDialog;
import org.pepsoft.worldpainter.layers.tunnel.TunnelLayer.Mode;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.exporting.IncidentalLayerExporter;
import org.pepsoft.worldpainter.layers.Bo2Layer;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.EditLayerDialog;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.LayerManager;
import org.pepsoft.worldpainter.layers.LayerTableCellRenderer;
import org.pepsoft.worldpainter.layers.groundcover.GroundCoverLayer;
import org.pepsoft.worldpainter.layers.plants.PlantLayer;

/**
 *
 * @author SchmitzP
 */
public class TunnelLayerDialog extends AbstractEditLayerDialog<TunnelLayer> implements ChangeListener, ListSelectionListener {
    public TunnelLayerDialog(Window parent, TunnelLayer layer, boolean extendedBlockIds, ColourScheme colourScheme, int maxHeight, int baseHeight, int waterLevel) {
        super(parent);
        this.layer = layer;
        this.baseHeight = baseHeight;
        this.waterLevel = waterLevel;
        this.maxHeight = maxHeight;
        
        initComponents();
        tableFloorLayers.getSelectionModel().addListSelectionListener(this);
        mixedMaterialSelectorFloor.setExtendedBlockIds(extendedBlockIds);
        mixedMaterialSelectorFloor.setColourScheme(colourScheme);
        mixedMaterialSelectorRoof.setExtendedBlockIds(extendedBlockIds);
        mixedMaterialSelectorRoof.setColourScheme(colourScheme);
        mixedMaterialSelectorWall.setExtendedBlockIds(extendedBlockIds);
        mixedMaterialSelectorWall.setColourScheme(colourScheme);
        labelPreview.setPreferredSize(new Dimension(128, 0));
        programmaticChange = true;
        try {
            ((SpinnerNumberModel) spinnerFloorLevel.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerRoofLevel.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerFloorMin.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerFloorMax.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerRoofMin.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerRoofMax.getModel()).setMaximum(maxHeight - 1);
            ((SpinnerNumberModel) spinnerFloodLevel.getModel()).setMaximum(maxHeight - 1);
        } finally {
            programmaticChange = false;
        }
        
        loadSettings();
        
        updatePreview();
        
        getRootPane().setDefaultButton(buttonOK);
        
        noiseSettingsEditorFloor.addChangeListener(this);
        noiseSettingsEditorRoof.addChangeListener(this);
        
        setLocationRelativeTo(parent);
    }

    // AbstractEditLayerDialog

    @Override
    public TunnelLayer getLayer() {
        return layer;
    }

    // ListSelectionListener

    @Override
    public void valueChanged(ListSelectionEvent e) {
        setControlStates();
    }
    
    // ChangeListener
    
    @Override
    public void stateChanged(ChangeEvent e) {
        generatePreview();
    }

    @Override
    protected void ok() {
        saveSettingsTo(layer, true);
        super.ok();
    }
    
    private void updatePreview() {
//        if ((radioButtonFloorFixedLevel.isSelected() && radioButtonRoofFixedLevel.isSelected())
//                || (radioButtonFloorInverse.isSelected() && radioButtonRoofInverse.isSelected())) {
//            labelTunnelHeight.setText("(tunnel height: " + Math.max(((Integer) spinnerRoofLevel.getValue() - (Integer) spinnerFloorLevel.getValue()), 0) + ")");
//        } else if (radioButtonFloorFixedDepth.isSelected() && radioButtonRoofFixedDepth.isSelected()) {
//            labelTunnelHeight.setText("(tunnel height: " + Math.max(((Integer) spinnerFloorLevel.getValue() - (Integer) spinnerRoofLevel.getValue()), 0) + ")");
//        } else {
//            labelTunnelHeight.setText("(tunnel height: variable)");
//        }
        generatePreview();
    }

    private void generatePreview() {
        TunnelLayer layer = new TunnelLayer("tmp", 0);
        saveSettingsTo(layer, false);
        TunnelLayerExporter exporter = new TunnelLayerExporter(layer);
        Insets insets = labelPreview.getInsets();
        int width = labelPreview.getWidth() - insets.left - insets.right;
        int height = labelPreview.getHeight() - insets.top - insets.bottom;
        if ((width > 0) && (height > 0)) {
            BufferedImage preview = exporter.generatePreview(width, height, waterLevel, baseHeight, Math.min(maxHeight - baseHeight, height - baseHeight));
            labelPreview.setIcon(new ImageIcon(preview));
        } else {
            labelPreview.setIcon(null);
        }
    }
    
    private void loadSettings() {
        programmaticChange = true;
        try {
            spinnerFloorLevel.setValue(layer.getFloorLevel());
            spinnerFloorMin.setValue(layer.getFloorMin());
            spinnerFloorMax.setValue(Math.min(layer.getFloorMax(), maxHeight - 1));
            mixedMaterialSelectorFloor.setMaterial(layer.getFloorMaterial());
            switch (layer.getFloorMode()) {
                case CONSTANT_DEPTH:
                    radioButtonFloorFixedDepth.setSelected(true);
                    break;
                case FIXED_HEIGHT:
                    radioButtonFloorFixedLevel.setSelected(true);
                    break;
                case INVERTED_DEPTH:
                    radioButtonFloorInverse.setSelected(true);
                    break;
            }
            NoiseSettings floorNoise = layer.getFloorNoise();
            if (floorNoise == null) {
                floorNoise = new NoiseSettings();
            }
            noiseSettingsEditorFloor.setNoiseSettings(floorNoise);
            spinnerRoofLevel.setValue(layer.getRoofLevel());
            spinnerRoofMin.setValue(layer.getRoofMin());
            spinnerRoofMax.setValue(Math.min(layer.getRoofMax(), maxHeight - 1));
            mixedMaterialSelectorRoof.setMaterial(layer.getRoofMaterial());
            switch (layer.getRoofMode()) {
                case CONSTANT_DEPTH:
                    radioButtonRoofFixedDepth.setSelected(true);
                    break;
                case FIXED_HEIGHT:
                    radioButtonRoofFixedLevel.setSelected(true);
                    break;
                case INVERTED_DEPTH:
                    radioButtonRoofInverse.setSelected(true);
                    break;
            }
            NoiseSettings roofNoise = layer.getRoofNoise();
            if (roofNoise == null) {
                roofNoise = new NoiseSettings();
            }
            noiseSettingsEditorRoof.setNoiseSettings(roofNoise);
            spinnerWallFloorDepth.setValue(layer.getFloorWallDepth());
            spinnerWallRoofDepth.setValue(layer.getRoofWallDepth());
            mixedMaterialSelectorWall.setMaterial(layer.getWallMaterial());
            textFieldName.setText(layer.getName());
            colourEditor1.setColour(layer.getColour());
            checkBoxRemoveWater.setSelected(layer.isRemoveWater());
            checkBoxFlood.setSelected(layer.getFloodLevel() > 0);
            spinnerFloodLevel.setValue((layer.getFloodLevel() > 0) ? layer.getFloodLevel() : waterLevel);
            checkBoxFloodWithLava.setSelected(layer.isFloodWithLava());

            Map<Layer, TunnelLayer.LayerSettings> floorLayers = layer.getFloorLayers();
            floorLayersTableModel = new TunnelFloorLayersTableModel(floorLayers, maxHeight);
            tableFloorLayers.setModel(floorLayersTableModel);
            tableFloorLayers.getColumnModel().getColumn(TunnelFloorLayersTableModel.COLUMN_NAME).setCellRenderer(new LayerTableCellRenderer());
        } finally {
            programmaticChange = false;
        }
        
        setControlStates();
    }

    private void saveSettingsTo(TunnelLayer layer, boolean registerMaterials) {
        layer.setFloorLevel((Integer) spinnerFloorLevel.getValue());
        layer.setFloorMin((Integer) spinnerFloorMin.getValue());
        layer.setFloorMax((Integer) spinnerFloorMax.getValue());
        MixedMaterial floorMaterial = mixedMaterialSelectorFloor.getMaterial();
        if ((floorMaterial != null) && registerMaterials) {
            // Make sure the material is registered, in case it's new
            floorMaterial = MixedMaterialManager.getInstance().register(floorMaterial);
        }
        layer.setFloorMaterial(floorMaterial);
        if (radioButtonFloorFixedDepth.isSelected()) {
            layer.setFloorMode(Mode.CONSTANT_DEPTH);
        } else if (radioButtonFloorFixedLevel.isSelected()) {
            layer.setFloorMode(Mode.FIXED_HEIGHT);
        } else {
            layer.setFloorMode(Mode.INVERTED_DEPTH);
        }
        NoiseSettings floorNoiseSettings = noiseSettingsEditorFloor.getNoiseSettings();
        if (floorNoiseSettings.getRange() == 0) {
            layer.setFloorNoise(null);
        } else {
            layer.setFloorNoise(floorNoiseSettings);
        }
        layer.setRoofLevel((Integer) spinnerRoofLevel.getValue());
        layer.setRoofMin((Integer) spinnerRoofMin.getValue());
        layer.setRoofMax((Integer) spinnerRoofMax.getValue());
        MixedMaterial roofMaterial = mixedMaterialSelectorRoof.getMaterial();
        if ((roofMaterial != null) && registerMaterials) {
            // Make sure the material is registered, in case it's new
            roofMaterial = MixedMaterialManager.getInstance().register(roofMaterial);
        }
        layer.setRoofMaterial(roofMaterial);
        if (radioButtonRoofFixedDepth.isSelected()) {
            layer.setRoofMode(Mode.CONSTANT_DEPTH);
        } else if (radioButtonRoofFixedLevel.isSelected()) {
            layer.setRoofMode(Mode.FIXED_HEIGHT);
        } else {
            layer.setRoofMode(Mode.INVERTED_DEPTH);
        }
        NoiseSettings roofNoiseSettings = noiseSettingsEditorRoof.getNoiseSettings();
        if (roofNoiseSettings.getRange() == 0) {
            layer.setRoofNoise(null);
        } else {
            layer.setRoofNoise(roofNoiseSettings);
        }
        layer.setFloorWallDepth((Integer) spinnerWallFloorDepth.getValue());
        layer.setRoofWallDepth((Integer) spinnerWallRoofDepth.getValue());
        MixedMaterial wallMaterial = mixedMaterialSelectorWall.getMaterial();
        if ((wallMaterial != null) && registerMaterials) {
            // Make sure the material is registered, in case it's new
            wallMaterial = MixedMaterialManager.getInstance().register(wallMaterial);
        }
        layer.setWallMaterial(wallMaterial);
        layer.setName(textFieldName.getText().trim());
        layer.setColour(colourEditor1.getColour());
        layer.setRemoveWater(checkBoxRemoveWater.isSelected());
        layer.setFloodLevel(checkBoxFlood.isSelected() ? (Integer) spinnerFloodLevel.getValue() : 0);
        layer.setFloodWithLava(checkBoxFloodWithLava.isSelected());
        
        Map<Layer, TunnelLayer.LayerSettings> floorLayers = floorLayersTableModel.getLayers();
        layer.setFloorLayers(((floorLayers != null) && (! floorLayers.isEmpty())) ? floorLayers : null);
    }
    
    private void setControlStates() {
        spinnerFloorMin.setEnabled(! radioButtonFloorFixedLevel.isSelected());
        spinnerFloorMax.setEnabled(! radioButtonFloorFixedLevel.isSelected());
        spinnerRoofMin.setEnabled(! radioButtonRoofFixedLevel.isSelected());
        spinnerRoofMax.setEnabled(! radioButtonRoofFixedLevel.isSelected());
        spinnerFloodLevel.setEnabled(checkBoxFlood.isSelected());
        checkBoxFloodWithLava.setEnabled(checkBoxFlood.isSelected());
        
        int selectedFloorRow = tableFloorLayers.getSelectedRow();
        if (selectedFloorRow != -1) {
            buttonRemoveFloorLayer.setEnabled(tableFloorLayers.getSelectedRowCount() > 0);
            Layer selectedLayer = floorLayersTableModel.getLayer(selectedFloorRow);
            buttonEditFloorLayer.setEnabled(selectedLayer instanceof CustomLayer);
        } else {
            buttonRemoveFloorLayer.setEnabled(false);
            buttonEditFloorLayer.setEnabled(false);
        }
    }

    private void removeFloorLayer() {
        int selectedRow = tableFloorLayers.getSelectedRow();
        if (selectedRow != -1) {
            floorLayersTableModel.removeLayer(selectedRow);
        }
    }

    private void editFloorLayer() {
        int selectedRow = tableFloorLayers.getSelectedRow();
        if (selectedRow != -1) {
            Layer layer = floorLayersTableModel.getLayer(selectedRow);
            if (layer instanceof CustomLayer) {
                EditLayerDialog<Layer> dialog = new EditLayerDialog<>(this, layer);
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    floorLayersTableModel.layerChanged(selectedRow);
                }
            }
        }
    }

    private void addFloorLayer() {
        JPopupMenu popupMenu = new JPopupMenu();
        LayerManager.getInstance().getLayers().stream()
            .filter(l -> l.getExporter() instanceof IncidentalLayerExporter)
            .forEach(l -> {
                JMenuItem menuItem = new JMenuItem(l.getName(), new ImageIcon(l.getIcon()));
                menuItem.addActionListener(e -> floorLayersTableModel.addLayer(l));
                popupMenu.add(menuItem);
            });
        App app = App.getInstance();
        Set<CustomLayer> customLayers = app.getCustomLayers().stream()
            .filter(l -> l.getExporter() instanceof IncidentalLayerExporter)
            .collect(Collectors.toSet());
        if (customLayers.size() > 15) {
            // If there are fifteen or more custom layers, split them by palette
            // and move them to separate submenus to try and conserve screen
            // space
            app.getCustomLayersByPalette().entrySet().stream()
                .map((entry) -> {
                    String palette = entry.getKey();
                    JMenu paletteMenu = new JMenu(palette != null ? palette : "Hidden Layers");
                    entry.getValue().stream()
                        .filter(l -> l.getExporter() instanceof IncidentalLayerExporter)
                        .forEach(l -> {
                            JMenuItem menuItem = new JMenuItem(l.getName(), new ImageIcon(l.getIcon()));
                            menuItem.addActionListener(e -> floorLayersTableModel.addLayer(l));
                            paletteMenu.add(menuItem);
                        });
                    return paletteMenu;
                }).filter((paletteMenu) -> (paletteMenu.getItemCount() > 0))
                .forEach((paletteMenu) -> {
                    popupMenu.add(paletteMenu);
                });
        } else {
            customLayers.forEach(l -> {
                JMenuItem menuItem = new JMenuItem(l.getName(), new ImageIcon(l.getIcon()));
                menuItem.addActionListener(e -> floorLayersTableModel.addLayer(l));
                popupMenu.add(menuItem);
            });
        }
        popupMenu.show(buttonAddFloorLayer, buttonAddFloorLayer.getWidth(), 0);
    }

    private void newFloorLayer() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("添加自定义模型层");
        item.addActionListener(e -> {
            EditLayerDialog<Bo2Layer> dialog = new EditLayerDialog(TunnelLayerDialog.this, Bo2Layer.class);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                Bo2Layer newLayer = dialog.getLayer();
                newLayer.setHide(true);
                floorLayersTableModel.addLayer(newLayer);
            }
        });
        popupMenu.add(item);
        item = new JMenuItem("添加自定义地表覆盖层");
        item.addActionListener(e -> {
            EditLayerDialog<GroundCoverLayer> dialog = new EditLayerDialog(TunnelLayerDialog.this, GroundCoverLayer.class);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                GroundCoverLayer newLayer = dialog.getLayer();
                newLayer.setHide(true);
                floorLayersTableModel.addLayer(newLayer);
            }
        });
        popupMenu.add(item);
        item = new JMenuItem("添加自定义植被");
        item.addActionListener(e -> {
            EditLayerDialog<PlantLayer> dialog = new EditLayerDialog(TunnelLayerDialog.this, PlantLayer.class);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                PlantLayer newLayer = dialog.getLayer();
                newLayer.setHide(true);
                floorLayersTableModel.addLayer(newLayer);
            }
        });
        popupMenu.add(item);
        popupMenu.show(buttonNewFloorLayer, buttonNewFloorLayer.getWidth(), 0);
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
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        buttonReset = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        textFieldName = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        colourEditor1 = new org.pepsoft.worldpainter.ColourEditor();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        radioButtonFloorFixedLevel = new javax.swing.JRadioButton();
        spinnerRoofLevel = new javax.swing.JSpinner();
        mixedMaterialSelectorFloor = new org.pepsoft.worldpainter.MixedMaterialSelector();
        jLabel12 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        spinnerFloorLevel = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        radioButtonRoofFixedDepth = new javax.swing.JRadioButton();
        spinnerFloorMin = new javax.swing.JSpinner();
        noiseSettingsEditorFloor = new org.pepsoft.worldpainter.NoiseSettingsEditor();
        jLabel17 = new javax.swing.JLabel();
        spinnerRoofMin = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        spinnerFloodLevel = new javax.swing.JSpinner();
        radioButtonFloorFixedDepth = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        checkBoxFloodWithLava = new javax.swing.JCheckBox();
        spinnerRoofMax = new javax.swing.JSpinner();
        mixedMaterialSelectorRoof = new org.pepsoft.worldpainter.MixedMaterialSelector();
        jLabel13 = new javax.swing.JLabel();
        noiseSettingsEditorRoof = new org.pepsoft.worldpainter.NoiseSettingsEditor();
        jLabel8 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        spinnerWallRoofDepth = new javax.swing.JSpinner();
        spinnerWallFloorDepth = new javax.swing.JSpinner();
        checkBoxRemoveWater = new javax.swing.JCheckBox();
        spinnerFloorMax = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        radioButtonFloorInverse = new javax.swing.JRadioButton();
        checkBoxFlood = new javax.swing.JCheckBox();
        radioButtonRoofFixedLevel = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        mixedMaterialSelectorWall = new org.pepsoft.worldpainter.MixedMaterialSelector();
        radioButtonRoofInverse = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        labelPreview = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableFloorLayers = new javax.swing.JTable();
        buttonNewFloorLayer = new javax.swing.JButton();
        buttonAddFloorLayer = new javax.swing.JButton();
        buttonEditFloorLayer = new javax.swing.JButton();
        buttonRemoveFloorLayer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("洞穴及其地面覆盖层设置");

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

        buttonReset.setText("重置");
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });

        jLabel1.setText("设置以下属性以创建自定义隧道和洞穴：");

        jLabel4.setText("名称：");

        textFieldName.setColumns(20);
        textFieldName.setText("jTextField1");

        jLabel11.setText("颜色：");

        buttonGroup1.add(radioButtonFloorFixedLevel);
        radioButtonFloorFixedLevel.setText("固定水平高度");
        radioButtonFloorFixedLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorFixedLevelActionPerformed(evt);
            }
        });

        spinnerRoofLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerRoofLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofLevelStateChanged(evt);
            }
        });

        jLabel12.setText("墙面设置：");

        jLabel5.setText("起伏：");

        jLabel9.setText("起伏：");

        spinnerFloorLevel.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerFloorLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorLevelStateChanged(evt);
            }
        });

        jLabel20.setText("其他设置：");

        buttonGroup3.add(radioButtonRoofFixedDepth);
        radioButtonRoofFixedDepth.setText("固定深度");
        radioButtonRoofFixedDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofFixedDepthActionPerformed(evt);
            }
        });

        spinnerFloorMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerFloorMin.setEnabled(false);
        spinnerFloorMin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorMinStateChanged(evt);
            }
        });

        jLabel17.setText("，最大值：");

        spinnerRoofMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerRoofMin.setEnabled(false);
        spinnerRoofMin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofMinStateChanged(evt);
            }
        });

        jLabel18.setText("固定深度的最小值：");

        jLabel2.setText("地面设置：");

        spinnerFloodLevel.setModel(new javax.swing.SpinnerNumberModel(1, 1, 255, 1));
        spinnerFloodLevel.setEnabled(false);
        spinnerFloodLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloodLevelStateChanged(evt);
            }
        });

        buttonGroup1.add(radioButtonFloorFixedDepth);
        radioButtonFloorFixedDepth.setText("固定深度");
        radioButtonFloorFixedDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorFixedDepthActionPerformed(evt);
            }
        });

        jLabel14.setText("材质：");

        checkBoxFloodWithLava.setText("填充熔岩：");
        checkBoxFloodWithLava.setEnabled(false);
        checkBoxFloodWithLava.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxFloodWithLava.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFloodWithLavaActionPerformed(evt);
            }
        });

        spinnerRoofMax.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerRoofMax.setEnabled(false);
        spinnerRoofMax.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRoofMaxStateChanged(evt);
            }
        });

        jLabel13.setText("底部延伸宽度：");

        jLabel8.setText("高度：");

        jLabel16.setText("固定深度的最小值：");

        spinnerWallRoofDepth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerWallRoofDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWallRoofDepthStateChanged(evt);
            }
        });

        spinnerWallFloorDepth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        spinnerWallFloorDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWallFloorDepthStateChanged(evt);
            }
        });

        checkBoxRemoveWater.setText("移除洞穴中的水或熔岩：");
        checkBoxRemoveWater.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxRemoveWater.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxRemoveWaterActionPerformed(evt);
            }
        });

        spinnerFloorMax.setModel(new javax.swing.SpinnerNumberModel(255, 0, 255, 1));
        spinnerFloorMax.setEnabled(false);
        spinnerFloorMax.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFloorMaxStateChanged(evt);
            }
        });

        jLabel19.setText("，最大值：");

        jLabel7.setText("材质：");

        jLabel6.setText("天花板设置：");

        buttonGroup1.add(radioButtonFloorInverse);
        radioButtonFloorInverse.setText("与地面相反的起伏");
        radioButtonFloorInverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonFloorInverseActionPerformed(evt);
            }
        });

        checkBoxFlood.setText("在洞穴内填充水/熔岩：");
        checkBoxFlood.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        checkBoxFlood.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFloodActionPerformed(evt);
            }
        });

        buttonGroup3.add(radioButtonRoofFixedLevel);
        radioButtonRoofFixedLevel.setSelected(true);
        radioButtonRoofFixedLevel.setText("固定水平高度");
        radioButtonRoofFixedLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofFixedLevelActionPerformed(evt);
            }
        });

        jLabel3.setText("高度：");

        buttonGroup3.add(radioButtonRoofInverse);
        radioButtonRoofInverse.setText("与地面相反的起伏");
        radioButtonRoofInverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonRoofInverseActionPerformed(evt);
            }
        });

        jLabel15.setText("顶部延伸宽度：");

        jLabel10.setText("材质：");

        labelPreview.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel21.setLabelFor(spinnerFloodLevel);
        jLabel21.setText("水平面高度：");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel6)
                    .addComponent(jLabel2)
                    .addComponent(jLabel20)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerWallFloorDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerWallRoofDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mixedMaterialSelectorWall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(radioButtonRoofFixedLevel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonRoofFixedDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonRoofInverse))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRoofMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noiseSettingsEditorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mixedMaterialSelectorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mixedMaterialSelectorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(radioButtonFloorFixedLevel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFloorFixedDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioButtonFloorInverse))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noiseSettingsEditorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(checkBoxFlood)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxFloodWithLava))
                            .addComponent(checkBoxRemoveWater))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelPreview, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioButtonRoofFixedLevel)
                            .addComponent(radioButtonRoofFixedDepth)
                            .addComponent(radioButtonRoofInverse))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(spinnerRoofLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)
                            .addComponent(spinnerRoofMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)
                            .addComponent(spinnerRoofMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(noiseSettingsEditorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(mixedMaterialSelectorRoof, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioButtonFloorFixedLevel)
                            .addComponent(radioButtonFloorFixedDepth)
                            .addComponent(radioButtonFloorInverse))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel18)
                                .addComponent(spinnerFloorMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel19)
                                .addComponent(spinnerFloorMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(spinnerFloorLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(noiseSettingsEditorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(mixedMaterialSelectorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(spinnerWallFloorDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(spinnerWallRoofDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(mixedMaterialSelectorWall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxRemoveWater)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxFlood)
                            .addComponent(jLabel21)
                            .addComponent(spinnerFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkBoxFloodWithLava)))
                    .addComponent(labelPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("洞穴设置", jPanel1);

        jLabel22.setText("你可以在这里添加自定义覆盖层以覆盖洞穴地面：");

        tableFloorLayers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableFloorLayers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableFloorLayersMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tableFloorLayers);

        buttonNewFloorLayer.setText("新建");
        buttonNewFloorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNewFloorLayerActionPerformed(evt);
            }
        });

        buttonAddFloorLayer.setText("添加");
        buttonAddFloorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddFloorLayerActionPerformed(evt);
            }
        });

        buttonEditFloorLayer.setText("编辑");
        buttonEditFloorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditFloorLayerActionPerformed(evt);
            }
        });

        buttonRemoveFloorLayer.setText("移除");
        buttonRemoveFloorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveFloorLayerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buttonAddFloorLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonNewFloorLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonEditFloorLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonRemoveFloorLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(buttonNewFloorLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonAddFloorLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonEditFloorLayer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemoveFloorLayer)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("洞穴地面覆盖层设置", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonReset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel)
                        .addGap(11, 11, 11))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(colourEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTabbedPane1))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(colourEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK)
                    .addComponent(buttonReset))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
        loadSettings();
        updatePreview();
    }//GEN-LAST:event_buttonResetActionPerformed

    private void radioButtonRoofInverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofInverseActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofInverseActionPerformed

    private void radioButtonRoofFixedLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofFixedLevelActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofFixedLevelActionPerformed

    private void checkBoxFloodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFloodActionPerformed
        setControlStates();
        updatePreview();
    }//GEN-LAST:event_checkBoxFloodActionPerformed

    private void radioButtonFloorInverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorInverseActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorInverseActionPerformed

    private void spinnerFloorMaxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorMaxStateChanged
        if (! programmaticChange) {
            if ((Integer) spinnerFloorMax.getValue() < (Integer) spinnerFloorMin.getValue()) {
                spinnerFloorMin.setValue(spinnerFloorMax.getValue());
            }
            updatePreview();
        }
    }//GEN-LAST:event_spinnerFloorMaxStateChanged

    private void checkBoxRemoveWaterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxRemoveWaterActionPerformed
        updatePreview();
    }//GEN-LAST:event_checkBoxRemoveWaterActionPerformed

    private void spinnerWallFloorDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWallFloorDepthStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerWallFloorDepthStateChanged

    private void spinnerWallRoofDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWallRoofDepthStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerWallRoofDepthStateChanged

    private void spinnerRoofMaxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofMaxStateChanged
        if (! programmaticChange) {
            if ((Integer) spinnerRoofMax.getValue() < (Integer) spinnerRoofMin.getValue()) {
                spinnerRoofMin.setValue(spinnerRoofMax.getValue());
            }
            updatePreview();
        }
    }//GEN-LAST:event_spinnerRoofMaxStateChanged

    private void checkBoxFloodWithLavaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFloodWithLavaActionPerformed
        updatePreview();
    }//GEN-LAST:event_checkBoxFloodWithLavaActionPerformed

    private void radioButtonFloorFixedDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorFixedDepthActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorFixedDepthActionPerformed

    private void spinnerFloodLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloodLevelStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerFloodLevelStateChanged

    private void spinnerRoofMinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofMinStateChanged
        if (! programmaticChange) {
            if ((Integer) spinnerRoofMax.getValue() < (Integer) spinnerRoofMin.getValue()) {
                spinnerRoofMax.setValue(spinnerRoofMin.getValue());
            }
            updatePreview();
        }
    }//GEN-LAST:event_spinnerRoofMinStateChanged

    private void spinnerFloorMinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorMinStateChanged
        if (! programmaticChange) {
            if ((Integer) spinnerFloorMax.getValue() < (Integer) spinnerFloorMin.getValue()) {
                spinnerFloorMax.setValue(spinnerFloorMin.getValue());
            }
            updatePreview();
        }
    }//GEN-LAST:event_spinnerFloorMinStateChanged

    private void radioButtonRoofFixedDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonRoofFixedDepthActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonRoofFixedDepthActionPerformed

    private void spinnerFloorLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerFloorLevelStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerFloorLevelStateChanged

    private void spinnerRoofLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRoofLevelStateChanged
        if (! programmaticChange) {
            updatePreview();
        }
    }//GEN-LAST:event_spinnerRoofLevelStateChanged

    private void radioButtonFloorFixedLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonFloorFixedLevelActionPerformed
        updatePreview();
        setControlStates();
    }//GEN-LAST:event_radioButtonFloorFixedLevelActionPerformed

    private void buttonNewFloorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNewFloorLayerActionPerformed
        newFloorLayer();
    }//GEN-LAST:event_buttonNewFloorLayerActionPerformed

    private void buttonAddFloorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddFloorLayerActionPerformed
        addFloorLayer();
    }//GEN-LAST:event_buttonAddFloorLayerActionPerformed

    private void buttonEditFloorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditFloorLayerActionPerformed
        editFloorLayer();
    }//GEN-LAST:event_buttonEditFloorLayerActionPerformed

    private void buttonRemoveFloorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveFloorLayerActionPerformed
        removeFloorLayer();
    }//GEN-LAST:event_buttonRemoveFloorLayerActionPerformed

    private void tableFloorLayersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableFloorLayersMouseClicked
        if ((! evt.isPopupTrigger()) && (evt.getClickCount() == 2)) {
            int column = tableFloorLayers.columnAtPoint(evt.getPoint());
            if (column == TunnelFloorLayersTableModel.COLUMN_NAME) {
                editFloorLayer();
            }
        }
    }//GEN-LAST:event_tableFloorLayersMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddFloorLayer;
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonEditFloorLayer;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton buttonNewFloorLayer;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonRemoveFloorLayer;
    private javax.swing.JButton buttonReset;
    private javax.swing.JCheckBox checkBoxFlood;
    private javax.swing.JCheckBox checkBoxFloodWithLava;
    private javax.swing.JCheckBox checkBoxRemoveWater;
    private org.pepsoft.worldpainter.ColourEditor colourEditor1;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelPreview;
    private org.pepsoft.worldpainter.MixedMaterialSelector mixedMaterialSelectorFloor;
    private org.pepsoft.worldpainter.MixedMaterialSelector mixedMaterialSelectorRoof;
    private org.pepsoft.worldpainter.MixedMaterialSelector mixedMaterialSelectorWall;
    private org.pepsoft.worldpainter.NoiseSettingsEditor noiseSettingsEditorFloor;
    private org.pepsoft.worldpainter.NoiseSettingsEditor noiseSettingsEditorRoof;
    private javax.swing.JRadioButton radioButtonFloorFixedDepth;
    private javax.swing.JRadioButton radioButtonFloorFixedLevel;
    private javax.swing.JRadioButton radioButtonFloorInverse;
    private javax.swing.JRadioButton radioButtonRoofFixedDepth;
    private javax.swing.JRadioButton radioButtonRoofFixedLevel;
    private javax.swing.JRadioButton radioButtonRoofInverse;
    private javax.swing.JSpinner spinnerFloodLevel;
    private javax.swing.JSpinner spinnerFloorLevel;
    private javax.swing.JSpinner spinnerFloorMax;
    private javax.swing.JSpinner spinnerFloorMin;
    private javax.swing.JSpinner spinnerRoofLevel;
    private javax.swing.JSpinner spinnerRoofMax;
    private javax.swing.JSpinner spinnerRoofMin;
    private javax.swing.JSpinner spinnerWallFloorDepth;
    private javax.swing.JSpinner spinnerWallRoofDepth;
    private javax.swing.JTable tableFloorLayers;
    private javax.swing.JTextField textFieldName;
    // End of variables declaration//GEN-END:variables

    private final TunnelLayer layer;
    private final int waterLevel, baseHeight, maxHeight;
    private TunnelFloorLayersTableModel floorLayersTableModel;
    private boolean programmaticChange;

    private static final long serialVersionUID = 1L;
}