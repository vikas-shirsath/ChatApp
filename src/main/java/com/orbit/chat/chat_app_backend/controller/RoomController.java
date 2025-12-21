package com.orbit.chat.chat_app_backend.controller;

import com.orbit.chat.chat_app_backend.entities.Room;
import com.orbit.chat.chat_app_backend.repo.RoomRepositories;
import com.orbit.chat.chat_app_backend.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    //create room
    @PostMapping("create")
    public ResponseEntity<?> createRoom(@RequestBody String roomId) {
        if (roomService.isRoomExist(roomId)){
            return ResponseEntity.badRequest().body("Room already exist");
        } else {
            try {
                return roomService.createRoom(roomId);
            } catch (Exception e) {
                //throw new RuntimeException(e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    //get room


    //get messages of room


}
