package org.by1337.bauc.util;

import org.by1337.bauction.nms.v1_20_6.SyncDetectorV206;
import org.by1337.blib.util.Version;
import org.by1337.bauction.api.SyncDetector;
import org.by1337.bauction.nms.v1_16_5.SyncDetectorV165;
import org.by1337.bauction.nms.v1_17_1.SyncDetectorV171;
import org.by1337.bauction.nms.v1_18_2.SyncDetectorV182;
import org.by1337.bauction.nms.v1_19_4.SyncDetectorV194;
import org.by1337.bauction.nms.v1_20_1.SyncDetectorV201;
import org.by1337.bauction.nms.v1_20_2.SyncDetectorV202;
import org.by1337.bauction.nms.v1_20_4.SyncDetectorV204;

public class SyncDetectorManager {
    private final static SyncDetector syncDetector;

    static {
        syncDetector = switch (Version.VERSION) {
            case V1_16_5 -> new SyncDetectorV165();
            case V1_17_1 -> new SyncDetectorV171();
            case V1_18_2 -> new SyncDetectorV182();
            case V1_19_4 -> new SyncDetectorV194();
            case V1_20_1 -> new SyncDetectorV201();
            case V1_20_2 -> new SyncDetectorV202();
            case V1_20_3, V1_20_4 -> new SyncDetectorV204();
            case V1_20_5, V1_20_6 -> new SyncDetectorV206();
            default -> throw new RuntimeException("unsupported version");
        };
    }

    public static boolean isSync() {
        return syncDetector.isSync();
    }
}
