package org.by1337.bauc.util;

import org.bukkit.persistence.PersistentDataContainer;
import org.by1337.bauction.nms.v1_20_6.ParsePDCTagsV206;
import org.by1337.blib.util.Version;
import org.by1337.bauction.api.ParsePDCTags;
import org.by1337.bauction.nms.v1_16_5.ParsePDCTagsV165;
import org.by1337.bauction.nms.v1_17_1.ParsePDCTagsV171;
import org.by1337.bauction.nms.v1_18_2.ParsePDCTagsV182;
import org.by1337.bauction.nms.v1_19_4.ParsePDCTagsV194;
import org.by1337.bauction.nms.v1_20_1.ParsePDCTagsV201;
import org.by1337.bauction.nms.v1_20_2.ParsePDCTagsV202;
import org.by1337.bauction.nms.v1_20_4.ParsePDCTagsV204;

import java.util.List;

public class ParsePDCTagsMagager {
    private static final ParsePDCTags parse;

    public static List<String> parseTags(PersistentDataContainer persistentDataContainer) {
        return parse.parseTags(persistentDataContainer);
    }

    static {
        parse = switch (Version.VERSION) {
            case V1_16_5 -> new ParsePDCTagsV165();
            case V1_17_1 -> new ParsePDCTagsV171();
            case V1_18_2 -> new ParsePDCTagsV182();
            case V1_19_4 -> new ParsePDCTagsV194();
            case V1_20_1 -> new ParsePDCTagsV201();
            case V1_20_2 -> new ParsePDCTagsV202();
            case V1_20_3, V1_20_4 -> new ParsePDCTagsV204();
            case V1_20_5, V1_20_6 -> new ParsePDCTagsV206();
            default -> throw new RuntimeException("unsupported version");
        };
    }

}
