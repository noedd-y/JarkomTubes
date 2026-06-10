import java.util.*;
import java.io.*;

public class ChatRoom {

    // nama room → list user di room itu
    public static HashMap<String, Vector<DataOutputStream>> rooms = new HashMap<>();

    // user → room name (biar tahu dia ada di room mana)
    public static HashMap<DataOutputStream, String> userRoom = new HashMap<>();

    //buat room baru
    public static void createRoom(String roomName) {
        rooms.putIfAbsent(roomName, new Vector<>());
        System.out.println("Room created: " + roomName);
    }

    //user masuk room
    public static void joinRoom(String roomName, DataOutputStream user) {

        createRoom(roomName);

        rooms.get(roomName).add(user);
        userRoom.put(user, roomName);

        System.out.println("User joined room: " + roomName);
    }

    //broadcast hanya ke 1 room
    public static void broadcast(String message, DataOutputStream sender) {
        try {
            String roomName = userRoom.get(sender);

            if (roomName == null) return;

            Vector<DataOutputStream> users = rooms.get(roomName);

            for (DataOutputStream out : users) {
                out.writeBytes(message + "\n");
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //user keluar room
    public static void leaveRoom(DataOutputStream user) {

        String roomName = userRoom.get(user);

        if (roomName != null) {
            rooms.get(roomName).remove(user);
            userRoom.remove(user);

            System.out.println("User left room: " + roomName);
        }
    }

    // Menampilkan semua room yang tersedia
    public static String listRooms() {

        if (rooms.isEmpty()) {
            return "Belum ada room yang tersedia.";
        }

        StringBuilder result = new StringBuilder();

        result.append("=== DAFTAR ROOM ===\n");

        for (String roomName : rooms.keySet()) {

            int jumlahUser = rooms.get(roomName).size();

            result.append(roomName)
                .append(" (")
                .append(jumlahUser)
                .append(" user)\n");
        }

        return result.toString();
    }
}