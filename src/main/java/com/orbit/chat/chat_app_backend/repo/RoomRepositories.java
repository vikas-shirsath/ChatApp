package com.orbit.chat.chat_app_backend.repo;

import com.orbit.chat.chat_app_backend.entities.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoomRepositories extends MongoRepository<Room,String> {
    //get room using room id
//    Room findByRoomId(String roomId);

    Optional<Room> findByRoomId(String roomId);

}
