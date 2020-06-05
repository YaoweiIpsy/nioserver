package com.ipsy.simpleNIOServer.application;

import static com.ipsy.utils.ApiCoreUtils.OBJECT_MAPPER;
import static com.ipsy.utils.ExceptionUtils.requireNoException;

import com.ipsy.simpleNIOServer.application.annotations.Controller;
import com.ipsy.simpleNIOServer.application.annotations.RequestMapping;
import com.ipsy.simpleNIOServer.application.models.Action;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.rxjava3.core.Single;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

@Slf4j
public class Server extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final int PORT = 8080;
  private static final List<Action> actions =
      new Reflections("com.ipsy")
          .getTypesAnnotatedWith(Controller.class)
          .stream()
          .filter(aClass -> !aClass.isInterface() && !Modifier.isAbstract(aClass.getModifiers()))
          .flatMap(aClass -> {
            final Object object = requireNoException(aClass::newInstance);
            return Arrays.stream(aClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .map(method -> new Action(aClass.getDeclaredAnnotation(Controller.class), method,
                    object));
          }).collect(Collectors.toList());

  public static void main(String[] args) throws Exception {
    EventLoopGroup group = new NioEventLoopGroup(1);
    ServerBootstrap b = new ServerBootstrap();
    b.option(ChannelOption.SO_BACKLOG, 1024);
    b.group(group)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<Channel>() {
          @Override
          protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(10 * 1024));
            p.addLast(new Server());
          }
        });
    Channel ch = b.bind(PORT).sync().channel();
    log.info("http://0.0.0.0:{}/ start", PORT);
    ch.closeFuture().sync();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request)
      throws Exception {
    Single<?> single = Single.just("");
    for (Action action : actions) {
      RequestMapping requestMapping = action.getMethod().getAnnotation(RequestMapping.class);
      if (requestMapping.method().equals(request.method().name())) {
        String uri = action.getController().uri() + requestMapping.uri();
        Object[] params = action.mapping(uri, request);
        if (params != null) {
          Object result = action.call(params);
          if (result instanceof Single) {
            single = ((Single<?>) result);
          } else if (result instanceof Future) {
            single = Single.fromFuture((Future<?>) result);
          } else {
            single = Single.just(result);
          }
        }
      }
    }
    single.subscribe(object -> {
      ByteBuf byteBuf = Unpooled.wrappedBuffer(OBJECT_MAPPER.writeValueAsBytes(object));
      FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
          HttpResponseStatus.OK, byteBuf);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
      response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
      if (!HttpUtil.isKeepAlive(request)) {
        channelHandlerContext.write(response).addListener(ChannelFutureListener.CLOSE);
      } else {
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        channelHandlerContext.write(response);
      }
      channelHandlerContext.flush();
    });
  }
}
