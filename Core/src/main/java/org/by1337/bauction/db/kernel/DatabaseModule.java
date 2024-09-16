package org.by1337.bauction.db.kernel;

import org.by1337.bauction.common.db.event.Event;
import org.by1337.bauction.common.db.event.EventPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DatabaseModule {
    Logger LOGGER = LoggerFactory.getLogger("BAuction");

    void preLoad(EventPipeline<Event> pipeline, MemoryDatabase database);

    void postLoad();
}
