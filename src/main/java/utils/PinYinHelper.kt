package utils

import base.Config
import com.github.promeg.pinyinhelper.Pinyin

private val pinyinCache: Map<String, String> by lazy {
    Config.stockTableHeader.associateWith { Pinyin.toPinyin(it, "") }
}

fun String.toPinYin(): String {
    return pinyinCache[this] ?: Pinyin.toPinyin(this, "")
}