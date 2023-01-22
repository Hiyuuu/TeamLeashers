package Listener

import TeamLeashers
import Utils.ScoreboardAPI
import Utils.Utils.Utils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Chicken
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Team
import java.util.*

class LeashListener : Listener {

    init { every() ; Bukkit.getPluginManager().registerEvents(this, TeamLeashers.plugin!!) }

    var locationDistance = 0.0
    var pairPlayerSize = 3
    private val LeashEntity = WeakHashMap<Entity, Entity>()

    private fun every() {
        object: BukkitRunnable() { override fun run() {
            leadPlayers()
            killLeadOfWorld()
            killNotConnectedEntity()
        }}.runTaskTimer(TeamLeashers.plugin!!, 0, 1)
    }

    /**
     * エンティティをロケーションに向けてバウンドさせる
     */
    fun leadEntity(entity: Entity, location: Location) {

        val entityLoc = entity.location
        val vectorToNearestPlayer = location.toVector().subtract(entityLoc.toVector()).normalize()
        val vectorMultiply = location.distance(entityLoc) / locationDistance

        if (location.distance(entityLoc) >= locationDistance) {
            if (!entity.isOnGround)
                entity.velocity = entity.velocity.add(vectorToNearestPlayer.multiply(0.04 * vectorMultiply))
            else
                entity.velocity = entity.velocity.add(vectorToNearestPlayer.multiply(entity.velocity.length() * 0.6 * vectorMultiply))
        }
    }

    /**
     * チームに所属しているプレイヤーをリード
     */
    fun leadPlayers() {

        if (locationDistance <= 0.0) return

        // 全チーム取得
        val teams = ScoreboardAPI.MainBoard?.teams ?: return

        teams.forEach { T ->

            // チームエントリーをエンティティへ変換
            val teamEntities = getTeamOnlineEntities(T)

            // エントリーの中心ロケーションを計算
            val center = getEntitiesCenter(teamEntities) ?: return@forEach

            teamEntities.forEach { E ->

                // リードを掛けるエンティティを取得
                val getLeashEntity = LeashEntity.get(E) as? Chicken

                // リードを掛けるエンティティがいない場合、生成する
                if (getLeashEntity != null && !getLeashEntity.isDead) {

                    (E as? Player)?.inventory?.setItemInOffHand(ItemStack(Material.LEAD))
                    getLeashEntity.setLeashHolder(E)

//                    val centerTop = center.world!!.getHighestBlockAt(center).location
//                    val loc = Location(center.world, center.x, centerTop.y, center.z)
//                    if (center.distance(loc) <= 3.0)
//                        getLeashEntity.teleport(loc.add(0.0, 0.5, 0.0))
//                    else
                    getLeashEntity.teleport(center.clone().add(0.0, 0.0, 0.0))

                } else {
                    val entity = (center.world!!.spawnEntity(center, EntityType.CHICKEN) as Chicken).apply {
                        this.isSilent = true
                        this.isInvulnerable = true
                        this.isInvisible = true
                        this.isVisualFire = false
                        this.isCollidable = false
                        this.setAI(false)
                        this.addScoreboardTag("LeashEntity")
                        this.setLeashHolder(E)
                        T.addEntry(this.uniqueId.toString())
                        T.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM)
                        T.setCanSeeFriendlyInvisibles(false)
                        //this.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1000000000, 1, true))
                        this.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1000000000, 1, true))
                    }
                    LeashEntity.set(E, entity)
                }

                // チームの誰かしら地面についている場合、エンティティを引っ張る処理を行う
                if (teamEntities.any { it.isOnGround }) leadEntity(E, center)
            }
        }
    }

    /**
     * ワールド全体のリードアイテムをキル
     */
    fun killLeadOfWorld()
            = Bukkit.getWorlds().forEach {
        it.entities
            .filter { it.type == EntityType.DROPPED_ITEM && (it as Item).itemStack.type == Material.LEAD }
            .forEach(Entity::remove)
    }

    /**
     * 繋がれていないリード用エンティティをキル
     */
    fun killNotConnectedEntity()
            = LeashEntity.filter { !it.key.isDead }.forEach { k, v ->
        if (runCatching { (v as? Chicken)?.leashHolder }.getOrNull() != k) v.remove()
    }

    /**
     * エンティティの中心点を計算
     */
    fun getEntitiesCenter(entities: List<Entity>) : Location? {
        val locations = entities.map { it.location }
        val firstLocation = locations.firstOrNull() ?: return null
        val x_averate = locations.map { it.x }.average()
        val y_averate = locations.map { it.y }.average()
        val z_averate = locations.map { it.z }.average()

        return Location(firstLocation.world, x_averate, y_averate, z_averate)
    }

    /**
     * 同じチームのエンティティ一覧を取得
     */
    fun getSameTeamEntities(entity: Entity) : List<Entity> {
        val entityTeam = getEntityTeam(entity) ?: return emptyList()
        return getTeamOnlineEntities(entityTeam).filter { it != entity }
    }

    /**
     * エンティティのチームを取得
     */
    fun getEntityTeam(entity: Entity) : Team? {
        val entry = if (entity is Player) entity.name else entity.uniqueId.toString()
        return ScoreboardAPI.MainBoard!!.getEntryTeam(entry)
    }

    /**
     * 指定チームのオンラインエンティティを取得
     */
    fun getTeamOnlineEntities(team: Team) : List<Entity>
        = team.entries
            .map {
                val uuid = Utils.stringToUUID(it)
                return@map Bukkit.getPlayerExact(it) ?: uuid?.let { Bukkit.getEntity(it) }
            }
            .filter { it?.type != EntityType.CHICKEN }
            .filter { it !is Player || it.isOnline || !it.isDead }
            .filterNotNull()

    /*
     *
     *    イベント
     *
     */

    @EventHandler
    private fun onJoinEvent(Event: PlayerJoinEvent)
            = getSameTeamEntities(Event.player).shuffled().firstOrNull()?.let { Event.player.teleport(it) }

    @EventHandler
    private fun onPlayerQuitEvent(Event: PlayerQuitEvent)
        = LeashEntity.get(Event.player)?.remove()


    @EventHandler
    private fun onPluginDisableEvent(Event: PluginDisableEvent)
        = takeIf { Event.plugin == TeamLeashers.plugin }
        ?.run { LeashEntity.forEach { t, u -> (u as? Chicken)?.setLeashHolder(null) ; u.remove() } }


    @EventHandler
    private fun onRespawnEvent(Event: PlayerRespawnEvent) {
        object: BukkitRunnable() { override fun run() {

            getSameTeamEntities(Event.player).shuffled().firstOrNull()?.let { Event.player.teleport(it) }
        }}.runTaskLater(TeamLeashers.plugin!!, 1)
    }

    @EventHandler
    private fun onChickenPropsEvent(Event: EntityDropItemEvent) {
        val throwerUUID = Event.itemDrop.thrower ?: return
        val thrower = Bukkit.getEntity(throwerUUID) ?: return

        if (LeashEntity.any { it.value == thrower }) Event.isCancelled = true
    }

    @EventHandler
    private fun onInventoryClickEvent(Event: InventoryClickEvent)
        = takeIf{ Event.currentItem?.type == Material.LEAD }
        ?.run { Event.isCancelled = true }

    @EventHandler
    private fun onItemDropEvent(Event: PlayerDropItemEvent)
        = takeIf{ Event.itemDrop.itemStack.type == Material.LEAD }
        ?.run { Event.isCancelled = true }

    @EventHandler
    private fun onSwapEvent(Event: PlayerSwapHandItemsEvent) { Event.isCancelled = true }

}