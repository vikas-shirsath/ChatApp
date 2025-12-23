package com.orbit.chat.chat_app_backend.service;

import com.orbit.chat.chat_app_backend.entities.Message;
import com.orbit.chat.chat_app_backend.entities.Room;
import com.orbit.chat.chat_app_backend.payload.MessageRequest;
import com.orbit.chat.chat_app_backend.repo.RoomRepositories;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatService {
    private final RoomRepositories roomRepositories;

    public ChatService(RoomRepositories roomRepositories) {
        this.roomRepositories = roomRepositories;
    }

    public Message sendMessage(String roomId, MessageRequest request) {
        Optional<Room> roomOpt = roomRepositories.findByRoomId(roomId);

        if (roomOpt.isEmpty()) {
            throw new IllegalArgumentException("Room not found");
        }

        Room room = roomOpt.get();

        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(request.getSender());

        room.getMessages().add(message);
        roomRepositories.save(room);

        return message;
    }


    //other way

//    public Message sendMessage(String roomId, MessageRequest request) {
//        return roomRepositories.findByRoomId(roomId)
//                .map(room -> {
//                    Message message = new Message();
//                    message.setContent(request.getContent());
//                    message.setSender(request.getSender());
//
//                    room.getMessages().add(message);
//                    roomRepositories.save(room);
//
//                    return message;
//                })
//                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
//    }

}
