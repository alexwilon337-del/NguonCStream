package com.nguonc

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.mvvm.logError

class NguonCProvider : MainAPI() {

    override var mainUrl        = "https://phim.nguonc.com"
    private  val apiUrl         = "https://phim.nguonc.com/api"
    override var name           = "NguonC — Kho Phim Việt"
    override val hasMainPage    = true
    override val hasSearch      = true
    override var lang           = "vi"
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.AsianDrama,
        TvType.Anime,
    )

    override val mainPage = mainPageOf(
        "$apiUrl/films/phim-moi-cap-nhat"           to "🔥 Phim Mới Cập Nhật",
        "$apiUrl/films/danh-sach/phim-bo"           to "📺 Phim Bộ",
        "$apiUrl/films/danh-sach/phim-le"           to "🎬 Phim Lẻ",
        "$apiUrl/films/danh-sach/hoat-hinh"         to "🎨 Hoạt Hình",
        "$apiUrl/films/danh-sach/tv-shows"          to "📡 TV Shows",
        "$apiUrl/films/danh-sach/phim-chieu-rap"    to "🎭 Phim Chiếu Rạp",
        "$apiUrl/films/quoc-gia/viet-nam"           to "🇻🇳 Việt Nam",
        "$apiUrl/films/quoc-gia/trung-quoc"         to "🇨🇳 Trung Quốc",
        "$apiUrl/films/quoc-gia/han-quoc"           to "🇰🇷 Hàn Quốc",
        "$apiUrl/films/quoc-gia/au-my"              to "🇺🇸 Âu Mỹ",
        "$apiUrl/films/quoc-gia/nhat-ban"           to "🇯🇵 Nhật Bản",
        "$apiUrl/films/quoc-gia/thai-lan"           to "🇹🇭 Thái Lan",
        "$apiUrl/films/quoc-gia/hong-kong"          to "🇭🇰 Hồng Kông",
        "$apiUrl/films/quoc-gia/dai-loan"           to "🇹🇼 Đài Loan",
        "$apiUrl/films/quoc-gia/an-do"              to "🇮🇳 Ấn Độ",
        "$apiUrl/films/quoc-gia/anh"                to "🇬🇧 Anh",
        "$apiUrl/films/quoc-gia/phap"               to "🇫🇷 Pháp",
        "$apiUrl/films/quoc-gia/duc"                to "🇩🇪 Đức",
        "$apiUrl/films/quoc-gia/tay-ban-nha"        to "🇪🇸 Tây Ban Nha",
        "$apiUrl/films/quoc-gia/canada"             to "🇨🇦 Canada",
        "$apiUrl/films/the-loai/hanh-dong"          to "💥 Hành Động",
        "$apiUrl/films/the-loai/tinh-cam"           to "❤️ Tình Cảm",
        "$apiUrl/films/the-loai/hai-huoc"           to "😂 Hài Hước",
        "$apiUrl/films/the-loai/co-trang"           to "⚔️ Cổ Trang",
        "$apiUrl/films/the-loai/kinh-di"            to "👻 Kinh Dị",
        "$apiUrl/films/the-loai/tam-ly"             to "🧠 Tâm Lý",
        "$apiUrl/films/the-loai/vien-tuong"         to "🚀 Viễn Tưởng",
        "$apiUrl/films/the-loai/phieu-luu"          to "🗺️ Phiêu Lưu",
        "$apiUrl/films/the-loai/hinh-su"            to "🔍 Hình Sự",
        "$apiUrl/films/the-loai/chien-tranh"        to "💣 Chiến Tranh",
        "$apiUrl/films/the-loai/the-thao"           to "⚽ Thể Thao",
        "$apiUrl/films/the-loai/am-nhac"            to "🎵 Âm Nhạc",
        "$apiUrl/films/the-loai/gia-dinh"           to "👨‍👩‍👧 Gia Đình",
        "$apiUrl/films/the-loai/hoc-duong"          to "🏫 Học Đường",
        "$apiUrl/films/the-loai/vo-thuat"           to "🥋 Võ Thuật",
        "$apiUrl/films/the-loai/than-thoai"         to "🧙 Thần Thoại",
        "$apiUrl/films/the-loai/tai-lieu"           to "📚 Tài Liệu",
        "$apiUrl/films/the-loai/bi-an"              to "🔮 Bí Ẩn",
        "$apiUrl/films/the-loai/kinh-dien"          to "🏆 Kinh Điển",
        "$apiUrl/films/the-loai/phim-18"            to "🔞 Phim 18+",
    )

    private fun Any?.str()  = this as? String ?: ""
    private fun Any?.int()  = when (this) {
        is Double -> toInt(); is Int -> this
        is String -> toIntOrNull() ?: 0; else -> 0
    }
    private fun Any?.map()  = this as? Map<*, *>
    private fun Any?.list() = this as? List<*> ?: emptyList<Any>()

    private fun extractItems(json: Map<String, Any>?): List<*> =
        json?.get("items").list().ifEmpty {
            json?.get("data").map()?.get("items").list()
        }

    private fun parseCard(item: Map<*, *>): SearchResponse? {
        val slug    = item["slug"].str().ifEmpty { return null }
        val name    = item["name"].str().ifEmpty { return null }
        val thumb   = item["thumb_url"].str().ifEmpty { item["poster_url"].str() }
        val year    = item["year"].int().takeIf { it > 0 }
        val curEp   = item["current_episode"].str()
        val totalEp = item["total_episodes"].int()
        val type    = item["type"].str()
        val isSeries= totalEp > 1 || type == "series" ||
            (curEp.isNotEmpty() && !curEp.contains("full", ignoreCase = true))
        val detailUrl = "$apiUrl/film/$slug"
        return if (isSeries) {
            newAnimeSearchResponse(name, detailUrl, TvType.TvSeries) {
                posterUrl = thumb; this.year = year
                addDubStatus(isDub = false, isSub = true)
            }
        } else {
            newMovieSearchResponse(name, detailUrl, TvType.Movie) {
                posterUrl = thumb; this.year = year
            }
        }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val json    = app.get("${request.data}?page=$page")
            .parsedSafe<Map<String, Any>>()
        val items   = extractItems(json)
        val results = items.mapNotNull { it.map()?.let { m -> parseCard(m) } }
        val total   = json?.get("paginate").map()?.get("total_pages").int() ?: 1
        return newHomePageResponse(request.name, results, hasNextPage = page < total)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val json = app.get("$apiUrl/films/search?keyword=${query.encodeUri()}")
            .parsedSafe<Map<String, Any>>()
        return extractItems(json).mapNotNull { it.map()?.let { m -> parseCard(m) } }
    }

    override suspend fun load(url: String): LoadResponse {
        val json  = app.get(url).parsedSafe<Map<String, Any>>()
        val movie = json?.get("movie").map()
            ?: throw ErrorLoadingException("Không tìm thấy phim")

        val name     = movie["name"].str()
        val origName = movie["original_name"].str()
        val desc     = movie["description"].str()
        val thumb    = movie["thumb_url"].str().ifEmpty { movie["poster_url"].str() }
        val poster   = movie["poster_url"].str().ifEmpty { thumb }
        val year     = movie["year"].int().takeIf { it > 0 }
        val quality  = movie["quality"].str()
        val director = movie["director"].str()
        val casts    = movie["casts"].str()
        val totalEp  = movie["total_episodes"].int()
        val curEp    = movie["current_episode"].str()
        val lang     = movie["language"].str()
        val time     = movie["time"].str()
        val type     = movie["type"].str()

        val actors = casts.split(",")
            .map { it.trim() }.filter { it.isNotEmpty() }
            .map { Actor(it) }

        val tags = mutableListOf<String>()
        movie["category"].map()?.values?.forEach { cat ->
            cat.map()?.get("list").list().forEach { item ->
                item.map()?.get("name").str()
                    .takeIf { it.isNotEmpty() }?.let { tags.add(it) }
            }
        }

        val plot = buildString {
            if (origName.isNotEmpty()) append("[$origName]\n\n")
            append(desc)
            append("\n\n")
            if (quality.isNotEmpty())  append("🎬 $quality  ")
            if (lang.isNotEmpty())     append("🗣 $lang  ")
            if (time.isNotEmpty())     append("⏱ $time")
            if (curEp.isNotEmpty() && totalEp > 1)
                append("\n📺 Tập $curEp / $totalEp")
            if (director.isNotEmpty()) append("\n🎬 Đạo diễn: $director")
            if (casts.isNotEmpty())    append("\n🎭 Diễn viên: $casts")
        }.trim()

        val episodesRaw = movie["episodes"].list()
        val isSeries = totalEp > 1 || type == "series" ||
            (curEp.isNotEmpty() && !curEp.contains("full", ignoreCase = true))

        return if (isSeries) {
            val episodes = mutableListOf<Episode>()
            episodesRaw.forEachIndexed { sIdx, sRaw ->
                val server     = sRaw.map() ?: return@forEachIndexed
                val serverName = server["server_name"].str()
                    .ifEmpty { "Server ${sIdx + 1}" }
                val items      = server["items"].list()
                items.forEachIndexed { eIdx, eRaw ->
                    val ep     = eRaw.map() ?: return@forEachIndexed
                    val epName = ep["name"].str().ifEmpty { "${eIdx + 1}" }
                    val embed  = ep["embed"].str().ifEmpty { return@forEachIndexed }
                    episodes.add(newEpisode(embed) {
                        this.name      = "[$serverName] Tập $epName"
                        this.episode   = epName.toIntOrNull() ?: (eIdx + 1)
                        this.season    = sIdx + 1
                        this.posterUrl = thumb
                    })
                }
            }
            newTvSeriesLoadResponse(name, url, TvType.TvSeries, episodes) {
                posterUrl           = poster
                backgroundPosterUrl = thumb
                this.year           = year
                this.plot           = plot
                this.tags           = tags
                addActors(actors)
            }
        } else {
            val embed = episodesRaw.firstOrNull().map()
                ?.get("items").list()
                .firstOrNull().map()
                ?.get("embed").str() ?: ""
            newMovieLoadResponse(name, url, TvType.Movie, embed) {
                posterUrl           = poster
                backgroundPosterUrl = thumb
                this.year           = year
                this.plot           = plot
                this.tags           = tags
                addActors(actors)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        return try {
            loadExtractor(data, mainUrl, subtitleCallback, callback)
            true
        } catch (e: Exception) {
            logError(e)
            callback(ExtractorLink(
                source  = name,
                name    = name,
                url     = data,
                referer = mainUrl,
                quality = Qualities.Unknown.value,
                isM3u8  = false,
            ))
            true
        }
    }
}
