package org.by1337.bauction.nms.v1_16_5;

import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.by1337.bauction.api.SyncDetector;

public class SyncDetectorV165 implements SyncDetector {
    public boolean isSync(){
        return Thread.currentThread() == MinecraftServer.getServer().serverThread;
    }
}
