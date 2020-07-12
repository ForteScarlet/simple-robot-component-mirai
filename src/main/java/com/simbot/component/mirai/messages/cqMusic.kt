@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.simbot.component.mirai.messages

import net.mamoe.mirai.message.data.ServiceMessage
import net.mamoe.mirai.message.data.buildXmlMessage


/**
 * CQ码音乐分享获取Xml格式的卡片分享
 */
interface CqMusicXml {
    val serviceMessage: ServiceMessage
}


/**
 * CQ码格式的音乐分享
 *
 * @param type 音乐类型
 * @param
 *
 */
//abstract class CqMusic(val type: String, val id: Long, val style: String? = null)

/**
 * type = qq
 */

internal class MusicQQ(id: String) : CqMusicXml {

    companion object Xml {
        fun toXml(songId: String): String {
            return """
                <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                <msg serviceID="2" templateID="1" action="web" brief="[分享] Into the Unknown" sourceMsgId="0"
                     url="https://i.y.qq.com/v8/playsong.html?_wv=1&amp;songid=$songId&amp;souce=qqshare&amp;source=qqshare&amp;ADTAG=qqshare"
                     flag="0" adverSign="0" multiMsgFlag="0">
                    <item layout="2">
                        <audio cover="http://imgcache.qq.com/music/photo/album_500/6/500_albumpic_9240506_0.jpg"
                               src="http://apd-vlive.apdcdn.tc.qq.com/amobile.music.tc.qq.com/C40000189mAI2BxJrT.m4a?guid=1608691028&amp;vkey=A4F35138288554D83749AE114211FAB34A94125676D5E11F003F03C54D7C2ED20B345F3E063E17ECB14972543CB505CA664FC82D30BA2E6A&amp;uin=0&amp;fromtag=38"/>
                        <title>Into the Unknown</title>
                        <summary>Idina Menzel/AURORA</summary>
                    </item>
                    <source name="QQ音乐" icon="https://i.gtimg.cn/open/app_icon/01/07/98/56/1101079856_100_m.png?date=20200712"
                            url="http://web.p.qq.com/qqmpmobile/aio/app.html?id=1101079856" action="app"
                            a_actionData="com.tencent.qqmusic" i_actionData="tencent1101079856://" appid="1101079856"/>
                </msg>
            """.trimIndent()
        }
    }

    private val qqMusicXml: ServiceMessage

    init {
        qqMusicXml = ServiceMessage(2, toXml(id))
//        qqMusicXml = buildXmlMessage(2) {
//            action = "web"
////            brief = "[分享] $title"
//
//
//        }
    }


    override val serviceMessage: ServiceMessage = qqMusicXml


}










