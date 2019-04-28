package org.pepsoft.worldpainter.biomeschemes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * All Minecraft 1.13 biome IDs, plus names and other info.
 */
public interface Minecraft1_13Biomes extends Minecraft1_7Biomes {
    int BIOME_END_SMALL_ISLANDS = 40;
    int BIOME_END_MIDLANDS = 41;
    int BIOME_END_HIGHLANDS = 42;
    int BIOME_END_BARRENS = 43;
    int BIOME_WARM_OCEAN = 44;
    int BIOME_LUKEWARM_OCEAN = 45;
    int BIOME_COLD_OCEAN = 46;
    int BIOME_DEEP_WARM_OCEAN = 47;
    int BIOME_DEEP_LUKEWARM_OCEAN = 48;
    int BIOME_DEEP_COLD_OCEAN = 49;
    int BIOME_DEEP_FROZEN_OCEAN = 50;

    int BIOME_VOID = 127;

    int FIRST_UNALLOCATED_ID = BIOME_DEEP_FROZEN_OCEAN + 1;

    String[] BIOME_NAMES = {
            "海洋",
            "平原",
            "沙漠",
            "山地",
            "森林",
            "针叶林",
            "沼泽",
            "河流",
            "下界",
            "末地",

            "冻洋",
            "冻河",
            "积雪的冻原",
            "雪山",
            "蘑菇岛",
            "蘑菇岛岸",
            "沙滩",
            "沙漠丘陵",
            "繁茂的丘陵",
            "针叶林丘陵",

            "山地边缘",
            "丛林",
            "丛林丘陵",
            "丛林边缘",
            "深海",
            "石岸",
            "积雪的沙滩",
            "桦木森林",
            "桦木森林丘陵",
            "黑森林",

            "积雪的针叶林",
            "积雪的针叶林丘陵",
            "巨型针叶林",
            "巨型针叶林丘陵",
            "繁茂的山地",
            "热带草原",
            "热带高原",
            "恶地",
            "繁茂的恶地高原",
            "恶地高原",

            "末地小型岛屿",
            "末地中型岛屿",
            "末地高岛",
            "末地荒岛",
            "暖水海洋",
            "温水海洋",
            "冷水海洋",
            "暖水深海",
            "温水深海",
            "冷水深海",

            "封冻深海",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "虚空",
            null,
            "向日葵平原",

            "沙漠湖泊",
            "沙砾山地",
            "繁花森林",
            "针叶林山地",
            "沼泽山丘",
            null,
            null,
            null,
            null,
            null,

            "冰刺平原",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "丛林变种",

            null,
            "丛林边缘变种",
            null,
            null,
            null,
            "高大桦木森林",
            "高大桦木丘陵",
            "黑森林丘陵",
            "积雪的针叶林山地",
            null,

            "巨型云杉针叶林",
            "巨型云杉针叶林丘陵",
            "沙砾山地+",
            "破碎的热带草原",
            "破碎的热带高原",
            "被风蚀的恶地",
            "繁茂的恶地高原变种",
            "恶地高原变种"
    };

    Set<Integer> DRY_BIOMES = new HashSet<>(Arrays.asList(BIOME_DESERT, BIOME_DESERT_HILLS, BIOME_DESERT_M, BIOME_HELL, BIOME_SAVANNA, BIOME_SAVANNA_M, BIOME_MESA, BIOME_MESA_BRYCE, BIOME_SAVANNA_PLATEAU, BIOME_SAVANNA_PLATEAU_M, BIOME_MESA_PLATEAU, BIOME_MESA_PLATEAU_F, BIOME_MESA_PLATEAU_F_M, BIOME_MESA_PLATEAU_M));
    Set<Integer> COLD_BIOMES = new HashSet<>(Arrays.asList(BIOME_FROZEN_OCEAN, BIOME_FROZEN_RIVER, BIOME_ICE_MOUNTAINS, BIOME_ICE_PLAINS, BIOME_TAIGA_HILLS, BIOME_ICE_PLAINS_SPIKES, BIOME_COLD_BEACH, BIOME_COLD_TAIGA, BIOME_COLD_TAIGA_M, BIOME_DEEP_FROZEN_OCEAN));
    Set<Integer> FORESTED_BIOMES = new HashSet<>(Arrays.asList(BIOME_FOREST, BIOME_SWAMPLAND, BIOME_TAIGA, BIOME_FOREST_HILLS, BIOME_TAIGA_HILLS, BIOME_JUNGLE, BIOME_JUNGLE_HILLS, BIOME_JUNGLE_EDGE, BIOME_JUNGLE_EDGE_M, BIOME_JUNGLE_M, BIOME_BIRCH_FOREST, BIOME_BIRCH_FOREST_HILLS, BIOME_BIRCH_FOREST_HILLS_M, BIOME_BIRCH_FOREST_M, BIOME_TAIGA_M, BIOME_COLD_TAIGA, BIOME_COLD_TAIGA_HILLS, BIOME_COLD_TAIGA_M, BIOME_MEGA_SPRUCE_TAIGA, BIOME_MEGA_SPRUCE_TAIGA_HILLS, BIOME_MEGA_TAIGA, BIOME_MEGA_TAIGA_HILLS, BIOME_ROOFED_FOREST, BIOME_ROOFED_FOREST_M, BIOME_SAVANNA, BIOME_SAVANNA_M, BIOME_SAVANNA_PLATEAU, BIOME_SAVANNA_PLATEAU_M));
    Set<Integer> SWAMPY_BIOMES = new HashSet<>(Arrays.asList(BIOME_SWAMPLAND, BIOME_SWAMPLAND_M));
}