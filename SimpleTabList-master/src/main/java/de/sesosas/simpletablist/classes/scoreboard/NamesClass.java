package de.sesosas.simpletablist.classes.scoreboard;

import de.sesosas.simpletablist.SimpleTabList;
import de.sesosas.simpletablist.api.utils.StringUtil;
import de.sesosas.simpletablist.config.CurrentConfig;
import de.sesosas.simpletablist.api.oCoreAPI.oCoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class NamesClass {

    private static Scoreboard mainScoreboard;
    private static final Map<Player, Scoreboard> originalScoreboards = new HashMap<>();

    public static void initScoreboard() {
        mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public static void updateAllPlayers() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (!CurrentConfig.getBoolean("Names.Sorting.Enable")) {
            players.sort(Comparator.comparing(Player::getName));
        } else {
            String sortType = CurrentConfig.getString("Names.Sorting.Type");
            boolean ascending = CurrentConfig.getBoolean("Names.Sorting.Ascending");

            if ("ocore_priority".equalsIgnoreCase(sortType)) {
                if (ascending) {
                    players.sort(Comparator.comparingInt(NamesClass::getPriority));
                } else {
                    players.sort((a, b) -> getPriority(b) - getPriority(a));
                }
            } else {
                players.sort(Comparator.comparing(Player::getName));
            }
        }

        for (Player p : players) {
            updatePlayerName(p);
        }
    }

    public static void updatePlayerName(Player player) {
        String nameFormat = CurrentConfig.getString("Names.Format.Default");
        String rankName = oCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getName();
        String prefix = oCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getPrefix();
        String suffix = oCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getSuffix();
        String color = oCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getColor();

        String visibleName = nameFormat
                .replace("%player_name%", player.getName())
                .replace("{Rank}", rankName)
                .replace("{Prefix}", prefix)
                .replace("{Suffix}", suffix)
                .replace("{Rank_Color}", color);

        player.setPlayerListName(StringUtil.Convert(visibleName, player));

        if (CurrentConfig.getBoolean("Names.Sorting.Enable")) {
            assignPlayerToTeam(player);
        } else {
            restoreOriginalScoreboard(player);
        }
    }

    private static int getPriority(Player player) {
        return oCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getPriority();
    }

    private static void assignPlayerToTeam(Player player) {
        if (!originalScoreboards.containsKey(player)) originalScoreboards.put(player, player.getScoreboard());

        // Remueve jugador de cualquier equipo anterior
        for (Team team : mainScoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                Bukkit.getScheduler().runTask(SimpleTabList.getPlugin(), () -> team.removeEntry(player.getName()));
            }
        }

        int priority = getPriority(player);

        // Prefijo basado en prioridad de rango (mayor prioridad primero)
        int maxPriority = 50; // Ajusta según tu rango más alto
        int inverted = maxPriority - priority;

        // Nombre de equipo único por jugador para asegurar orden correcto
        String teamName = "STL_" + String.format("%02d", inverted) + "_" + player.getName();

        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Team team = mainScoreboard.getTeam(teamName);
        if (team == null) team = mainScoreboard.registerNewTeam(teamName);

        // Variable final para usar dentro del lambda
        Team finalTeam = team;
        Bukkit.getScheduler().runTask(SimpleTabList.getPlugin(), () -> finalTeam.addEntry(player.getName()));
    }

    private static void restoreOriginalScoreboard(Player player) {
        for (Team team : mainScoreboard.getTeams()) {
            if (team.getName().startsWith("STL") && team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
        originalScoreboards.remove(player);
    }

    public static void resetPlayerNames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListName(player.getName());
            restoreOriginalScoreboard(player);
        }
        cleanupSTLTeams();
    }

    private static void cleanupSTLTeams() {
        for (Team team : new ArrayList<>(mainScoreboard.getTeams())) {
            if (team.getName().startsWith("STL") && team.getEntries().isEmpty()) {
                team.unregister();
            }
        }
    }

    public static void handlePlayerQuit(Player player) {
        restoreOriginalScoreboard(player);
    }
}
