package com.bijoysingh.quicknote.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.external.ImportNoteFromFileActivity.Companion.convertStreamToString
import com.bijoysingh.quicknote.database.Note
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.github.bijoysingh.uibasics.views.UITextView


class ExternalIntentActivity : ThemedActivity() {

  lateinit var context: Context
  lateinit var store: DataStore

  var filenameText: String = ""
  var titleText: String = ""
  var contentText: String = ""

  lateinit var filename: TextView
  lateinit var title: TextView
  lateinit var content: TextView

  lateinit var backButton: ImageView
  lateinit var actionDone: UITextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_external_intent)

    context = this
    store = DataStore.get(context)

    setView()
    requestSetNightMode(store.get(ThemedActivity.getKey(), false))
    val shouldHandleIntent = handleIntent()
    if (!shouldHandleIntent) {
      finish()
      return
    }

    content.setText(contentText)
    title.setText(titleText)
    title.visibility = if (TextUtils.isNullOrEmpty(titleText)) View.GONE else View.VISIBLE
    filename.setText(filenameText)
  }

  private fun setView() {
    filename = findViewById(R.id.filename)
    title = findViewById(R.id.title)
    content = findViewById(R.id.description)
    backButton = findViewById(R.id.back_button)
    actionDone = findViewById(R.id.import_or_edit_to_app)

    backButton.setOnClickListener { onBackPressed() }
    actionDone.setOnClickListener {
      val note = Note.gen(titleText, contentText)
      note.save(this)
      startActivity(ViewAdvancedNoteActivity.getIntent(this, note, isNightMode))
      finish()
    }
  }

  fun handleIntent(): Boolean {
    val hasSendIntent = handleSendText(intent)
    if (hasSendIntent) {
      val note = Note.gen(titleText, contentText)
      note.save(this)
      startActivity(ViewAdvancedNoteActivity.getIntent(this, note, isNightMode))
      return false
    }
    val hasFileIntent = handleFileIntent(intent)
    if (hasFileIntent) {
      return true
    }
    return false
  }

  fun handleSendText(intent: Intent): Boolean {
    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    val sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
    val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

    titleText = sharedSubject ?: sharedTitle ?: ""
    contentText = sharedText ?: ""
    return sharedText != null
  }

  fun handleFileIntent(intent: Intent): Boolean {
    val data = intent.data
    try {
      val inputStream = contentResolver.openInputStream(data)
      contentText = convertStreamToString(inputStream)
      filenameText = data.lastPathSegment
      inputStream.close()
      return true
    } catch (exception: Exception) {
      return false
    }
  }

  override fun notifyNightModeChange() {
    setSystemTheme();

    val containerLayout = findViewById<View>(R.id.container_layout);
    containerLayout.setBackgroundColor(getThemeColor());

    val toolbarIconColor = getColor(R.color.material_blue_grey_700, R.color.light_secondary_text);
    backButton.setColorFilter(toolbarIconColor)

    val textColor = getColor(R.color.dark_secondary_text, R.color.light_secondary_text);
    filename.setTextColor(textColor)
    title.setTextColor(textColor)
    content.setTextColor(textColor)

    val actionColor = getColor(R.color.material_blue_grey_600, R.color.light_primary_text)
    actionDone.setImageTint(actionColor)
    actionDone.setTextColor(actionColor)
  }
}
