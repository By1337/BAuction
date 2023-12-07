import org.by1337.bauction.Main;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.util.UniqueName;
import org.by1337.bauction.util.UniqueNameGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UniqueNameTest {
    @Test
    public void run(){
        Map<UniqueName, Object> map = new HashMap<>();

        UniqueNameGenerator generator = new UniqueNameGenerator(1337);
        generator.setCurrentPosition(8923);
        UniqueName name = generator.getNextCombination();
        map.put(name, 123);
        UniqueName name1 = new CUniqueName("2jiEiTGSa1ynYwhd04UljMgLKmRZBxDf7CJH");
        UniqueName name2 = UniqueNameGenerator.fromSeedAndPos(1337, 8923);
        Assert.assertEquals(name, name1);
        Assert.assertEquals(name, name2);
        Assert.assertEquals(name1, name2);

        Assert.assertEquals(map.get(name1), 123);
        Assert.assertEquals(map.get(name2), 123);

        Assert.assertTrue(map.containsKey(name1));
        Assert.assertTrue(map.containsKey(name2));
    }
}
