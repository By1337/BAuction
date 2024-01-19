package org.by1337.bauction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.Api;
import org.by1337.api.BLib;
import org.by1337.api.chat.util.Message;
import org.by1337.api.command.CommandUtil;
import org.by1337.api.factory.PacketEntityFactory;
import org.by1337.api.factory.PacketFactory;
import org.by1337.api.inventory.FakeTitle;
import org.by1337.api.inventory.FakeTitleFactory;
import org.by1337.api.inventory.ItemStackSerialize;
import org.by1337.api.network.clientbound.entity.*;
import org.by1337.api.util.AsyncCatcher;
import org.by1337.api.world.BLocation;
import org.by1337.api.world.entity.BEquipmentSlot;
import org.by1337.api.world.entity.PacketEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            public @NotNull ItemStack deserialize(@NotNull String s) throws IllegalArgumentException {
                return new ItemStack(Material.DIRT);
            }
        };
    }

    @Override
    public @NotNull FakeTitleFactory getFakeTitleFactory() {
        return () -> (FakeTitle) (inventory, s) -> {
        };
    }
}
