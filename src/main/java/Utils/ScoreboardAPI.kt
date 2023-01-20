package Utils

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team

/*
 * スコアボードAPI v1.1
 *
 * @author HIYU
 * @changeLog
 *  　V1.0 - コード作成
 * 　 V1.1 - getScoreのスコア設定状況(isSetScore)を考慮するようになった
 */
object ScoreboardAPI {

    val MainBoard = Bukkit.getScoreboardManager()?.mainScoreboard

    // -------------------------------------------------------
    //
    //                        スコアAPI
    //
    // -------------------------------------------------------

    /*
     *　スコアを設定
     *  @Params Objective - オブジェクティブ名
     *  @Params Entry - スコアボードエントリー名
     *  @Params Value - 設定スコア
     *  @Return スコア追加の成否
     */
    fun setScore(Objective: String, Entry: String, Value: Int) : Boolean {
        val Obj = getObjective(Objective) ?: return false
        val Score = Obj.getScore(Entry)
        Score.setScore(Value)

        return true
    }

    /*
     *　スコアを追加
     *　　@Params Objective - オブジェクティブ名
     * 　@Params Entry - スコアボードエントリー名
     * 　@Params Value - 追加スコア
     *  @Return スコア追加の成否
     */
    fun addScore(Objective: String, Entry: String, Value: Int) : Boolean {
        return setScore(Objective, Entry, getScore(Objective, Entry)?.plus(Value) ?: Value)
    }

    /*
     *　スコアを除算
     *　　@Params Objective - オブジェクティブ名
     * 　@Params Entry - スコアボードエントリー名
     * 　@Params Value - 除算スコア
     *  @Return スコア除算成否
     */
    fun removeScore(Objective: String, Entry: String, Value: Int) : Boolean {
        setScore(Objective, Entry, getScore(Objective, Entry)?.minus(Value) ?: Value)
        return true
    }

    /*
     *　スコアを取得
     *　　@Params Objective - オブジェクティブ名
     * 　@Params Entry - スコアボードエントリー名
     *  @Return スコア
     */
    fun getScore(Objective: String, Entry: String) : Int? {
        val Obj = getObjective(Objective) ?: return null
        val Score = Obj.getScore(Entry)
        if (!Score.isScoreSet) return null
        return Score.score
    }

    /*
     *　スコアをリセット
     *　@Params Objective - オブジェクティブ名
     *  @Params Entry - スコアボードエントリー名
     *  @Return リセットの成否
     */
    fun resetScore(Objective: String, Entry: String) : Boolean {
        val Obj = getObjective(Objective) ?: return false
        Obj.scoreboard?.resetScores(Entry)
        return true
    }

    // -------------------------------------------------------
    //
    //                        オブジェクティブAPI
    //
    // -------------------------------------------------------
    /*
     * オブジェクティブ有無取得
     *  @Param Name オブジェクティブ名
     *  @Return オブジェクティブ存在
     */
    fun isExistObjective(Name: String) : Boolean
        = getObjective(Name) != null

    /*
     * オブジェクティブ生成
     *  @Param Name オブジェクティブ名
     *  @Param Creteria クリテリア
     *  @Param DisplayName 表示名
     *  @Return オブジェクティブ作成成否
     */
    fun createObjective(Name: String, Creteria: String, DisplayName: String = Name) : Boolean
        = runCatching { MainBoard?.registerNewObjective(Name, Creteria, DisplayName) }.isSuccess

    /*
     * オブジェクティブ削除
     *  @Param Name オブジェクティブ名
     *  @Return オブジェクティブ削除成否
     */
    fun deleteObjective(Name: String) : Boolean {
        val Obj = getObjective(Name) ?: return false
        return runCatching { Obj.unregister() }.isSuccess
    }

    /*
     * オブジェクティブ取得
     *  @Param Name オブジェクティブ名
     *  @Return オブジェクティブ削除成否
     */
    fun getObjective(Name: String) : Objective?
        = runCatching { MainBoard?.getObjective(Name) }.getOrNull()

    /*
 *　エントリー一覧を取得
 *　@Params Objective - オブジェクティブ名
 *  @Params Entry - スコアボードエントリー名
 *  @Return リセットの成否
 */
    fun getObjectiveEntries(Objective: String) : List<String> {
        val Obj = getObjective(Objective) ?: return listOf()
        return Obj.scoreboard?.entries?.filter { Obj.getScore(it).isScoreSet } ?: listOf()
    }

    /*
     *　全オブジェクティブ取得
     *  @Return オブジェクティブ配列
     */
    fun getObjectives() : List<String> = MainBoard?.objectives?.map { it.name } ?: listOf()

    // -------------------------------------------------------
    //
    //                        チームAPI
    //
    // -------------------------------------------------------

    /*
     * プレイヤーがチームに所属するか否か
     *  @Param Entry エントリー名
     *  @Param TeamName チーム名
     *  @Param IgnoreCase 大文字小文字無視
     *  @Return 所属の有無
     */
    fun isTeam(Entry: String, TeamName: String, IgnoreCase: Boolean = true) : Boolean {
        val getTeam = getTeam(TeamName) ?: return false
        return getTeam.entries.any { it.contains(Entry, IgnoreCase) }
    }

