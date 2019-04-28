package org.pepsoft.worldpainter.layers;

import org.pepsoft.util.IconUtils;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.biomeschemes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static org.pepsoft.worldpainter.biomeschemes.Minecraft1_7Biomes.*;
import static org.pepsoft.worldpainter.layers.BiomesPanel.BiomeOption.*;

/**
 * Created by pepijn on 27-05-15.
 */
public class BiomesPanel extends JPanel implements CustomBiomeManager.CustomBiomeListener {
    public BiomesPanel(ColourScheme colourScheme, CustomBiomeManager customBiomeManager, Listener listener, ButtonGroup buttonGroup) {
        this.customBiomeManager = customBiomeManager;
        this.listener = listener;
        this.buttonGroup = buttonGroup;
        biomeHelper = new BiomeHelper(BIOME_SCHEME, colourScheme, customBiomeManager);

        initComponents(colourScheme);

        customBiomeManager.addListener(this);
    }

    // CustomBiomeListener

    @Override
    public void customBiomeAdded(CustomBiome customBiome) {
        addButton(customBiome);
    }

    @Override
    public void customBiomeChanged(CustomBiome customBiome) {
        for (Component component: grid.getComponents()) {
            if ((component instanceof JToggleButton) && (((Integer) ((JToggleButton) component).getClientProperty(KEY_BIOME)) == customBiome.getId())) {
                JToggleButton button = (JToggleButton) component;
                button.setIcon(IconUtils.createScaledColourIcon(customBiome.getColour()));
                button.setToolTipText(customBiome.getName());
                return;
            }
        }
    }

    @Override
    public void customBiomeRemoved(CustomBiome customBiome) {
        for (Component component: grid.getComponents()) {
            if ((component instanceof JToggleButton) && (((Integer) ((JToggleButton) component).getClientProperty(KEY_BIOME)) == customBiome.getId())) {
                JToggleButton button = (JToggleButton) component;
                if (button.isSelected()) {
                    button.setSelected(false);
                    selectedBiome = BIOME_PLAINS;
                    notifyListener();
                }
                grid.remove(component);
                forceRepaint();
                return;
            }
        }
    }

    private void initComponents(ColourScheme colourScheme) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        label1.setHorizontalTextPosition(JLabel.LEADING);
        label1.setAlignmentX(0.0f);
        add(label1);
        label2.setAlignmentX(0.0f);
        add(label2);

        for (final int biome : BIOME_ORDER) {
            if (biome != -1) {
                final JToggleButton button = new JToggleButton(new ImageIcon(BiomeSchemeManager.createImage(BIOME_SCHEME, biome, colourScheme)));
                button.putClientProperty(KEY_BIOME, biome);
                button.setMargin(App.BUTTON_INSETS);
                StringBuilder tooltip = new StringBuilder();
                tooltip.append(AutoBiomeScheme.BIOME_NAMES[biome]);
                tooltip.append(" (");
                List<Integer> variantIds = findVariants(biome);
                boolean first = true;
                for (Integer variantId : variantIds) {
                    if (first) {
                        first = false;
                    } else {
                        tooltip.append(", ");
                    }
                    tooltip.append(variantId);
                }
                tooltip.append(')');
                button.setToolTipText(tooltip.toString());
                buttonGroup.add(button);
                button.addActionListener(e -> {
                    if (button.isSelected()) {
                        selectBaseBiome(biome);
                    }
                });
                grid.add(button);
            } else {
                grid.add(new JLabel());
            }
        }

