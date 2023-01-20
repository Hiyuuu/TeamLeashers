package Command

import TeamLeashers
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DistanceCommand : CommandExecutor {

    init { Bukkit.getPluginCommand("distance")!!.setExecutor(this) }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        val distance = args.getOrNull(0)?.toDoubleOrNull() ?: return false

        TeamLeashers.leashListener?.locationDistance = distance

        sender.sendMessage("§f[§b§l${TeamLeashers.plugin!!.name}§f] §a§l範囲を ${distance} に設定しました")
        (sender as? Player)?.let { it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F) }

        return true
    }

}