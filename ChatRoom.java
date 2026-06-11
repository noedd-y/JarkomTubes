import java.util.*;
import java.io.*;

public class ChatRoom {

    // nama room → list user di room itu
    //public static HashMap<String, Vector<DataOutputStream>> rooms = new HashMap<>();

    // user → room name (biar tahu dia ada di room mana)
    //public static HashMap<DataOutputStream, String> userRoom = new HashMap<>();
    public static HashMap<String, Vector<User>> rooms = new HashMap<>();

    // user → username
    //public static HashMap<DataOutputStream, String> userNames = new HashMap<>();
    public static HashMap<DataOutputStream, User> users = new HashMap<>();

    //buat room baru
    public static void createRoom(String roomName) {
        rooms.putIfAbsent(roomName, new Vector<>());
        System.out.println("Room created: " + roomName);
    }

    //set username
    public static void setUserName(DataOutputStream out, String name) {
        User user = users.get(out);
        user.setUsername(name);
    }
    // public static void setUserName(DataOutputStream user, String name) {
    //     userNames.put(user, name);
    //     System.out.println("User set name: " + name);
    // }

    //user masuk room
    public static void joinRoom(String roomName, DataOutputStream out) {
        User user = users.get(out);

        // keluar dari room lama dulu
        if (user.getCurrentRoom() != null) {
            rooms.get(user.getCurrentRoom()).remove(user);
        }

        createRoom(roomName);

        rooms.get(roomName).add(user);
        user.setCurrentRoom(roomName);
    }
    // public static void joinRoom(String roomName, DataOutputStream user) {

    //     createRoom(roomName);

    //     rooms.get(roomName).add(user);
    //     userRoom.put(user, roomName);

    //     System.out.println("User joined room: " + roomName);
    // }

    //broadcast hanya ke 1 room
    public static void broadcast(String message, DataOutputStream sender) {
        User user = users.get(sender);
        String roomName = user.getCurrentRoom();

        if (roomName == null) return;

        String formatted = "[" + user.getUsername() + "]: " + message;

        for (User u : rooms.get(roomName)) {
            try {
                u.getOut().writeBytes(formatted + "\n");
                u.getOut().flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // public static void broadcast(String message, DataOutputStream sender) {
    //     try {
    //         String roomName = userRoom.get(sender);

    //         if (roomName == null) return;

    //         String userName = userNames.getOrDefault(sender, "Anonymous");
    //         String formattedMsg = "[" + userName + "]: " + message;

    //         Vector<DataOutputStream> users = rooms.get(roomName);

    //         for (DataOutputStream out : users) {
    //             out.writeBytes(formattedMsg + "\n");
    //             out.flush();
    //         }

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    //user keluar room
    public static void leaveRoom(DataOutputStream out) {
        User user = users.get(out);
        if (user == null) return;

        String roomName = user.getCurrentRoom();

        if (roomName != null) {
            rooms.get(roomName).remove(user);
            user.setCurrentRoom(null);
        }

        System.out.println("User left room: " + roomName);
    }
    // public static void leaveRoom(DataOutputStream user) {

    //     String roomName = userRoom.get(user);

    //     if (roomName != null) {
    //         rooms.get(roomName).remove(user);
    //         userRoom.remove(user);
    //         userNames.remove(user);

    //         System.out.println("User left room: " + roomName);
    //     }
    // }

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