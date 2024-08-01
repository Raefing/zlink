package com.zlink.base;

import com.zlink.base.report.ProbePointLeader;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractManager<T> implements IManager<T>, ProbePointLeader {
    private final IBeanFactory beanFactory;
    private int status;

    public AbstractManager(IBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    public <TB> TB getBean(String name, Class<TB> tClass) {
        return beanFactory.getBean(name, tClass);
    }
}
