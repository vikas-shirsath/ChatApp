package com.orbit.chat.chat_app_backend.controller;

import com.orbit.chat.chat_app_backend.entities.Message;
import com.orbit.chat.chat_app_backend.entities.Room;
import com.orbit.chat.chat_app_backend.payload.MessageRequest;
import com.orbit.chat.chat_app_backend.repo.RoomRepositories;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ChatController {

    RoomRepositories roomRepositories;

    public ChatController(RoomRepositories roomRepositories) {
        this.roomRepositories = roomRepositories;
    }

    @MessageMapping("/sendMessage/{roomId}")
    @SendTo("topic/room/{roomId}")
    public Message sendMessage(
            @DestinationVariable String roomId,
            @RequestBody MessageRequest request
    ) {
        Room room = roomRepositories.findByRoomId(roomId);
        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(request.getSender());

        if (room != null) {
            room.getMessages().add(message);
            roomRepositories.save(room);
        } else {
            throw new RuntimeException("Room Not Found");
        }

        return message;
    }
}