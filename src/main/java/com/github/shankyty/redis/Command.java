package com.github.shankyty.redis;

import java.util.List;

public interface Command {

    CommandType getType();

    List<String> getArgs();

    List<String> getResponse();
}
