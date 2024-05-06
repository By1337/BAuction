package org.by1337.bauction.nms.v1_20_6;

import net.minecraft.server.MinecraftServer;
import org.by1337.bauction.api.SyncDetector;

public class SyncDetectorV206 implements SyncDetector {
    public boolean isSync(){
        return Thread.currentThread() == MinecraftServer.getServer().serverThread;
    }
}
