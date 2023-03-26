package com.github.shankyty.redis;

import java.util.ArrayList;
import java.util.List;

public class CommandFactory {

    public Command getCommand(String commandKey, List<String> args){
        CommandType type = tryParse(commandKey);
        if(CommandType.unknown.equals(type)){
            args = List.of(commandKey);
        }
        return new CommandImpl(type, args);
    }

    private CommandType tryParse(String type) {
        try{
            return CommandType.valueOf(type);
        } catch (Exception ex){
            return CommandType.unknown;
        }
    }
}
