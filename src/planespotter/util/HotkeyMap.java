package planespotter.util;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Hotkey;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class HotkeyMap {

    private final Map<Hotkey, Runnable> hotkeys;

    public HotkeyMap() {
        this(null);
    }

    public HotkeyMap(Map<Hotkey, Runnable> hotkeys) {
        this.hotkeys = new ConcurrentHashMap<>();
        if (hotkeys != null) {
            this.hotkeys.putAll(hotkeys);
        }
    }

    public static HotkeyMap read(byte[] bytes) throws IOException {
        if (bytes.length < 5) {
            throw new IOException("byte data too short");
        }
        int len;
        Map<Hotkey, Runnable> map = new HashMap<>();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            len = ois.readInt();
            for (int i = 0; i < len; i++) {
                Hotkey h; Runnable r;
                try {
                    h = (Hotkey) ois.readObject();
                    r = (Runnable) ois.readObject();
                } catch (ClassNotFoundException ignored) {
                    continue;
                }
                if (h != null && r != null) {
                    map.put(h, r);
                }
            }
        }
        return new HotkeyMap(map);
    }

    public static HotkeyMap read(@NotNull File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return read(fis.readAllBytes());
        }
    }

    public static void write(@NotNull File file, HotkeyMap hmap) throws IOException {
        write(file, hmap.toByteArray());
    }

    public static void write(@NotNull File file, byte[] mapBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(mapBytes);
        }
    }

    public boolean addHotkey(Hotkey hotkey, Runnable action) {
        return hotkeys.putIfAbsent(hotkey, action) == null;
    }

    public boolean removeHotkey(Hotkey hotkey) {
        return hotkeys.remove(hotkey) != null;
    }

    public byte[] toByteArray() throws IOException {
        AtomicInteger errorCount = new AtomicInteger();
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeInt(hotkeys.size());
            hotkeys.forEach((h, r) -> {
                try {
                    oout.writeObject(h);
                    oout.writeObject(r);
                } catch (IOException ioe) {
                    errorCount.getAndIncrement();
                }

            });
            return bout.toByteArray();
        }
    }


}

