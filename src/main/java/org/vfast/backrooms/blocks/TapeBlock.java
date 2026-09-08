package org.vfast.backrooms.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.OrderedTick;

import javax.swing.text.html.BlockView;
import java.util.Map;
import java.util.function.Function;

public class TapeBlock extends Block {
    public static final MapCodec<TapeBlock> CODEC = createCodec(TapeBlock::new);

    @Override
    public MapCodec<TapeBlock> codec() {
        return CODEC;
    }

    public static final EnumProperty<Direction> FACE = EnumProperty.of("face", Direction.class);

    public static final EnumProperty<Side> NORTH = EnumProperty.of("north", TapeBlock.Side.class);;
    public static final EnumProperty<TapeBlock.Side> EAST  = EnumProperty.of("east", TapeBlock.Side.class);;
    public static final EnumProperty<TapeBlock.Side> SOUTH = EnumProperty.of("south", TapeBlock.Side.class);;
    public static final EnumProperty<TapeBlock.Side> WEST  = EnumProperty.of("west", TapeBlock.Side.class);;

    public static final BooleanProperty FACE_OPPOSITE = BooleanProperty.of("opposite");

    private static final Map<Direction, Direction[]> TANGENTS_BY_FACE;
    private static final EnumProperty<TapeBlock.Side>[] SLOT_PROPS = new EnumProperty[]{ NORTH, EAST, SOUTH, WEST };

    static {
        Map<Direction, Direction[]> m = Maps.newEnumMap(Direction.class);
        m.put(Direction.DOWN,  new Direction[]{ Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST });
        m.put(Direction.UP,    new Direction[]{ Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST });
        m.put(Direction.NORTH, new Direction[]{ Direction.UP,    Direction.EAST, Direction.DOWN,  Direction.WEST });
        m.put(Direction.SOUTH, new Direction[]{ Direction.UP,    Direction.WEST, Direction.DOWN,  Direction.EAST });
        m.put(Direction.EAST,  new Direction[]{ Direction.UP,    Direction.NORTH, Direction.DOWN, Direction.SOUTH });
        m.put(Direction.WEST,  new Direction[]{ Direction.UP,    Direction.SOUTH, Direction.DOWN, Direction.NORTH });
        TANGENTS_BY_FACE = ImmutableMap.copyOf(m);
    }

    private static EnumProperty<TapeBlock.Side> slotProp(int slot) {
        return SLOT_PROPS[slot];
    }

    private static final Map<Direction, VoxelShape> DOT_SHAPES = buildDotShapes();

    private final Function<BlockState, VoxelShape> shapes;

