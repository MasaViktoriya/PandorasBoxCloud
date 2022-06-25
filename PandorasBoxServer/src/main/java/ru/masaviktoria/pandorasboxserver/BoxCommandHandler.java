package ru.masaviktoria.pandorasboxserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.services.FileHandlingService;
import ru.masaviktoria.pandorasboxserver.services.RegAndAuthService;
import ru.masaviktoria.pandorasboxserver.services.NavigationService;

import java.nio.file.Path;

public class BoxCommandHandler extends SimpleChannelInboundHandler<BoxCommand> {

    public Path currentDir;
    public String user;

    public BoxCommandHandler() {
        currentDir = Path.of(ServerConstants.SERVERROOTDIRECTORY);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BoxCommand boxCommand) {
        try {
            ProcessingResult processingResult = new ProcessingResult();
            if (boxCommand instanceof AuthRequest authRequest) {
                processingResult = RegAndAuthService.authRequestHandle(authRequest);
            } else if (boxCommand instanceof RegistrationRequest registrationRequest) {
                processingResult = RegAndAuthService.registrationRequestHandle(registrationRequest);
            } else if (boxCommand instanceof LogoutRequest) {
                processingResult = RegAndAuthService.logoutRequestHandle();
            } else if (boxCommand instanceof FileRequest fileRequest) {
                processingResult = FileHandlingService.fileRequestHandle(fileRequest, currentDir);
            } else if (boxCommand instanceof FileContainer fileContainer) {
                processingResult = FileHandlingService.fileContainerHandle(fileContainer, currentDir, user);
            } else if (boxCommand instanceof PathUpRequest) {
                processingResult = NavigationService.pathUpRequestHandle(currentDir);
            } else if (boxCommand instanceof PathInRequest pathInRequest) {
                processingResult = NavigationService.pathInRequestHandle(pathInRequest, currentDir);
            } else if (boxCommand instanceof  NewDirectoryRequest newDirectoryRequest) {
                processingResult = FileHandlingService.newDirectoryHandle(newDirectoryRequest, currentDir);
            } else if (boxCommand instanceof  RenameRequest renameRequest){
                processingResult = FileHandlingService.renameFileOrDirectory(renameRequest, currentDir);
            } else if (boxCommand instanceof  DeleteRequest deleteRequest){
                processingResult = FileHandlingService.deleteFileOrDirectory(deleteRequest, currentDir);
            }
            updateUserState(processingResult);
            ctx.writeAndFlush(processingResult.getCommand());
            if(processingResult.getCommand() instanceof AuthOK){
                ctx.writeAndFlush(new FileList(processingResult.getNewCurrentDir()));
            } else if (processingResult.getCommand() instanceof  DeleteFailed || processingResult.getCommand() instanceof RenameFailed){
                ctx.writeAndFlush(new FileList(currentDir));
            }
        } catch (Exception e) {
            System.out.println("Runtime exception occurred");
            e.printStackTrace();
        }
    }

    private void updateUserState(ProcessingResult processingResult) {
        if (processingResult.getNewCurrentDir() !=null) {
            currentDir = processingResult.getNewCurrentDir();
        }
        if (processingResult.getUser() != null) {
            user = processingResult.getUser();
        }
    }
}