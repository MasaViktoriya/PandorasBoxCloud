package ru.masaviktoria.pandorasboxserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.authentication.AuthService;

import java.nio.file.Files;
import java.nio.file.Path;

public class BoxFileHandler extends SimpleChannelInboundHandler<BoxMessage> {

    private static Path currentDir;

    public BoxFileHandler() {
        currentDir = Path.of(CommandsAndConstants.SERVERROOTDIRECTORY);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BoxMessage boxMessage) throws Exception {
        if (boxMessage instanceof AuthRequest authRequest) {
            if (AuthService.checkLogin(authRequest.getLogin(), authRequest.getPassword())) {
                currentDir = currentDir.resolve(authRequest.getLogin());
                System.out.println("Authentication successful");
                ctx.writeAndFlush(new AuthOK());
                ctx.writeAndFlush(new FileList(currentDir));
            } else {
                System.out.println("Login or password is incorrect");
                ctx.writeAndFlush(new AuthFailed());
            }
        } else if (boxMessage instanceof RegistrationRequest registrationRequest) {
            if (!AuthService.checkExistingUser(registrationRequest.getNewLogin())) {
                AuthService.newUserToDatabase(registrationRequest.getNewLogin(), registrationRequest.getNewPassword());
                System.out.println("New user created in database");
                currentDir = Files.createDirectory(Path.of(CommandsAndConstants.SERVERROOTDIRECTORY).resolve(registrationRequest.getNewLogin()));
                System.out.println("New folder " + registrationRequest.getNewLogin() + " created in " + CommandsAndConstants.SERVERROOTDIRECTORY);
                ctx.writeAndFlush(new AuthOK());
            }else {
                System.out.println("User " + registrationRequest.getNewLogin() + " already exists");
                ctx.writeAndFlush(new AuthFailed());
            }
        } else if (boxMessage instanceof LogoutRequest) {
            currentDir = Path.of(CommandsAndConstants.SERVERROOTDIRECTORY);
            ctx.writeAndFlush(new LogoutOK());
            System.out.println("User logged out");
        } else if (boxMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileContainer(currentDir.resolve(fileRequest.getName())));
            System.out.println("File " + fileRequest.getName() + " sent to user");
        } else if (boxMessage instanceof FileContainer fileContainer) {
            Files.write(currentDir.resolve(fileContainer.getFileName()), fileContainer.getData());
            System.out.println("File " + fileContainer.getFileName() + " saved on server");
            ctx.writeAndFlush(new FileList(currentDir));
        } else if (boxMessage instanceof PathUpRequest) {
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
