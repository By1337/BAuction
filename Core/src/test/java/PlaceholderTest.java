import org.by1337.bauction.Main;
import org.by1337.bauction.placeholder.Placeholder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PlaceholderTest {

    @Test
    public void run() {
        Placeholder placeholder = new Placeholder(null)
                .addSubPlaceholder(new Placeholder("player")
                        .addSubPlaceholder(new Placeholder("deal")
                                .addSubPlaceholder(new Placeholder("sum")
                                        .executor(player -> {
                                            if (player == null) return "player is null!";
                                            return String.valueOf(Main.getStorage().getUser(player.getUniqueId()).getDealSum());
                                        })
                                )
                                .addSubPlaceholder(new Placeholder("count")
                                        .executor(player -> {
                                            if (player == null) return "player is null!";
                                            return String.valueOf(Main.getStorage().getUser(player.getUniqueId()).getDealCount());
                                        })
                                )
                        )
                );

        Assert.assertEquals(placeholder.process(null, "player_deal_sum".split("_")), "player is null!");
        Assert.assertEquals(placeholder.getAllPlaceHolders(), List.of("player_deal_count", "player_deal_sum"));

    }
}