    /*
     * チームが存在するか否か
     *  @Param TeamName チーム名
     *  @Return チームの有無
     */
    fun isExistTeam(TeamName: String) : Boolean
        = getTeam(TeamName) != null

    /*
     * チーム生成
     *  @Param TeamName チーム名
     *  @Return チーム生成の成否
     */
    fun createTeam(TeamName: String) : Boolean
        = runCatching { MainBoard?.registerNewTeam(TeamName) }.isSuccess

    /*
     * チーム取得
     *  @Param TeamName チーム名
     *  @Return チームインスタンス
     */
    fun getTeam(TeamName: String) : Team?
        = runCatching { MainBoard?.getTeam(TeamName) }.getOrNull()

    /*
     * エントリーのチーム取得
     *  @Param Entry エントリー名
     *  @Return チームインスタンス
     */
    fun getTeamEntry(Entry: String) : Team?
        = runCatching { MainBoard?.getEntryTeam(Entry) }.getOrNull()

    /*
     * チームのエントリー配列を取得
     *  @Param  TeamName チーム名
     *  @Return チームインスタンス
     */
    fun getTeamEntries(TeamName: String) : List<String>
        = getTeam(TeamName)?.entries?.toList() ?: emptyList()

    /*
     * チーム削除
     *  @Param TeamName チーム名
     *  @Return チーム削除の成否
     */
    fun deleteTeam(TeamName: String) : Boolean {
        return runCatching { MainBoard?.getTeam(TeamName)?.unregister() }.isSuccess
    }

    /*
     * チーム詳細設定
     *  @Param TeamName チーム名
     *  @Param Option チームオプション
     *  @Param Status オプション設定
     *  @Return 詳細設定成否
     */
    fun setTeamModify(TeamName: String, OptionName: String, Value: String) : Boolean {
        val getTeam = getTeam(TeamName) ?: return false

        when (OptionName.replace("_", "")) {

            "collisionRule" -> {
                val Status = Team.OptionStatus.values()
                        .find { it.name.replace("_", "").equals(Value, true) }
                        ?: return false
                getTeam.setOption(Team.Option.COLLISION_RULE, Status)
                return true
            }

            "color" -> {
                val Color = ChatColor.values()
                        .find { it.name.replace("_", "").equals(Value, true) }
                        ?: return false
                getTeam.setColor(Color)
                return true
            }

            "deathMessageVisibility" -> {
                val Status = Team.OptionStatus.values()
                        .find { it.name.replace("_", "").equals(Value, true) }
                        ?: return false
                getTeam.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Status)
                return true
            }

            "displayName" -> {
                getTeam.setDisplayName(Value.replace("&", "§"))
                return true
            }

            "friendlyfire" -> {
                if (Value.equals("true", true) || Value.equals("false", true)) {
                    val isFriendlyFire = Value.toBoolean()
                    getTeam.setAllowFriendlyFire(isFriendlyFire)
                    return true
                }
            }

            "nametagVisibility" -> {
                val Status = Team.OptionStatus.values()
                        .find { it.name.replace("_", "").equals(Value, true) }
                        ?: return false
                getTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Status)
                return true
            }

            "prefix" -> {
                getTeam.setPrefix(Value.replace("&", "§"))
                return true
            }

            "seeFriendlyInvisibles" -> {
                if (Value.equals("true", true) || Value.equals("false", true)) {
                    val isFriendlyInvisible = Value.toBoolean()
                    getTeam.setCanSeeFriendlyInvisibles(isFriendlyInvisible)
                    return true
                }
            }

            "suffix" -> {
                getTeam.setPrefix(Value.replace("&", "§"))
                return true
            }
        }

        return false
    }

    /*
     * チーム詳細取得
     *  @Param TeamName チーム名
     *  @Param Option チームオプション
     *  @Return オプション
     */
    fun getTeamModify(TeamName: String, OptionName: String) : String? {
        val getTeam = getTeam(TeamName) ?: return null

        when (OptionName.replace("_", "")) {

            "collisionRule" -> {
                return runCatching { getTeam.getOption(Team.Option.COLLISION_RULE).name }.getOrNull()
            }

            "color" -> {
                return getTeam.color.name
            }

            "deathMessageVisibility" -> {
                return runCatching { getTeam.getOption(Team.Option.DEATH_MESSAGE_VISIBILITY).name }.getOrNull()
            }

            "displayName" -> {
                return getTeam.displayName
            }

            "friendlyfire" -> {
                return getTeam.allowFriendlyFire().toString()
            }

            "nametagVisibility" -> {
                return runCatching { getTeam.getOption(Team.Option.NAME_TAG_VISIBILITY).name }.getOrNull()
            }

            "prefix" -> {
                return getTeam.prefix
            }

            "seeFriendlyInvisibles" -> {
                return getTeam.canSeeFriendlyInvisibles().toString()
            }

            "suffix" -> {
                return getTeam.suffix
            }
        }

        return null
    }

}
