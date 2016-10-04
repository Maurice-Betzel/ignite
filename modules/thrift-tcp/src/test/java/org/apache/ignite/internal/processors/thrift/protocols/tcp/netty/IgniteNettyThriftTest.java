/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.thrift.protocols.tcp.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test for {@link GridNettyTcpBootstrap}.
 * <p>
 * Created by mbetzel on 02.10.2016.
 */

public class IgniteNettyThriftTest extends GridCommonAbstractTest {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "localhost");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
    static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    static final EventLoopGroup clientGroup = new NioEventLoopGroup();
    static final EventLoopGroup serverGroup = new NioEventLoopGroup();
    private SslContext sslCtx;

    public IgniteNettyThriftTest() {
        super(true);
    }

    @Before
    public void beforeTest() throws Exception {
        grid().<Integer, String>getOrCreateCache(defaultCacheConfiguration());
        // Configure SSL.
        if (SSL) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }
    }

    @After
    public void afterTest() throws Exception {
        grid().cache(null).clear();
        clientGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        serverGroup.shutdownGracefully();
    }

    /**
     * Sends one message when a connection is open and echoes back any received
     * data to the server.  Simply put, the echo client initiates the ping-pong
     * traffic between the echo client and server by sending the first message to
     * the server.
     */
    public void testNettyTcpEchoServer() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, serverGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 100)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline channelPipeline = ch.pipeline();
                                if (sslCtx != null) {
                                    channelPipeline.addLast(sslCtx.newHandler(ch.alloc()));
                                }
                                channelPipeline.addLast(new EchoServerHandler(latch));
                            }
                        });
                try {
                    // Start the server.
                    ChannelFuture f = serverBootstrap.bind(PORT).sync();
                    // Wait until the server socket is closed.
                    f.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                }
            }
        });
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(clientGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline channelPipeline = ch.pipeline();
                                if (sslCtx != null) {
                                    channelPipeline.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
                                }
                                channelPipeline.addLast(new EchoClientHandler(latch));
                            }
                        });
                // Start the client.
                ChannelFuture channelFuture = null;
                try {
                    channelFuture = bootstrap.connect(HOST, PORT).sync();
                    // Wait until the connection is closed.
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                }
            }
        });
        serverThread.start();
        clientThread.start();
        latch.await(5000, TimeUnit.MILLISECONDS);
        assertEquals(0, latch.getCount());
    }

}