package com.nexrblx.app

import org.json.JSONArray
import org.json.JSONObject

object Config {
    // TODO: ضع اسم حسابك واسم المستودع على GitHub (الملف data.json في الجذر)
    const val DATA_URL = "https://raw.githubusercontent.com/YOUR_USER/YOUR_REPO/main/data.json"
    const val REQUEST_REPO = "YOUR_USER/YOUR_REPO"

    const val BEEBOM_HUB = "https://beebom.com/roblox-games-codes-list/"
    const val BEEBOM_SEARCH = "https://beebom.com/?s="
    const val SCRIPTBLOX = "https://scriptblox.com/search?q="
}

data class Game(
    val id: String,
    val name: String,
    val kind: String,        // "game" | "template"
    val category: String,
    val image: String,
    val desc: String,
    val descAr: String,
    val popularity: Int,
    val updated: String
) {
    fun description(ar: Boolean) = if (ar && descAr.isNotBlank()) descAr else desc
}

data class StudioScript(
    val id: String,
    val gameId: String,
    val title: String,
    val desc: String,
    val descAr: String,
    val version: String,
    val updated: String,
    val category: String,
    val popularity: Int,
    val place: String,
    val code: String
) {
    fun description(ar: Boolean) = if (ar && descAr.isNotBlank()) descAr else desc
}

data class RedeemCode(
    val gameId: String,
    val code: String,
    val reward: String,
    val rewardAr: String,
    val status: String,      // "new" | "active" | "expired"
    val added: String
) {
    fun rewardText(ar: Boolean) = if (ar && rewardAr.isNotBlank()) rewardAr else reward
}

data class AppData(
    val version: Int,
    val websiteName: String,
    val website: String,
    val games: List<Game>,
    val scripts: List<StudioScript>,
    val codes: List<RedeemCode>
) {
    companion object {
        val EMPTY = AppData(0, "", "", emptyList(), emptyList(), emptyList())
    }
}

private fun <T> JSONArray?.mapObjects(f: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    val out = ArrayList<T>(length())
    for (i in 0 until length()) out.add(f(getJSONObject(i)))
    return out
}

fun parseData(text: String): AppData {
    val root = JSONObject(text)
    return AppData(
        version = root.optInt("version", 0),
        websiteName = root.optString("websiteName", ""),
        website = root.optString("website", ""),
        games = root.optJSONArray("games").mapObjects {
            Game(
                id = it.getString("id"),
                name = it.getString("name"),
                kind = it.optString("kind", "game"),
                category = it.optString("category", ""),
                image = it.optString("image", ""),
                desc = it.optString("desc", ""),
                descAr = it.optString("descAr", ""),
                popularity = it.optInt("popularity", 0),
                updated = it.optString("updated", "")
            )
        },
        scripts = root.optJSONArray("scripts").mapObjects {
            StudioScript(
                id = it.getString("id"),
                gameId = it.optString("gameId", ""),
                title = it.getString("title"),
                desc = it.optString("desc", ""),
                descAr = it.optString("descAr", ""),
                version = it.optString("version", "1.0"),
                updated = it.optString("updated", ""),
                category = it.optString("category", ""),
                popularity = it.optInt("popularity", 0),
                place = it.optString("place", ""),
                code = it.optString("code", "")
            )
        },
        codes = root.optJSONArray("codes").mapObjects {
            RedeemCode(
                gameId = it.optString("gameId", ""),
                code = it.getString("code"),
                reward = it.optString("reward", ""),
                rewardAr = it.optString("rewardAr", ""),
                status = it.optString("status", "active"),
                added = it.optString("added", "")
            )
        }
    )
}
