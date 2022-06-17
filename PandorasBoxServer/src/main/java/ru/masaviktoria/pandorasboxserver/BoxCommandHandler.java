package ru.masaviktoria.pandorasboxserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.services.FileHandlingService;
import ru.masaviktoria.pandorasboxserver.services.RegAndAuthService;
import ru.masaviktoria.pandorasboxserver.services.UIService;

import java.nio.file.Path;

public class BoxCommandHandler extends SimpleChannelInboundHandler<BoxCommand> {

    public Path currentDir;
    public String user;

    public BoxCommandHandler() {
        currentDir = Path.of(CommandsAndConstants.SERVERROOTDIRECTORY);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BoxCommand boxCommand) throws Exception {
        try {
            HandlingResult handlingResult = new HandlingResult();
            if (boxCommand instanceof AuthRequest authRequest) {
                handlingResult = RegAndAuthService.authRequestHandle(authRequest);
            } else if (boxCommand instanceof RegistrationRequest registrationRequest) {
                handlingResult = RegAndAuthService.registrationRequestHandle(registrationRequest);
            } else if (boxCommand instanceof LogoutRequest) {
                handlingResult = RegAndAuthService.logoutRequestHandle();
            } else if (boxCommand instanceof FileRequest fileRequest) {
                handlingResult = FileHandlingService.fileRequestHandle(fileRequest, currentDir);
            } else if (boxCommand instanceof FileContainer fileContainer) {
                handlingResult = FileHandlingService.fileContainerHandle(fileContainer, currentDir, user);
            } else if (boxCommand instanceof PathUpRequest) {
                handlingResult = UIService.pathUpRequestHandle(currentDir);
            } else if (boxCommand instanceof PathInRequest pathInRequest) {
                handlingResult = UIService.pathInRequestHandle(pathInRequest, currentDir);
            }
            updateUserState(handlingResult);
            ctx.writeAndFlush(handlingResult.getCommand());
            if(handlingResult.getCommand() instanceof AuthOK){
                ctx.writeAndFlush(new FileList(handlingResult.getNewCurrentDir()));
            }
        } catch (Exception e) {
            System.out.println("Runtime exception occurred");
            e.printStackTrace();
        }
    }

    private void updateUserState(HandlingResult handlingResult) {
        if (handlingResult.getNewCurrentDir() !=null) {
            currentDir = handlingResult.getNewCurrentDir();
        }
        if (handlingResult.getUser() != null) {
            user = handlingResult.getUser();
        }
    }
}

    // update state fields
            /*for(var field : state.updatedFilds)
            {
                 switch (field.Key){
                     case StateField.CurrenDir : currentDir = field.value; break;
                     case //
                 }
            }*/


