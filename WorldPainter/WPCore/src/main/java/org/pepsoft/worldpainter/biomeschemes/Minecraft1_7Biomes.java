package org.pepsoft.worldpainter.biomeschemes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * All Minecraft 1.7 and 1.8 biome IDs, plus names and other info.
 *
 * Created by pepijn on 27-4-15.
 */
public interface Minecraft1_7Biomes {
    int BIOME_OCEAN = 0;
    int BIOME_PLAINS = 1;
    int BIOME_DESERT = 2;
    int BIOME_EXTREME_HILLS = 3;
    int BIOME_FOREST = 4;
    int BIOME_TAIGA = 5;
    int BIOME_SWAMPLAND = 6;
    int BIOME_RIVER = 7;
    int BIOME_HELL = 8;
    int BIOME_SKY = 9;
    int BIOME_FROZEN_OCEAN = 10;
    int BIOME_FROZEN_RIVER = 11;
    int BIOME_ICE_PLAINS = 12;
    int BIOME_ICE_MOUNTAINS = 13;
    int BIOME_MUSHROOM_ISLAND = 14;
    int BIOME_MUSHROOM_ISLAND_SHORE = 15;
    int BIOME_BEACH = 16;
    int BIOME_DESERT_HILLS = 17;
    int BIOME_FOREST_HILLS = 18;
    int BIOME_TAIGA_HILLS = 19;
    int BIOME_EXTREME_HILLS_EDGE = 20;
    int BIOME_JUNGLE = 21;
    int BIOME_JUNGLE_HILLS = 22;
    int BIOME_JUNGLE_EDGE = 23;
    int BIOME_DEEP_OCEAN = 24;
    int BIOME_STONE_BEACH = 25;
    int BIOME_COLD_BEACH = 26;
    int BIOME_BIRCH_FOREST = 27;
    int BIOME_BIRCH_FOREST_HILLS = 28;
    int BIOME_ROOFED_FOREST = 29;
    int BIOME_COLD_TAIGA = 30;
    int BIOME_COLD_TAIGA_HILLS = 31;
    int BIOME_MEGA_TAIGA = 32;
    int BIOME_MEGA_TAIGA_HILLS = 33;
    int BIOME_EXTREME_HILLS_PLUS = 34;
    int BIOME_SAVANNA = 35;
    int BIOME_SAVANNA_PLATEAU = 36;
    int BIOME_MESA = 37;
    int BIOME_MESA_PLATEAU_F = 38;
    int BIOME_MESA_PLATEAU = 39;

    int BIOME_SUNFLOWER_PLAINS = 129;
    int BIOME_DESERT_M = 130;
    int BIOME_EXTREME_HILLS_M = 131;
    int BIOME_FLOWER_FOREST = 132;
    int BIOME_TAIGA_M = 133;
    int BIOME_SWAMPLAND_M = 134;

    int BIOME_ICE_PLAINS_SPIKES = 140;
    int BIOME_ICE_MOUNTAINS_SPIKES = 141;

    int BIOME_JUNGLE_M = 149;

    int BIOME_JUNGLE_EDGE_M = 151;

    int BIOME_BIRCH_FOREST_M = 155;
    int BIOME_BIRCH_FOREST_HILLS_M = 156;
    int BIOME_ROOFED_FOREST_M = 157;
    int BIOME_COLD_TAIGA_M = 158;

    int BIOME_MEGA_SPRUCE_TAIGA = 160;
    int BIOME_MEGA_SPRUCE_TAIGA_HILLS = 161;
    int BIOME_EXTREME_HILLS_PLUS_M = 162;
    int BIOME_SAVANNA_M = 163;
    int BIOME_SAVANNA_PLATEAU_M = 164;
    int BIOME_MESA_BRYCE = 165;
    int BIOME_MESA_PLATEAU_F_M = 166;
    int BIOME_MESA_PLATEAU_M = 167;

    int HIGHEST_BIOME_ID = BIOME_MESA_PLATEAU_M;

