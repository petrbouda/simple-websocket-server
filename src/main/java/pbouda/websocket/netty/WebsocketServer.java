package pbouda.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;

public class WebsocketServer implements AutoCloseable {

    private final ServerBootstrap bootstrap;
    private final ChannelGroup channelGroup;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;

    public WebsocketServer() {
        this(5555);
    }

    public WebsocketServer(int port) {
        this.channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
        this.bossEventLoopGroup = new NioEventLoopGroup(1, new NamedThreadFactory("server-accept"));
        this.workerEventLoopGroup = new NioEventLoopGroup(1, new NamedThreadFactory("server-io"));

        this.bootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(bossEventLoopGroup, workerEventLoopGroup)
                .localAddress(port)
                // .handler(new LoggingHandler(LogLevel.INFO))
//                .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024)
//                .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childHandler(new RouterChannelInitializer(channelGroup));

        /*
         * The maximum queue length for incoming connection indications
         * (a request to connect) is set to the backlog parameter. If
         * a connection indication arrives when the queue is full,
         * the connection is refused.
         */
        // bootstrap.option(ChannelOption.SO_BACKLOG, 100);
        // bootstrap.handler(new LoggingHandler(LogLevel.INFO));
        // Receive and Send Buffer - always be able to fill in an entire entity.
        // bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);
    }

    public Channel start(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        ChannelFuture serverBindFuture = bootstrap.bind();
        // Start consuming from Rabbit after the websocket server is started
        serverBindFuture.addListeners(listeners);
        // Wait for the binding is completed
        serverBindFuture.syncUninterruptibly();

        System.out.println("Netty Server started");
        return serverBindFuture.channel();
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    @Override
    public void close() {
        Future<?> boss = bossEventLoopGroup.shutdownGracefully();
        Future<?> workers = workerEventLoopGroup.shutdownGracefully();
        boss.syncUninterruptibly();
        workers.syncUninterruptibly();
    }
}