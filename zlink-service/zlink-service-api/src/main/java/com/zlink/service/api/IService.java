package com.zlink.service.api;

public interface IService<I, O> {
    O doService(I i);
}
