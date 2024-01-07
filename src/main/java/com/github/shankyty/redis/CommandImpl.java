package com.github.shankyty.redis;

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
            case ping:
                return args.isEmpty() ? Collections.singletonList("PONG") : args;
            case echo:
                return args;
            case set:
                if(args.size() == 2)
                    InMemoryStore.getInstance().set(args.get(0),
                            args.get(1));
                else if(args.size() == 4){
                    InMemoryStore.getInstance().set(args.get(0),
                            args.get(1),
                            Integer.parseInt(args.get(3)));
                }
                return Collections.singletonList("OK");
            case get:
                return Collections.singletonList(InMemoryStore.getInstance().get(args.get(0)));
            case config:
                if("GET".equalsIgnoreCase(args.get(0))) {
                    return Arrays.asList(args.get(1),
                                ConfigStore.getInstance().get(args.get(1)));
                } else if ("--dir".equalsIgnoreCase(args.get(0))) {
                    ConfigStore.getInstance().set("dir", args.get(1));
                    return Collections.singletonList("OK");
                } else if ("--dbfilename".equalsIgnoreCase(args.get(0))) {
                    ConfigStore.getInstance().set("dbfilename", args.get(1));
                    return Collections.singletonList("OK");
                } else {
                    return Arrays.asList("ERR unknown command ",
                            args.get(0),
                            args.get(1));
                }
            case unknown:
                return Arrays.asList("ERR unknown command ",
                        args.get(0));
        }

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
