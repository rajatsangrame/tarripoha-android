package com.tarripoha.android.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.tarripoha.android.R
import com.tarripoha.android.data.model.Comment
import com.tarripoha.android.data.model.Word
import com.tarripoha.android.databinding.ActivityWordCardBinding
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.getPackage
import java.io.File
import java.io.FileOutputStream


class WordCardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WordCardActivity"
        private const val MAX_WIDTH = 1024
        private const val LOGO_SIZE = 140
        private const val PLAY_STORE_ICON_WIDTH = 258
        private const val PLAY_STORE_ICON_HEIGHT = 100
        private const val LOGO_MARGIN = 48F
        private const val START_X = 140F
        private const val KEY_COMMENTS = "comments"
        private const val KEY_WORD = "word"

        fun startMe(context: Context, word: Word, comments: ArrayList<Comment>? = null) {
            val intent = Intent(context, WordCardActivity::class.java)
            intent.putParcelableArrayListExtra(KEY_COMMENTS, comments)
            intent.putExtra(KEY_WORD, word)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityWordCardBinding
    private var word: Word? = null
    private var comments: ArrayList<Comment>? = null
    private var darkMode: Boolean = true

    private fun getNameProperty(): TextProperty {
        val color = if (isDarkMode()) {
            ContextCompat.getColor(this, R.color.colorPureWhite)
        } else ContextCompat.getColor(this, R.color.colorBlack)
        return TextProperty(
            font = ResourcesCompat.getFont(this, R.font.montserrat_bold)!!,
            textColor = color,
            textSize = 120F
        )
    }

    private fun getCommentProperty(): TextProperty {
        val color = if (isDarkMode()) {
            ContextCompat.getColor(this, R.color.colorPureWhite)
        } else ContextCompat.getColor(this, R.color.colorBlack)
        return TextProperty(
            font = ResourcesCompat.getFont(this, R.font.montserrat_medium)!!,
            textColor = color,
            textSize = 45F
        )
    }

    private val normalTextProperty: TextProperty by lazy {
        TextProperty(
            font = ResourcesCompat.getFont(this, R.font.montserrat_medium)!!,
            textColor = ContextCompat.getColor(this, R.color.colorGrey),
            textSize = 60F
        )
    }

    private val smallTextProperty: TextProperty by lazy {
        TextProperty(
            font = ResourcesCompat.getFont(this, R.font.montserrat_medium)!!,
            textColor = ContextCompat.getColor(this, R.color.colorGrey),
            textSize = 35F
        )
    }

    private val italicTextProperty: TextProperty by lazy {
        TextProperty(
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
            val comments = intent?.getParcelableArrayListExtra<Comment>(KEY_COMMENTS)
            if (comments != null) {
                this.comments = comments
            }

        } else {
            setUserMessage(getString(R.string.error_unknown))
        }
    }

    private fun invalidateWordCard() {
        if (!isCommentShareMode()) {
            createWordCard()
            return
        }
        createWordCardWithComments()
    }

    private fun createWordCard() {

        if (word == null) {
            setUserMessage(getString(R.string.error_unknown))
            return
        }

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
            val p = getPaint(normalTextProperty)
            canvas.drawText(it, START_X, 400F, p)
        }
        word?.eng?.let {
            val p = getPaint(italicTextProperty)
            drawMultilineText(
                text = it, canvas = canvas, paint = p, x = START_X, y = 500F, yNextLine = 80F
            ) {}
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

    private fun createWordCardWithComments() {

        if (word == null) {
            setUserMessage(getString(R.string.error_unknown))
            return
        }

        val commentList = comments
        if (commentList.isNullOrEmpty()) {
            setUserMessage(getString(R.string.error_unknown))
            return
        }
        var yMAX = MAX_WIDTH
        var yComment = 500F
        commentList.forEach {
            yMAX += (256 + 48)
            val length = it.comment.length
            val lines = length / 20
            yMAX += (lines * 80)
        }

        val resultBitmap = Bitmap.createBitmap(MAX_WIDTH, yMAX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = getBackGroundColor()
        canvas.drawPaint(paint)

        word?.name?.let {
            val p = getPaint(getNameProperty())
            canvas.drawText(it, START_X, 300F, p)
        }
        word?.meaning?.let {
            val p = getPaint(normalTextProperty)
            canvas.drawText(it, START_X, 400F, p)
        }
        word?.eng?.let {
            val p = getPaint(italicTextProperty)
            drawMultilineText(
                text = it, canvas = canvas, paint = p, x = START_X, y = 500F, yNextLine = 80F
            ) {
                yComment = it + 300F
            }
        }

        commentList.forEach {

            // Avatar
            val textPaint = getPaint(smallTextProperty)
            canvas.drawCircle(START_X - 20F, yComment - 15F, 50F, textPaint)
            val user = it.userName ?: getString(R.string.user)

            // Avatar text
            val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            whitePaint.textSize = 50F
            whitePaint.color = ContextCompat.getColor(this, R.color.colorPureWhite)
            canvas.drawText(user[0].uppercase(), START_X - 35F, yComment, whitePaint)

            // Username
            canvas.drawText(user, START_X + 75, yComment - 20F, textPaint)

            // Comment
            val p = getPaint(getCommentProperty())
            drawMultilineText(
                text = it.comment,
                canvas = canvas,
                paint = p,
                x = START_X + 75,
                y = yComment + 45F,
                yNextLine = 80F
            ) {
                yComment = it
            }

            val time = TPUtils.getTime(this, it.timestamp)
            var count = 0
            it.likes?.forEach { map ->
                if (map.value) {
                    count++
                }
            }
            val likes: String = when (count) {
                0 -> {
                    ""
                }
                1 -> {
                    getString(R.string.like, TPUtils.prettyCount(count))
                }
                else -> {
                    getString(R.string.likes, TPUtils.prettyCount(count))
                }
            }
            canvas.drawText("$time   $likes", START_X + 75F, yComment + 70F, textPaint)
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
            /*TOP*/yMAX - LOGO_SIZE - LOGO_MARGIN,//finalY + 400F - LOGO_SIZE - LOGO_MARGIN,
            Paint()
        )

        binding.previewIv.setImageBitmap(resultBitmap)

    }

    private fun drawMultilineText(
        text: String, canvas: Canvas, paint: Paint,
        x: Float, y: Float, yNextLine: Float,
        maxLength: Int = 20,
        finalY: (Float) -> Unit
    ) {
        var startY = y
        val size = text.length
        if (size < maxLength) {
            canvas.drawText(text, x, startY, paint)
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
                canvas.drawText(line, x, startY, paint)
                startY += yNextLine
            }
        }
        finalY(startY)
    }

    private fun isCommentShareMode(): Boolean = comments != null

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

    private fun getPaint(property: TextProperty): Paint {
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

    fun share(view: View) {
        val shareText =
            "Download Tarri Poha from the PlayStore https://play.google.com/store/apps/details?id=${getPackage()}"
        val bitmap = binding.previewIv.drawable.toBitmap()
        try {
            val file = File(externalCacheDir, "share.png")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val imageUri = FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                file
            )
            intent.putExtra(Intent.EXTRA_STREAM, imageUri)
            intent.type = "image/png"
            startActivity(Intent.createChooser(intent, "Share image via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class TextProperty(val font: Typeface, val textSize: Float, val textColor: Int)
}