package org.by1337.bauction;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.by1337.blib.Api;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.command.BukkitCommandRegister;
import org.by1337.blib.command.CommandUtil;
import org.by1337.blib.factory.PacketEntityFactory;
import org.by1337.blib.factory.PacketFactory;
import org.by1337.blib.inventory.FakeTitle;
import org.by1337.blib.inventory.FakeTitleFactory;
import org.by1337.blib.inventory.ItemStackSerialize;
import org.by1337.blib.nbt.ParseCompoundTag;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.blib.network.clientbound.entity.*;
import org.by1337.blib.text.LegacyConvertor;
import org.by1337.blib.util.AsyncCatcher;
import org.by1337.blib.world.BLocation;
import org.by1337.blib.world.entity.BEquipmentSlot;
import org.by1337.blib.world.entity.PacketEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class BLibApi implements Api {
    private static boolean isSet;

    public static void setApi(){
        if (!isSet){
            BLib.setApi(new BLibApi());
            isSet = true;
        }
    }
    @Override
    public @NotNull PacketEntityFactory getPacketEntityFactory() {
        return new PacketEntityFactory() {
            @Override
            public <T extends PacketEntity> T create(BLocation bLocation, Class<T> aClass) {
                return null;
            }
        };
    }

    @Override
    public @NotNull Logger getLogger() {
        return Logger.getLogger("test_logger");
    }

    @Override
    public @NotNull PacketFactory getPacketFactory() {
        return new PacketFactory() {
            @Override
            public PacketAddEntity createPacketAddEntity(PacketEntity packetEntity) {
                return null;
            }

            @Override
            public PacketRemoveEntity createPacketRemoveEntity(PacketEntity packetEntity) {
                return null;
            }

            @Override
            public PacketRemoveEntity createPacketRemoveEntity(int... ints) {
                return null;
            }

            @Override
            public PacketSetEntityData createPacketSetEntityData(PacketEntity packetEntity) {
                return null;
            }

            @Override
            public TeleportEntityPacket createTeleportEntityPacket(PacketEntity packetEntity) {
                return null;
            }

            @Override
            public TeleportEntityPacket createTeleportEntityPacket(int i, double v, double v1, double v2, float v3, float v4, boolean b) {
                return null;
            }

            @Override
            public PacketSetEquipment createPacketSetEquipment(int i, Map<BEquipmentSlot, ItemStack> map) {
                return null;
            }
        };
    }

    @Override
    public @NotNull CommandUtil getCommandUtil() {
        return new CommandUtil() {
            @Override
            public void summon(@NotNull String s, @NotNull BLocation bLocation, @Nullable String s1) {

            }

            @Override
            public void tellRaw(@NotNull String s, @NotNull Player player) {

            }
        };
    }

    @Override
    public @NotNull AsyncCatcher getAsyncCatcher() {
        return s -> {
        };
    }

    @Override
    public @NotNull Message getMessage() {
        return new Message(Logger.getLogger("test_logger"));
    }

    @Override
    public @NotNull ItemStackSerialize getItemStackSerialize() {
        return new ItemStackSerialize() {
            @Override
            public @NotNull String serialize(@NotNull ItemStack itemStack) throws IllegalArgumentException {
                return "empty";
            }

            @Override
            public @NotNull String serializeAndCompress(@NotNull ItemStack itemStack) throws IllegalArgumentException {
                return "empty";
            }

            @Override
            public @NotNull ItemStack deserialize(@NotNull String s) throws IllegalArgumentException {
                return new ItemStack(Material.DIRT);
            }

            @Override
            public @NotNull ItemStack decompressAndDeserialize(@NotNull String data) throws IllegalArgumentException {
                return new ItemStack(Material.DIRT);
            }

            @Override
            public String compress(String raw) throws IOException {
                return "";
            }

            @Override
            public String decompress(String raw) throws IOException {
                return "";
            }
        };
    }

    @Override
    public @NotNull FakeTitleFactory getFakeTitleFactory() {
        return () -> new FakeTitle() {
            @Override
            public void send(Inventory inventory, String newTitle) {
            }

            @Override
            public void send(Inventory inventory, Component newTitle) {
            }
        };
    }

    @Override
    public @NotNull BukkitCommandRegister getBukkitCommandRegister() {
        return new BukkitCommandRegister() {
            @Override
            public void register(BukkitCommand bukkitCommand) {
            }

            @Override
            public void unregister(BukkitCommand bukkitCommand) {
            }
        };
    }

    @Override
    public @NotNull ParseCompoundTag getParseCompoundTag() {
        return new ParseCompoundTag() {
            @Override
            public CompoundTag copy(ItemStack itemStack) {
                return new CompoundTag();
            }

            @Override
            public ItemStack create(CompoundTag compoundTag) {
                return new ItemStack(Material.AIR);
            }
        };
    }

    @Override
    public @NotNull LegacyConvertor getLegacyConvertor() {
        return legacy -> Component.text(legacy);
    }
}
