package com.vsdata.melsec.client;

import com.vsdata.melsec.MelsecClientOptions;
import com.vsdata.melsec.codec.ClientFrame3EAsciiMessageDecoder;
import com.vsdata.melsec.codec.ClientFrameEMessageEncoder;
import com.vsdata.melsec.codec.Frame3EAsciiByteDecoder;
import com.vsdata.melsec.message.Function;
import com.vsdata.melsec.message.e.Frame3EAsciiCommand;
import com.vsdata.melsec.message.e.FrameEResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.CompletableFuture;

/**
 * @author liumin
 */
public class Melsec3EAsciiTcpClient extends AbstractTcpClient {

    public Melsec3EAsciiTcpClient(MelsecClientConfig config) {
        super(config);
    }

    @Override
    protected void initChannel(ChannelPipeline pipeline) {
        pipeline.addLast(ClientFrameEMessageEncoder.INSTANCE);
        pipeline.addLast(new Frame3EAsciiByteDecoder());
        pipeline.addLast(new ClientFrame3EAsciiMessageDecoder());
    }

    @Override
    public CompletableFuture<ByteBuf> batchRead(String address, int points) {
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();
        CompletableFuture<FrameEResponse> response = sendRequest(new Frame3EAsciiCommand(
            Function.BATCH_READ,
            address,
            points,
            new MelsecClientOptions(config.getNetworkNo(),
                config.getPcNo(),
                config.getRequestDestinationModuleIoNo(),
                config.getRequestDestinationModuleStationNo())));
        response.whenComplete((r, ex) -> {
            if (r != null) {
                future.complete(r.getData());
            } else {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> batchWrite(String address, int points, ByteBuf data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture<FrameEResponse> response = sendRequest(new Frame3EAsciiCommand(
            Function.BATCH_WRITE,
            address,
            points,
            data,
            new MelsecClientOptions(config.getNetworkNo(),
                config.getPcNo(),
                config.getRequestDestinationModuleIoNo(),
                config.getRequestDestinationModuleStationNo())));
        response.whenComplete((r, ex) -> {
            if (r != null) {
                future.complete(null);
            } else {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }
}
