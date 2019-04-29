package org.pepsoft.worldpainter.layers.plants;

import org.pepsoft.minecraft.Direction;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;

import java.util.Optional;
import java.util.Random;

import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.minecraft.Material.*;
import static org.pepsoft.worldpainter.layers.plants.Category.*;

/**
 * A collection of Minecraft plants. These are prototypes which cannot be
 * actually be rendered; you must always invoke
 * {@link Plant#realise(int, Platform)} to obtain a concrete instances of the
 * plant which can be rendered. The dimensions of the prototypes indicate the
 * maximum dimensions of the plant.
 */
public class Plants {
    public static void main(String[] args) {
        for (Plant plant: ALL_PLANTS) {
            System.out.println(plant);
        }
    }

    public static final Plant GRASS = new SimplePlant("草", Material.GRASS, PLANTS_AND_FLOWERS);
    public static final Plant FERN = new SimplePlant("蕨类", Material.FERN, PLANTS_AND_FLOWERS);
    public static final Plant DEAD_SHRUB = new SimplePlant("枯萎的灌木", Material.DEAD_SHRUBS, PLANTS_AND_FLOWERS) {
        @Override
        public boolean isValidFoundation(MinecraftWorld world, int x, int y, int z) {
            final Material material = world.getMaterialAt(x, y, z);
            return material.isNamed(MC_SAND) || material.isNamed(MC_RED_SAND) || material.isNamed(MC_TERRACOTTA);
        }
    };
    public static final Plant DANDELION = new SimplePlant("蒲公英", Material.DANDELION, PLANTS_AND_FLOWERS);
    public static final Plant POPPY = new SimplePlant("虞美人", Material.ROSE, PLANTS_AND_FLOWERS);
    public static final Plant BLUE_ORCHID = new SimplePlant("兰花", Material.BLUE_ORCHID, PLANTS_AND_FLOWERS);
    public static final Plant ALLIUM = new SimplePlant("绒球葱", Material.ALLIUM, PLANTS_AND_FLOWERS);
    public static final Plant AZURE_BLUET = new SimplePlant("茜草花", Material.AZURE_BLUET, PLANTS_AND_FLOWERS);
    public static final Plant TULIP_RED = new SimplePlant("红色郁金香", Material.RED_TULIP, PLANTS_AND_FLOWERS);
    public static final Plant TULIP_ORANGE = new SimplePlant("橙色郁金香", Material.ORANGE_TULIP, PLANTS_AND_FLOWERS);
    public static final Plant TULIP_WHITE = new SimplePlant("白色郁金香", Material.WHITE_TULIP, PLANTS_AND_FLOWERS);
    public static final Plant TULIP_PINK = new SimplePlant("粉红色郁金香", Material.PINK_TULIP, PLANTS_AND_FLOWERS);
    public static final Plant OXEYE_DAISY = new SimplePlant("滨菊", Material.OXEYE_DAISY, PLANTS_AND_FLOWERS);
    public static final Plant SUNFLOWER = new DoubleHighPlant("向日葵", Material.SUNFLOWER, PLANTS_AND_FLOWERS, "block/sunflower_front.png");
    public static final Plant LILAC = new DoubleHighPlant("欧丁香", Material.LILAC, PLANTS_AND_FLOWERS);
    public static final Plant TALL_GRASS = new DoubleHighPlant("高草从", Material.TALL_GRASS, PLANTS_AND_FLOWERS);
    public static final Plant LARGE_FERN = new DoubleHighPlant("大型蕨", Material.LARGE_FERN, PLANTS_AND_FLOWERS);
    public static final Plant ROSE_BUSH = new DoubleHighPlant("玫瑰丛", Material.ROSE_BUSH, PLANTS_AND_FLOWERS);
    public static final Plant PEONY = new DoubleHighPlant("牡丹", Material.PEONY, PLANTS_AND_FLOWERS);
    public static final Plant SAPLING_OAK = new SimplePlant("橡树树苗", Material.OAK_SAPLING, SAPLINGS);
    public static final Plant SAPLING_DARK_OAK = new SimplePlant("深色橡树树苗", Material.DARK_OAK_SAPLING, SAPLINGS);
    public static final Plant SAPLING_PINE = new SimplePlant("云杉树苗", Material.PINE_SAPLING, SAPLINGS);
    public static final Plant SAPLING_BIRCH = new SimplePlant("白桦树苗", Material.BIRCH_SAPLING, SAPLINGS);
    public static final Plant SAPLING_JUNGLE = new SimplePlant("丛林树苗", Material.JUNGLE_SAPLING, SAPLINGS);
    public static final Plant SAPLING_ACACIA = new SimplePlant("金合欢树苗", Material.ACACIA_SAPLING, SAPLINGS);
    public static final Plant MUSHROOM_RED = new SimplePlant("红色蘑菇", Material.RED_MUSHROOM, MUSHROOMS);
    public static final Plant MUSHROOM_BROWN = new SimplePlant("棕色蘑菇", Material.BROWN_MUSHROOM, MUSHROOMS);
    public static final Plant WHEAT = new AgingPlant("小麦", Material.WHEAT, CROPS, "block/wheat_stage7.png", 8);
    public static final Plant CARROTS = new AgingPlant("胡萝卜", Material.CARROTS, CROPS, "block/carrots_stage3.png", 8);
    public static final Plant POTATOES = new AgingPlant("马铃薯", Material.POTATOES, CROPS, "block/potatoes_stage3.png", 8);
    public static final Plant PUMPKIN_STEMS = new AgingPlant("南瓜梗", Material.PUMPKIN_STEM, CROPS, "block/pumpkin_side.png", 8) {
        @Override
        public Material getMaterial(int x, int y, int z) {
            return material.withProperty(FACING, Direction.values()[RANDOM.nextInt(4)]);
        }
    };
    public static final Plant MELON_STEMS = new AgingPlant("西瓜梗", Material.MELON_STEM, CROPS, "block/melon_side.png", 8) {
        @Override
        public Material getMaterial(int x, int y, int z) {
            return material.withProperty(FACING, Direction.values()[RANDOM.nextInt(4)]);
        }
    };
    public static final Plant BEETROOTS = new AgingPlant("甜菜", Material.BEETROOTS, CROPS, "block/beetroots_stage3.png", 4);
    public static final Plant CACTUS = new VariableHeightPlant("仙人掌", Material.CACTUS, Category.CACTUS, "block/cactus_side.png", 3);
    public static final Plant SUGAR_CANE = new VariableHeightPlant("甘蔗", Material.SUGAR_CANE, Category.SUGAR_CANE, 3);
    public static final Plant LILY_PAD = new SimplePlant("睡莲", Material.LILY_PAD, Category.FLOATING_PLANTS);
    public static final Plant NETHER_WART = new AgingPlant("地狱疣", Material.NETHER_WART, Category.NETHER, "block/nether_wart_stage2.png", 4);
    public static final Plant CHORUS_PLANT = new VariableHeightPlant("紫颂植物", Material.CHORUS_PLANT, Material.CHORUS_FLOWER, Category.END, "block/chorus_flower.png", 5);
    public static final Plant TUBE_CORAL = new SimplePlant("管珊瑚", Material.TUBE_CORAL, WATER_PLANTS);
    public static final Plant BRAIN_CORAL = new SimplePlant("脑纹珊瑚", Material.BRAIN_CORAL, WATER_PLANTS);
    public static final Plant BUBBLE_CORAL = new SimplePlant("气泡珊瑚", Material.BUBBLE_CORAL, WATER_PLANTS);
    public static final Plant FIRE_CORAL = new SimplePlant("火珊瑚", Material.FIRE_CORAL, WATER_PLANTS);
    public static final Plant HORN_CORAL = new SimplePlant("鹿角珊瑚", Material.HORN_CORAL, WATER_PLANTS);
    public static final Plant TUBE_CORAL_FAN = new SimplePlant("管珊瑚扇", Material.TUBE_CORAL_FAN, WATER_PLANTS);
    public static final Plant BRAIN_CORAL_FAN = new SimplePlant("脑纹珊瑚扇", Material.BRAIN_CORAL_FAN, WATER_PLANTS);
    public static final Plant BUBBLE_CORAL_FAN = new SimplePlant("气泡珊瑚扇", Material.BUBBLE_CORAL_FAN, WATER_PLANTS);
    public static final Plant FIRE_CORAL_FAN = new SimplePlant("火珊瑚扇", Material.FIRE_CORAL_FAN, WATER_PLANTS);
    public static final Plant HORN_CORAL_FAN = new SimplePlant("鹿角珊瑚扇", Material.HORN_CORAL_FAN, WATER_PLANTS);
    public static final Plant KELP = new VariableHeightPlant("海带", Material.KELP_PLANT, Material.KELP, WATER_PLANTS, 26) {
        @Override
        Optional<Material> getTopMaterial() {
            return Optional.of(Material.KELP.withProperty(AGE, RANDOM.nextInt(26)));
        }
    };
    public static final Plant SEAGRASS = new SimplePlant("海草", Material.SEAGRASS, WATER_PLANTS);
    public static final Plant TALL_SEAGRASS = new DoubleHighPlant("高海草", Material.TALL_SEAGRASS, WATER_PLANTS);
    public static final AgingPlant SEA_PICKLE = new AgingPlant("海泡菜", Material.SEA_PICKLE, WATER_PLANTS, "item/sea_pickle.png", 4) {
        @Override
        public AgingPlant realise(int growth, Platform platform) {
            return new AgingPlant(name, material.withProperty(PICKLES, growth), category, iconName, maxGrowth);
        }
    };

