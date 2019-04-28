/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import org.pepsoft.minecraft.Material;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.PerlinNoise;
import org.pepsoft.util.RandomField;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.minecraft.Material.*;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.worldpainter.DefaultPlugin.JAVA_ANVIL_1_13;
import static org.pepsoft.worldpainter.biomeschemes.Minecraft1_13Biomes.*;

/**
 *
 * @author pepijn
 */

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//                                   WARNING!                                 //
//                                                                            //
// These values are saved in tiles and on disk by their name AND by their     //
// ordinal! It is therefore very important NOT to change the names OR the     //
// order, and to add new entries at the end!                                  //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

@SuppressWarnings("ConstantConditions") // Future-proofing
public enum Terrain {
    GRASS ("草丛", "生长着一簇一簇的花丛、草丛和蕨类的草方块", BIOME_PLAINS, 2) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            // TODOMC13 migrate this to modern materials
            if (dz > 2) {
                return AIR;
            } else if (dz == 2) {
                final Random rnd = new Random(seed + (x * 65537) + (y * 4099));
                final int rndNr = rnd.nextInt(FLOWER_INCIDENCE);
                if (rndNr == 0) {
                    if (dandelionNoise.getSeed() != (seed + DANDELION_SEED_OFFSET)) {
                        dandelionNoise.setSeed(seed + DANDELION_SEED_OFFSET);
                        roseNoise.setSeed(seed + ROSE_SEED_OFFSET);
                        flowerTypeField.setSeed(seed + FLOWER_TYPE_FIELD_OFFSET);
                    }
                    // Use 1 instead of 2, even though dz == 2, to get consistent results for the lower and upper blocks
                    // Keep the "1 / SMALLBLOBS" and the two noise generators for consistency with existing maps
                    if ((dandelionNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, 1 / SMALL_BLOBS) > FLOWER_CHANCE)
                            || (roseNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, 1 / SMALL_BLOBS) > FLOWER_CHANCE)) {
                        Material flower = FLOWER_TYPES[flowerTypeField.getValue(x, y)];
                        if (flower.blockType == BLK_LARGE_FLOWERS) {
                            if (platform == JAVA_ANVIL_1_13) {
                                return flower.withProperty(HALF, "upper");
                            } else {
                                return LARGE_FLOWER_TOP;
                            }
                        } else {
                            return AIR;
                        }
                    } else {
                        return AIR;
                    }
                } else {
                    if (grassNoise.getSeed() != (seed + GRASS_SEED_OFFSET)) {
                        grassNoise.setSeed(seed + GRASS_SEED_OFFSET);
                        tallGrassNoise.setSeed(seed + DOUBLE_TALL_GRASS_SEED_OFFSET);
                    }
                    // Use 1 instead of 2, even though dz == 2, to get consistent results for the lower and upper blocks
                    // Keep the "1 / SMALLBLOBS" for consistency with existing maps
                    final float grassValue = grassNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, 1 / SMALL_BLOBS) + (rnd.nextFloat() * 0.3f - 0.15f);
                    if ((grassValue > DOUBLE_TALL_GRASS_CHANCE) && (tallGrassNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, 1 / SMALL_BLOBS) > 0)) {
                        if (platform == JAVA_ANVIL_1_13) {
                            if (rnd.nextInt(4) == 0) {
                                return DOUBLE_TALL_FERN_BOTTOM.withProperty(HALF, "upper");
                            } else {
                                return DOUBLE_TALL_GRASS_BOTTOM.withProperty(HALF, "upper");
                            }
                        } else {
                            return LARGE_FLOWER_TOP;
                        }
                    } else {
                        return AIR;
                    }
                }
            } else if (dz == 1) {
                final Random rnd = new Random(seed + (x * 65537) + (y * 4099));
                final int rndNr = rnd.nextInt(FLOWER_INCIDENCE);
                if (rndNr == 0) {
                    if (dandelionNoise.getSeed() != (seed + DANDELION_SEED_OFFSET)) {
                        dandelionNoise.setSeed(seed + DANDELION_SEED_OFFSET);
                        roseNoise.setSeed(seed + ROSE_SEED_OFFSET);
                        flowerTypeField.setSeed(seed + FLOWER_TYPE_FIELD_OFFSET);
                    }
                    // Keep the "1 / SMALLBLOBS" and the two noise generators for constistency with existing maps
                    if ((dandelionNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, 1 / SMALL_BLOBS) > FLOWER_CHANCE)
                            || (roseNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, 1 / SMALL_BLOBS) > FLOWER_CHANCE)) {
                        return FLOWER_TYPES[flowerTypeField.getValue(x, y)];
                    } else {
                        return AIR;
                    }
                } else {
                    if (grassNoise.getSeed() != (seed + GRASS_SEED_OFFSET)) {
                        grassNoise.setSeed(seed + GRASS_SEED_OFFSET);
                        tallGrassNoise.setSeed(seed + DOUBLE_TALL_GRASS_SEED_OFFSET);
                    }
                    // Keep the "1 / SMALLBLOBS" for constistency with existing maps
                    final float grassValue = grassNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, 1 / SMALL_BLOBS) + (rnd.nextFloat() * 0.3f - 0.15f);
                    if (grassValue > GRASS_CHANCE) {
                        if (tallGrassNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, 1 / SMALL_BLOBS) > 0) {
                            // Double tallness
                            if (grassValue > DOUBLE_TALL_GRASS_CHANCE) {
                                if (rnd.nextInt(4) == 0) {
                                    return DOUBLE_TALL_FERN_BOTTOM;
                                } else {
                                    return DOUBLE_TALL_GRASS_BOTTOM;
                                }
                            } else  {
                                if (rnd.nextInt(4) == 0) {
                                    return FERN;
                                } else {
                                    return Material.GRASS;
                                }
                            }
                        } else {
                            if (grassValue > FERN_CHANCE) {
                                return FERN;
                            } else {
                                return Material.GRASS;
                            }
                        }
                    } else {
                        return AIR;
                    }
                }
            } else {
                // The post process step will take care of changing all covered
                // grass blocks into dirt
                return Material.GRASS_BLOCK;
            }
        }

        private final PerlinNoise dandelionNoise = new PerlinNoise(0);
        private final PerlinNoise roseNoise = new PerlinNoise(0);
        private final PerlinNoise grassNoise = new PerlinNoise(0);
        private final RandomField flowerTypeField = new RandomField(4, SMALL_BLOBS, 0);
        private final PerlinNoise tallGrassNoise = new PerlinNoise(0);

        private final Material[] FLOWER_TYPES = {
            DANDELION,
            ROSE,
            Material.get(BLK_ROSE, 1), // Blue orchid
            Material.get(BLK_ROSE, 2), // Allium
            Material.get(BLK_ROSE, 3), // Azure bluet
            Material.get(BLK_ROSE, 4), // Red tulip
            Material.get(BLK_ROSE, 5), // Orange tulip
            Material.get(BLK_ROSE, 6), // White tulip
            Material.get(BLK_ROSE, 7), // Pink tulip
            Material.get(BLK_ROSE, 8), // Oxeye daisy
            Material.get(BLK_LARGE_FLOWERS, 0), // Sunflower
            Material.get(BLK_LARGE_FLOWERS, 1), // Lilac
            Material.get(BLK_LARGE_FLOWERS, 4), // Rose bush
            Material.get(BLK_LARGE_FLOWERS, 5), // Peony
            DANDELION, // Again to make them a bit more common
            ROSE,      // Again to make them a bit more common
        };

        private final Material DOUBLE_TALL_GRASS_BOTTOM = Material.get(BLK_LARGE_FLOWERS, 2);
        private final Material LARGE_FLOWER_TOP         = Material.get(BLK_LARGE_FLOWERS, 8);
        private final Material DOUBLE_TALL_FERN_BOTTOM  = Material.get(BLK_LARGE_FLOWERS, 3);

        private static final long DANDELION_SEED_OFFSET = 145351781L;
        private static final long ROSE_SEED_OFFSET = 28286488L;
        private static final long GRASS_SEED_OFFSET = 169191195L;
        private static final long FLOWER_TYPE_FIELD_OFFSET = 65226710L;
        private static final long DOUBLE_TALL_GRASS_SEED_OFFSET = 31695680L;
        private static final int FLOWER_INCIDENCE = 10;
    },
    DIRT("泥土", BLK_DIRT, BLK_DIRT, "裸露的泥土", BIOME_PLAINS),
    SAND("沙子", BLK_SAND, BLK_SAND, "裸露的沙子", BIOME_PLAINS),
    SANDSTONE("砂岩", BLK_SANDSTONE, BLK_SANDSTONE, "砂岩", BIOME_PLAINS),
    STONE("石头", BLK_STONE, BLK_STONE, "裸露的岩石", BIOME_PLAINS),
    ROCK("岩石", "石头和圆石的混合", BIOME_PLAINS) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 0) {
                return AIR;
            } else {
                if (perlinNoise.getSeed() != (seed + STONE_SEED_OFFSET)) {
                    perlinNoise.setSeed(seed + STONE_SEED_OFFSET);
                }
                if (perlinNoise.getPerlinNoise(x / TINY_BLOBS, y / TINY_BLOBS, z / TINY_BLOBS) > 0) {
                    return Material.STONE;
                } else {
                    return Material.COBBLESTONE;
                }
            }
        }

        private final PerlinNoise perlinNoise = new PerlinNoise(0);

        private static final int STONE_SEED_OFFSET = 188434540;
    },
    WATER("水", BLK_WATER, BLK_WATER, "流动的水", BIOME_RIVER),
    LAVA("熔岩", BLK_LAVA, BLK_LAVA, "流动的熔岩", BIOME_PLAINS),
    @Deprecated
    SNOW("岩石积雪", "覆盖在石头和圆石上的薄薄的积雪", BIOME_ICE_PLAINS, 1) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 1) {
                return AIR;
            } else if (dz == 1) {
                return Material.SNOW;
            } else {
                if (perlinNoise.getSeed() != (seed + STONE_SEED_OFFSET)) {
                    perlinNoise.setSeed(seed + STONE_SEED_OFFSET);
                }
                if (perlinNoise.getPerlinNoise(x / TINY_BLOBS, y / TINY_BLOBS, z / TINY_BLOBS) > 0) {
                    return Material.STONE;
                } else {
                    return Material.COBBLESTONE;
                }
            }
        }

        @Override
        public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {
            return colourScheme.getColour(BLK_SNOW);
        }

        private final PerlinNoise perlinNoise = new PerlinNoise(0);

        private static final int STONE_SEED_OFFSET = 188434540;
    },
    DEEP_SNOW("深雪", BLK_SNOW_BLOCK, BLK_SNOW_BLOCK, "一层厚厚的积雪", BIOME_ICE_PLAINS),
    GRAVEL("沙砾", BLK_GRAVEL, BLK_GRAVEL, "砂砾", BIOME_PLAINS),
    CLAY("粘土", BLK_CLAY, BLK_CLAY, "粘土", BIOME_PLAINS),
    COBBLESTONE("圆石", BLK_COBBLESTONE, BLK_COBBLESTONE, "圆石", BIOME_PLAINS),
    MOSSY_COBBLESTONE("苔石", BLK_MOSSY_COBBLESTONE, BLK_MOSSY_COBBLESTONE, "苔石", BIOME_PLAINS),
    NETHERRACK("地狱岩", BLK_NETHERRACK, BLK_NETHERRACK, "地狱岩", BIOME_PLAINS),
    SOUL_SAND("灵魂沙", BLK_SOUL_SAND, BLK_SOUL_SAND, "灵魂沙", BIOME_PLAINS),
    OBSIDIAN("黑曜石", BLK_OBSIDIAN, BLK_OBSIDIAN, "非常坚硬的火山玻璃", BIOME_PLAINS),
    BEDROCK("基岩", BLK_BEDROCK, BLK_BEDROCK, "坚不可摧的基岩", BIOME_PLAINS),
    DESERT("沙漠", "随机生长着仙人掌和枯萎的灌木的沙子", BIOME_DESERT, 3) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz <= 0) {
                return Material.SAND;
            } else {
                final int rnd = new Random(seed + (x * 65537) + (y * 4099)).nextInt(CACTUS_CHANCE);
                final int cactusHeight;
                boolean shrub = false;
                if (rnd < 3) {
                    cactusHeight = rnd + 1;
                } else {
                    cactusHeight = 0;
                    if (rnd < 6) {
                        shrub = true;
                    }
                }
                if (dz > cactusHeight) {
                    if ((dz == 1) && shrub) {
                        return DEAD_SHRUBS;
                    } else {
                        return AIR;
                    }
                } else {
                    return CACTUS;
                }
            }
        }

        private static final int CACTUS_CHANCE = 1000;
    },
    NETHERLIKE("模拟下界", "混合着成块的熔岩、灵魂沙和萤石，顶部有一簇簇火焰的地狱岩", BIOME_HELL, 1) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 1) {
                return AIR;
            } else if (dz == 1) {
                final int rnd = new Random(seed + (x * 65537) + (y * 4099)).nextInt(FIRE_CHANCE);
                if (rnd == 0) {
                    return FIRE;
                } else {
                    return AIR;
                }
            } else {
                if (glowstoneNoise.getSeed() != (seed + GLOWSTONE_SEED_OFFSET)) {
                    glowstoneNoise.setSeed(seed + GLOWSTONE_SEED_OFFSET);
                    soulSandNoise.setSeed(seed + SOUL_SAND_SEED_OFFSET);
                    lavaNoise.setSeed(seed + LAVA_SEED_OFFSET);
                }
                if (glowstoneNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > .4) {
                    return GLOWSTONE;
                } else if(soulSandNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > .4) {
                    return Material.SOUL_SAND;
                } else if(lavaNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > .4) {
                    return Material.LAVA;
                } else {
                    return Material.NETHERRACK;
                }
            }
        }

        private final PerlinNoise glowstoneNoise = new PerlinNoise(0);
        private final PerlinNoise soulSandNoise = new PerlinNoise(0);
        private final PerlinNoise lavaNoise = new PerlinNoise(0);

        private static final int GLOWSTONE_SEED_OFFSET =  57861047;
        private static final int LAVA_SEED_OFFSET      = 189831882;
        private static final int SOUL_SAND_SEED_OFFSET =  81867522;
        private static final int FIRE_CHANCE           =       150;
    },
    @Deprecated
    RESOURCES("资源", "混合着成块的煤、矿物、砂砾、泥土、熔岩和水的地表石头", BIOME_PLAINS) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            if (z > height) {
                return AIR;
            } else {
                if (goldNoise.getSeed() != (seed + GOLD_SEED_OFFSET)) {
                    goldNoise.setSeed(       seed + GOLD_SEED_OFFSET);
                    ironNoise.setSeed(       seed + IRON_SEED_OFFSET);
                    coalNoise.setSeed(       seed + COAL_SEED_OFFSET);
                    lapisLazuliNoise.setSeed(seed + LAPIS_LAZULI_SEED_OFFSET);
                    diamondNoise.setSeed(    seed + DIAMOND_SEED_OFFSET);
                    redstoneNoise.setSeed(   seed + REDSTONE_SEED_OFFSET);
                    waterNoise.setSeed(      seed + WATER_SEED_OFFSET);
                    lavaNoise.setSeed(       seed + LAVA_SEED_OFFSET);
                    dirtNoise.setSeed(       seed + DIRT_SEED_OFFSET);
                    gravelNoise.setSeed(     seed + GRAVEL_SEED_OFFSET);
                }
                final double dx = x / TINY_BLOBS, dy = y / TINY_BLOBS, dz = z / TINY_BLOBS;
                final double dirtX = x / SMALL_BLOBS, dirtY = y / SMALL_BLOBS, dirtZ = z / SMALL_BLOBS;
                if ((z <= COAL_LEVEL) && (coalNoise.getPerlinNoise(dx, dy, dz) >= COAL_CHANCE)) {
                    return COAL;
                } else if ((z <= DIRT_LEVEL) && (dirtNoise.getPerlinNoise(dirtX, dirtY, dirtZ) >= DIRT_CHANCE)) {
                    return Material.DIRT;
                } else if ((z <= GRAVEL_LEVEL) && (gravelNoise.getPerlinNoise(dirtX, dirtY, dirtZ) >= GRAVEL_CHANCE)) {
                    return Material.GRAVEL;
                } else if ((z <= REDSTONE_LEVEL) && (redstoneNoise.getPerlinNoise(dx, dy, dz) >= REDSTONE_CHANCE)) {
                    return REDSTONE_ORE;
                } else if ((z <= IRON_LEVEL) && (ironNoise.getPerlinNoise(dx, dy, dz) >= IRON_CHANCE)) {
                    return IRON_ORE;
                } else if ((z <= WATER_LEVEL) && (waterNoise.getPerlinNoise(dx, dy, dz) >= WATER_CHANCE)) {
                    return Material.WATER;
                } else if ((z <= LAVA_LEVEL) && (lavaNoise.getPerlinNoise(dx, dy, dz) >= (LAVA_CHANCE + (z * z / 65536f)))) {
    //                System.out.println("Lava at level " + z);
    //                if (z > highestLava) {
    //                    highestLava = z;
    //                }
    //                System.out.println("Highest lava: " + highestLava);
                    return Material.LAVA;
                } else if ((z <= GOLD_LEVEL) && (goldNoise.getPerlinNoise(dx, dy, dz) >= GOLD_CHANCE)) {
                    return GOLD_ORE;
                } else if ((z <= LAPIS_LAZULI_LEVEL) && (lapisLazuliNoise.getPerlinNoise(dx, dy, dz) >= LAPIS_LAZULI_CHANCE)) {
                    return LAPIS_LAZULI_ORE;
                } else if ((z <= DIAMOND_LEVEL) && (diamondNoise.getPerlinNoise(dx, dy, dz) >= DIAMOND_CHANCE)) {
                    return DIAMOND_ORE;
                } else {
                    return Material.STONE;
                }
            }
        }

        private final PerlinNoise goldNoise        = new PerlinNoise(0);
        private final PerlinNoise ironNoise        = new PerlinNoise(0);
        private final PerlinNoise coalNoise        = new PerlinNoise(0);
        private final PerlinNoise lapisLazuliNoise = new PerlinNoise(0);
        private final PerlinNoise diamondNoise     = new PerlinNoise(0);
        private final PerlinNoise redstoneNoise    = new PerlinNoise(0);
        private final PerlinNoise waterNoise       = new PerlinNoise(0);
        private final PerlinNoise lavaNoise        = new PerlinNoise(0);
        private final PerlinNoise dirtNoise        = new PerlinNoise(0);
        private final PerlinNoise gravelNoise      = new PerlinNoise(0);

