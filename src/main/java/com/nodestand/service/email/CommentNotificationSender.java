package com.nodestand.service.email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.nodestand.nodes.comment.Comment;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class CommentNotificationSender {

    // See https://console.aws.amazon.com/ses/home?region=us-east-1#verified-senders-email:
    public static final String FROM_ADDRESS = "notifications@finerpoints.org";

    private final AmazonSimpleEmailService simpleEmailService;

    public CommentNotificationSender(AmazonSimpleEmailService simpleEmailService) {
        this.simpleEmailService = simpleEmailService;
    }

    public void sendNotification(String emailAddress, Comment parentComment, Comment reply) {


        SendEmailRequest emailRequest = new SendEmailRequest();
        emailRequest.setSource(FROM_ADDRESS);
        emailRequest.setDestination(new Destination(Collections.singletonList(emailAddress)));

        Content subject = new Content()
                .withData("New comment reply on Finer Points!")
                .withCharset(StandardCharsets.UTF_8.name());
        Body body = new Body()
                .withHtml(new Content()
                    .withData(makeHtml(parentComment, reply))
                    .withCharset(StandardCharsets.UTF_8.name()));

        Message message = new Message(subject, body);
        emailRequest.setMessage(message);

        SendEmailResult sendEmailResult = simpleEmailService.sendEmail(emailRequest);
    }

    private String makeHtml(Comment parentComment, Comment reply) {

        String url = "https://www.finerpoints.org"; // TODO: make this point to an exact comment

        return String.format("<p>There's a new comment reply on Finer Points!</p>" +
                        "<p><a href=\"%s\">%s</a></p><p>%s</p>" +
                        "<blockquote>%s</blockquote>", url, url, parentComment.body, reply.body);
    }
}
