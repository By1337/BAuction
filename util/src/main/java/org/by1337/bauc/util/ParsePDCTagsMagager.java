package org.by1337.bauc.util;

import org.bukkit.persistence.PersistentDataContainer;
import org.by1337.api.util.Version;
import org.by1337.bauction.ParsePDCTags;
import org.by1337.v1_16_5.bauc.ParsePDCTagsV165;
import org.by1337.v1_17_1.bauc.ParsePDCTagsV171;
import org.by1337.v1_18_2.bauc.ParsePDCTagsV182;
import org.by1337.v1_19_4.bauc.ParsePDCTagsV194;
import org.by1337.v1_20_1.bauc.ParsePDCTagsV201;
import org.by1337.v1_20_2.bauc.ParsePDCTagsV202;
import org.by1337.v1_20_4.bauc.ParsePDCTagsV204;

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
            case V1_20_3 -> new ParsePDCTagsV204(); // test
            case V1_20_4 -> new ParsePDCTagsV204();
            default -> throw new RuntimeException("unsupported version");
        };
    }

}
