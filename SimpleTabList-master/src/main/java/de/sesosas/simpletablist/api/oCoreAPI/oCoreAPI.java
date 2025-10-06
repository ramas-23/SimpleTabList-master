package de.sesosas.simpletablist.api.oCoreAPI;

import java.util.UUID;

public class oCoreAPI {

    public static final oCoreAPI INSTANCE = new oCoreAPI();

    private oCoreAPI() {
        // Constructor privado para singleton
    }

    public Rank getPlayerRank(UUID uuid) {
        // Llama internamente a tu fork de oCore
        // Aquí se debería obtener el rango real y prioridad desde tu core
        return new Rank("Admin", "§c", "", "§c", 100); // Ejemplo
    }

    public static class Rank {
        private final String name;
        private final String prefix;
        private final String suffix;
        private final String color;
        private final int priority; // mayor prioridad = más arriba en la tablist

        public Rank(String name, String prefix, String suffix, String color, int priority) {
            this.name = name;
            this.prefix = prefix;
            this.suffix = suffix;
            this.color = color;
            this.priority = priority;
        }

        public String getName() { return name; }
        public String getPrefix() { return prefix; }
        public String getSuffix() { return suffix; }
        public String getColor() { return color; }
        public int getPriority() { return priority; }
    }
}
