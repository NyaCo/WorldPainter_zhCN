package org.pepsoft.minecraft;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

import static org.pepsoft.minecraft.Constants.*;

/**
 * A database of Minecraft block information. Accessed by using the block ID as
 * index in the {@link #BLOCKS} array. Implements the Enumeration pattern,
 * meaning there is only ever one instance of this class for each block ID,
 * allowing use of the equals operator (==) for comparing instances.
 *
 * Created by pepijn on 17-3-15.
 */
public final class Block implements Serializable {
    private Block(int id, int transparency, String name, boolean terrain,
                 boolean insubstantial, boolean veryInsubstantial, boolean resource, boolean tileEntity, boolean treeRelated,
                 boolean vegetation, int blockLight, boolean natural) {
        this.id = id;
        this.transparency = transparency;
        this.name = name;
        this.transparent = (transparency == 0);
        this.translucent = (transparency < 15);
        this.opaque = (transparency == 15);
        this.terrain = terrain;
        if (INSUBSTANTIAL_OVERRIDES.get(id)) {
            this.insubstantial = true;
            this.veryInsubstantial = true;
        } else {
            this.insubstantial = insubstantial;
            this.veryInsubstantial = veryInsubstantial;
        }
        this.solid = ! veryInsubstantial;
        this.resource = resource;
        this.tileEntity = tileEntity;
        this.treeRelated = treeRelated;
        this.vegetation = vegetation;
        this.blockLight = blockLight;
        this.lightSource = (blockLight > 0);
        this.natural = natural;

        // Sanity checks
        if ((id < 0) || (id > 4095)
                || (transparency < 0) || (transparency > 15)
                || (insubstantial && (! veryInsubstantial))
                || (blockLight < 0) || (blockLight > 15)
                || (treeRelated && vegetation)) {
            throw new IllegalArgumentException(Integer.toString(id));
        }

        // Determine the category
        if (id == BLK_AIR) {
            category = CATEGORY_AIR;
        } else if ((id == BLK_WATER) || (id == BLK_STATIONARY_WATER) || (id == BLK_LAVA) || (id == BLK_STATIONARY_LAVA)) {
            category = CATEGORY_FLUID;
        } else if (veryInsubstantial) {
            category = CATEGORY_INSUBSTANTIAL;
        } else if (! natural) {
            category = CATEGORY_MAN_MADE;
        } else if (resource) {
            category = CATEGORY_RESOURCE;
        } else {
            category = CATEGORY_NATURAL_SOLID;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return (name != null) ? name : Integer.toString(id);
    }

    /**
     * The block ID.
     */
    public final int id;

    /**
     * How much light the block blocks.
     */
    public final transient int transparency;

    /**
     * The name of the block.
     */
    public final transient String name;

    /**
     * Whether the block is fully transparent ({@link #transparency} == 0)
     */
    public final transient boolean transparent;

    /**
     * Whether the block is translucent ({@link #transparency} < 15)
     */
    public final transient boolean translucent;

    /**
     * Whether the block is fully opaque ({@link #transparency} == 15)
     */
    public final transient boolean opaque;

    /**
     * Whether the block is part of Minecraft-generated natural ground; more
     * specifically whether the block type should be assigned a terrain type
     * when importing a Minecraft map.
     */
    public final transient boolean terrain;

    /**
     * Whether the block is insubstantial, meaning that they are fully
     * transparent, not man-made, removing them would have no effect on the
     * surrounding blocks and be otherwise inconsequential. In other words
     * mostly decorative blocks that users presumably would not mind being
     * removed.
     */
    public final transient boolean insubstantial;

    /**
     * Whether the block is even more insubstantial. Implies
     * {@link #insubstantial} and adds air, water, lava and leaves.
     */
    public final transient boolean veryInsubstantial;

    /**
     * Whether the block is solid (meaning not {@link #insubstantial} or
     * {@link #veryInsubstantial}).
     */
    public final transient boolean solid;

    /**
     * Whether the block is a mineable ore or resource.
     */
    public final transient boolean resource;

    /**
     * Whether the block is a tile entity.
     */
    public final transient boolean tileEntity;

    /**
     * Whether the block is part of or attached to naturally occurring
     * trees or giant mushrooms. Also includes saplings, but not normal
     * mushrooms.
     */
    public final transient boolean treeRelated;

    /**
     * Whether the block is a plant. Excludes {@link #treeRelated} blocks.
     */
    public final transient boolean vegetation;

    /**
     * The amount of blocklight emitted by this block.
     */
    public final transient int blockLight;

    /**
     * Whether the block is a source of blocklight ({@link #blockLight} > 0).
     */
    public final transient boolean lightSource;

    /**
     * Whether the block can occur as part of a pristine Minecraft-generated
     * landscape, <em>excluding</em> artificial structures such as abandoned
     * mineshafts, villages, temples, strongholds, etc.
     */
    public final transient boolean natural;

    /**
     * Type of block encoded in a single category
     */
    public final transient int category;

    private Object readResolve() throws ObjectStreamException {
        return BLOCKS[id];
    }

    public static final Block[] BLOCKS = new Block[4096];

    private static final BitSet INSUBSTANTIAL_OVERRIDES = new BitSet();

    static {
        String insubStr = System.getProperty("org.pepsoft.worldpainter.insubstantialBlocks");
        if (insubStr != null) {
            Arrays.stream(insubStr.split("[, ]+")).forEach(s -> INSUBSTANTIAL_OVERRIDES.set(Integer.parseInt(s)));
        }
    }

    // Tr == Transparency, meaning in this case how much light is *blocked* by the block, 0 being fully transparent and
    // 15 being fully opaque
    static {
        System.arraycopy(new Block[] {
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(  0,  0,                            "空气", false, false,  true, false, false, false, false,  0,  true),
                new Block(  1, 15,                            "石头",  true, false, false, false, false, false, false,  0,  true),
                new Block(  2, 15,                          "草方块",  true, false, false, false, false, false, false,  0,  true),
                new Block(  3, 15,                            "泥土",  true, false, false, false, false, false, false,  0,  true),
                new Block(  4, 15,                            "圆石", false, false, false, false, false, false, false,  0, false),
                new Block(  5, 15,                            "木板", false, false, false, false, false, false, false,  0, false),
                new Block(  6,  0,                            "树苗", false,  true,  true, false, false,  true, false,  0, false),
                new Block(  7, 15,                            "基岩",  true, false, false, false, false, false, false,  0,  true),
                new Block(  8,  3,                              "水", false, false,  true, false, false, false, false,  0,  true),
                new Block(  9,  3,                          "静态水", false, false,  true, false, false, false, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 10,  0,                            "熔岩", false, false,  true, false, false, false, false, 15,  true),
                new Block( 11,  0,                        "静态熔岩", false, false,  true, false, false, false, false, 15,  true),
                new Block( 12, 15,                            "沙子",  true, false, false, false, false, false, false,  0,  true),
                new Block( 13, 15,                            "沙砾",  true, false, false, false, false, false, false,  0,  true),
                new Block( 14, 15,                          "金矿石",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 15, 15,                          "铁矿石",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 16, 15,                          "煤矿石",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 17, 15,                            "木头", false, false, false, false, false,  true, false,  0,  true),
                new Block( 18,  1,                            "树叶", false, false,  true, false, false,  true, false,  0,  true),
                new Block( 19, 15,                            "海绵", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 20,  0,                            "玻璃", false, false, false, false, false, false, false,  0, false),
                new Block( 21, 15,                      "青金石矿石",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 22, 15,                        "青金石块", false, false, false, false, false, false, false,  0, false),
                new Block( 23, 15,                          "发射器", false, false, false, false,  true, false, false,  0, false),
                new Block( 24, 15,                            "砂岩",  true, false, false, false, false, false, false,  0,  true),
                new Block( 25, 15,                          "音符盒", false, false, false, false,  true, false, false,  0, false),
                new Block( 26,  0,                              "床", false, false, false, false, false, false, false,  0, false),
                new Block( 27,  0,                        "充能铁轨", false, false, false, false, false, false, false,  0, false),
                new Block( 28,  0,                        "探测铁轨", false, false, false, false, false, false, false,  0, false),
                new Block( 29, 15,                        "粘性活塞", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 30,  0,                          "蜘蛛网", false,  true,  true, false, false, false, false,  0, false),
                new Block( 31,  0,                              "草", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 32,  0,                      "枯萎的灌木", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 33,  0,                            "活塞", false, false, false, false, false, false, false,  0, false),
                new Block( 34,  0,                          "活塞臂", false, false, false, false,  true, false, false,  0, false),
                new Block( 35, 15,                            "羊毛", false, false, false, false, false, false, false,  0, false),
                new Block( 36,  0,                    "移动的活塞臂", false, false, false, false, false, false, false,  0, false),
                new Block( 37,  0,                          "蒲公英", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 38,  0,                          "虞美人", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 39,  0,                        "棕色蘑菇", false,  true,  true, false, false, false,  true,  1,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 40,  0,                        "红色蘑菇", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 41, 15,                            "金块", false, false, false, false, false, false, false,  0, false),
                new Block( 42, 15,                            "铁块", false, false, false, false, false, false, false,  0, false),
                new Block( 43, 15,                        "双石台阶", false, false, false, false, false, false, false,  0, false),
                new Block( 44, 15,                          "石台阶", false, false, false, false, false, false, false,  0, false),
                new Block( 45, 15,                            "砖块", false, false, false, false, false, false, false,  0, false),
                new Block( 46, 15,                             "TNT", false, false, false, false, false, false, false,  0, false),
                new Block( 47, 15,                            "书架", false, false, false, false, false, false, false,  0, false),
                new Block( 48, 15,                            "苔石", false, false, false, false, false, false, false,  0, false),
                new Block( 49, 15,                          "黑曜石",  true, false, false, false, false, false, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 50,  0,                            "火把", false, false, false, false, false, false, false, 14, false),
                new Block( 51,  0,                              "火", false,  true,  true, false, false, false, false, 15,  true),
                new Block( 52, 15,                          "刷怪箱", false, false, false, false,  true, false, false,  0, false),
                new Block( 53, 15,                        "橡木楼梯", false, false, false, false, false, false, false,  0, false),
                new Block( 54,  0,                            "箱子", false, false, false, false,  true, false, false,  0, false),
                new Block( 55,  0,                          "红石线", false, false, false, false, false, false, false,  0, false),
                new Block( 56, 15,                        "钻石矿石",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 57, 15,                          "钻石块", false, false, false, false, false, false, false,  0, false),
                new Block( 58, 15,                          "工作台", false, false, false, false, false, false, false,  0, false),
                new Block( 59,  0,                            "小麦", false,  true,  true, false, false, false,  true,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 60, 15,                            "耕地",  true, false, false, false, false, false, false,  0, false),
                new Block( 61, 15,                            "熔炉", false, false, false, false,  true, false, false,  0, false),
                new Block( 62, 15,                      "燃烧的熔炉", false, false, false, false,  true, false, false, 13, false),
                new Block( 63,  0,                    "站立的告示牌", false, false, false, false,  true, false, false,  0, false),
                new Block( 64,  0,                          "橡木门", false, false, false, false, false, false, false,  0, false),
                new Block( 65,  0,                            "梯子", false, false, false, false, false, false, false,  0, false),
                new Block( 66,  0,                            "铁轨", false, false, false, false, false, false, false,  0, false),
                new Block( 67, 15,                        "圆石楼梯", false, false, false, false, false, false, false,  0, false),
                new Block( 68,  0,                    "墙上的告示牌", false, false, false, false,  true, false, false,  0, false),
                new Block( 69,  0,                            "拉杆", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 70,  0,                      "石质压力板", false, false, false, false, false, false, false,  0, false),
                new Block( 71,  0,                            "铁门", false, false, false, false, false, false, false,  0, false),
                new Block( 72,  0,                      "木质压力板", false, false, false, false, false, false, false,  0, false),
                new Block( 73, 15,                        "红石矿石",  true, false, false,  true, false, false, false,  0,  true),
                new Block( 74, 15,                  "发光的红石矿石",  true, false, false, false, false, false, false,  9,  true),
                new Block( 75,  0,             "红石火把 （未开启）", false, false, false, false, false, false, false,  0, false),
                new Block( 76,  0,             "红石火把 （已开启）", false, false, false, false, false, false, false,  7, false),
                new Block( 77,  0,                        "石质按钮", false, false, false, false, false, false, false,  0, false),
                new Block( 78,  0,                              "雪", false,  true,  true, false, false, false, false,  0,  true),
                new Block( 79,  3,                              "冰", false, false, false, false, false, false, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 80, 15,                              "雪",  true,  true,  true, false, false, false, false,  0,  true),
                new Block( 81,  0,                          "仙人掌", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 82, 15,                            "粘土",  true, false, false, false, false, false, false,  0,  true),
                new Block( 83,  0,                            "甘蔗", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 84, 15,                          "唱片机", false, false, false, false,  true, false, false,  0, false),
                new Block( 85,  0,                        "橡木栅栏", false, false, false, false, false, false, false,  0, false),
                new Block( 86,  0,                            "南瓜", false,  true,  true, false, false, false,  true,  0,  true),
                new Block( 87, 15,                          "地狱岩",  true, false, false, false, false, false, false,  0,  true),
                new Block( 88, 15,                          "灵魂沙",  true, false, false, false, false, false, false,  0,  true),
                new Block( 89, 15,                            "荧石", false, false, false, false, false, false, false, 15,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block( 90,  0,                  "下界传送门方块", false, false, false, false, false, false, false, 11, false),
                new Block( 91, 15,                          "南瓜灯", false, false, false, false, false, false, false, 15, false),
                new Block( 92,  0,                            "蛋糕", false, false, false, false, false, false, false,  0, false),
                new Block( 93,  0,           "红石中继器 （未开启）", false, false, false, false, false, false, false,  0, false),
                new Block( 94,  0,           "红石中继器 （已开启）", false, false, false, false, false, false, false,  9, false),
                new Block( 95, 15,                        "染色玻璃", false, false, false, false, false, false, false,  0, false),
                new Block( 96,  0,                          "活板门", false, false, false, false, false, false, false,  0, false),
                new Block( 97, 15,                          "怪物蛋",  true, false, false, false, false, false, false,  0,  true),
                new Block( 98, 15,                            "石砖", false, false, false, false, false, false, false,  0, false),
                new Block( 99, 15,                    "棕色巨型蘑菇", false, false, false, false, false,  true, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(100, 15,                    "红色巨型蘑菇", false, false, false, false, false,  true, false,  0,  true),
                new Block(101,  0,                          "铁栏杆", false, false, false, false, false, false, false,  0, false),
                new Block(102,  0,                          "玻璃板", false, false, false, false, false, false, false,  0, false),
                new Block(103, 15,                            "西瓜", false,  true,  true, false, false, false,  true,  0, false),
                new Block(104,  0,                          "南瓜梗", false,  true,  true, false, false, false,  true,  0, false),
                new Block(105,  0,                          "西瓜梗", false,  true,  true, false, false, false,  true,  0, false),
                new Block(106,  0,                            "藤蔓", false,  true,  true, false, false,  true, false,  0,  true),
                new Block(107,  0,                          "栅栏门", false, false, false, false, false, false, false,  0, false),
                new Block(108, 15,                        "砖块楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(109, 15,                        "石砖楼梯", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(110, 15,                            "菌丝",  true, false, false, false, false, false, false,  0,  true),
                new Block(111,  0,                            "睡莲", false,  true,  true, false, false, false,  true,  0,  true),
                new Block(112, 15,                          "地狱砖", false, false, false, false, false, false, false,  0, false),
                new Block(113,  0,                      "地狱砖栅栏", false, false, false, false, false, false, false,  0, false),
                new Block(114, 15,                      "地狱砖楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(115,  0,                          "地狱疣", false,  true,  true, false, false, false,  true,  0,  true),
                new Block(116,  0,                          "附魔台", false, false, false, false,  true, false, false,  0, false),
                new Block(117,  0,                          "酿造台", false, false, false, false,  true, false, false,  1, false),
                new Block(118,  0,                          "炼药锅", false, false, false, false, false, false, false,  0, false),
                new Block(119,  0,                  "末地传送门方块", false, false, false, false,  true, false, false, 15, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(120, 15,                  "末地传送门框架", false, false, false, false, false, false, false,  1, false),
                new Block(121, 15,                          "末地石",  true, false, false, false, false, false, false,  0,  true),
                new Block(122,  0,                            "龙蛋", false, false, false, false, false, false, false,  1, false),
                new Block(123, 15,               "红石灯 （未开启）", false, false, false, false, false, false, false,  0, false),
                new Block(124, 15,               "红石灯 （已开启）", false, false, false, false, false, false, false, 15, false),
                new Block(125, 15,                        "双木台阶", false, false, false, false, false, false, false,  0, false),
                new Block(126, 15,                          "木台阶", false, false, false, false, false, false, false,  0, false),
                new Block(127,  0,                          "可可果", false,  true,  true, false, false,  true, false,  0,  true),
                new Block(128, 15,                        "砂岩楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(129, 15,                      "绿宝石矿石", false, false, false,  true, false, false, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(130,  0,                          "末影箱", false, false, false, false,  true, false, false,  7, false),
                new Block(131,  0,                          "绊线钩", false, false, false, false, false, false, false,  0, false),
                new Block(132,  0,                            "绊线", false, false, false, false, false, false, false,  0, false),
                new Block(133, 15,                        "绿宝石块", false, false, false, false, false, false, false,  0, false),
                new Block(134, 15,                        "云杉楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(135, 15,                      "白桦木楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(136, 15,                        "丛林楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(137, 15,                        "命令方块", false, false, false, false,  true, false, false,  0, false),
                new Block(138, 15,                            "信标", false, false, false, false,  true, false, false, 15, false),
                new Block(139,  0,                          "圆石墙", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(140,  0,                            "花盆", false, false, false, false,  true, false, false,  0, false),
                new Block(141,  0,                          "胡萝卜", false,  true,  true, false, false, false,  true,  0, false),
                new Block(142,  0,                          "马铃薯", false,  true,  true, false, false, false,  true,  0, false),
                new Block(143,  0,                        "木质按钮", false, false, false, false, false, false, false,  0, false),
                new Block(144,  0,                        "生物头颅", false, false, false, false,  true, false, false,  0, false),
                new Block(145, 15,                            "铁砧", false, false, false, false, false, false, false,  0, false),
                new Block(146,  0,                          "陷阱箱", false, false, false, false,  true, false, false,  0, false),
                new Block(147,  0,             "测重压力板 （轻质）", false, false, false, false, false, false, false,  0, false),
                new Block(148,  0,             "测重压力板 （重质）", false, false, false, false, false, false, false,  0, false),
                new Block(149,  0,                      "红石比较器", false, false, false, false,  true, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(150,  0,           "红石比较器 （已弃用）", false, false, false, false,  true, false, false,  0, false),
                new Block(151,  0,                      "阳光传感器", false, false, false, false,  true, false, false,  0, false),
                new Block(152, 15,                          "红石块", false, false, false, false, false, false, false,  0, false),
                new Block(153, 15,                    "下界石英矿石",  true, false, false,  true, false, false, false,  0,  true),
                new Block(154,  0,                            "漏斗", false, false, false, false,  true, false, false,  0, false),
                new Block(155, 15,                          "石英块", false, false, false, false, false, false, false,  0, false),
                new Block(156, 15,                        "石英楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(157,  0,                        "激活铁轨", false, false, false, false, false, false, false,  0, false),
                new Block(158,  0,                          "投掷器", false, false, false, false,  true, false, false,  0, false),
                new Block(159, 15,                        "染色陶瓦",  true, false, false, false, false, false, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(160,  0,                      "染色玻璃板", false, false, false, false, false, false, false,  0, false),
                new Block(161,  1,        "树叶 （金合欢/深色橡木）", false, false,  true, false, false,  true, false,  0,  true),
                new Block(162, 15,        "木头 （金合欢/深色橡木）", false, false, false, false, false,  true, false,  0,  true),
                new Block(163, 15,                      "金合欢楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(164, 15,                    "深色橡木楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(165,  0,                          "粘液块", false, false, false, false, false, false, false,  0, false),
                new Block(166,  0,                            "屏障", false, false, false, false, false, false, false,  0, false),
                new Block(167,  0,                        "铁活板门", false, false, false, false, false, false, false,  0, false),
                new Block(168, 15,                          "海晶石", false, false, false, false, false, false, false,  0, false),
                new Block(169, 15,                          "海晶灯", false, false, false, false, false, false, false, 15, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(170, 15,                          "干草块", false, false, false, false, false, false, false,  0, false),
                new Block(171,  0,                            "地毯", false, false, false, false, false, false, false,  0, false),
                new Block(172, 15,                            "陶瓦",  true, false, false, false, false, false, false,  0,  true),
                new Block(173, 15,                          "煤炭块", false, false, false, false, false, false, false,  0, false),
                new Block(174, 15,                            "浮冰", false, false, false, false, false, false, false,  0,  true),
                new Block(175,  0,                          "大型花", false,  true,  true, false, false, false,  true,  0,  true),
                new Block(176,  0,                      "站立的旗帜", false, false, false, false,  true, false, false,  0, false),
                new Block(177,  0,                      "墙上的旗帜", false, false, false, false,  true, false, false,  0, false),
                new Block(178,  0,                  "反向阳光传感器", false, false, false, false,  true, false, false,  0, false),
                new Block(179, 15,                          "红砂岩",  true, false, false, false, false, false, false,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(180, 15,                      "红砂岩楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(181, 15,                    "双红砂岩台阶", false, false, false, false, false, false, false,  0, false),
                new Block(182, 15,                      "红砂岩台阶", false, false, false, false, false, false, false,  0, false),
                new Block(183,  0,                    "云杉木栅栏门", false, false, false, false, false, false, false,  0, false),
                new Block(184,  0,                    "白桦木栅栏门", false, false, false, false, false, false, false,  0, false),
                new Block(185,  0,                    "丛林木栅栏门", false, false, false, false, false, false, false,  0, false),
                new Block(186,  0,                  "深色橡木栅栏门", false, false, false, false, false, false, false,  0, false),
                new Block(187,  0,                    "金合欢栅栏门", false, false, false, false, false, false, false,  0, false),
                new Block(188,  0,                      "云杉木栅栏", false, false, false, false, false, false, false,  0, false),
                new Block(189,  0,                      "白桦木栅栏", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(190,  0,                      "丛林木栅栏", false, false, false, false, false, false, false,  0, false),
                new Block(191,  0,                    "深色橡木栅栏", false, false, false, false, false, false, false,  0, false),
                new Block(192,  0,                      "金合欢栅栏", false, false, false, false, false, false, false,  0, false),
                new Block(193,  0,                        "云杉木门", false, false, false, false, false, false, false,  0, false),
                new Block(194,  0,                        "白桦木门", false, false, false, false, false, false, false,  0, false),
                new Block(195,  0,                        "丛林木门", false, false, false, false, false, false, false,  0, false),
                new Block(196,  0,                      "金合欢木门", false, false, false, false, false, false, false,  0, false),
                new Block(197,  0,                      "深色橡木门", false, false, false, false, false, false, false,  0, false),
                new Block(198,  0,                          "末地烛", false, false, false, false, false, false, false, 14, false),
                new Block(199,  0,                        "紫颂植物", false,  true,  true,  false, false, false, true,  0,  true),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(200,  0,                          "紫颂花", false,  true,  true,  false, false, false, true,  0,  true),
                new Block(201, 15,                          "紫珀块", false, false, false, false, false, false, false,  0, false),
                new Block(202, 15,                      "竖纹紫珀块", false, false, false, false, false, false, false,  0, false),
                new Block(203, 15,                      "紫珀块楼梯", false, false, false, false, false, false, false,  0, false),
                new Block(204, 15,                      "双紫珀台阶", false, false, false, false, false, false, false,  0, false),
                new Block(205, 15,                      "紫珀块台阶", false, false, false, false, false, false, false,  0, false),
                new Block(206, 15,                        "末地石砖", false, false, false, false, false, false, false,  0, false),
                new Block(207,  0,                        "甜菜种子", false,  true,  true, false, false, false,  true,  0, false),
                new Block(208, 15,                            "草径",  true, false, false, false, false, false, false,  0, false),
                new Block(209, 15,                  "末地折跃门方块", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(210, 15,                  "循环型命令方块", false, false, false, false,  true, false, false,  0, false),
                new Block(211, 15,                  "连锁型命令方块", false, false, false, false,  true, false, false,  0, false),
                new Block(212,  3,                            "霜冰", false, false, false, false, false, false, false,  0, false),
                new Block(213, 15,                          "熔岩块",  true, false, false, false, false, false, false,  3,  true),
                new Block(214, 15,                        "地狱疣块", false, false, false, false, false, false, false,  0, false),
                new Block(215, 15,                      "红色地狱砖", false, false, false, false, false, false, false,  0, false),
                new Block(216, 15,                            "骨块", false, false, false, false, false, false, false,  0,  true),
                new Block(217,  0,                        "结构空位", false,  true,  true, false, false, false, false,  0, false),
                new Block(218,  0,                          "侦测器", false, false, false, false, false, false, false,  0, false),
                new Block(219,  0,                      "白色潜影盒", false, false, false, false, true,  false, false,  0, false),
//                         ID,  0,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(220,  0,                      "橙色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(221,  0,                    "品红色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(222,  0,                    "淡蓝色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(223,  0,                      "黄色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(224,  0,                    "黄绿色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(225,  0,                    "粉红色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(226,  0,                      "灰色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(227,  0,                    "淡灰色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(228,  0,                      "青色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(229,  0,                      "紫色潜影盒", false, false, false, false, true,  false, false,  0, false),
//                         ID,  0,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(230,  0,                      "蓝色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(231,  0,                      "棕色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(232,  0,                      "绿色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(233,  0,                      "红色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(234,  0,                      "黑色潜影盒", false, false, false, false, true,  false, false,  0, false),
                new Block(235, 15,                    "白色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(236, 15,                    "橙色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(237, 15,                  "品红色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(238, 15,                  "淡蓝色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(239, 15,                    "黄色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(240, 15,                  "黄绿色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(241, 15,                  "粉红色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(242, 15,                    "灰色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(243, 15,                  "淡灰色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(244, 15,                    "青色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(245, 15,                    "紫色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(246, 15,                    "蓝色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(247, 15,                    "棕色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(248, 15,                    "绿色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(249, 15,                    "红色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
                new Block(250, 15,                    "黑色带釉陶瓦", false, false, false, false, false, false, false,  0, false),
                new Block(251, 15,                          "混凝土", false, false, false, false, false, false, false,  0, false),
                new Block(252, 15,                      "混凝土粉末", false, false, false, false, false, false, false,  0, false),
                new Block(253,  0,                              null, false, false, false, false, false, false, false,  0, false),
                new Block(254,  0,                              null, false, false, false, false, false, false, false,  0, false),
                new Block(255, 15,                        "结构方块", false, false, false, false,  true, false, false,  0, false),
//                         ID, Tr,                      Display Name, Terra, Insub, VryIn, Resou, TileE, TreeR, Veget, Li, Natural
        }, 0, BLOCKS, 0, 256);

        for (int i = 256; i < 4096; i++) {
            BLOCKS[i] = new Block(i, 15, null, false, false, false, false, false, false, false, 0, false);
        }
    }

    public static final String[] BLOCK_TYPE_NAMES = new String[HIGHEST_KNOWN_BLOCK_ID + 1];
    public static final int[] BLOCK_TRANSPARENCY = new int[256];
    public static final int[] LIGHT_SOURCES = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            BLOCK_TYPE_NAMES[i] = BLOCKS[i].name;
            BLOCK_TRANSPARENCY[i] = BLOCKS[i].transparency;
            LIGHT_SOURCES[i] = BLOCKS[i].blockLight;
        }
    }

    public static final int CATEGORY_AIR           = 0;
    public static final int CATEGORY_FLUID         = 1;
    public static final int CATEGORY_INSUBSTANTIAL = 2;
    public static final int CATEGORY_MAN_MADE      = 3;
    public static final int CATEGORY_RESOURCE      = 4;
    public static final int CATEGORY_NATURAL_SOLID = 5;

    private static final long serialVersionUID = 3037884633022467720L;
}