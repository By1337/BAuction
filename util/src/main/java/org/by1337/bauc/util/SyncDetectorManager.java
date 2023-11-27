package org.by1337.bauc.util;

import org.by1337.api.util.Version;
import org.by1337.bauction.SyncDetector;
import org.by1337.v1_16_5.bauc.SyncDetectorV165;
import org.by1337.v1_17_1.bauc.SyncDetectorV171;
import org.by1337.v1_18_2.bauc.SyncDetectorV182;
import org.by1337.v1_20_1.bauc.SyncDetectorV201;

public class SyncDetectorManager {
    private final static SyncDetector syncDetector;

    static {
        syncDetector = switch (Version.VERSION) {
            case UNKNOWN, V1_19_4, V1_17, V1_18, V1_18_1, V1_19, V1_19_1, V1_19_2, V1_19_3 ->
                    throw new RuntimeException("unsupported version");
            case V1_16_5 -> new SyncDetectorV165();
            case V1_17_1 -> new SyncDetectorV171();
            case V1_18_2 -> new SyncDetectorV182();
            case V1_20_1 -> new SyncDetectorV201();
        };
    }
    public static boolean isSync(){
        return syncDetector.isSync();
    }
}
