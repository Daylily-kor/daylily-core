package com.daylily.domain.docker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.daylily.proto.service.DockerServiceGrpc;
import com.daylily.proto.service.DockerServiceGrpc.DockerServiceBlockingStub;

@Configuration
@PropertySource("classpath:daylily.properties")
public class DockerGrpcClientConfig {

    @Value("${daylily.grpc.server.address}")
    private String address;

    @Value("${daylily.grpc.server.port}")
    private int port;

    @Bean("grpcClient")
    DockerServiceBlockingStub grpcClient(GrpcChannelFactory grpcChannelFactory) {
        return DockerServiceGrpc.newBlockingStub(grpcChannelFactory.createChannel("%s:%d".formatted(address, port)));
    }
}
