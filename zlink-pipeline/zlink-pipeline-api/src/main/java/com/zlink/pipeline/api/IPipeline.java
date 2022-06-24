package com.zlink.pipeline.api;

public interface IPipeline {
    IPipeline addLast(IPipelineHandler handler);

    IPipeline addLast(String name, IPipelineHandler handler);

    IPipeline addFirst(IPipelineHandler handler);

    IPipeline addFirst(String name, IPipelineHandler handler);

    IPipeline add(IPipelineHandler handler);

    IPipeline add(String name, IPipelineHandler handler);


}
