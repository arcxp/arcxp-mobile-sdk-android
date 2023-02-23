package com.arcxp.content

import com.arcxp.commons.util.MoshiController
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.Image
import com.arcxp.content.models.Text
import com.arcxp.content.models.WebsiteSection
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class JsonTests {


    @Before
    fun setUp() {

    }


    @Test
    fun `Verify WebsiteSection Section polymorphic type in ArcXPStory`() {

        val storyJson = getJson("story1.json")
        val story = MoshiController.fromJson(
            storyJson,
            ArcXPStory::class.java
        )!!


        assertTrue(story.websites!!["estadao"]!!.website_section is WebsiteSection.Section)
    }

    @Test
    fun `Verify WebsiteSection Referent polymorphic type in ArcXPStory`() {

        val storyJson = getJson("story2.json")
        val story = MoshiController.fromJson(
            storyJson,
            ArcXPStory::class.java
        )!!


        assertTrue(story.websites!!["arcsales"]!!.website_section is WebsiteSection.Reference)
        assertTrue(story.websites!!["mckinsey"]!!.website_section is WebsiteSection.Reference)
    }

    @Test
    fun `Verify StoryElement polymorphic types in ArcXPStory`() {

        val storyJson = getJson("story2.json")
        val story = MoshiController.fromJson(
            storyJson,
            ArcXPStory::class.java
        )!!

        assertTrue(story.content_elements!![0] is Text)
        assertTrue(story.content_elements!![1] is Text)
        assertTrue(story.content_elements!![2] is Text)
        assertTrue(story.content_elements!![3] is Image)
        assertTrue(story.content_elements!![4] is Image)
        assertTrue(story.content_elements!![5] is Image)
        assertTrue(story.content_elements!![6] is Image)
        assertTrue(story.content_elements!![7] is Text)
        assertTrue(story.content_elements!![8] is Text)

    }

    private fun getJson(fileName: String): String {
        val file = File(
            javaClass.classLoader?.getResource(fileName)?.path
                ?: throw NullPointerException("No path find!")
        )
        return String(file.readBytes())
    }
}