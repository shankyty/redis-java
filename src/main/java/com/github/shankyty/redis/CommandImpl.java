package com.github.shankyty.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandImpl implements Command {
    private CommandType type;
    private List<String> args;

    public CommandImpl(CommandType type, List<String> args) {

        this.type = type;
        this.args = args;
    }


    public List<String> getArgs() {
        return args;
    }

    @Override
    public List<String> getResponse() {
        switch (type) {
            case ping : return args.isEmpty() ? Collections.singletonList("PONG") : args;
            case echo : return args;
            case unknown : return Arrays.asList("ERR unknown command ", args.get(0));
        };
        return Collections.emptyList();
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    @Override
    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "CommandImpl{" +
                "type=" + type +
                ", args=" + args +
                '}';
    }
}
