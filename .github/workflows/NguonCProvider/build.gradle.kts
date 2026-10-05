plugins {
    id("com.android.library")
    id("com.lagradost.cloudstream3.gradle")
}

cloudstream {
    description = "Kho phim Việt toàn diện — phim.nguonc.com | 41 danh mục | Vietsub & Thuyết minh"
    authors     = listOf("NguonCStream")
    status      = 1
    tvTypes     = listOf("Movie","TvSeries","AsianDrama","Anime")
    iconUrl     = "https://phim.nguonc.com/favicon.ico"
    language    = "vi"
}

android {
    namespace  = "com.nguonc"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
    val cloudstream by configurations
    cloudstream("com.lagradost:cloudstream3:pre-release")
}
