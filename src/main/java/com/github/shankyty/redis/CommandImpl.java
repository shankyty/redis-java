package com.github.shankyty.redis;

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
        return switch (type) {
            case ping -> args.isEmpty() ? List.of("PONG") : args;
            case echo -> args;
            case unknown -> List.of("ERR unknown command ", args.get(0));
        };
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
