import Command.DistanceCommand
import Command.PairSizeCommand
import Command.StartCommand
import Listener.LeashListener
import org.bukkit.plugin.java.JavaPlugin

class TeamLeashers : JavaPlugin() {
    override fun onEnable() { plugin = this

        leashListener = LeashListener()
        StartCommand()
        DistanceCommand()
        PairSizeCommand()
        logger.info("[${this.name}] プラグインが有効になった")
    }
    override fun onDisable() {
        logger.info("[${this.name}] プラグインが無効になった")
    }
    companion object {
        var plugin: JavaPlugin? = null
            private set
        var leashListener : LeashListener? = null
    }
}