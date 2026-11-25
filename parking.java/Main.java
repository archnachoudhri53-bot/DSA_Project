import java.io.*;
import java.util.*;

public class Main {

    // ---------- Globals ----------
    static final int CAPACITY = 3;
    static final String FILE_NAME = "parking.txt";
    static String[] slots = new String[CAPACITY];                 // Array: slot number -> vehicle
    static Stack<String> parked = new Stack<>();                  // Stack: parked vehicles (last in, first out)
    static Queue<String> waiting = new LinkedList<>();            // Queue: waiting vehicles (first in, first out)
    static HashMap<String, Integer> slotOf = new HashMap<>();     // HashMap: vehicle -> slot (fast search)
    static Scanner sc = new Scanner(System.in);

    // ---------- Helpers ----------
    static boolean validNumber(String v) {
        // Format like CG04AB1234 (also accepts shorter forms like CG04A1)
        return v.matches("[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{1,4}");
    }

    static int freeSlot() {
        for (int i = 0; i < CAPACITY; i++) if (slots[i] == null) return i;
        return -1;
    }

    static void placeVehicle(String v, int index) {
        slots[index] = v;
        parked.push(v);
        slotOf.put(v, index + 1);
    }

    static String readVehicle() {
        System.out.print("Vehicle: ");
        String v = sc.nextLine().replaceAll("[\\s-]", "").toUpperCase();   // spaces and hyphens ignored
        if (!validNumber(v)) {
            System.out.println("Invalid vehicle number. Use the form CG04AB1234.");
            return null;
        }
        return v;
    }

    // ---------- File Handling ----------
    static void loadFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String s;
            while ((s = br.readLine()) != null) {
                s = s.trim();
                if (s.isEmpty()) continue;
                String[] p = s.split(",");
                if (p[0].equals("P") && p.length == 3) {
                    int slot = Integer.parseInt(p[1].trim());
                    if (slot >= 1 && slot <= CAPACITY && slots[slot - 1] == null)
                        placeVehicle(p[2].trim(), slot - 1);
                } else if (p[0].equals("W") && p.length == 2) {
                    waiting.add(p[1].trim());
                }
            }
        } catch (Exception e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }

    static void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (String v : parked) pw.println("P," + slotOf.get(v) + "," + v);   // bottom to top
            for (String v : waiting) pw.println("W," + v);                         // front to back
        } catch (IOException e) {
            System.out.println("Could not save file: " + e.getMessage());
        }
    }

    // ---------- Features (Methods) ----------
    static void parkVehicle() {
        String v = readVehicle();
        if (v == null) return;
        if (slotOf.containsKey(v) || waiting.contains(v)) {
            System.out.println("Vehicle " + v + " is already in the system.");
            return;
        }
        int i = freeSlot();
        if (i == -1) {
            waiting.add(v);
            System.out.println("Lot full. Vehicle added to waiting queue. Position: " + waiting.size());
        } else {
            placeVehicle(v, i);
            System.out.println("Vehicle " + v + " parked. Slot: " + (i + 1));
        }
    }

    static void exitVehicle() {
        String v = readVehicle();
        if (v == null) return;
        if (!slotOf.containsKey(v)) {
            System.out.println("Vehicle " + v + " not found.");
            return;
        }
        int slot = slotOf.get(v);
        // Pop vehicles above it, remove it, push the others back in the same order
        Stack<String> temp = new Stack<>();
        while (!parked.peek().equals(v)) temp.push(parked.pop());
        parked.pop();
        while (!temp.isEmpty()) parked.push(temp.pop());
        slots[slot - 1] = null;
        slotOf.remove(v);

        if (!waiting.isEmpty()) {
            String next = waiting.poll();
            placeVehicle(next, slot - 1);
            System.out.println(v + " exited. " + next + " parked from queue. Slot: " + slot);
        } else {
            System.out.println(v + " exited. Slot " + slot + " is now free.");
        }
    }

    static void searchVehicle() {
        String v = readVehicle();
        if (v == null) return;
        Integer s = slotOf.get(v);
        if (s != null) System.out.println("Vehicle " + v + " found. Slot: " + s);
        else if (waiting.contains(v)) System.out.println("Vehicle " + v + " is in the waiting queue.");
        else System.out.println("Vehicle " + v + " not found.");
    }

    static void display() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPACITY; i++) {
            if (i > 0) sb.append(" | ");
            sb.append("Slot ").append(i + 1).append(": ").append(slots[i] == null ? "Empty" : slots[i]);
        }
        System.out.println(sb);
        System.out.println("Waiting: " + waiting.size());
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        loadFromFile();
        while (true) {
            System.out.println("\n===== PARKING LOT MANAGER =====");
            System.out.println("1. Park vehicle");
            System.out.println("2. Exit vehicle");
            System.out.println("3. Search vehicle");
            System.out.println("4. Display");
            System.out.println("5. Save and quit");
            System.out.print("Choose option: ");
            if (!sc.hasNextLine()) { saveToFile(); return; }
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": parkVehicle(); break;
                case "2": exitVehicle(); break;
                case "3": searchVehicle(); break;
                case "4": display(); break;
                case "5":
                    saveToFile();
                    System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}


// OUTPUT
//  : ===== PARKING LOT MANAGER =====
// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 1
// Vehicle: CG04A1
// Vehicle CG04A1 parked. Slot: 1

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 1
// Vehicle: CG04B2
// Vehicle CG04B2 parked. Slot: 2

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 1
// Vehicle: CG04C3
// Vehicle CG04C3 parked. Slot: 3

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 1
// Vehicle: CG04D4
// Lot full. Vehicle added to waiting queue. Position: 1

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 4
// Slot 1: CG04A1 | Slot 2: CG04B2 | Slot 3: CG04C3
// Waiting: 1

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 3
// Vehicle: CG04B2
// Vehicle CG04B2 found. Slot: 2

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 2
// Vehicle: CG04C3
// CG04C3 exited. CG04D4 parked from queue. Slot: 3

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 4
// Slot 1: CG04A1 | Slot 2: CG04B2 | Slot 3: CG04D4
// Waiting: 0

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: CG04C3
// Invalid option. Try again.

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 4
// Slot 1: CG04A1 | Slot 2: CG04B2 | Slot 3: CG04D4
// Waiting: 0

// ===== PARKING LOT MANAGER =====
// 1. Park vehicle
// 2. Exit vehicle
// 3. Search vehicle
// 4. Display
// 5. Save and quit
// Choose option: 5
// Data saved to parking.txt. Goodbye!
// PS C:\Users\ARCHANA\Desktop\project.java> 