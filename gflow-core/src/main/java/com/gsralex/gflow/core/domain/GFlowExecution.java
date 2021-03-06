package com.gsralex.gflow.core.domain;

import com.gsralex.gflow.core.dao.helper.annotation.AliasField;
import com.gsralex.gflow.core.dao.helper.annotation.IdField;

/**
 * @author gsralex
 * @date 2018/2/17
 */
public class GFlowExecution {

    @IdField
    private long id;
    @AliasField(name = "trigger_group_id")
    private long triggerGroupId;
    private int type;
    private int interval;
    private String time;
    private String date;
    private int dayOfWeek;
    private int dayOfMonth;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTriggerGroupId() {
        return triggerGroupId;
    }

    public void setTriggerGroupId(long triggerGroupId) {
        this.triggerGroupId = triggerGroupId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
