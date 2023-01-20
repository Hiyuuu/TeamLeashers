package Command

import TeamLeashers
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PairSizeCommand : CommandExecutor {

    init { Bukkit.getPluginCommand("pairsize")!!.setExecutor(this) }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        val size = args.getOrNull(0)?.toIntOrNull() ?: return false

        TeamLeashers.leashListener?.pairPlayerSize = size

        sender.sendMessage("§f[§b§l${TeamLeashers.plugin!!.name}§f] §a§lチーム最大数を ${size}人 に設定しました")
        (sender as? Player)?.let { it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F) }

        return true
    }

}