package com.zqy.hci.theme

import android.content.Context
import android.util.AttributeSet
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.ArrayList

class UIThemeFactory(context: Context) {

    private val INVALID_RES_ID: Int = 0
    private val XML_NAME_RES_ID_ATTRIBUTE = "nameResId"
    private val XML_THEME_RES_ID_ATTRIBUTE = "themeRes"
    private val XML_ICONS_THEME_RES_ID_ATTRIBUTE = "iconsThemeRes"
    private val XML_ROOT_TAG = "Themes"
    private val XML_THEME_TAG = "Theme"

    fun createThemes(context: Context, id: Int): ArrayList<UITheme> {
        context.resources.getXml(id).use { xml ->
            return parseThemesFromXml(
                context,
                xml
            )
        }
    }

    private fun parseThemesFromXml(packContext: Context, xml: XmlPullParser): ArrayList<UITheme> {
        val themes: ArrayList<UITheme> = ArrayList<UITheme>()
        try {
            var event: Int
            var inRoot = false
            while (xml.next().also { event = it } != XmlPullParser.END_DOCUMENT) {
                val tag = xml.name
                if (event == XmlPullParser.START_TAG) {
                    if (XML_ROOT_TAG == tag) {
                        inRoot = true
                    } else if (inRoot && XML_THEME_TAG == tag) {
                        val attrs = Xml.asAttributeSet(xml)
                        val theme: UITheme = createThemeFromXmlAttributes(attrs, packContext)
                        if (theme != null) {
                            themes.add(theme)
                        }
                    }
                } else if (event == XmlPullParser.END_TAG && XML_ROOT_TAG == tag) {
                    inRoot = false
                    break
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
        return themes
    }

    private fun createThemeFromXmlAttributes(attrs: AttributeSet, packContext: Context): UITheme {
        val name = attrs.getAttributeValue(
            null,
            XML_NAME_RES_ID_ATTRIBUTE
        )
        val keyboardThemeResId = attrs.getAttributeResourceValue(
            null,
            XML_THEME_RES_ID_ATTRIBUTE,
            0
        )
        val iconsThemeResId = attrs.getAttributeResourceValue(
            null,
            XML_ICONS_THEME_RES_ID_ATTRIBUTE,
            0
        )
        return UITheme(packContext, name, keyboardThemeResId, iconsThemeResId)
    }

    private fun getTextFromResourceOrText(
        context: Context, attrs: AttributeSet, attributeName: String?
    ): CharSequence? {
        val stringResId = attrs.getAttributeResourceValue(null, attributeName, INVALID_RES_ID)
        return if (stringResId != INVALID_RES_ID) {
            context.resources.getString(stringResId)
        } else {
            attrs.getAttributeValue(null, attributeName)
        }
    }

}