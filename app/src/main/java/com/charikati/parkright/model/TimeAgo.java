package com.charikati.parkright.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeAgo {

    private static final Map<String, Long> times = new LinkedHashMap<>();

    static {
        times.put("year", TimeUnit.DAYS.toMillis(365));
        times.put("month", TimeUnit.DAYS.toMillis(30));
        times.put("week", TimeUnit.DAYS.toMillis(7));
        times.put("day", TimeUnit.DAYS.toMillis(1));
        times.put("hour", TimeUnit.HOURS.toMillis(1));
        times.put("minute", TimeUnit.MINUTES.toMillis(1));
        times.put("second", TimeUnit.SECONDS.toMillis(1));
    }

    public static String toRelative(long duration, int maxLevel) {
        StringBuilder res = new StringBuilder();
        int level = 0;
        for (Map.Entry<String, Long> time : times.entrySet()){
            long timeDelta = duration / time.getValue();
            if (timeDelta > 0){
                res.append(timeDelta)
                        .append(" ")
                        .append(time.getKey())
                        .append(timeDelta > 1 ? "s" : "")
                        .append(", ");
                duration -= time.getValue() * timeDelta;
                level++;
            }
            if (level == maxLevel){
                break;
            }
        }
        if ("".equals(res.toString())) {
            return "0 seconds ago";
        } else {
            res.setLength(res.length() - 2);
            res.append(" ago");
            return res.toString();
        }
    }

//    private static String toRelative(long duration) {
//        return toRelative(duration, times.size());
//    }

//    private static String toRelative(Date start, Date end){
//        assert start.after(end);
//        return toRelative(end.getTime() - start.getTime());
//    }
//
//    private static String toRelative(Date start, Date end, int level){
//        assert start.after(end);
//        return toRelative(end.getTime() - start.getTime(), level);
//    }
}