    // The code which uses this assumes there will never be more than 128
    // plants. If that ever happens it needs to be overhauled!
    // IMPORTANT: indices into this array are stored in layer settings! New
    // entries MUST be added at the end, and the order MUST never be changed!
    public static final Plant[] ALL_PLANTS = {GRASS, TALL_GRASS,
            FERN, LARGE_FERN, DEAD_SHRUB, DANDELION, POPPY, BLUE_ORCHID, ALLIUM,
            AZURE_BLUET, TULIP_RED, TULIP_ORANGE, TULIP_WHITE, TULIP_PINK,
            OXEYE_DAISY, SUNFLOWER, LILAC, ROSE_BUSH, PEONY, SAPLING_OAK,
            SAPLING_DARK_OAK, SAPLING_PINE, SAPLING_BIRCH, SAPLING_JUNGLE,
            SAPLING_ACACIA, MUSHROOM_RED, MUSHROOM_BROWN, WHEAT, CARROTS, POTATOES,
            PUMPKIN_STEMS, MELON_STEMS, CACTUS, SUGAR_CANE, LILY_PAD, BEETROOTS,
            NETHER_WART, CHORUS_PLANT, TUBE_CORAL, BRAIN_CORAL, BUBBLE_CORAL,
            FIRE_CORAL, HORN_CORAL, TUBE_CORAL_FAN, BRAIN_CORAL_FAN,
            BUBBLE_CORAL_FAN, FIRE_CORAL_FAN, HORN_CORAL_FAN, KELP, SEAGRASS,
            TALL_SEAGRASS, SEA_PICKLE};

    private static final Random RANDOM = new Random();
}
