package org.by1337.bauction.nms.v1_17_1;

import net.minecraft.server.MinecraftServer;
import org.by1337.bauction.api.SyncDetector;

public class SyncDetectorV171 implements SyncDetector {
    public boolean isSync(){
        return Thread.currentThread() == MinecraftServer.getServer().serverThread;
    }
}