        JButton addCustomBiomeButton = new JButton(IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/plus.png"));
        addCustomBiomeButton.setMargin(App.BUTTON_INSETS);
        addCustomBiomeButton.setToolTipText("添加自定义生物群系");
        addCustomBiomeButton.addActionListener(e -> {
            final Window parent = SwingUtilities.getWindowAncestor(BiomesPanel.this);
            final int id = customBiomeManager.getNextId();
            if (id == -1) {
                JOptionPane.showMessageDialog(parent, "Maximum number of custom biomes reached", "Maximum Reached", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CustomBiome customBiome = new CustomBiome("Custom", id, Color.ORANGE.getRGB());
            CustomBiomeDialog dialog = new CustomBiomeDialog(parent, customBiome, true);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                customBiomeManager.addCustomBiome(parent, customBiome);
            }
        });
        grid.add(addCustomBiomeButton);
        grid.setAlignmentX(0.0f);
        add(grid);

        checkBoxHillsShore.setEnabled(false);
        checkBoxEdgePlateau.setEnabled(false);
        checkBoxM.setEnabled(false);
        checkBoxF.setEnabled(false);
        checkBoxVariant.setEnabled(false);

        ActionListener optionActionListener = e -> updateOptions();
        checkBoxHillsShore.addActionListener(optionActionListener);
        checkBoxEdgePlateau.addActionListener(optionActionListener);
        checkBoxM.addActionListener(optionActionListener);
        checkBoxF.addActionListener(optionActionListener);
        checkBoxVariant.addActionListener(optionActionListener);

        add(checkBoxHillsShore);
        checkBoxEdgePlateau.setAlignmentX(0.0f);
        add(checkBoxEdgePlateau);
        JPanel lowerRowPanel = new JPanel();
        lowerRowPanel.setLayout(new BoxLayout(lowerRowPanel, BoxLayout.LINE_AXIS));
        lowerRowPanel.add(checkBoxM);
        lowerRowPanel.add(checkBoxF);
        lowerRowPanel.add(checkBoxVariant);
        lowerRowPanel.setAlignmentX(0.0f);
        add(lowerRowPanel);
    }

    private void selectBaseBiome(int biome) {
        selectedBaseBiome = biome;
        selectedBiome = biome;
        notifyListener();
        resetOptions();
        updateLabels();
    }

    private void resetOptions() {
        checkBoxHillsShore.setSelected(false);
        checkBoxEdgePlateau.setSelected(false);
        checkBoxM.setSelected(false);
        checkBoxF.setSelected(false);
        checkBoxVariant.setSelected(false);
        Set<BiomeOption> availableOptions = findAvailableOptions(selectedBaseBiome, null);
        checkBoxHillsShore.setEnabled(availableOptions.contains(HILLS_SHORE));
        checkBoxEdgePlateau.setEnabled(availableOptions.contains(EDGE_PLATEAU));
        checkBoxM.setEnabled(availableOptions.contains(M));
        checkBoxF.setEnabled(availableOptions.contains(F));
        checkBoxVariant.setEnabled(availableOptions.contains(VARIANT));
    }

    private void updateOptions() {
        Set<BiomeOption> selectedOptions = getSelectedOptions();
        selectedBiome = findBiome(selectedBaseBiome, selectedOptions);
        notifyListener();
        Set<BiomeOption> availableOptions = findAvailableOptions(selectedBaseBiome, selectedOptions);
        checkBoxHillsShore.setEnabled(availableOptions.contains(HILLS_SHORE));
        checkBoxEdgePlateau.setEnabled(availableOptions.contains(EDGE_PLATEAU));
        checkBoxM.setEnabled(availableOptions.contains(M));
        checkBoxF.setEnabled(availableOptions.contains(F));
        checkBoxVariant.setEnabled(availableOptions.contains(VARIANT));
        updateLabels();
    }

    private Set<BiomeOption> getSelectedOptions() {
        Set<BiomeOption> selectedOptions = EnumSet.noneOf(BiomeOption.class);
        if (checkBoxHillsShore.isSelected()) {
            selectedOptions.add(HILLS_SHORE);
        }
        if (checkBoxEdgePlateau.isSelected()) {
            selectedOptions.add(EDGE_PLATEAU);
        }
        if (checkBoxM.isSelected()) {
            selectedOptions.add(M);
        }
        if (checkBoxF.isSelected()) {
            selectedOptions.add(F);
        }
        if (checkBoxVariant.isSelected()) {
            selectedOptions.add(VARIANT);
        }
        return selectedOptions;
    }

    /**
     * Find the actual biome ID for a specific base biome and a set of selected
     * options.
     *
     * @param baseId The base ID of the biome.
     * @param options The selected options.
     * @return The actual biome ID for the specified base biome and options.
     * @throws IllegalArgumentException If the specified base ID or options are
     *     invalid or don't specify an existing actual biome.
     */
    private int findBiome(int baseId, Set<BiomeOption> options) {
        for (BiomeDescriptor descriptor: DESCRIPTORS) {
            if ((descriptor.getBaseId() == baseId) && descriptor.getOptions().equals(options)) {
                return descriptor.getId();
            }
        }
        throw new IllegalArgumentException("There is no biome with base ID " + baseId + " and options " + options);
    }

    private void updateLabels() {
        label1.setText("选中的群系：" + selectedBiome);
        label1.setIcon(biomeHelper.getBiomeIcon(selectedBiome));
        label2.setText(biomeHelper.getBiomeName(selectedBiome));
    }

    private void addButton(CustomBiome customBiome) {
        final int biome = customBiome.getId();
        final JToggleButton button = new JToggleButton(IconUtils.createScaledColourIcon(customBiome.getColour()));
        button.putClientProperty(KEY_BIOME, biome);
        button.setMargin(App.BUTTON_INSETS);
        button.setToolTipText(customBiome.getName() + " (" + biome + "); right-click for options");
        button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopupMenu(e);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopupMenu(e);
                    }
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopupMenu(e);
                    }
                }

                private void showPopupMenu(MouseEvent e) {
                    JPopupMenu popup = new JPopupMenu();
                    
                    JMenuItem item = new JMenuItem("Edit...");
                    item.addActionListener(actionEvent -> {
                        CustomBiomeDialog dialog = new CustomBiomeDialog(SwingUtilities.getWindowAncestor(button), customBiome, false);
                        dialog.setVisible(true);
                        if (! dialog.isCancelled()) {
                            customBiomeManager.editCustomBiome(customBiome);
                        }
                    });
                    popup.add(item);
                    
                    item = new JMenuItem("Remove...");
                    item.addActionListener(actionEvent -> {
                        if (JOptionPane.showConfirmDialog(button, "Are you sure you want to remove custom biome \"" + customBiome.getName() + "\" (ID: " + customBiome.getId() + ")?\nAny occurrences will be replaced with Automatic Biomes", "Confirm Removal", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            customBiomeManager.removeCustomBiome(customBiome);
                        }
                    });
                    popup.add(item);
                    
                    popup.show(button, e.getX(), e.getY());
                }
            });
        buttonGroup.add(button);
        button.addActionListener(e -> {
            if (button.isSelected()) {
                selectBaseBiome(biome);
            }
        });
        grid.add(button, grid.getComponentCount() - 1);
        forceRepaint();
    }

    private void forceRepaint() {
        // Not sure why this is necessary. Swing bug?
        Window parent = SwingUtilities.getWindowAncestor(this);
        if (parent != null) {
            parent.validate();
        }
    }

    /**
     * Find the available biome options given a particular base biome and a set
     * of already selected options.
     *
     * @param baseId The ID of the base biome.
     * @param options The already selected options. May be <code>null</code> or
     *     empty.
     * @return The total available options for the specified base biome and
     *     selected options. May be empty, but not <code>null</code>.
     */
    private static Set<BiomeOption> findAvailableOptions(int baseId, Set<BiomeOption> options) {
        if (BIOME_SCHEME.isBiomePresent(baseId)) {
            Set<BiomeOption> availableOptions = (options != null) ? EnumSet.copyOf(options) : EnumSet.noneOf(BiomeOption.class);
            for (BiomeDescriptor descriptor: DESCRIPTORS) {
                if ((descriptor.getBaseId() == baseId) && ((options == null) || descriptor.getOptions().containsAll(options))) {
                    availableOptions.addAll(descriptor.getOptions());
                }
            }

            // Special cases
            if (baseId == BIOME_MESA) {
                if ((options == null) || options.isEmpty()) {
                    // There is no Mesa M, Mesa F or Mesa M F, so if only Mesa is
                    // selected, M and F should not yet be available
                    availableOptions.remove(M);
                    availableOptions.remove(F);
                } else if (options.contains(M) || options.contains(F)) {
                    // On the other hand, once M or F are selected, it should
                    // no longer be possible to deselect "edge/plateau"
                    availableOptions.remove(EDGE_PLATEAU);
                }
            }

            return availableOptions;
        } else {
            return Collections.EMPTY_SET;
        }
    }

    /**
     * Find the IDs of all variants of the specified base biome.
     *
     * @param baseId The ID of the base biome.
     * @return The IDs of all variants of the specified base biome (including
     *     the base biome itself).
     */
    private List<Integer> findVariants(int baseId) {
        List<Integer> variants = new ArrayList<>();
        for (BiomeDescriptor descriptor: DESCRIPTORS) {
            if (descriptor.getBaseId() == baseId) {
                variants.add(descriptor.getId());
            }
        }
        return variants;
    }

    private void notifyListener() {
        listener.biomeSelected(selectedBiome);
    }

    private final JPanel grid = new JPanel(new GridLayout(0, 4));
    private final ButtonGroup buttonGroup;
    private final JCheckBox checkBoxHillsShore = new JCheckBox("山丘/岩石");
    private final JCheckBox checkBoxEdgePlateau = new JCheckBox("边缘/高原");
    private final JCheckBox checkBoxM = new JCheckBox("M");
    private final JCheckBox checkBoxF = new JCheckBox("F");
    private final JCheckBox checkBoxVariant = new JCheckBox("特殊");
    private final JLabel label1 = new JLabel("选中的群系：1"), label2 = new JLabel("草原");

    private final CustomBiomeManager customBiomeManager;
    private final BiomeHelper biomeHelper;
    private final Listener listener;
    private int selectedBiome = BIOME_PLAINS, selectedBaseBiome = BIOME_PLAINS;

    private static final AutoBiomeScheme BIOME_SCHEME = new AutoBiomeScheme(null);
    private static final int[] BIOME_ORDER = {
        BIOME_PLAINS, BIOME_FOREST, BIOME_SWAMPLAND, BIOME_JUNGLE,
        BIOME_BIRCH_FOREST, BIOME_ROOFED_FOREST, BIOME_EXTREME_HILLS, BIOME_MUSHROOM_ISLAND,
        BIOME_TAIGA, BIOME_MEGA_TAIGA, BIOME_MEGA_SPRUCE_TAIGA, -1,
        BIOME_DESERT, BIOME_SAVANNA, BIOME_MESA, -1,
        BIOME_OCEAN, BIOME_RIVER, BIOME_BEACH, BIOME_STONE_BEACH,
        BIOME_FROZEN_OCEAN, BIOME_FROZEN_RIVER, BIOME_COLD_BEACH, BIOME_ICE_PLAINS,
        BIOME_ICE_MOUNTAINS, BIOME_COLD_TAIGA, BIOME_HELL, BIOME_SKY
    };
    private static final String KEY_BIOME = BiomesPanel.class.getName() + ".biome";

    private static final BiomeDescriptor[] DESCRIPTORS = {
        new BiomeDescriptor("海洋", 0, 0),
        new BiomeDescriptor("草原", 1, 1),
        new BiomeDescriptor("沙漠", 2, 2),
        new BiomeDescriptor("山地", 3, 3),
        new BiomeDescriptor("森林", 4, 4),
        new BiomeDescriptor("针叶林", 5, 5),
        new BiomeDescriptor("沼泽", 6, 6),
        new BiomeDescriptor("河流", 7, 7),
        new BiomeDescriptor("下界", 8, 8),
        new BiomeDescriptor("末地", 9, 9),
        new BiomeDescriptor("冻洋", 10, 10),
        new BiomeDescriptor("冻河", 11, 11),
        new BiomeDescriptor("积雪的冻原", 12, 12),
        new BiomeDescriptor("雪山", 13, 13),
        new BiomeDescriptor("蘑菇岛", 14, 14),
        new BiomeDescriptor("蘑菇岛岸", 15, 14, HILLS_SHORE),
        new BiomeDescriptor("沙滩", 16, 16),
        new BiomeDescriptor("沙漠丘陵", 17, 2, HILLS_SHORE),
        new BiomeDescriptor("繁茂的丘陵", 18, 4, HILLS_SHORE),
        new BiomeDescriptor("针叶林丘陵", 19, 5, HILLS_SHORE),
        new BiomeDescriptor("山地边缘", 20, 3, EDGE_PLATEAU),
        new BiomeDescriptor("丛林", 21, 21),
        new BiomeDescriptor("丛林丘陵", 22, 21, HILLS_SHORE),
        new BiomeDescriptor("丛林边缘", 23, 21, EDGE_PLATEAU),
        new BiomeDescriptor("深海", 24, 0, VARIANT),
        new BiomeDescriptor("石岸", 25, 25),
        new BiomeDescriptor("积雪的沙滩", 26, 26),
        new BiomeDescriptor("桦木森林", 27, 27),
        new BiomeDescriptor("桦木森林丘陵", 28, 27, HILLS_SHORE),
        new BiomeDescriptor("黑森林", 29, 29),
        new BiomeDescriptor("积雪的针叶林", 30, 30),
        new BiomeDescriptor("积雪的针叶林丘陵", 31, 30, HILLS_SHORE),
        new BiomeDescriptor("巨型针叶林", 32, 32),
        new BiomeDescriptor("巨型针叶林丘陵", 33, 32, HILLS_SHORE),
        new BiomeDescriptor("繁茂的山地", 34, 3, VARIANT),
        new BiomeDescriptor("热带草原", 35, 35),
        new BiomeDescriptor("热带高原", 36, 35, EDGE_PLATEAU),
        new BiomeDescriptor("恶地", 37, 37),
        new BiomeDescriptor("繁茂的恶地高原", 38, 37, EDGE_PLATEAU, F),
        new BiomeDescriptor("恶地高原", 39, 37, EDGE_PLATEAU),
        new BiomeDescriptor("向日葵平原", 129, 1, VARIANT),
        new BiomeDescriptor("沙漠湖泊", 130, 2, M),
        new BiomeDescriptor("沙砾山地", 131, 3, M),
        new BiomeDescriptor("繁花森林", 132, 4, VARIANT),
        new BiomeDescriptor("针叶林山地", 133, 5, M),
        new BiomeDescriptor("沼泽山丘", 134, 6, M),
        new BiomeDescriptor("冰刺平原", 140, 12, VARIANT),
        new BiomeDescriptor("丛林变种", 149, 21, M),
        new BiomeDescriptor("丛林边缘变种", 151, 21, EDGE_PLATEAU, M),
        new BiomeDescriptor("高大桦木森林", 155, 27, M),
        new BiomeDescriptor("高大桦木丘陵", 156, 27, HILLS_SHORE, M),
        new BiomeDescriptor("黑森林丘陵", 157, 29, M),
        new BiomeDescriptor("积雪的针叶林山地", 158, 30, M),
        new BiomeDescriptor("巨型云杉针叶林", 160, 160),
        new BiomeDescriptor("巨型云杉针叶林丘陵", 161, 160, HILLS_SHORE),
        new BiomeDescriptor("沙砾山地+", 162, 3, VARIANT, M),
        new BiomeDescriptor("破碎的热带草原", 163, 35, M),
        new BiomeDescriptor("破碎的热带高原", 164, 35, EDGE_PLATEAU, M),
        new BiomeDescriptor("被风蚀的恶地", 165, 37, VARIANT),
        new BiomeDescriptor("繁茂的恶地高原变种", 166, 37, EDGE_PLATEAU, F, M),
        new BiomeDescriptor("恶地高原变种", 167, 37, EDGE_PLATEAU, M),
    };

    public enum BiomeOption {HILLS_SHORE, EDGE_PLATEAU, M, F, VARIANT}

    public static class BiomeDescriptor {
        public BiomeDescriptor(String name, int id, int baseId, BiomeOption... options) {
            this.name = name;
            this.id = id;
            this.baseId = baseId;
            this.options = ((options != null) && (options.length > 0)) ? EnumSet.copyOf(Arrays.asList(options)) : Collections.EMPTY_SET;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public int getBaseId() {
            return baseId;
        }

        public Set<BiomeOption> getOptions() {
            return options;
        }

        private final String name;
        private final int id, baseId;
        private final Set<BiomeOption> options;
    }

    public interface Listener {
        void biomeSelected(int biomeId);
    }
}