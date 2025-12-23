package com.orbit.chat.chat_app_backend.service;

import com.orbit.chat.chat_app_backend.entities.Message;
import com.orbit.chat.chat_app_backend.entities.Room;
import com.orbit.chat.chat_app_backend.repo.RoomRepositories;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    private final RoomRepositories roomRepositories;

    public RoomService(RoomRepositories roomRepositories) {
        this.roomRepositories = roomRepositories;
    }

    public boolean isRoomExist(String roomId) {
        return roomRepositories.findByRoomId(roomId) != null;
    }

    public boolean createRoom(String roomId) {
        try {
            Room room = new Room(roomId);
            roomRepositories.save(room);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<Room> getRoom(String roomId) {
        return roomRepositories.findByRoomId(roomId);
    }


    public List<Message> getMessages(String roomId, int page, int size) {

        Optional<Room> roomOpt = roomRepositories.findByRoomId(roomId);

        if (roomOpt.isEmpty()) {
            return List.of();
        }

        List<Message> messages = roomOpt.get().getMessages();

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, messages.size());

        if (fromIndex >= messages.size()) {
            return List.of();
        }

        return messages.subList(fromIndex, toIndex);
    }

}
