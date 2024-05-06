package org.by1337.bauction.event;

import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;

import java.util.List;

public class EventListener {
    private final EventType eventType;
    private final List<String> commands;
    private final Command<Event> command;

    public EventListener(EventType eventType, List<String> commands, Command<Event> command) {
        this.eventType = eventType;
        this.commands = commands;
        this.command = command;
    }

    void onEvent(Event event) throws CommandException {
        if (event.getType() != eventType) return;
        for (String s : commands) {
            command.process(event, s.split(" "));
        }
    }
}
