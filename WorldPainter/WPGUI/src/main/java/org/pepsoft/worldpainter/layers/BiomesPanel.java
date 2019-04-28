package org.pepsoft.worldpainter.layers;

import org.pepsoft.util.IconUtils;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.biomeschemes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static javax.swing.BoxLayout.PAGE_AXIS;
import static org.pepsoft.worldpainter.biomeschemes.Minecraft1_13Biomes.*;
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
        setLayout(new BoxLayout(this, PAGE_AXIS));

        label1.setHorizontalTextPosition(JLabel.LEADING);
        label1.setAlignmentX(0.0f);
        add(label1);
        label2.setAlignmentX(0.0f);
        add(label2);

        for (final int biome: BIOME_ORDER) {
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
        }

        JButton addCustomBiomeButton = new JButton(IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/plus.png"));
        addCustomBiomeButton.setMargin(App.BUTTON_INSETS);
        addCustomBiomeButton.setToolTipText("添加自定义生物群系");
        addCustomBiomeButton.addActionListener(e -> {
            final Window parent = SwingUtilities.getWindowAncestor(BiomesPanel.this);
            final int id = customBiomeManager.getNextId();
            if (id == -1) {
                JOptionPane.showMessageDialog(parent, "已经到达自定义生物群系的最大数量", "到达上限", JOptionPane.ERROR_MESSAGE);
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

        optionsPanel.setLayout(new BoxLayout(optionsPanel, PAGE_AXIS));
        add(optionsPanel);
    }

    private void selectBaseBiome(int biome) {
        selectedBaseBiome = biome;
        selectedBiome = biome;
        notifyListener();
        resetOptions();
        updateLabels();
    }

    private void resetOptions() {
        Set<BiomeOption> availableOptions = findAvailableOptions(selectedBaseBiome);
        optionsPanel.removeAll();
        for (BiomeOption option: availableOptions) {
            JCheckBox checkBox = new JCheckBox(option.cnname);
            checkBox.addActionListener(event -> updateOptions());
            checkBox.putClientProperty(PROPERTY_BIOME_OPTION, option);
            checkBox.setEnabled(findBiome(selectedBaseBiome, EnumSet.of(option)) != -1);
            optionsPanel.add(checkBox);
        }
    }

    private void updateOptions() {
        Set<BiomeOption> selectedOptions = getSelectedOptions();
        selectedBiome = findBiome(selectedBaseBiome, selectedOptions);
        notifyListener();
        for (Component component: optionsPanel.getComponents()) {
            JCheckBox checkBox = (JCheckBox) component;
            BiomeOption biomeOption = (BiomeOption) checkBox.getClientProperty(PROPERTY_BIOME_OPTION);
            if (selectedOptions.contains(biomeOption)) {
                checkBox.setEnabled(true);
            } else {
                EnumSet<BiomeOption> optionsCopy = EnumSet.copyOf(selectedOptions);
                optionsCopy.add(biomeOption);
                checkBox.setEnabled(findBiome(selectedBaseBiome, optionsCopy) != -1);
            }
        }
        updateLabels();
    }

    private Set<BiomeOption> getSelectedOptions() {
        Set<BiomeOption> selectedOptions = EnumSet.noneOf(BiomeOption.class);
        for (Component component: optionsPanel.getComponents()) {
            JCheckBox checkBox = (JCheckBox) component;
            if (checkBox.isSelected()) {
                selectedOptions.add((BiomeOption) checkBox.getClientProperty(PROPERTY_BIOME_OPTION));
            }
        }
        return selectedOptions;
    }

    /**
     * Find the actual biome ID for a specific base biome and a set of selected
     * options.
     *
     * @param baseId The base ID of the biome.
     * @param options The selected options.
     * @return The actual biome ID for the specified base biome and options, or
     * -1 if the specified base ID or options are invalid or don't specify an
     * existing actual biome.
     */
    private int findBiome(int baseId, Set<BiomeOption> options) {
        for (BiomeDescriptor descriptor: DESCRIPTORS) {
            if ((descriptor.getBaseId() == baseId) && descriptor.getOptions().equals(options)) {
                return descriptor.getId();
            }
        }
        return -1;
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
        button.setToolTipText(customBiome.getName() + "（" + biome + "）；右键展开选项");
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
                    
                    JMenuItem item = new JMenuItem("编辑...");
                    item.addActionListener(actionEvent -> {
                        CustomBiomeDialog dialog = new CustomBiomeDialog(SwingUtilities.getWindowAncestor(button), customBiome, false);
                        dialog.setVisible(true);
                        if (! dialog.isCancelled()) {
                            customBiomeManager.editCustomBiome(customBiome);
                        }
                    });
                    popup.add(item);
                    
                    item = new JMenuItem("移除...");
                    item.addActionListener(actionEvent -> {
                        if (JOptionPane.showConfirmDialog(button, "你确定要移除自定义生物群系“" + customBiome.getName() + "”（ID：" + customBiome.getId() + "）吗？\n这个生物群系覆盖的范围会被填充自动生物群系取代", "确认移除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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
     * Find the available biome options given a particular base biome.
     *
     * @param baseId The ID of the base biome.
     * @return The total available options for the specified base biome. May be
     * empty, but not <code>null</code>.
     */
    private static Set<BiomeOption> findAvailableOptions(int baseId) {
        if (BIOME_SCHEME.isBiomePresent(baseId)) {
            Set<BiomeOption> availableOptions = EnumSet.noneOf(BiomeOption.class);
            for (BiomeDescriptor descriptor: DESCRIPTORS) {
                if (descriptor.getBaseId() == baseId) {
                    availableOptions.addAll(descriptor.getOptions());
                }
            }

            return availableOptions;
        } else {
            return Collections.emptySet();
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

    private final JPanel grid = new JPanel(new GridLayout(0, 4)), optionsPanel = new JPanel();
    private final ButtonGroup buttonGroup;
    private final JLabel label1 = new JLabel("选中的群系：1"), label2 = new JLabel("平原");

    private final CustomBiomeManager customBiomeManager;
    private final BiomeHelper biomeHelper;
    private final Listener listener;
    private int selectedBiome = BIOME_PLAINS, selectedBaseBiome = BIOME_PLAINS;

    private static final AutoBiomeScheme BIOME_SCHEME = new AutoBiomeScheme(null);
    private static final int[] BIOME_ORDER = {
        BIOME_PLAINS, BIOME_FOREST, BIOME_SWAMPLAND, BIOME_JUNGLE,
        BIOME_BIRCH_FOREST, BIOME_ROOFED_FOREST, BIOME_EXTREME_HILLS, BIOME_MUSHROOM_ISLAND,
        BIOME_TAIGA, BIOME_MEGA_TAIGA, BIOME_MEGA_SPRUCE_TAIGA, BIOME_ICE_PLAINS,
        BIOME_DESERT, BIOME_SAVANNA, BIOME_MESA, BIOME_ICE_PLAINS_SPIKES,
        BIOME_OCEAN, BIOME_RIVER, BIOME_BEACH, BIOME_STONE_BEACH,
        BIOME_HELL, BIOME_SKY, BIOME_VOID
    };
    private static final String KEY_BIOME = BiomesPanel.class.getName() + ".biome";
    private static final String PROPERTY_BIOME_OPTION = "org.pepsoft.worldpainter.layers.BiomesPanel.biomeOption";

    private static final BiomeDescriptor[] DESCRIPTORS = {
        new BiomeDescriptor("海洋", 0, 0),
        new BiomeDescriptor("平原", 1, 1),
        new BiomeDescriptor("沙漠", 2, 2),
        new BiomeDescriptor("山地", 3, 3),
        new BiomeDescriptor("森林", 4, 4),
        new BiomeDescriptor("针叶林", 5, 5),
        new BiomeDescriptor("沼泽", 6, 6),
        new BiomeDescriptor("河流", 7, 7),
        new BiomeDescriptor("下界", 8, 8),
        new BiomeDescriptor("末地", 9, 9),

        new BiomeDescriptor("冻洋", 10, 0, FROZEN),
        new BiomeDescriptor("冻河", 11, 7, FROZEN),
        new BiomeDescriptor("积雪的冻原", 12, 12),
        new BiomeDescriptor("雪山", 13, 3, SNOWY),
        new BiomeDescriptor("蘑菇岛", 14, 14),
        new BiomeDescriptor("蘑菇岛岸", 15, 14, SHORE),
        new BiomeDescriptor("沙滩", 16, 16),
        new BiomeDescriptor("沙漠丘陵", 17, 2, HILLS),
        new BiomeDescriptor("繁茂的丘陵", 18, 4, HILLS),
        new BiomeDescriptor("针叶林丘陵", 19, 5, HILLS),

        new BiomeDescriptor("山地边缘", 20, 3, EDGE),
        new BiomeDescriptor("丛林", 21, 21),
        new BiomeDescriptor("丛林丘陵", 22, 21, HILLS),
        new BiomeDescriptor("丛林边缘", 23, 21, EDGE),
        new BiomeDescriptor("深海", 24, 0, DEEP),
        new BiomeDescriptor("石岸", 25, 25),
        new BiomeDescriptor("积雪的沙滩", 26, 16, SNOWY),
        new BiomeDescriptor("桦木森林", 27, 27),
        new BiomeDescriptor("桦木森林丘陵", 28, 27, HILLS),
        new BiomeDescriptor("黑森林", 29, 29),

        new BiomeDescriptor("积雪的针叶林", 30, 5, SNOWY),
        new BiomeDescriptor("积雪的针叶林丘陵", 31, 5, SNOWY, HILLS),
        new BiomeDescriptor("巨型针叶林", 32, 32),
        new BiomeDescriptor("巨型针叶林丘陵", 33, 32, HILLS),
        new BiomeDescriptor("繁茂的山地", 34, 3, WOODED),
        new BiomeDescriptor("热带草原", 35, 35),
        new BiomeDescriptor("热带高原", 36, 35, PLATEAU),
        new BiomeDescriptor("恶地", 37, 37),
        new BiomeDescriptor("繁茂的恶地高原", 38, 37, WOODED, PLATEAU),
        new BiomeDescriptor("恶地高原", 39, 37, PLATEAU),

        new BiomeDescriptor("末地小型岛屿", 40, 9, SMALL_ISLANDS),
        new BiomeDescriptor("末地中型岛屿", 41, 9, MIDLANDS),
        new BiomeDescriptor("末地高岛", 42, 9, HIGHLANDS),
        new BiomeDescriptor("末地荒岛", 43, 9, BARRENS),
        new BiomeDescriptor("暖水海洋", 44, 0, WARM),
        new BiomeDescriptor("温水海洋", 45, 0, LUKEWARM),
        new BiomeDescriptor("冷水海洋", 46, 0, COLD),
        new BiomeDescriptor("暖水深海", 47, 0, DEEP, WARM),
        new BiomeDescriptor("温水深海", 48, 0, DEEP, LUKEWARM),
        new BiomeDescriptor("冷水深海", 49, 0, DEEP, COLD),

        new BiomeDescriptor("封冻深海", 50, 0, DEEP, FROZEN),

        new BiomeDescriptor("虚空", 127, 127),
        new BiomeDescriptor("向日葵平原", 129, 1, FLOWERS),

        new BiomeDescriptor("沙漠湖泊", 130, 2, LAKES),
        new BiomeDescriptor("沙砾山地", 131, 3, GRAVELLY),
        new BiomeDescriptor("繁花森林", 132, 4, FLOWERS),
        new BiomeDescriptor("针叶林山地", 133, 5, MOUNTAINOUS),
        new BiomeDescriptor("沼泽山丘", 134, 6, HILLS),

        new BiomeDescriptor("冰刺平原", 140, 140),
        new BiomeDescriptor("丛林变种", 149, 21, MODIFIED),

        new BiomeDescriptor("丛林边缘变种", 151, 21, MODIFIED, EDGE),
        new BiomeDescriptor("高大桦木森林", 155, 27, TALL),
        new BiomeDescriptor("高大桦木丘陵", 156, 27, HILLS, TALL),
        new BiomeDescriptor("黑森林丘陵", 157, 29, HILLS),
        new BiomeDescriptor("积雪的针叶林山地", 158, 5, SNOWY, MOUNTAINOUS),

        new BiomeDescriptor("巨型云杉针叶林", 160, 160),
        new BiomeDescriptor("巨型云杉针叶林丘陵", 161, 160, HILLS),
        new BiomeDescriptor("沙砾山地+", 162, 3, GRAVELLY, VARIANT),
        new BiomeDescriptor("破碎的热带草原", 163, 35, SHATTERED),
        new BiomeDescriptor("破碎的热带高原", 164, 35, SHATTERED, PLATEAU),
        new BiomeDescriptor("被风蚀的恶地", 165, 37, ERODED),
        new BiomeDescriptor("繁茂的恶地高原变种", 166, 37, MODIFIED, WOODED, PLATEAU),
        new BiomeDescriptor("恶地高原变种", 167, 37, MODIFIED, PLATEAU),
    };

    public enum BiomeOption {HILLS("丘陵"), SHORE("岛岸"), EDGE("边缘"), PLATEAU("高原"), MOUNTAINOUS("山地"), VARIANT("变体"), FROZEN("冻河"), SNOWY("积雪"), DEEP("深海"), WOODED("繁茂"), WARM("温暖"),
        LUKEWARM("温水"), COLD("寒冷"), TALL("高大"), FLOWERS("花"), LAKES("湖泊"), GRAVELLY("沙砾"), SHATTERED("破碎"), SMALL_ISLANDS("小型岛屿"), MIDLANDS("中型岛屿"), HIGHLANDS("高岛"), BARRENS("荒岛"),
        MODIFIED("变种"), ERODED("风蚀");
        final String cnname;
        private BiomeOption(String cnname) {
            this.cnname = cnname;
        }
    }

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