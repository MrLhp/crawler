package com.lhp.crawler.enumns;

import java.util.HashMap;
import java.util.Map;

public enum MoviesStatus {
    自动匹配(0),
    匹配成功(1),
    匹配失败(2),
    未知(-1);

    static final Map<Integer, MoviesStatus> map = new HashMap<Integer, MoviesStatus>();
    static {
        for (final MoviesStatus mtype : MoviesStatus.values()) {
            MoviesStatus.map.put(mtype.getValue(), mtype);
        }
    }

    public static MoviesStatus getEnum(final int value) {
        return MoviesStatus.map.containsKey(value) ? MoviesStatus.map.get(value)
                : MoviesStatus.未知;
    }

    private int value;
    MoviesStatus(int i) {
        this.value=i;
    }

    public int getValue() {
        return this.value;
    }
}
