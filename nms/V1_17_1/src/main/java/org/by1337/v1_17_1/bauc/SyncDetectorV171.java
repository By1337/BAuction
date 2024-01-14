package org.by1337.v1_17_1.bauc;

import net.minecraft.server.MinecraftServer;
import org.by1337.bauction.SyncDetector;

public class SyncDetectorV171 implements SyncDetector {
    public boolean isSync(){
        return Thread.currentThread() == MinecraftServer.getServer().serverThread;
    }
}
