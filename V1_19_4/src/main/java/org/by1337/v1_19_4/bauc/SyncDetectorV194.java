package org.by1337.v1_19_4.bauc;

import net.minecraft.server.MinecraftServer;
import org.by1337.bauction.SyncDetector;

public class SyncDetectorV194 implements SyncDetector {
    public boolean isSync(){
        return Thread.currentThread() == MinecraftServer.getServer().serverThread;
    }
}
