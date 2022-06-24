package com.zlink.ui.system;

import com.zlink.ui.ResponseData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemInfoController {
    @Value("${system.name:后台管理系统}")
    private String name;
    @Value("${system.version:1.0}")
    private String version;

    @RequestMapping("/system")
    public ResponseData sysInfo() {
        SystemInfo info = new SystemInfo();
        info.setName(name);
        info.setVersion(version);
        return ResponseData.success().setData(info);
    }
}