    String[] BIOME_NAMES = {
            "海洋",
            "草原",
            "沙漠",
            "峭壁",
            "森林",
            "针叶林",
            "沼泽",
            "河流",
            "地狱（下界）",
            "末路之地",

            "冻洋",
            "冻河",
            "冰原",
            "雪山",
            "蘑菇岛",
            "蘑菇岛岸",
            "沙滩",
            "沙漠山丘",
            "森林山丘",
            "针叶林山丘",

            "悬崖",
            "丛林",
            "丛林山丘",
            "丛林边缘",
            "深海",
            "石滩",
            "寒冷沙滩",
            "桦木森林",
            "桦木森林山丘",
            "黑森林",

            "冷针叶林",
            "冷针叶林山丘",
            "大型针叶林",
            "大型针叶林山丘",
            "峭壁+",
            "热带草原",
            "热带高原",
            "平顶山",
            "平顶山高原 F",
            "平顶山高原",

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
            "向日葵草原",

            "沙漠 M",
            "峭壁 M",
            "繁花森林",
            "针叶林 M",
            "沼泽 M",
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
            "丛林 M",

            null,
            "丛林边缘 M",
            null,
            null,
            null,
            "桦木森林 M",
            "桦木森林山丘 M",
            "黑森林 M",
            "冷针叶林 M",
            null,

            "红木森林",
            "红木山丘",
            "峭壁+ M",
            "热带草原 M",
            "热带高原 M",
            "平顶山（岩柱）",
            "平顶山高原 F M",
            "平顶山高原 M"
        };

    Set<Integer> DRY_BIOMES = new HashSet<>(Arrays.asList(BIOME_DESERT, BIOME_DESERT_HILLS, BIOME_DESERT_M, BIOME_HELL, BIOME_SAVANNA, BIOME_SAVANNA_M, BIOME_MESA, BIOME_MESA_BRYCE, BIOME_SAVANNA_PLATEAU, BIOME_SAVANNA_PLATEAU_M, BIOME_MESA_PLATEAU, BIOME_MESA_PLATEAU_F, BIOME_MESA_PLATEAU_F_M, BIOME_MESA_PLATEAU_M));
    Set<Integer> COLD_BIOMES = new HashSet<>(Arrays.asList(BIOME_FROZEN_OCEAN, BIOME_FROZEN_RIVER, BIOME_ICE_MOUNTAINS, BIOME_ICE_PLAINS, BIOME_TAIGA_HILLS, BIOME_ICE_PLAINS_SPIKES, BIOME_COLD_BEACH, BIOME_COLD_TAIGA, BIOME_COLD_TAIGA_M));
    Set<Integer> FORESTED_BIOMES = new HashSet<>(Arrays.asList(BIOME_FOREST, BIOME_SWAMPLAND, BIOME_TAIGA, BIOME_FOREST_HILLS, BIOME_TAIGA_HILLS, BIOME_JUNGLE, BIOME_JUNGLE_HILLS, BIOME_JUNGLE_EDGE, BIOME_JUNGLE_EDGE_M, BIOME_JUNGLE_M, BIOME_BIRCH_FOREST, BIOME_BIRCH_FOREST_HILLS, BIOME_BIRCH_FOREST_HILLS_M, BIOME_BIRCH_FOREST_M, BIOME_TAIGA_M, BIOME_COLD_TAIGA, BIOME_COLD_TAIGA_HILLS, BIOME_COLD_TAIGA_M, BIOME_MEGA_SPRUCE_TAIGA, BIOME_MEGA_SPRUCE_TAIGA_HILLS, BIOME_MEGA_TAIGA, BIOME_MEGA_TAIGA_HILLS, BIOME_ROOFED_FOREST, BIOME_ROOFED_FOREST_M, BIOME_SAVANNA, BIOME_SAVANNA_M, BIOME_SAVANNA_PLATEAU, BIOME_SAVANNA_PLATEAU_M));
    Set<Integer> SWAMPY_BIOMES = new HashSet<>(Arrays.asList(BIOME_SWAMPLAND, BIOME_SWAMPLAND_M));
}