    public TapeBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACE,  Direction.DOWN)
                        .setValue(NORTH, TapeBlock.Side.NONE)
                        .setValue(EAST,  TapeBlock.Side.NONE)
                        .setValue(SOUTH, TapeBlock.Side.NONE)
                        .setValue(WEST,  TapeBlock.Side.NONE)
                        .setValue(FACE_OPPOSITE, false)
        );

        this.shapes = this.getShapeForEachState(this::computeShape);

        this.defaultBlockState()
                .setValue(NORTH, TapeBlock.Side.SIDE)
                .setValue(EAST, TapeBlock.Side.SIDE)
                .setValue(SOUTH, TapeBlock.Side.SIDE)
                .setValue(WEST, TapeBlock.Side.SIDE)
                .setValue(FACE_OPPOSITE, false);
    }

    private static Map<Direction, VoxelShape> buildDotShapes() {
        VoxelShape floorDot = Block.createCuboidShape(3, 0,  3, 13,  1, 13);
        VoxelShape ceilDot  = Block.createCuboidShape(3, 15, 3, 13, 16, 13);
        VoxelShape northDot = Block.createCuboidShape(3,  3, 0, 13, 13,  1);
        VoxelShape southDot = Block.createCuboidShape(3,  3, 15, 13, 13, 16);
        VoxelShape westDot  = Block.createCuboidShape(0,  3, 3,  1, 13, 13);
        VoxelShape eastDot  = Block.createCuboidShape(15, 3, 3, 16, 13, 13);

        return ImmutableMap.<Direction, VoxelShape>builder()
                .put(Direction.DOWN,  floorDot)
                .put(Direction.UP,    ceilDot)
                .put(Direction.NORTH, northDot)
                .put(Direction.SOUTH, southDot)
                .put(Direction.WEST,  westDot)
                .put(Direction.EAST,  eastDot)
                .build();
    }

    private VoxelShape computeShape(BlockState state) {
        Direction face = state.getValue(FACE);
        VoxelShape shape = DOT_SHAPES.get(face);

        boolean isOpposite = state.getValue(FACE_OPPOSITE);
        if (isOpposite) {
            shape = VoxelShapes.union(shape, DOT_SHAPES.get(face.getOpposite()));
        }

        Direction[] tangents = TANGENTS_BY_FACE.get(face);

        for (int slot = 0; slot < 4; slot++) {
            TapeBlock.Side side = state.getValue(slotProp(slot));
            if (side == TapeBlock.Side.NONE) continue;

            Direction armDir = tangents[slot];
            VoxelShape arm = buildArmShape(face, armDir, side == TapeBlock.Side.UP);

            if (arm != null) {
                shape = VoxelShapes.union(shape, arm);
            }

            if (isOpposite) {
                VoxelShape armOpposite = buildArmShape(face.getOpposite(), armDir, false); // always flat cause it's done in `arm`
                if (armOpposite != null) {
                    shape = VoxelShapes.union(shape, armOpposite);
                }
            }
        }

        return shape;
    }

    private static VoxelShape buildArmShape(Direction face, Direction armDir, boolean bendUp) {
        if (armDir == face || armDir == face.getOpposite()) return null;

        final double W = 3.0, E = 13.0;

        double x0, y0, z0, x1, y1, z1;

        switch (face) {
            case DOWN -> {
                y0 = 0; y1 = 1;
                switch (armDir) {
                    case NORTH -> { x0=W; x1=E; z0=0;  z1=8;  }
                    case SOUTH -> { x0=W; x1=E; z0=8;  z1=16; }
                    case WEST  -> { x0=0; x1=8; z0=W;  z1=E;  }
                    default    -> { x0=8; x1=16; z0=W; z1=E;  }
                }
                VoxelShape flat = Block.createCuboidShape(x0, y0, z0, x1, y1, z1);
                if (!bendUp) return flat;
                VoxelShape rise = buildRise(face, armDir);
                return rise == null ? flat : VoxelShapes.union(flat, rise);
            }
            case UP -> {
                y0 = 15; y1 = 16;
                switch (armDir) {
                    case NORTH -> { x0=W; x1=E; z0=0;  z1=8;  }
                    case SOUTH -> { x0=W; x1=E; z0=8;  z1=16; }
                    case WEST  -> { x0=0; x1=8; z0=W;  z1=E;  }
                    default    -> { x0=8; x1=16; z0=W; z1=E;  }
                }
                VoxelShape flat = Block.createCuboidShape(x0, y0, z0, x1, y1, z1);
                if (!bendUp) return flat;
                VoxelShape rise = buildRise(face, armDir);
                return rise == null ? flat : VoxelShapes.union(flat, rise);
            }
            case NORTH -> {
                z0 = 0; z1 = 1;
                switch (armDir) {
                    case WEST  -> { x0=0;  x1=8;  y0=W; y1=E; }
                    case EAST  -> { x0=8;  x1=16; y0=W; y1=E; }
                    case DOWN  -> { x0=W;  x1=E;  y0=0; y1=8; }
                    default    -> { x0=W;  x1=E;  y0=8; y1=16; }
                }
                VoxelShape flat = Block.createCuboidShape(x0, y0, z0, x1, y1, z1);
                if (!bendUp) return flat;
                VoxelShape rise = buildRise(face, armDir);
                return rise == null ? flat :VoxelShapes.union(flat, rise);
            }
            case SOUTH -> {
                z0 = 15; z1 = 16;
                switch (armDir) {
                    case WEST  -> { x0=0;  x1=8;  y0=W; y1=E; }
                    case EAST  -> { x0=8;  x1=16; y0=W; y1=E; }
                    case DOWN  -> { x0=W;  x1=E;  y0=0; y1=8; }
                    default    -> { x0=W;  x1=E;  y0=8; y1=16; }
                }
                VoxelShape flat = Block.createCuboidShape(x0, y0, z0, x1, y1, z1);
                if (!bendUp) return flat;
                VoxelShape rise = buildRise(face, armDir);
                return rise == null ? flat :VoxelShapes.union(flat, rise);
            }
            case WEST -> {
                x0 = 0; x1 = 1;
                switch (armDir) {
                    case NORTH -> { z0=0;  z1=8;  y0=W; y1=E; }
                    case SOUTH -> { z0=8;  z1=16; y0=W; y1=E; }
                    case DOWN  -> { z0=W;  z1=E;  y0=0; y1=8; }
                    default    -> { z0=W;  z1=E;  y0=8; y1=16; }
                }
                VoxelShape flat = Block.createCuboidShape(x0, y0, z0, x1, y1, z1);
                if (!bendUp) return flat;
                VoxelShape rise = buildRise(face, armDir);
                return rise == null ? flat : VoxelShapes.union(flat, rise);
            }
            case EAST -> {
                x0 = 15; x1 = 16;
                switch (armDir) {
                    case NORTH -> { z0=0;  z1=8;  y0=W; y1=E; }
                    case SOUTH -> { z0=8;  z1=16; y0=W; y1=E; }
                    case DOWN  -> { z0=W;  z1=E;  y0=0; y1=8; }
                    default    -> { z0=W;  z1=E;  y0=8; y1=16; }
                }
                VoxelShape flat = Block.createCuboidShape(x0, y0, z0, x1, y1, z1);
                if (!bendUp) return flat;
                VoxelShape rise = buildRise(face, armDir);
                return rise == null ? flat : VoxelShapes.union(flat, rise);
            }
            default -> { return null; }
        }
    }

    private static VoxelShape buildRise(Direction face, Direction armDir) {
        final double W = 3.0, E = 13.0;

        return switch (face) {
            case DOWN, UP -> switch (armDir) {
                case NORTH -> Block.createCuboidShape(W, 0, 0, E, 16, 1);
                case SOUTH -> Block.createCuboidShape(W, 0, 15, E, 16, 16);
                case WEST -> Block.createCuboidShape(0, 0, W,  1, 16,  E);
                case EAST -> Block.createCuboidShape(15, 0, W, 16, 16,  E);
                default -> null;
            };
            case NORTH, SOUTH -> switch (armDir) {
                case WEST -> Block.createCuboidShape(0, W, 0, 1,  E, 16);
                case EAST -> Block.createCuboidShape(15, W, 0, 16,  E, 16);
                case DOWN -> Block.createCuboidShape(W, 0, 0, E, 1, 16);
                case UP -> Block.createCuboidShape(W, 15, 0, E, 16, 16);
                default -> null;
            };
            case WEST, EAST -> switch (armDir) {
                case NORTH -> Block.createCuboidShape(0, W,  0,  16,  E,  1);
                case SOUTH -> Block.createCuboidShape(0, W, 15,  16,  E, 16);
                case DOWN -> Block.createCuboidShape(0,  0, W,  16,  1,  E);
                case UP -> Block.createCuboidShape(0, 15, W,  16, 16,  E);
                default -> null;
            };
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        return this.shapes.apply(state);
    }

    @Override
    public BlockState getStateForPlacement(ItemPlacementContext context) {
        Direction clickedFace = context.getClickedFace();
        Direction face = clickedFace.getOpposite();
        BlockState base = this.defaultBlockState().setValue(FACE, face);
        return this.getConnectionState(context.getLevel(), base, context.getClickedPos());
    }

    @Override
    protected boolean canSurvive(BlockState state, WorldView level, BlockPos pos) {
        Direction face = state.getValue(FACE);
        BlockPos supportPos = pos.relative(face);
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, face.getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, WorldView level, OrderedTick ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, Random random) {
        Direction face = state.getValue(FACE);

        if (directionToNeighbour == face) {
            return state.canPlaceAt(level, pos) ? this.getConnectionState(level, state, pos) : Blocks.AIR.defaultBlockState();
        }

        return this.getConnectionState(level, state, pos);
    }

    private BlockState getConnectionState(BlockView level, BlockState state, BlockPos pos) {
        Direction face = state.getValue(FACE);
        boolean wasDot = isDot(state);

        state = this.getTapeConnections(level, this.defaultBlockState().setValue(FACE, face), pos);

        if (wasDot && isDot(state)) return state;

        boolean s0 = state.getValue(slotProp(0)).isConnected();
        boolean s1 = state.getValue(slotProp(1)).isConnected();
        boolean s2 = state.getValue(slotProp(2)).isConnected();
        boolean s3 = state.getValue(slotProp(3)).isConnected();

        boolean axis02Empty = !s0 && !s2;
        boolean axis13Empty = !s1 && !s3;

        if (!s0 && axis13Empty) state = state.setValue(slotProp(0), TapeBlock.Side.SIDE);
        if (!s2 && axis13Empty) state = state.setValue(slotProp(2), TapeBlock.Side.SIDE);
        if (!s1 && axis02Empty) state = state.setValue(slotProp(1), TapeBlock.Side.SIDE);
        if (!s3 && axis02Empty) state = state.setValue(slotProp(3), TapeBlock.Side.SIDE);

        return state;
    }

    private BlockState getTapeConnections(BlockView level, BlockState state, BlockPos pos) {
        Direction face = state.getValue(FACE);
        Direction[] tangents = TANGENTS_BY_FACE.get(face);

        for (int slot = 0; slot < 4; slot++) {
            Direction tangent = tangents[slot];
            TapeBlock.Side connection = getConnectingSide(level, pos, tangent, face);
            state = state.setValue(slotProp(slot), connection);
        }
        return state;
    }

    private TapeBlock.Side getConnectingSide(BlockView level, BlockPos pos, Direction dir, Direction face) {
        BlockPos neighbourPos = pos.relative(dir);
        BlockState neighbourState = level.getBlockState(neighbourPos);

        if (isTapeOnFace(neighbourState, face)) {
            return TapeBlock.Side.SIDE;
        }

        BlockPos cornerPos = neighbourPos.relative(face);
        BlockState cornerState = level.getBlockState(cornerPos);

        if (isTapeOnFace(cornerState, dir)) {
            return TapeBlock.Side.UP;
        }

        return TapeBlock.Side.NONE;
    }

    private static boolean isTapeOnFace(BlockState state, Direction face) {
        return state.getBlock() instanceof TapeBlock && state.getValue(FACE) == face;
    }

    private static boolean isDot(BlockState state) {
        return !state.getValue(NORTH).isConnected()
                && !state.getValue(SOUTH).isConnected()
                && !state.getValue(EAST).isConnected()
                && !state.getValue(WEST).isConnected();
    }

    private static boolean isCross(BlockState state) {
        return state.getValue(NORTH).isConnected()
                && state.getValue(SOUTH).isConnected()
                && state.getValue(EAST).isConnected()
                && state.getValue(WEST).isConnected();
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        Direction newFace = rotation.rotate(state.getValue(FACE));
        state = state.setValue(FACE, newFace);

        return switch (rotation) {
            case CLOCKWISE_180 -> state
                    .setValue(NORTH, state.getValue(SOUTH))
                    .setValue(EAST,  state.getValue(WEST))
                    .setValue(SOUTH, state.getValue(NORTH))
                    .setValue(WEST,  state.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> state
                    .setValue(NORTH, state.getValue(EAST))
                    .setValue(EAST,  state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(WEST))
                    .setValue(WEST,  state.getValue(NORTH));
            case CLOCKWISE_90 -> state
                    .setValue(NORTH, state.getValue(WEST))
                    .setValue(EAST,  state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(EAST))
                    .setValue(WEST,  state.getValue(SOUTH));
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state
                    .setValue(NORTH, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state
                    .setValue(EAST,  state.getValue(WEST))
                    .setValue(WEST,  state.getValue(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACE, NORTH, EAST, SOUTH, WEST, FACE_OPPOSITE);
    }

    public enum Side implements StringIdentifiable {
        NONE("none"),
        SIDE("side"),
        UP("up");

        private final String name;

        Side(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return this.name;
        }

        public boolean isConnected() {
            return this != NONE;
        }
    }
}