package com.orbit.chat.chat_app_backend.service;

import com.orbit.chat.chat_app_backend.entities.Room;
import com.orbit.chat.chat_app_backend.repo.RoomRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<?> createRoom(String roomId) {
        try {
            Room room = new Room(roomId);
            roomRepositories.save(room);
            return new ResponseEntity<>(room, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
