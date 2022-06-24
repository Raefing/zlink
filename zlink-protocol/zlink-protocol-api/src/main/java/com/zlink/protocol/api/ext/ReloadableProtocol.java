package com.zlink.protocol.api.ext;

import com.zlink.protocol.api.IProtocolConfig;
import com.zlink.protocol.exception.ProtocolException;

public interface ReloadableProtocol {
    void reload(IProtocolConfig newConfig) throws ProtocolException;
}
