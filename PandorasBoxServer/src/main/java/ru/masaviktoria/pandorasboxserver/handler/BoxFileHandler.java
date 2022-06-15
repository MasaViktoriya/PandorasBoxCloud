package ru.masaviktoria.pandorasboxserver.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.masaviktoria.pandorasboxmodel.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class BoxFileHandler extends SimpleChannelInboundHandler<BoxMessage> {

    private static Path currentDir;

    public BoxFileHandler() {
        currentDir = Path.of("server_files");
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new FileList(currentDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BoxMessage boxMessage) throws Exception {
        if (boxMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
        } else if (boxMessage instanceof FileMessage fileMessage) {
            Files.write(currentDir.resolve(fileMessage.getFileName()), fileMessage.getData());
            ctx.writeAndFlush(new FileList(currentDir));
        } else if (boxMessage instanceof PathUpRequest pathUpRequest) {
            currentDir = currentDir.getParent();
            ctx.writeAndFlush(new FileList(currentDir));
        } else if (boxMessage instanceof PathInRequest pathInRequest) {
            if (Files.isDirectory(currentDir.resolve(pathInRequest.getSelectedDirectory()))) {
                currentDir = currentDir.resolve(pathInRequest.getSelectedDirectory());
                ctx.writeAndFlush(new FileList(currentDir));
            }
        }
    }
}
