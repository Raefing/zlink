package com.zlink.base.service;

public interface ReloadableService<T> extends ZLinkService {
    void reload(T t);
}
