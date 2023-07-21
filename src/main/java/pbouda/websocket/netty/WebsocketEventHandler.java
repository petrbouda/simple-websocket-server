package pbouda.websocket.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketEventHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketEventHandler.class);

    private final ChannelGroup channelGroup;

    WebsocketEventHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            context.pipeline().remove(HttpRequestHandler.class);
            channelGroup.add(context.channel());
            LOG.info("WS Client added: " + context.channel().remoteAddress());

        } else if (event == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_TIMEOUT) {
            LOG.info("WS HandshakeTimeout occurred: " + context.channel().remoteAddress());

        } else {
            super.userEventTriggered(context, event);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext context) throws Exception {
        if (!context.channel().isWritable()) {
            LOG.error("Channel '{}' became not writable (probably slower consumer)", context.channel().remoteAddress());
        } else {
            LOG.info("Channel '{}' became writable again", context.channel().remoteAddress());
        }

        super.channelWritabilityChanged(context);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame msg) {
        context.fireChannelRead(msg.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        LOG.error("An exception occurred, closing client " + context.channel().remoteAddress(), cause);
        context.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        LOG.info("Closing WS Client: " + context.channel().remoteAddress());
    }
}