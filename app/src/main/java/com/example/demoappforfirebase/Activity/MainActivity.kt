package com.example.demoappforfirebase


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.ascendik.diary.util.ImageUtil
import com.ascendik.diary.util.ImageUtil.REQUEST_GALLERY_PHOTO
import com.ascendik.diary.util.ImageUtil.REQUEST_TAKE_PHOTO
import com.example.demoappforfirebase.Activity.SignUpActivity
import com.example.demoappforfirebase.Adapter.CategoriesAdapter
import com.example.demoappforfirebase.Fragment.*
import com.example.demoappforfirebase.Model.*
import com.example.demoappforfirebase.Utils.*
import com.example.demoappforfirebase.Utils.StyleUtil.getAttributeColor
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_book.*
import kotlinx.android.synthetic.main.view_bottom_toolbar.*
import kotlinx.android.synthetic.main.view_content_main.*
import kotlinx.android.synthetic.main.view_content_main.fragment_container
import kotlinx.android.synthetic.main.view_content_sign.*


class MainActivity : AppCompatActivity() {
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var bookVM: BookViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var userVM: UserViewModel
    private lateinit var chatVM: ChatViewModel

    enum class SortType { NEWER_FIRST, OLDER_FIRST, A_TO_Z, Z_TO_A, MOST_LIKED, MOST_COMMENTED }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_content_main)
        StyleUtil.stylizeStatusBar(this@MainActivity, true)
        setSupportActionBar(toolbar)
        initHelpers()
        setToolbarListeners()
        fragmentHelper.initFragment(savedInstanceState)
        chatVM.hasNewMessages.observe(this, {
            invalidateOptionsMenu()
        })
        updateChatIndicator()
        getUser()
    }

    private fun initHelpers() {
        fragmentHelper = FragmentHelper(this)
        bookVM = ViewModelProvider(this).get(BookViewModel::class.java)
        userVM = ViewModelProvider(this).get(UserViewModel::class.java)
        chatVM = ViewModelProvider(this).get(ChatViewModel::class.java)
        preferencesHelper = PreferencesHelper(this)
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
    }

    private fun setToolbarListeners() {
        toolbarItemAdd.setOnClickListener {
            fragmentHelper.replaceFragment(BookFragment::class.java)
        }
        addLayout.setOnClickListener {
            fragmentHelper.replaceFragment(BookFragment::class.java)
        }
        toolbarItemProfile.setOnClickListener {
            fragmentHelper.replaceFragment(UserProfileFragment::class.java)
        }
        profileLayout.setOnClickListener {
            fragmentHelper.replaceFragment(UserProfileFragment::class.java)
        }
    }

    private fun getUser() {
        val userQuery = database.child("Users").child(preferencesHelper.getUserId())
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userVM.currentUser = dataSnapshot.getValue(User::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {
                AnalyticsUtil.logError(this@MainActivity, error.toString())
            }
        })
    }

    private fun updateChatIndicator() {
        val databaseListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists() && fragmentHelper.isFragmentVisible(BookListFragment::class.java)) {
                        for (postSnapshot in dataSnapshot.children) {
                            if (postSnapshot.key == "Books") {
                                for (snapShot in postSnapshot.children) {
                                    val book: Book = snapShot.getValue(Book::class.java)!!
                                    bookVM.addBook(book)
                                }
                                bookVM.currentBooks.value = bookVM.oldBooks
                            } else if (postSnapshot.key == "Users") {
                                for (snapShot in postSnapshot.children) {
                                    val user: User = snapShot.getValue(User::class.java)!!
                                    userVM.allUsers.add(user)
                                }
                            }
                        }
                    }

                    chatVM.idsOfUserThatOwnerChatsWith.clear()
                    chatVM.updateIdsOfUsersThatOwnerChatsWith(dataSnapshot)
                    if (chatVM.idsOfUserThatOwnerChatsWith.isNotEmpty()) {
                        for (user in chatVM.idsOfUserThatOwnerChatsWith) {
                            val id = chatVM.recreateChatIdWithUser(user)
                            val lastMessages = arrayListOf<Message>()
                            val allMessages = arrayListOf<Message>()
                            val databaseListener = object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (postSnapshot in snapshot.children) {
                                        if (postSnapshot.key == "Chats") {
                                            for (chat in postSnapshot.children) {
                                                if (chat.key == id) {
                                                    for (chatChild in chat.children) {
                                                        val msg = chatChild.getValue(Message::class.java)
                                                        if (msg!!.author != preferencesHelper.getUserId()) {
                                                            allMessages.add(msg)
                                                        }
                                                    }
                                                    if (allMessages.isNotEmpty()) {
                                                        lastMessages.add(allMessages.last())
                                                        for (msg in lastMessages) {
                                                            if (!msg.isRead) {
                                                                chatVM.hasNewMessages.value = true
                                                                break
                                                            }
                                                            chatVM.hasNewMessages.value = false
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    allMessages.clear()
                                    lastMessages.clear()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    AnalyticsUtil.logError(this@MainActivity, error.toString())
                                }
                            }
                            database.addValueEventListener(databaseListener)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                AnalyticsUtil.logError(this@MainActivity, databaseError.toString())
            }
        }

        database.addValueEventListener(databaseListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImageUtil.dispatchTakePictureIntent(this)
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_GALLERY_PHOTO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                    getIntent.type = "image/*"
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    pickIntent.type = "image/*"
                    val chooserIntent = Intent.createChooser(getIntent, "Select Image")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
                    startActivityForResult(chooserIntent, REQUEST_GALLERY_PHOTO)
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                if (resultCode == RESULT_OK) {
                    bookVM.imageUrl = ImageUtil.addImageFromCamera(this)
                    val bmOptions = BitmapFactory.Options()
                    val bitmap = BitmapFactory.decodeFile(ImageUtil.pathForImage, bmOptions)
                    if (fragmentHelper.isFragmentVisible(BookFragment::class.java)) {
                        bookImage.setImageBitmap(bitmap)
                        bookVM.imageUrl = bitmap?.let { ImageUtil.encodeBitmap(it) }
                        bookVM.bookImageChanged.value = true
                    } else if (fragmentHelper.isFragmentVisible(UserProfileFragment::class.java)) {
                        userVM.imageUrl.value = bitmap?.let { ImageUtil.encodeBitmap(it) }
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    ImageUtil.addingPictureCanceled(this)
                }
            }
            else -> {
                if (resultCode == RESULT_OK && data != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
                    bookVM.imageUrl = bitmap?.let { ImageUtil.encodeBitmap(it) }
                    if (fragmentHelper.isFragmentVisible(BookFragment::class.java)) {
                        bookImage.setImageBitmap(bitmap)
                        bookVM.bookImageChanged.value = true
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.subtitle = null
        chatLayout.isVisible = fragmentHelper.isFragmentVisible(ChatFragment::class.java)
        if (fragmentHelper.isFragmentVisible(BookListFragment::class.java)) {
            fragment_container.setPadding(0, 0, 0, resources.getDimension(R.dimen.bottom_toolbar_height).toInt())
        } else {
            fragment_container.setPadding(0)
        }
        when {
            fragmentHelper.isFragmentVisible(BookFragment::class.java) ->
                supportActionBar!!.title = "Book"
            fragmentHelper.isFragmentVisible(BookListFragment::class.java) -> {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                supportActionBar!!.title = "Book exchange"
            }
            fragmentHelper.isFragmentVisible(BookMoreDetailsFragment::class.java) ->
                supportActionBar!!.title = "More details"
            fragmentHelper.isFragmentVisible(ChatListFragment::class.java) ||
                    fragmentHelper.isFragmentVisible(ChatFragment::class.java) ->
                supportActionBar!!.title = "Messages"
            fragmentHelper.isFragmentVisible(UserProfileFragment::class.java) ->
                supportActionBar!!.title = "Profile"
        }

        bottomAppBar.isVisible = fragmentHelper.isFragmentVisible(BookListFragment::class.java)
        menuInflater.inflate(R.menu.menu_main, menu)

        setItemChat(menu)
        setItemLogOut(menu)
        setItemSort(menu)
        setActionSearch(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setActionSearch(menu: Menu?) {
        val actionSearch = menu?.findItem(R.id.action_search)
        actionSearch?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                invalidateOptionsMenu()
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                menu.findItem(R.id.action_sort).isVisible = false
                return true
            }
        })

        val searchView = actionSearch?.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                bookVM.updateBooksByAuthorAndTitle(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                bookVM.updateBooksByAuthorAndTitle(query)
                searchView.clearFocus()
                return true
            }
        })
        searchLayout.setOnClickListener {
            actionSearch.expandActionView()
        }
        toolbarItemSearch.setOnClickListener {
            actionSearch.expandActionView()
        }
    }

    private fun setItemSort(menu: Menu?) {
        val actionSort = menu?.findItem(R.id.action_sort)
        actionSort?.isVisible = fragmentHelper.isFragmentVisible(BookListFragment::class.java)
        actionSort?.setOnMenuItemClickListener {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width: Int = displayMetrics.widthPixels
            val popupWindow = getSortPopupWindow()
            popupWindow.showAsDropDown(toolbar, width, 0)
            true
        }
    }

    private fun getSortPopupWindow(): PopupWindow {
        val popupWindow = PopupWindow(this)
        popupWindow.setBackgroundDrawable(BitmapDrawable())
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.view_sort_popup_window, null)
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColorFilter(getAttributeColor(this, R.attr.colorBackgroundFloating), PorterDuff.Mode.SRC_IN)
        view.findViewById<FrameLayout>(R.id.sortAndTagDropDownMenuParent).setBackgroundDrawable(drawable)
        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view

        view.findViewById<RadioGroup>(R.id.action_sort_radio_group).setOnCheckedChangeListener { radioGroup, _ ->
            when (radioGroup.checkedRadioButtonId) {
                view.findViewById<RadioButton>(R.id.action_newer_first).id -> bookVM.setNewSortType(SortType.NEWER_FIRST.ordinal)
                view.findViewById<RadioButton>(R.id.action_older_first).id -> bookVM.setNewSortType(SortType.OLDER_FIRST.ordinal)
                view.findViewById<RadioButton>(R.id.action_a_to_z).id -> bookVM.setNewSortType(SortType.A_TO_Z.ordinal)
                view.findViewById<RadioButton>(R.id.action_z_to_a).id -> bookVM.setNewSortType(SortType.Z_TO_A.ordinal)
                view.findViewById<RadioButton>(R.id.action_most_liked).id -> bookVM.setNewSortType(SortType.MOST_LIKED.ordinal)
                view.findViewById<RadioButton>(R.id.action_most_commented).id -> bookVM.setNewSortType(SortType.MOST_COMMENTED.ordinal)
            }
        }
        view.findViewById<RadioGroup>(R.id.action_sort_radio_group).check(
            when (preferencesHelper.getSortType()) {
                SortType.NEWER_FIRST.ordinal -> view.findViewById<RadioButton>(R.id.action_newer_first).id
                SortType.OLDER_FIRST.ordinal -> view.findViewById<RadioButton>(R.id.action_older_first).id
                SortType.A_TO_Z.ordinal -> view.findViewById<RadioButton>(R.id.action_a_to_z).id
                SortType.Z_TO_A.ordinal -> view.findViewById<RadioButton>(R.id.action_z_to_a).id
                SortType.MOST_LIKED.ordinal -> view.findViewById<RadioButton>(R.id.action_most_liked).id
                else -> view.findViewById<RadioButton>(R.id.action_most_commented).id
            }
        )
        view.findViewById<TextView>(R.id.action_newer_first_text).setOnClickListener {
            view.findViewById<RadioGroup>(R.id.action_sort_radio_group).check(view.findViewById<RadioButton>(R.id.action_newer_first).id)
        }
        view.findViewById<TextView>(R.id.action_older_first_text).setOnClickListener {
            view.findViewById<RadioGroup>(R.id.action_sort_radio_group).check(view.findViewById<RadioButton>(R.id.action_older_first).id)
        }
        view.findViewById<TextView>(R.id.action_a_to_z_text).setOnClickListener {
            view.findViewById<RadioGroup>(R.id.action_sort_radio_group).check(view.findViewById<RadioButton>(R.id.action_a_to_z).id)
        }
        view.findViewById<TextView>(R.id.action_z_to_a_text).setOnClickListener {
            view.findViewById<RadioGroup>(R.id.action_sort_radio_group).check(view.findViewById<RadioButton>(R.id.action_z_to_a).id)
        }
        view.findViewById<TextView>(R.id.action_most_liked).setOnClickListener {
            view.findViewById<RadioGroup>(R.id.action_sort_radio_group).check(view.findViewById<RadioButton>(R.id.action_most_liked).id)
        }
        view.findViewById<TextView>(R.id.action_most_commented).setOnClickListener {
            view.findViewById<RadioGroup>(R.id.action_sort_radio_group).check(view.findViewById<RadioButton>(R.id.action_most_commented).id)
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.categoriesRecycler)
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.layoutManager = layoutManager
        val params = recyclerView.layoutParams as LinearLayout.LayoutParams
        recyclerView.layoutParams = params
        val adapter = CategoriesAdapter()
        recyclerView.adapter = adapter

        return popupWindow
    }

    private fun setItemLogOut(menu: Menu?) {
        val actionLogOut = menu?.findItem(R.id.action_log_out)
        actionLogOut?.isVisible = fragmentHelper.isFragmentVisible(UserProfileFragment::class.java) && userVM.isMyProfile

        actionLogOut?.setOnMenuItemClickListener {
            preferencesHelper.setUserId("")
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
            true
        }
    }

    private fun setItemChat(menu: Menu?) {
        val actionOpenChats = menu?.findItem(R.id.action_chat)
        actionOpenChats?.icon = BitmapUtil.getMessageIndicator(this, chatVM.hasNewMessages.value!!)
        actionOpenChats?.isVisible = fragmentHelper.isFragmentVisible(BookListFragment::class.java)

        actionOpenChats?.setOnMenuItemClickListener {
            fragmentHelper.replaceFragment(ChatListFragment::class.java)
            true
        }
    }

    public override fun onStart() {
        super.onStart()
        if (preferencesHelper.getUserId().isEmpty()) {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}