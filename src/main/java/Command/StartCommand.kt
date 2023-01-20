package Command

import TeamLeashers
import Utils.ScoreboardAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

class StartCommand : CommandExecutor {

    init { Bukkit.getPluginCommand("start")!!.setExecutor(this) }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        val mainBoard = ScoreboardAPI.MainBoard ?: return false
        val leashListener = TeamLeashers.leashListener ?: return false

        mainBoard.teams.forEach(Team::unregister)

        val teamColors = ArrayList<ChatColor>().apply { addAll(ChatColor.values().filter { it.isColor }) }

        var createdTeam = mainBoard.registerNewTeam("Team0")
        createdTeam.color = teamColors.shuffled().firstOrNull() ?: return false

        var count = 0
        var teamIndex = 1
        Bukkit.getOnlinePlayers()
            .filter { it.gameMode != GameMode.SPECTATOR }
            .shuffled()
            .forEach { P ->

                if (count >= leashListener.pairPlayerSize) { count = 0
                    createdTeam = mainBoard.registerNewTeam("Team${teamIndex}")
                    val randColor = teamColors.shuffled().firstOrNull() ?: return false
                    createdTeam.color = randColor
                    teamColors.remove(randColor)
                }

                createdTeam.addEntry(P.name)

                count++
                teamIndex++
            }

        sender.sendMessage("§f[§b§l${TeamLeashers.plugin!!.name}§f] §a§lチーム振り分けが完了しました")
        (sender as? Player)?.let { it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F) }

        return true
    }

}