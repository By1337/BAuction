package org.by1337.bauction.nms.v1_20_2;

import net.minecraft.server.MinecraftServer;
import org.by1337.bauction.api.SyncDetector;

public class SyncDetectorV202 implements SyncDetector {
    public boolean isSync(){
        return Thread.currentThread() == MinecraftServer.getServer().serverThread;
    }
}
