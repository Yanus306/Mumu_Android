package kr.ac.anu.mumu

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.ac.anu.mumu.databinding.DialogDiaryFormBinding
import kr.ac.anu.mumu.databinding.DialogPetFormBinding
import kr.ac.anu.mumu.databinding.ItemPetProfileBinding
import kr.ac.anu.mumu.presentation.main.bindDiaryMood
import kr.ac.anu.mumu.presentation.main.fitContentHeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProfileDiaryLayoutTest {
    @Test
    fun renderCompactLayouts() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = ContextThemeWrapper(instrumentation.targetContext, R.style.Theme_Mumu)
            val inflater = LayoutInflater.from(context)
            for (width in listOf(320, 360, 411)) {
                val diary = DialogDiaryFormBinding.inflate(inflater)
                diary.tvFormTitle.visibility = View.GONE
                diary.btnCancel.visibility = View.GONE
                diary.etDiaryDate.setText("2026-09-15")
                diary.fitContentHeight((648 * context.resources.displayMetrics.density).toInt())
                diary.moodNormal.bindDiaryMood("normal")
                diary.moodSad.bindDiaryMood("sad")
                diary.moodHappy.bindDiaryMood("happy")
                render(diary.root, width, 648, "diary_$width")
                assertTrue(diary.btnSaveDiary.width > 0)
                assertEquals(diary.etDiaryTitle.width, diary.btnSaveDiary.width)
                assertTrue(diary.moodSad.width >= diary.moodSad.compoundDrawables[1].bounds.width())

                val pet = DialogPetFormBinding.inflate(inflater)
                render(pet.root, width, 648, "pet_form_$width")
                assertEquals(View.GONE, pet.layoutExtraInfo.visibility)
                assertTrue(pet.etPetName.width > 0)
                assertEquals(pet.etPetName.width, pet.btnSavePet.width)

                val card = ItemPetProfileBinding.inflate(inflater)
                card.tvPetName.text = "우리집 반려동물"
                card.tvPetInfo.text = "5살 · 강아지"
                render(card.root, width - 82, 120, "pet_card_$width")
                assertTrue(card.tvPetName.width > 0)
                assertTrue(card.ivPetProfile.right < card.tvPetName.left + (card.tvPetName.parent as View).left)
            }
        }
    }

    private fun render(view: View, widthDp: Int, heightDp: Int, name: String) {
        val density = view.resources.displayMetrics.density
        val width = (widthDp * density).toInt()
        val height = (heightDp * density).toInt()
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        view.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        File(view.context.cacheDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
