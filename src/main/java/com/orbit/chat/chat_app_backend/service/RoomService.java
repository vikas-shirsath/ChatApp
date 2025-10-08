package com.orbit.chat.chat_app_backend.service;

import com.orbit.chat.chat_app_backend.entities.Room;
import com.orbit.chat.chat_app_backend.repo.RoomRepositories;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
    private RoomRepositories roomRepositories;

    public RoomService(RoomRepositories roomRepositories) {
        this.roomRepositories = roomRepositories;
    }

    public boolean isRoomExist(String roomId) {
        if(roomRepositories.findByRoomId(roomId) != null) {
            return false;
        } else {
            return true;
        }
    }

    public void createRoom(Room room) {
        roomRepositories.save(room);
    }
}