//        private int highestLava = 0;

        private static final long GOLD_SEED_OFFSET         = 148503743;
        private static final long IRON_SEED_OFFSET         = 171021655;
        private static final long COAL_SEED_OFFSET         = 81779663;
        private static final long LAPIS_LAZULI_SEED_OFFSET = 174377337;
        private static final long DIAMOND_SEED_OFFSET      = 14554756;
        private static final long REDSTONE_SEED_OFFSET     = 48636151;
        private static final long WATER_SEED_OFFSET        = 42845153;
        private static final long LAVA_SEED_OFFSET         = 62452072;
        private static final long DIRT_SEED_OFFSET         = 193567846;
        private static final long GRAVEL_SEED_OFFSET       = 19951397;
    },
    BEACHES("沙滩", "混合着成块的沙子、砂砾和粘土的草地", BIOME_BEACH) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 0) {
                return AIR;
            } else {
                if (sandNoise.getSeed() != (seed + SAND_SEED_OFFSET)) {
                    sandNoise.setSeed(seed + SAND_SEED_OFFSET);
                    clayNoise.setSeed(seed + CLAY_SEED_OFFSET);
                }
                float noise = clayNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS);
                if (noise >= BEACH_CLAY_CHANCE) {
                    return Material.CLAY;
                } else {
                    noise = sandNoise.getPerlinNoise(x / HUGE_BLOBS, y / HUGE_BLOBS, z / SMALL_BLOBS);
                    noise += sandNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) / 2;
                    if (noise >= BEACH_SAND_CHANCE) {
                        return Material.SAND;
                    } else if (-noise >= BEACH_GRAVEL_CHANCE) {
                        return Material.GRAVEL;
                    } else if (dz == 0) {
                        return Material.GRASS_BLOCK;
                    } else {
                        return Material.DIRT;
                    }
                }
            }
        }

        private final PerlinNoise sandNoise = new PerlinNoise(0);
        private final PerlinNoise clayNoise = new PerlinNoise(0);

        private static final long SAND_SEED_OFFSET = 26796036;
        private static final long CLAY_SEED_OFFSET = 161603308;
    },
    CUSTOM_1("自定义 1",                                  "自定义材质 1", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(0);
    },
    CUSTOM_2("自定义 2",                                  "自定义材质 2", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(1);
    },
    CUSTOM_3("自定义 3",                                  "自定义材质 3", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(2);
    },
    CUSTOM_4("自定义 4",                                  "自定义材质 4", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(3);
    },
    CUSTOM_5("自定义 5",                                  "自定义材质 5", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(4);
    },
    MYCELIUM("菌丝", BLK_MYCELIUM, BLK_DIRT, "菌丝", BIOME_MUSHROOM_ISLAND),
    END_STONE("末地石", BLK_END_STONE, BLK_END_STONE, "末地石", BIOME_SKY),
    BARE_GRASS("草方块", BLK_GRASS, BLK_GRASS, "裸露的草地（没有花、草丛和蕨类）", BIOME_PLAINS),
    CUSTOM_6("自定义 6",                                  "自定义材质 6", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(5);
    },
    CUSTOM_7("自定义 7",                                  "自定义材质 7", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(6);
    },
    CUSTOM_8("自定义 8",                                  "自定义材质 8", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(7);
    },
    CUSTOM_9("自定义 9",                                  "自定义材质 9", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(8);
    },
    CUSTOM_10("自定义 10",                                "自定义材质 10", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(9);
    },
    CUSTOM_11("自定义 11",                                "自定义材质 11", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(10);
    },
    CUSTOM_12("自定义 12",                                "自定义材质 12", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(11);
    },
    CUSTOM_13("自定义 13",                                "自定义材质 13", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(12);
    },
    CUSTOM_14("自定义 14",                                "自定义材质 14", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(13);
    },
    CUSTOM_15("自定义 15",                                "自定义材质 15", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(14);
    },
    CUSTOM_16("自定义 16",                                "自定义材质 16", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(15);
    },
    CUSTOM_17("自定义 17",                                "自定义材质 17", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(16);
    },
    CUSTOM_18("自定义 18",                                "自定义材质 18", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(17);
    },
    CUSTOM_19("自定义 19",                                "自定义材质 19", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(18);
    },
    CUSTOM_20("自定义 20",                                "自定义材质 20", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(19);
    },
    CUSTOM_21("自定义 21",                                "自定义材质 21", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(20);
    },
    CUSTOM_22("自定义 22",                                "自定义材质 22", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(21);
    },
    CUSTOM_23("自定义 23",                                "自定义材质 23", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(22);
    },
    CUSTOM_24("自定义 24",                                "自定义材质 24", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(23);
    },
    PERMADIRT("砂土", Material.PERMADIRT, Material.PERMADIRT, "寸草不生的砂土", BIOME_PLAINS),
    PODZOL("灰化土", Material.PODZOL, Material.DIRT, "灰化土", BIOME_PLAINS),
    RED_SAND("红沙", Material.RED_SAND, Material.RED_SAND, "红沙", BIOME_MESA),
    HARDENED_CLAY("陶瓦", Material.HARDENED_CLAY, Material.HARDENED_CLAY, "陶瓦", BIOME_MESA),
    WHITE_STAINED_CLAY("白色陶瓦", Material.WHITE_CLAY, Material.WHITE_CLAY, "白色染色陶瓦", BIOME_MESA),
    ORANGE_STAINED_CLAY("橙色陶瓦", Material.ORANGE_CLAY, Material.ORANGE_CLAY, "橙色染色陶瓦", BIOME_MESA),
    MAGENTA_STAINED_CLAY("品红色陶瓦", Material.MAGENTA_CLAY, Material.MAGENTA_CLAY, "品红色染色陶瓦", BIOME_PLAINS),
    LIGHT_BLUE_STAINED_CLAY("淡蓝色陶瓦", Material.LIGHT_BLUE_CLAY, Material.LIGHT_BLUE_CLAY, "淡蓝色染色陶瓦", BIOME_PLAINS),
    YELLOW_STAINED_CLAY("黄色陶瓦", Material.YELLOW_CLAY, Material.YELLOW_CLAY, "黄色染色陶瓦", BIOME_MESA),
    LIME_STAINED_CLAY("黄绿色陶瓦", Material.LIME_CLAY, Material.LIME_CLAY, "黄绿色染色陶瓦", BIOME_PLAINS),
    PINK_STAINED_CLAY("粉红色陶瓦", Material.PINK_CLAY, Material.PINK_CLAY, "粉红色染色陶瓦", BIOME_PLAINS),
    GREY_STAINED_CLAY("灰色陶瓦", Material.GREY_CLAY, Material.GREY_CLAY, "灰色染色陶瓦", BIOME_PLAINS),
    LIGHT_GREY_STAINED_CLAY("淡灰色陶瓦", Material.LIGHT_GREY_CLAY, Material.LIGHT_GREY_CLAY, "淡灰色染色陶瓦", BIOME_MESA),
    CYAN_STAINED_CLAY("青色陶瓦", Material.CYAN_CLAY, Material.CYAN_CLAY, "青色染色陶瓦", BIOME_PLAINS),
    PURPLE_STAINED_CLAY("紫色陶瓦", Material.PURPLE_CLAY, Material.PURPLE_CLAY, "紫色染色陶瓦", BIOME_PLAINS),
    BLUE_STAINED_CLAY("蓝色陶瓦", Material.BLUE_CLAY, Material.BLUE_CLAY, "蓝色染色陶瓦", BIOME_PLAINS),
    BROWN_STAINED_CLAY("棕色陶瓦", Material.BROWN_CLAY, Material.BROWN_CLAY, "棕色染色陶瓦", BIOME_MESA),
    GREEN_STAINED_CLAY("绿色陶瓦", Material.GREEN_CLAY, Material.GREEN_CLAY, "绿色染色陶瓦", BIOME_PLAINS),
    RED_STAINED_CLAY("红色陶瓦", Material.RED_CLAY, Material.RED_CLAY, "红色染色陶瓦", BIOME_MESA),
    BLACK_STAINED_CLAY("黑色陶瓦", Material.BLACK_CLAY, Material.BLACK_CLAY, "黑色染色陶瓦", BIOME_PLAINS),
    MESA("平顶山", "一层一层的红沙，硬化黏土和染色黏土，随机生长着枯萎的灌木", BIOME_MESA, 1) {
        @Override
        public Material getMaterial(Platform platform, final long seed, final int x, final int y, final int z, final int height) {
            return getMaterial(platform, seed, x, y, (float) z, height);
        }

        @Override
        public Material getMaterial(Platform platform, final long seed, final int x, final int y, final float z, final int height) {
            if (seed != this.seed) {
                init(seed);
            }
            final int dz = (int) (z + 0.5f) - height;
            if (dz <= 0) {
                return LAYERS[(int) (z + (perlinNoise.getPerlinNoise(x / GIGANTIC_BLOBS, y / GIGANTIC_BLOBS) * 4 + perlinNoise.getPerlinNoise(x / HUGE_BLOBS, y / HUGE_BLOBS) + perlinNoise.getPerlinNoise(x / LARGE_BLOBS, y / LARGE_BLOBS) + perlinNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS) / 4 + 3.125f) * 8) % LAYER_COUNT];
            } else if (dz == 1) {
                final int rnd = new Random(seed + (x * 65537) + (y * 4099)).nextInt(SHRUB_CHANCE);
                if (rnd < 3) {
                    return DEAD_SHRUBS;
                } else {
                    return AIR;
                }
            } else {
                return AIR;
            }
        }

        private void init(long seed) {
            this.seed = seed;
            perlinNoise.setSeed(seed + NOISE_SEED_OFFSET);
            final Random random = new Random(seed);
            Arrays.fill(LAYERS, Material.HARDENED_CLAY);
            for (int i = 0; i < LAYER_COUNT / 2; i++) {
                final int index = random.nextInt(LAYER_COUNT - 1);
                final Material material = MATERIALS[random.nextInt(MATERIALS.length)];
                LAYERS[index] = material;
                LAYERS[index + 1] = material;
            }
        }

        private final Material[] LAYERS = new Material[LAYER_COUNT];
        private final PerlinNoise perlinNoise = new PerlinNoise(0);
        private long seed = Long.MIN_VALUE;

        private final Material[] MATERIALS = {Material.RED_SAND, Material.HARDENED_CLAY, Material.WHITE_CLAY, Material.LIGHT_GREY_CLAY, Material.YELLOW_CLAY, Material.ORANGE_CLAY, Material.RED_CLAY, Material.BROWN_CLAY};

        private static final int LAYER_COUNT = 64;
        private static final int SHRUB_CHANCE = 500;
        private static final long NOISE_SEED_OFFSET = 110335839L;
    },
    RED_DESERT("红沙沙漠", "生长着仙人掌和枯萎的灌木的红沙", BIOME_MESA, 3) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz <= 0) {
                return Material.RED_SAND;
            } else {
                final int rnd = new Random(seed + (x * 65537) + (y * 4099)).nextInt(CACTUS_CHANCE);
                final int cactusHeight;
                boolean shrub = false;
                if (rnd < 3) {
                    cactusHeight = rnd + 1;
                } else {
                    cactusHeight = 0;
                    if (rnd < 12) {
                        shrub = true;
                    }
                }
                if (dz > cactusHeight) {
                    if ((dz == 1) && shrub) {
                        return DEAD_SHRUBS;
                    } else {
                        return AIR;
                    }
                } else {
                    return CACTUS;
                }
            }
        }

        private static final int CACTUS_CHANCE = 2000;
    },
    RED_SANDSTONE("红砂岩", BLK_RED_SANDSTONE, BLK_RED_SANDSTONE, "红砂岩", BIOME_MESA),
    GRANITE("花岗岩", Material.GRANITE, Material.GRANITE, "花岗岩", BIOME_PLAINS),
    DIORITE("闪长岩", Material.DIORITE, Material.DIORITE, "闪长岩", BIOME_PLAINS),
    ANDESITE("安山岩", Material.ANDESITE, Material.ANDESITE, "安山岩", BIOME_PLAINS),
    STONE_MIX("混合岩石", "混合着花岗岩、闪长岩和安山岩的岩石", BIOME_PLAINS) {
        @Override
        public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {
            final int dz = z - height;
            if (dz > 0) {
                return AIR;
            } else {
                if (graniteNoise.getSeed() != (seed + GRANITE_SEED_OFFSET)) {
                    graniteNoise.setSeed(seed + GRANITE_SEED_OFFSET);
                    dioriteNoise.setSeed(seed + DIORITE_SEED_OFFSET);
                    andesiteNoise.setSeed(seed + ANDESITE_SEED_OFFSET);
                }
                if (graniteNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > GRANITE_CHANCE) {
                    return Material.GRANITE;
                } else if(dioriteNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > DIORITE_CHANCE) {
                    return Material.DIORITE;
                } else if(andesiteNoise.getPerlinNoise(x / SMALL_BLOBS, y / SMALL_BLOBS, z / SMALL_BLOBS) > ANDESITE_CHANCE) {
                    return Material.ANDESITE;
                } else {
                    return Material.STONE;
                }
            }
        }

        private final PerlinNoise graniteNoise  = new PerlinNoise(0);
        private final PerlinNoise dioriteNoise  = new PerlinNoise(0);
        private final PerlinNoise andesiteNoise = new PerlinNoise(0);

        private static final int GRANITE_SEED_OFFSET  = 145827825;
        private static final int DIORITE_SEED_OFFSET  =  59606124;
        private static final int ANDESITE_SEED_OFFSET =  87772192;
    },
    CUSTOM_25("自定义 25",                                  "自定义材质 25", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(24);
    },
    CUSTOM_26("自定义 26",                                  "自定义材质 26", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(25);
    },
    CUSTOM_27("自定义 27",                                  "自定义材质 27", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(26);
    },
    CUSTOM_28("自定义 28",                                  "自定义材质 28", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(27);
    },
    CUSTOM_29("自定义 29",                                  "自定义材质 29", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(28);
    },
    CUSTOM_30("自定义 30",                                  "自定义材质 30", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(29);
    },
    CUSTOM_31("自定义 31",                                  "自定义材质 31", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(30);
    },
    CUSTOM_32("自定义 32",                                  "自定义材质 32", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(31);
    },
    CUSTOM_33("自定义 33",                                  "自定义材质 33", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(32);
    },
    CUSTOM_34("自定义 34",                                  "自定义材质 34", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(33);
    },
    CUSTOM_35("自定义 35",                                  "自定义材质 35", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(34);
    },
    CUSTOM_36("自定义 36",                                  "自定义材质 36", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(35);
    },
    CUSTOM_37("自定义 37",                                  "自定义材质 37", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(36);
    },
    CUSTOM_38("自定义 38",                                  "自定义材质 38", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(37);
    },
    CUSTOM_39("自定义 39",                                  "自定义材质 39", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(38);
    },
    CUSTOM_40("自定义 40",                                  "自定义材质 40", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(39);
    },
    CUSTOM_41("自定义 41",                                  "自定义材质 41", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(40);
    },
    CUSTOM_42("自定义 42",                                  "生命、宇宙和一切", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(41);
    },
    CUSTOM_43("自定义 43",                                  "自定义材质 43", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(42);
    },
    CUSTOM_44("自定义 44",                                  "自定义材质 44", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(43);
    },
    CUSTOM_45("自定义 45",                                  "自定义材质 45", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(44);
    },
    CUSTOM_46("自定义 46",                                  "自定义材质 46", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(45);
    },
    CUSTOM_47("自定义 47",                                  "自定义材质 47", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(46);
    },
    CUSTOM_48("自定义 48",                                  "自定义材质 48", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(47);
    },
    GRASS_PATH("草径", BLK_GRASS_PATH, BLK_GRASS, "草径", BIOME_PLAINS),
    MAGMA("熔岩", BLK_MAGMA, BLK_MAGMA, "熔岩", BIOME_PLAINS), // TODO: or should this be mapped to stone and magma added to the Resources layer?
    CUSTOM_49("自定义 49", "自定义材质 49", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(48);
    },
    CUSTOM_50("自定义 50", "自定义材质 50", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(49);
    },
    CUSTOM_51("自定义 51", "自定义材质 51", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(50);
    },
    CUSTOM_52("自定义 52", "自定义材质 52", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(51);
    },
    CUSTOM_53("自定义 53", "自定义材质 53", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(52);
    },
    CUSTOM_54("自定义 54", "自定义材质 54", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(53);
    },
    CUSTOM_55("自定义 55", "自定义材质 55", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(54);
    },
    CUSTOM_56("自定义 56", "自定义材质 56", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(55);
    },
    CUSTOM_57("自定义 57", "自定义材质 57", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(56);
    },
    CUSTOM_58("自定义 58", "自定义材质 58", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(57);
    },
    CUSTOM_59("自定义 59", "自定义材质 59", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(58);
    },
    CUSTOM_60("自定义 60", "自定义材质 60", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(59);
    },
    CUSTOM_61("自定义 61", "自定义材质 61", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(60);
    },
    CUSTOM_62("自定义 62", "自定义材质 62", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(61);
    },
    CUSTOM_63("自定义 63", "自定义材质 63", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(62);
    },
    CUSTOM_64("自定义 64", "自定义材质 64", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(63);
    },
    CUSTOM_65("自定义 65", "自定义材质 65", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(64);
    },
    CUSTOM_66("自定义 66", "自定义材质 66", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(65);
    },
    CUSTOM_67("自定义 67", "自定义材质 67", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(66);
    },
    CUSTOM_68("自定义 68", "自定义材质 68", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(67);
    },
    CUSTOM_69("自定义 69", "自定义材质 69", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(68);
    },
    CUSTOM_70("自定义 70", "自定义材质 70", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(69);
    },
    CUSTOM_71("自定义 71", "自定义材质 71", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(70);
    },
    CUSTOM_72("自定义 72", "自定义材质 72", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(71);
    },
    CUSTOM_73("自定义 73", "自定义材质 73", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(72);
    },
    CUSTOM_74("自定义 74", "自定义材质 74", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(73);
    },
    CUSTOM_75("自定义 75", "自定义材质 75", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(74);
    },
    CUSTOM_76("自定义 76", "自定义材质 76", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(75);
    },
    CUSTOM_77("自定义 77", "自定义材质 77", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(76);
    },
    CUSTOM_78("自定义 78", "自定义材质 78", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(77);
    },
    CUSTOM_79("自定义 79", "自定义材质 79", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(78);
    },
    CUSTOM_80("自定义 80", "自定义材质 80", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(79);
    },
    CUSTOM_81("自定义 81", "自定义材质 81", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(80);
    },
    CUSTOM_82("自定义 82", "自定义材质 82", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(81);
    },
    CUSTOM_83("自定义 83", "自定义材质 83", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(82);
    },
    CUSTOM_84("自定义 84", "自定义材质 84", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(83);
    },
    CUSTOM_85("自定义 85", "自定义材质 85", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(84);
    },
    CUSTOM_86("自定义 86", "自定义材质 86", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(85);
    },
    CUSTOM_87("自定义 87", "自定义材质 87", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(86);
    },
    CUSTOM_88("自定义 88", "自定义材质 88", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(87);
    },
    CUSTOM_89("自定义 89", "自定义材质 89", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(88);
    },
    CUSTOM_90("自定义 90", "自定义材质 90", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(89);
    },
    CUSTOM_91("自定义 91", "自定义材质 91", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(90);
    },
    CUSTOM_92("自定义 92", "自定义材质 92", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(91);
    },
    CUSTOM_93("自定义 93", "自定义材质 93", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(92);
    },
    CUSTOM_94("自定义 94", "自定义材质 94", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(93);
    },
    CUSTOM_95("自定义 95", "自定义材质 95", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(94);
    },
    CUSTOM_96("自定义 96", "自定义材质 96", BIOME_PLAINS) {
        @Override public Material getMaterial(Platform platform, long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public Material getMaterial(Platform platform, long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}

        @Override public String getName() {return helper.getName();}

        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}

        @Override public boolean isCustom() {return true;}

        @Override public boolean isConfigured() {return helper.isConfigured();}

        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}

        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}

        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}

        private final CustomTerrainHelper helper = new CustomTerrainHelper(95);
    };

    Terrain(String name, String description, int defaultBiome) {
        this(name, Material.STONE, Material.STONE, description, defaultBiome, 0);
    }

    Terrain(String name, String description, int defaultBiome, int toppingHeight) {
        this(name, Material.STONE, Material.STONE, description, defaultBiome, toppingHeight);
    }

    Terrain(String name, int topMaterial, int topLayerMaterial, String description, int defaultBiome) {
        this(name, Material.get(topMaterial), Material.get(topLayerMaterial), description, defaultBiome, 0);
    }

    Terrain(String name, Material topMaterial, Material topLayerMaterial, String description, int defaultBiome) {
        this(name, topMaterial, topLayerMaterial, description, defaultBiome, 0);
    }

    Terrain(String name, Material topMaterial, Material topLayerMaterial, String description, int defaultBiome, int toppingHeight) {
        this.name = name;
        this.topMaterial = topMaterial;
        this.topLayerMaterial = topLayerMaterial;
        this.toppingHeight = toppingHeight;
        this.description = description;
        this.defaultBiome = defaultBiome;
        icon = IconUtils.scaleIcon(IconUtils.loadUnscaledImage("org/pepsoft/worldpainter/icons/" + name().toLowerCase() + ".png"), 16);
    }

    public String getName() {
        return name;
    }

    /**
     * Get the material to use for this terrain type at a specific location in
     * the world, relative to the surface, for an unspecified platform.
     *
     * <p>The default implementation forwards to
     * {@link #getMaterial(Platform, long, int, int, int, int)}.
     *
     * @param seed The world seed.
     * @param x The absolute X position of the block in WorldPainter coordinates.
     * @param y The absolute Y position of the block in WorldPainter coordinates.
     * @param z The absolute Z position of the block in WorldPainter coordinates.
     * @param height The height of the terrain at the specified X and Y
     *     coordinates.
     * @return The material at the specified location in the terrain.
     */
    public Material getMaterial(final long seed, final int x, final int y, final float z, final int height) {
        return getMaterial(JAVA_ANVIL_1_13, seed, x, y, (int) (z + 0.5f), height);
    }

    /**
     * Get the material to use for this terrain type at a specific location in
     * the world, relative to the surface, for an unspecified platform.
     *
     * <p>The default implementation forwards to
     * {@link #getMaterial(Platform, long, int, int, int, int)}.
     *
     * @param seed The world seed.
     * @param x The absolute X position of the block in WorldPainter coordinates.
     * @param y The absolute Y position of the block in WorldPainter coordinates.
     * @param z The absolute Z position of the block in WorldPainter coordinates.
     * @param height The height of the terrain at the specified X and Y
     *     coordinates.
     * @return The material at the specified location in the terrain.
     */
    public Material getMaterial(final long seed, final int x, final int y, final int z, final int height) {
        return getMaterial(JAVA_ANVIL_1_13, seed, x, y, z, height);
    }

    /**
     * Get the material to use for this terrain type at a specific location in
     * the world, relative to the surface, for a specific platform.
     *
     * <p>The default implementation forwards to
     * {@link #getMaterial(Platform, long, int, int, int, int)}.
     *
     * @param platform The platform for which to get the block type.
     * @param seed The world seed.
     * @param x The absolute X position of the block in WorldPainter coordinates.
     * @param y The absolute Y position of the block in WorldPainter coordinates.
     * @param z The absolute Z position of the block in WorldPainter coordinates.
     * @param height The height of the terrain at the specified X and Y
     *     coordinates.
     * @return The material at the specified location in the terrain.
     */
    public Material getMaterial(final Platform platform, final long seed, final int x, final int y, final float z, final int height) {
        return getMaterial(platform, seed, x, y, (int) (z + 0.5f), height);
    }

    /**
     * Get the material to use for this terrain type at a specific location in
     * the world, relative to the surface, for a specific platform.
     *
     * @param platform The platform for which to get the block type.
     * @param seed The world seed.
     * @param x The absolute X position of the block in WorldPainter coordinates.
     * @param y The absolute Y position of the block in WorldPainter coordinates.
     * @param z The absolute Z position of the block in WorldPainter coordinates.
     * @param height The height of the terrain at the specified X and Y
     *     coordinates.
     * @return The material at the specified location in the terrain.
     */
    public Material getMaterial(final Platform platform, final long seed, final int x, final int y, final int z, final int height) {
        final int dz = z - height;
        if (dz > 0) {
            return Material.AIR;
        } else if (dz == 0) {
            return topMaterial;
        } else {
            return topLayerMaterial;
        }
    }

    public String getDescription() {
        return description;
    }

    public BufferedImage getIcon(ColourScheme colourScheme) {
        return icon;
    }

    public int getColour(final long seed, final int x, final int y, final float z, final int height, final ColourScheme colourScheme) {
        try {
            return colourScheme.getColour(getMaterial(seed, x, y, z, height));
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getClass().getSimpleName() + " while getting colour of material " + getMaterial(seed, x, y, z, height) + " @ " + x + "," + y + "," + z + "," + height + " for terrain " + this, e);
        }
    }

    public int getColour(final long seed, final int x, final int y, final int z, final int height, final ColourScheme colourScheme) {
        try {
            return colourScheme.getColour(getMaterial(seed, x, y, z, height));
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getClass().getSimpleName() + " while getting colour of material " + getMaterial(seed, x, y, z, height) + " @ " + x + "," + y + "," + z + "," + height + " for terrain " + this, e);
        }
    }

    public int getDefaultBiome() {
        return defaultBiome;
    }

    public boolean isCustom() {
        return false;
    }

    public boolean isConfigured() {
        return true;
    }

    public int getCustomTerrainIndex() {
        throw new IllegalArgumentException("Not a custom terrain");
    }

    public int getToppingHeight() {
        return toppingHeight;
    }

    // Object

    @Override
    public String toString() {
        return name;
    }

    public static boolean isCustomMaterialConfigured(int index) {
        return customMaterials[index] != null;
    }

    public static int getConfiguredCustomMaterialCount() {
        return (int) Arrays.stream(customMaterials).filter(Objects::nonNull).count();
    }

    public static MixedMaterial getCustomMaterial(int index) {
        return customMaterials[index];
    }
    
    public static void setCustomMaterial(int index, MixedMaterial material) {
        customMaterials[index] = material;
    }
    
    public static Terrain getCustomTerrain(int index) {
        return (index < 48)
                ? ((index < 24)
                    ? VALUES[index + 47]
                    : VALUES[index + 52])
                : VALUES[index + 54];
    }
    
    public static Terrain[] getConfiguredValues() {
        ArrayList<Terrain> values = new ArrayList<>(VALUES.length);
        for (Terrain terrain: VALUES) {
            if ((! terrain.isCustom()) || terrain.isConfigured()) {
                values.add(terrain);
            }
        }
        return values.toArray(new Terrain[values.size()]);
    }

    private final Material topMaterial, topLayerMaterial;
    private final int toppingHeight;
    private final String name, description;
    private final BufferedImage icon;
    private final int defaultBiome;
    
    public static final int CUSTOM_TERRAIN_COUNT = 96;

    static final MixedMaterial[] customMaterials = new MixedMaterial[CUSTOM_TERRAIN_COUNT];

    static final int GOLD_LEVEL         = 32;
    static final int IRON_LEVEL         = 48;
    static final int COAL_LEVEL         = Integer.MAX_VALUE;
    static final int LAPIS_LAZULI_LEVEL = 32;
    static final int DIAMOND_LEVEL      = 16;
    static final int REDSTONE_LEVEL     = 16;
    static final int WATER_LEVEL        = Integer.MAX_VALUE;
    static final int LAVA_LEVEL         = 80;
    static final int DIRT_LEVEL         = Integer.MAX_VALUE;
    static final int GRAVEL_LEVEL       = Integer.MAX_VALUE;
        
    static final float GOLD_CHANCE         = PerlinNoise.getLevelForPromillage(1);
    static final float IRON_CHANCE         = PerlinNoise.getLevelForPromillage(5);
    static final float COAL_CHANCE         = PerlinNoise.getLevelForPromillage(9);
    static final float LAPIS_LAZULI_CHANCE = PerlinNoise.getLevelForPromillage(1);
    static final float DIAMOND_CHANCE      = PerlinNoise.getLevelForPromillage(1);
    static final float REDSTONE_CHANCE     = PerlinNoise.getLevelForPromillage(6);
    static final float WATER_CHANCE        = PerlinNoise.getLevelForPromillage(1);
    static final float LAVA_CHANCE         = PerlinNoise.getLevelForPromillage(1);
    static final float DIRT_CHANCE         = PerlinNoise.getLevelForPromillage(9);
    static final float GRAVEL_CHANCE       = PerlinNoise.getLevelForPromillage(9);
    
    static final float FLOWER_CHANCE       = PerlinNoise.getLevelForPromillage(10);
    static final float FERN_CHANCE         = PerlinNoise.getLevelForPromillage(10);
    static final float GRASS_CHANCE        = PerlinNoise.getLevelForPromillage(100);

    static final float DOUBLE_TALL_GRASS_CHANCE = PerlinNoise.getLevelForPromillage(50);
    
    static final float BEACH_SAND_CHANCE   = PerlinNoise.getLevelForPromillage(400) * 1.5f;
    static final float BEACH_GRAVEL_CHANCE = PerlinNoise.getLevelForPromillage(200) * 1.5f;
    static final float BEACH_CLAY_CHANCE   = PerlinNoise.getLevelForPromillage(40);

    static final float GRANITE_CHANCE  = PerlinNoise.getLevelForPromillage(45);
    static final float DIORITE_CHANCE  = PerlinNoise.getLevelForPromillage(45);
    static final float ANDESITE_CHANCE = PerlinNoise.getLevelForPromillage(45);

    /**
     * This information is now public, so don't change it! Only add new values
     * at the end!
     */
    public static final Terrain[] VALUES = {
        Terrain.GRASS,
        Terrain.BARE_GRASS,
        Terrain.DIRT,
        Terrain.PERMADIRT,
        Terrain.PODZOL,
        Terrain.SAND,
        Terrain.RED_SAND,
        Terrain.DESERT,
        Terrain.RED_DESERT,
        Terrain.MESA,

        Terrain.HARDENED_CLAY,
        Terrain.WHITE_STAINED_CLAY,
        Terrain.ORANGE_STAINED_CLAY,
        Terrain.MAGENTA_STAINED_CLAY,
        Terrain.LIGHT_BLUE_STAINED_CLAY,
        Terrain.YELLOW_STAINED_CLAY,
        Terrain.LIME_STAINED_CLAY,
        Terrain.PINK_STAINED_CLAY,
        Terrain.GREY_STAINED_CLAY,
        Terrain.LIGHT_GREY_STAINED_CLAY,

        Terrain.CYAN_STAINED_CLAY,
        Terrain.PURPLE_STAINED_CLAY,
        Terrain.BLUE_STAINED_CLAY,
        Terrain.BROWN_STAINED_CLAY,
        Terrain.GREEN_STAINED_CLAY,
        Terrain.RED_STAINED_CLAY,
        Terrain.BLACK_STAINED_CLAY,
        Terrain.SANDSTONE,
        Terrain.STONE,
        Terrain.ROCK,

        Terrain.COBBLESTONE,
        Terrain.MOSSY_COBBLESTONE,
        Terrain.OBSIDIAN,
        Terrain.BEDROCK,
        Terrain.GRAVEL,
        Terrain.CLAY,
        Terrain.BEACHES,
        Terrain.WATER,
        Terrain.LAVA,
        Terrain.SNOW,

        Terrain.DEEP_SNOW,
        Terrain.NETHERRACK,
        Terrain.SOUL_SAND,
        Terrain.NETHERLIKE,
        Terrain.MYCELIUM,
        Terrain.END_STONE,
        Terrain.RESOURCES,
        Terrain.CUSTOM_1,
        Terrain.CUSTOM_2,
        Terrain.CUSTOM_3,

        Terrain.CUSTOM_4,
        Terrain.CUSTOM_5,
        Terrain.CUSTOM_6,
        Terrain.CUSTOM_7,
        Terrain.CUSTOM_8,
        Terrain.CUSTOM_9,
        Terrain.CUSTOM_10,
        Terrain.CUSTOM_11,
        Terrain.CUSTOM_12,
        Terrain.CUSTOM_13,

        Terrain.CUSTOM_14,
        Terrain.CUSTOM_15,
        Terrain.CUSTOM_16,
        Terrain.CUSTOM_17,
        Terrain.CUSTOM_18,
        Terrain.CUSTOM_19,
        Terrain.CUSTOM_20,
        Terrain.CUSTOM_21,
        Terrain.CUSTOM_22,
        Terrain.CUSTOM_23,

        Terrain.CUSTOM_24,
        Terrain.RED_SANDSTONE,
        Terrain.GRANITE,
        Terrain.DIORITE,
        Terrain.ANDESITE,
        Terrain.STONE_MIX,
        Terrain.CUSTOM_25,
        Terrain.CUSTOM_26,
        Terrain.CUSTOM_27,
        Terrain.CUSTOM_28,

        Terrain.CUSTOM_29,
        Terrain.CUSTOM_30,
        Terrain.CUSTOM_31,
        Terrain.CUSTOM_32,
        Terrain.CUSTOM_33,
        Terrain.CUSTOM_34,
        Terrain.CUSTOM_35,
        Terrain.CUSTOM_36,
        Terrain.CUSTOM_37,
        Terrain.CUSTOM_38,

        Terrain.CUSTOM_39,
        Terrain.CUSTOM_40,
        Terrain.CUSTOM_41,
        Terrain.CUSTOM_42,
        Terrain.CUSTOM_43,
        Terrain.CUSTOM_44,
        Terrain.CUSTOM_45,
        Terrain.CUSTOM_46,
        Terrain.CUSTOM_47,
        Terrain.CUSTOM_48,

        Terrain.GRASS_PATH,
        Terrain.MAGMA,
        Terrain.CUSTOM_49,
        Terrain.CUSTOM_50,
        Terrain.CUSTOM_51,
        Terrain.CUSTOM_52,
        Terrain.CUSTOM_53,
        Terrain.CUSTOM_54,
        Terrain.CUSTOM_55,
        Terrain.CUSTOM_56,

        Terrain.CUSTOM_57,
        Terrain.CUSTOM_58,
        Terrain.CUSTOM_59,
        Terrain.CUSTOM_60,
        Terrain.CUSTOM_61,
        Terrain.CUSTOM_62,
        Terrain.CUSTOM_63,
        Terrain.CUSTOM_64,
        Terrain.CUSTOM_65,
        Terrain.CUSTOM_66,

        Terrain.CUSTOM_67,
        Terrain.CUSTOM_68,
        Terrain.CUSTOM_69,
        Terrain.CUSTOM_70,
        Terrain.CUSTOM_71,
        Terrain.CUSTOM_72,
        Terrain.CUSTOM_73,
        Terrain.CUSTOM_74,
        Terrain.CUSTOM_75,
        Terrain.CUSTOM_76,

        Terrain.CUSTOM_77,
        Terrain.CUSTOM_78,
        Terrain.CUSTOM_79,
        Terrain.CUSTOM_80,
        Terrain.CUSTOM_81,
        Terrain.CUSTOM_82,
        Terrain.CUSTOM_83,
        Terrain.CUSTOM_84,
        Terrain.CUSTOM_85,
        Terrain.CUSTOM_86,

        Terrain.CUSTOM_87,
        Terrain.CUSTOM_88,
        Terrain.CUSTOM_89,
        Terrain.CUSTOM_90,
        Terrain.CUSTOM_91,
        Terrain.CUSTOM_92,
        Terrain.CUSTOM_93,
        Terrain.CUSTOM_94,
        Terrain.CUSTOM_95,
        Terrain.CUSTOM_96
    };

    /**
     * This list is meant to present to the user. It has a more logical order
     * and lacks the custom and deprecated terrain types. This list may be
     * changed in any way.
     */
    public static final Terrain[] PICK_LIST = {
        Terrain.GRASS,
        Terrain.BARE_GRASS,
        Terrain.GRASS_PATH,
        Terrain.DIRT,
        Terrain.PERMADIRT,
        Terrain.PODZOL,
        Terrain.SAND,
        Terrain.RED_SAND,
        Terrain.DESERT,
        Terrain.RED_DESERT,

        Terrain.MESA,
        Terrain.HARDENED_CLAY,
        Terrain.SANDSTONE,
        Terrain.RED_SANDSTONE,
        Terrain.STONE_MIX,
        Terrain.STONE,
        Terrain.GRANITE,
        Terrain.DIORITE,
        Terrain.ANDESITE,
        Terrain.ROCK,

        Terrain.COBBLESTONE,
        Terrain.MOSSY_COBBLESTONE,
        Terrain.OBSIDIAN,
        Terrain.BEDROCK,
        Terrain.GRAVEL,
        Terrain.CLAY,
        Terrain.BEACHES,
        Terrain.WATER,
        Terrain.LAVA,
        Terrain.MAGMA,

        Terrain.DEEP_SNOW,
        Terrain.NETHERRACK,
        Terrain.SOUL_SAND,
        Terrain.NETHERLIKE,
        Terrain.MYCELIUM,
        Terrain.END_STONE,
        Terrain.WHITE_STAINED_CLAY,
        Terrain.ORANGE_STAINED_CLAY,
        Terrain.MAGENTA_STAINED_CLAY,
        Terrain.LIGHT_BLUE_STAINED_CLAY,

        Terrain.YELLOW_STAINED_CLAY,
        Terrain.LIME_STAINED_CLAY,
        Terrain.PINK_STAINED_CLAY,
        Terrain.GREY_STAINED_CLAY,
        Terrain.LIGHT_GREY_STAINED_CLAY,
        Terrain.CYAN_STAINED_CLAY,
        Terrain.PURPLE_STAINED_CLAY,
        Terrain.BLUE_STAINED_CLAY,
        Terrain.BROWN_STAINED_CLAY,
        Terrain.GREEN_STAINED_CLAY,

        Terrain.RED_STAINED_CLAY,
        Terrain.BLACK_STAINED_CLAY
    };

    /*
     * A helper method for generating additional custom terrain types. Should be
     * edited before use.
     */
    public static void main(String[] args) {
        String[] tens = {"forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
        String[] ones = {"", "-one", "-two", "-three", "-four", "-five", "-six", "-seven", "-eight", "-nine"};
        for (int i = 49; i <= 96; i++) {
            System.out.printf("    CUSTOM_%1$d(\"Custom %1$d\", \"custom material %3$s%4$s\", BIOME_PLAINS) {%n" +
                            "        @Override public Material getMaterial(long seed, int x, int y, int z, int height) {return helper.getMaterial(seed, x, y, z, height);}%n" +
                            "%n" +
                            "        @Override public Material getMaterial(long seed, int x, int y, float z, int height) {return helper.getMaterial(seed, x, y, z, height);}%n" +
                            "%n" +
                            "        @Override public String getName() {return helper.getName();}%n" +
                            "%n" +
                            "        @Override public BufferedImage getIcon(ColourScheme colourScheme) {return helper.getIcon(colourScheme);}%n" +
                            "%n" +
                            "        @Override public boolean isCustom() {return true;}%n" +
                            "%n" +
                            "        @Override public boolean isConfigured() {return helper.isConfigured();}%n" +
                            "%n" +
                            "        @Override public int getDefaultBiome() {return helper.getDefaultBiome();}%n" +
                            "%n" +
                            "        @Override public int getCustomTerrainIndex() {return helper.getCustomTerrainIndex();}%n" +
                            "%n" +
                            "        @Override public int getColour(long seed, int x, int y, int z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}%n" +
                            "%n" +
                            "        @Override public int getColour(long seed, int x, int y, float z, int height, ColourScheme colourScheme) {return helper.getColour(seed, x, y, z, height, colourScheme);}%n" +
                            "%n" +
                            "        private final CustomTerrainHelper helper = new CustomTerrainHelper(%2$d);%n" +
                            "    },%n",
                    i,
                    i - 1,
                    tens[(i / 10) - 4],
                    ones[i % 10]);
        }
        for (int i = 49; i <= 96; i++) {
            System.out.println("       Terrain.CUSTOM_" + i);
        }
    }
}