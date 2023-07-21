package pbouda.websocket.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    HttpRequestHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) {
        context.fireChannelRead(request.retain());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) {
        context.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        // Closed connections for HTTP are not interesting
        // Very likely, it's Prometheus scrape mechanism
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();
        context.close();
    }
}