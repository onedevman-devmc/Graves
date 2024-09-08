package mc.graves;

import mc.compendium.types.Pair;
import mc.graves.utils.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Graves implements Listener {

    public static class WorldHeightOverflowException extends Exception {
        public WorldHeightOverflowException() { super(); }
        public WorldHeightOverflowException(String message) { super(message); }
        public WorldHeightOverflowException(String message, Throwable cause) { super(message, cause); }
        public WorldHeightOverflowException(Throwable cause) { super(cause); }
    }

    //

    private static final List<UUID> PlayersBeingBuried = new ArrayList<>();
    private static final ItemStack SilkTouchNehteritePickaxe = new ItemStack(Material.NETHERITE_PICKAXE);

    //

    static {
        SilkTouchNehteritePickaxe.addEnchantment(Enchantment.SILK_TOUCH, 1);
    }

    //

    private enum LocationCardinal {
        NORTH(0, BlockFace.NORTH),
        SOUTH(2, BlockFace.SOUTH),
        EAST(1, BlockFace.EAST),
        WEST(3, BlockFace.WEST);

        //

        private static final LocationCardinal[] ROTATION_LIST = {NORTH, EAST, SOUTH, WEST};

        //

        private final int index;
        private final BlockFace blockFace;

        //

        LocationCardinal(int index, BlockFace blockFace) {
            this.index = index;
            this.blockFace = blockFace;
        }

        //

        public BlockFace blockFace() { return blockFace; }

        //

        public LocationCardinal rotate(int rotation) {
            if(rotation < 0) return this.rotate(4 - ((-rotation) % 4));
            else return ROTATION_LIST[(this.index + rotation) % 4];
        }

        public LocationCardinal left(int n) { return this.rotate(-n); }
        public LocationCardinal left() { return this.left(1); }

        public LocationCardinal right(int n) { return this.rotate(n); }
        public LocationCardinal right() { return this.right(1); }

        public LocationCardinal opposit() { return this.left(2); }

        //

        public static LocationCardinal get(Location location) {
            LocationCardinal result = null;

            float yaw = location.getYaw();
            if( (135 < yaw && yaw <= 180) || (-180 < yaw && yaw < -135) ) { result = LocationCardinal.NORTH; }
            else if(-135 <= yaw && yaw <= -45) { result = LocationCardinal.EAST; }
            else if( (-45 < yaw && yaw <= -0) || (0 < yaw && yaw <= 45) ) { result = LocationCardinal.SOUTH; }
            else if(45 < yaw && yaw <= 135) { result = LocationCardinal.WEST; }

            return result;
        }
    }

    //

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if(
            PlayersBeingBuried.contains(player.getUniqueId())
            || !player.hasPermission("mc.graves.bury.self")
        ) return;

        if(Graves.bury(player)) {
            event.getDrops().clear();
            event.setDeathMessage(null);
        }
    }

    //

    public static boolean bury(Player player) {
        return bury(player, PluginMain.instance().getConfig().getBoolean("graves.keep-bedrock"));
    }

    public static boolean bury(Player player, boolean keepBedrock) {
        boolean success = false;

        //

        PlayersBeingBuried.add(player.getUniqueId());

        //

        ItemStack[] storageContent = player.getInventory().getStorageContents();

        ItemStack[] armorContent = player.getInventory().getArmorContents();
        Arrays.reverse(armorContent);

        ItemStack[] extraContent = player.getInventory().getExtraContents();

        ItemStack[] hotbarContent = new ItemStack[9];
        System.arraycopy(storageContent, 0, hotbarContent, 0, hotbarContent.length);

        ItemStack[] innerContent = new ItemStack[9 * 3];
        System.arraycopy(storageContent, hotbarContent.length, innerContent, 0, innerContent.length);

        ItemStack[] firstContainerContent = new ItemStack[9 * 3];
        System.arraycopy(armorContent, 0, firstContainerContent, 0, armorContent.length);
        System.arraycopy(extraContent, 0, firstContainerContent, 9, extraContent.length);
        System.arraycopy(hotbarContent, 0, firstContainerContent, 9 * 2, hotbarContent.length);

        //

        Location[] graveLocations = { player.getLocation(), player.getLocation() };
        graveLocations[0].setY(player.getWorld().getHighestBlockYAt(graveLocations[0]) + 1);

        Location finalGraveLocation = null;
        int i = 0;
        for(; i < graveLocations.length && !success; i++) {
            try {
                Pair<Location, Container[]> pair = place(graveLocations[i], player, keepBedrock);
                finalGraveLocation = pair.first();
                Container[] containers = pair.last();

                try {
                    containers[0].getInventory().setContents(firstContainerContent);
                    containers[1].getInventory().setContents(innerContent);

                    player.getInventory().clear();
                } catch (Exception e) {
                    for (Container container : containers) {
                        container.getInventory().clear();
                    }
                }

                success = true;
            } catch(WorldHeightOverflowException e) {
                PluginMain.instance().getLogger().warning(e.getMessage());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(!success) PluginMain.instance().getLogger().warning("Trying next location...");
            }
        }

        if(success) {
            if(i > 1) PluginMain.instance().getLogger().warning("Location succeed !");

            player.setHealth(0);
        }
        else PluginMain.instance().getLogger().warning("All grave locations failed.");

        //

        PlayersBeingBuried.remove(player.getUniqueId());

        if(success) {
            player.setLastDeathLocation(finalGraveLocation);
        }

        return success;
    }

    private static Pair<Location, Container[]> place(Location location, boolean keepBedrock) throws WorldHeightOverflowException {
        return place(location, null, keepBedrock);
    }

    private static Pair<Location, Container[]> place(Location location, Player player, boolean keepBedrock) throws WorldHeightOverflowException {
        Location center = location.getBlock().getLocation();
        center.setY(Math.max(center.getY(), center.getWorld().getMinHeight()+1));
        LocationCardinal cardinal = LocationCardinal.get(location);

        //

        boolean bedrockBlocksInRadius = keepBedrock;
        boolean worldHeightOverflow = false;

        if(bedrockBlocksInRadius) {
            center.add(0, -1, 0);

            int checkX = cardinal.equals(LocationCardinal.EAST) || cardinal.equals(LocationCardinal.WEST) ? 1 : 0;
            int checkZ = cardinal.equals(LocationCardinal.NORTH) || cardinal.equals(LocationCardinal.SOUTH) ? 1 : 0;

            while(bedrockBlocksInRadius && !(worldHeightOverflow = center.getY() >= center.getWorld().getMaxHeight() - 1)) {
                center.add(0, 1, 0);
                bedrockBlocksInRadius = false;

                for(int i = -1; i <= 1 && !bedrockBlocksInRadius; i++) {
                    for(int j = -1; j <= 0 && !bedrockBlocksInRadius; j++) {
                        Location checkPos = center.clone().add(i * checkX, j, i * checkZ);

                        if(checkPos.getBlock().getType().equals(Material.BEDROCK)) bedrockBlocksInRadius = true;
                    }
                }
            }
        }
        else worldHeightOverflow = center.getY() >= center.getWorld().getMaxHeight() - 1;

        if(worldHeightOverflow) throw new WorldHeightOverflowException("Unable to find a position to place the grave.");

        //

        Location lowerCenter = center.clone().add(0, -1, 0);
        Location barrelPos1 = lowerCenter.clone();
        Location barrelPos2 = lowerCenter.clone();
        Location stairPos = lowerCenter.clone();

        //

        if(cardinal.equals(LocationCardinal.NORTH)) {
            barrelPos2.add(0, 0, -1);
            stairPos.add(0, 0, 1);
        }
        else if(cardinal.equals(LocationCardinal.SOUTH)) {
            barrelPos2.add(0, 0, 1);
            stairPos.add(0, 0, -1);
        }
        else if(cardinal.equals(LocationCardinal.EAST)) {
            barrelPos2.add(1, 0, 0);
            stairPos.add(-1, 0, 0);
        }
        else if(cardinal.equals(LocationCardinal.WEST)) {
            barrelPos2.add(-1, 0, 0);
            stairPos.add(1, 0, 0);
        }

        //

        BlockState barrelState1 = barrelPos1.getBlock().getState();
        BlockState barrelState2 = barrelPos2.getBlock().getState();

        for(BlockState barrelState : Set.of(barrelState1, barrelState2)) {
            Block barrelBlock = barrelState.getBlock();

            if(player == null) barrelBlock.breakNaturally(SilkTouchNehteritePickaxe);
            else if(!player.breakBlock(barrelBlock) && !barrelBlock.getType().equals(Material.AIR)) throw new RuntimeException("Unable to break block.");

            BlockData barrel = Bukkit.createBlockData(Material.BARREL);
            ((Directional) barrel).setFacing(cardinal.left().blockFace());

            barrelState.setBlockData(barrel);

            barrelState.update(true);
        }

        //

        BlockState stairState = stairPos.getBlock().getState();

        Block stairBlock = stairState.getBlock();
        if(player == null) stairBlock.breakNaturally(SilkTouchNehteritePickaxe);
        else if(!player.breakBlock(stairBlock) && !stairBlock.getType().equals(Material.AIR)) throw new RuntimeException("Unable to break block.");

        Stairs stair = (Stairs) Bukkit.createBlockData(Material.MOSSY_COBBLESTONE_STAIRS);
        stair.setHalf(Bisected.Half.TOP);

        stair.setFacing(cardinal.blockFace());
        stairState.setBlockData(stair);

        stairState.update(true);

        //

        Location wallPos = stairPos.clone().add(0, 1, 0);
        BlockState wallState = wallPos.getBlock().getState();

        Block wallBlock = wallState.getBlock();
        if(player == null) wallBlock.breakNaturally(SilkTouchNehteritePickaxe);
        else if(!player.breakBlock(wallBlock) && !wallBlock.getType().equals(Material.AIR)) throw new RuntimeException("Unable to break block.");

        wallState.setType(Material.MOSSY_COBBLESTONE_WALL);

        wallState.update(true);

        //

        Location signPos = center.clone();
        BlockState signState = signPos.getBlock().getState();

        Block signBlock = signState.getBlock();
        if(player == null) signBlock.breakNaturally(SilkTouchNehteritePickaxe);
        else if(!player.breakBlock(signBlock) && !signBlock.getType().equals(Material.AIR)) throw new RuntimeException("Unable to break block.");

        WallSign signData = (WallSign) Bukkit.createBlockData(Material.SPRUCE_WALL_SIGN);
        signData.setFacing(cardinal.blockFace());
        signState.setBlockData(signData);

        signState.update(true);

        Sign sign = (Sign) signState.getBlock().getState();

        sign.setWaxed(true);
        sign.getSide(Side.FRONT).setGlowingText(true);
        sign.getSide(Side.FRONT).setLine(1, "R.I.P");
        if(player != null) sign.getSide(Side.FRONT).setLine(2, player.getName());

        sign.update(true);

        //

        Location playerHeadPos = stairPos.clone().add(0, 2, 0);
        BlockState playerHeadState = playerHeadPos.getBlock().getState();

        if(!keepBedrock || !playerHeadState.getType().equals(Material.BEDROCK)) {
            Block playerHeadBlock = playerHeadState.getBlock();
            if(player == null) playerHeadBlock.breakNaturally(SilkTouchNehteritePickaxe);
            else if(!player.breakBlock(playerHeadBlock) && !playerHeadState.getType().equals(Material.AIR)) throw new RuntimeException("Unable to break block.");

            playerHeadState.setType(Material.PLAYER_HEAD);
            playerHeadState.update(true);

            Skull playerHead = (Skull) playerHeadState.getBlock().getState();

            if (player != null) playerHead.setOwningPlayer(player);
            Rotatable playerHeadData = (Rotatable) playerHead.getBlockData();
            playerHeadData.setRotation(cardinal.opposit().blockFace());
            playerHead.setBlockData(playerHeadData);

            playerHead.update(true);
        }

        return Pair.of(
            center,
            new Container[] {
                (Barrel) barrelState1.getBlock().getState(),
                (Barrel) barrelState2.getBlock().getState()
            }
        );
    }

}
