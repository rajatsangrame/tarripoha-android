package com.tarripoha.android.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.tarripoha.android.R
import com.tarripoha.android.data.model.Comment
import com.tarripoha.android.data.model.Word
import com.tarripoha.android.databinding.ActivityWordCardBinding
import com.tarripoha.android.util.TPUtils


class WordCardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WordCardActivity"
        private const val MAX_WIDTH = 1024
        private const val LOGO_SIZE = 140
        private const val PLAY_STORE_ICON_WIDTH = 258
        private const val PLAY_STORE_ICON_HEIGHT = 100
        private const val LOGO_MARGIN = 48F
        private const val START_X = 140F
        private const val KEY_COMMENT = "comment"
        private const val KEY_WORD = "word"
        fun startMe(context: Context, word: Word, comment: Comment? = null) {
            val intent = Intent(context, WordCardActivity::class.java)
            intent.putExtra(KEY_COMMENT, comment)
            intent.putExtra(KEY_WORD, word)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityWordCardBinding
    private var word: Word? = null
    private var comment: Comment? = null
    private var darkMode: Boolean = true

    private fun getNameProperty(): TextProperties {
        val color = if (isDarkMode()) {
            ContextCompat.getColor(this, R.color.colorPureWhite)
        } else ContextCompat.getColor(this, R.color.colorBlack)
        return TextProperties(
            font = ResourcesCompat.getFont(this, R.font.montserrat_bold)!!,
            textColor = color,
            textSize = 120F
        )
    }

    private val meaning: TextProperties by lazy {
        TextProperties(
            font = ResourcesCompat.getFont(this, R.font.montserrat_medium)!!,
            textColor = ContextCompat.getColor(this, R.color.colorGrey),
            textSize = 60F
        )
    }
    private val english: TextProperties by lazy {
        TextProperties(
            font = ResourcesCompat.getFont(this, R.font.montserrat_medium_italic)!!,
            textColor = ContextCompat.getColor(this, R.color.colorGrey),
            textSize = 60F
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setupListener()
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            invalidateWordCard()
        }, 300)
    }

    private fun init() {
        if (intent.hasExtra(KEY_WORD)) {
            val word = intent?.getParcelableExtra<Word>(KEY_WORD)
            if (word == null) {
                setUserMessage(getString(R.string.error_unknown))
                return
            }
            this.word = word

            val comment = intent?.getParcelableExtra<Comment>(KEY_COMMENT)
            if (comment == null) {
                this.comment = comment
            }

        } else {
            setUserMessage(getString(R.string.error_unknown))
        }
    }

    private fun invalidateWordCard() {
        if (!isCommentShareMode()) {
            createWordCard()
        }
    }

    private fun createWordCard() {

        val resultBitmap = Bitmap.createBitmap(MAX_WIDTH, MAX_WIDTH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = getBackGroundColor()
        canvas.drawPaint(paint)

        word?.name?.let {
            val p = getPaint(getNameProperty())
            canvas.drawText(it, START_X, 300F, p)
        }
        word?.meaning?.let {
            val p = getPaint(meaning)
            canvas.drawText(it, START_X, 400F, p)
        }
        word?.eng?.let {
            val p = getPaint(english)
            drawMultilineText(
                text = it, canvas = canvas, paint = p, y = 500F, yNextLine = 80F
            )
        }

        // Placing at right corner
        canvas.drawBitmap(
            getPlayStoreLogo(),
            /*Left*/ (MAX_WIDTH - PLAY_STORE_ICON_WIDTH - LOGO_MARGIN),
            /*TOP*/ LOGO_MARGIN,
            Paint()
        )

        // Placing at bottom center
        canvas.drawBitmap(
            getTPLogo(),
            /*Left*/(MAX_WIDTH / 2F - LOGO_SIZE / 2F),
            /*TOP*/MAX_WIDTH - LOGO_SIZE - LOGO_MARGIN,
            Paint()
        )
        binding.previewIv.setImageBitmap(resultBitmap)

    }

    private fun drawMultilineText(
        text: String, canvas: Canvas, paint: Paint,
        y: Float, yNextLine: Float,
        maxLength: Int = 20
    ) {
        var startY = y
        val size = text.length
        if (size < maxLength) {
            canvas.drawText(text, START_X, startY, paint)
        } else {
            val list = mutableListOf<String>()
            val arr = text.split(" ")
            var temp = ""
            arr.forEach { w ->
                if (temp.length < maxLength) {
                    temp += "$w "
                } else {
                    list.add(temp)
                    temp = "$w "
                }
            }
            if (temp.isNotEmpty()) {
                list.add(temp)
            }
            list.forEach { line ->
                canvas.drawText(line, START_X, startY, paint)
                startY += yNextLine
            }
        }
    }

    private fun isCommentShareMode(): Boolean = comment != null

    private fun isDarkMode(): Boolean = this.darkMode

    private fun getBackGroundColor(): Int {
        return if (isDarkMode()) {
            ContextCompat.getColor(this, R.color.colorBlack)
        } else ContextCompat.getColor(this, R.color.colorPureWhite)
    }

    private fun setUserMessage(message: String) {
        TPUtils.showSnackBar(this, message)
    }

    private fun getTPLogo(): Bitmap {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo_round)
        return Bitmap.createScaledBitmap(bitmap, LOGO_SIZE, LOGO_SIZE, true)
    }

    private fun getPlayStoreLogo(): Bitmap {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.google_play)
        return Bitmap.createScaledBitmap(
            bitmap,
            PLAY_STORE_ICON_WIDTH,
            PLAY_STORE_ICON_HEIGHT,
            true
        )
    }

    private fun getPaint(property: TextProperties): Paint {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.typeface = property.font
        paint.textSize = property.textSize
        paint.color = property.textColor
        return paint
    }

    private fun setupListener() {
        binding.toggleBlack.setOnCheckedChangeListener { _, isChecked ->
            this.darkMode = isChecked
            invalidateWordCard()
        }
    }

    data class TextProperties(val font: Typeface, val textSize: Float, val textColor: Int)
}