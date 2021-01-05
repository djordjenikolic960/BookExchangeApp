package com.example.demoappforfirebase


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.ascendik.diary.util.ImageUtil
import com.ascendik.diary.util.ImageUtil.REQUEST_GALLERY_PHOTO
import com.ascendik.diary.util.ImageUtil.REQUEST_TAKE_PHOTO
import com.example.demoappforfirebase.Activity.SignUpActivity
import com.example.demoappforfirebase.Fragment.*
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.Model.UserViewModel
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.example.demoappforfirebase.Utils.PreferencesHelper
import com.example.demoappforfirebase.Utils.StyleUtil
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_book.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.view_bottom_toolbar.*
import kotlinx.android.synthetic.main.view_content_main.*
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var bookVM: BookViewModel
    private lateinit var usersRecycler: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var userVM: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StyleUtil.stylizeStatusBar(this@MainActivity, true)
        setSupportActionBar(toolbar)
        initHelpers()
        initDrawer()
        setToolbarListeners()
        /*usersRecycler = findViewById(R.id.usersRecycler)
        usersRecycler.layoutManager = LinearLayoutManager(this)
        database = FirebaseDatabase.getInstance().reference
        // Initialize Firebase Auth

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val bookQuery: Query =
                database.child("user1")
            bookQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.ref.removeValue()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //Log.e(TAG, "onCancelled", databaseError.toException())
                }
            })
        }

        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            val bookQuery: Query =
                database.child("user1")
            bookQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.ref.setValue(Book("novo", "update"))
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //Log.e(TAG, "onCancelled", databaseError.toException())
                }
            })
        }

        findViewById<Button>(R.id.btnShowUsers).setOnClickListener {
            val userQuery: Query =
                database.child("simpleChat").child("users")
            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (child in dataSnapshot.children) {
                        Toast.makeText(applicationContext, child.children.first().value.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        findViewById<Button>(R.id.btnSendMsg).setOnClickListener {
            val userQuery: Query =
                database.child("simpleChat").child("users")
            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val receiverId = child.children.first().value
                        val senderId = auth.currentUser!!.uid
                        database.child("messages").child(
                            if (receiverId.toString() > senderId) {
                                receiverId.toString() + senderId
                            } else {
                                senderId + receiverId.toString()
                            }
                        ).child("message").setValue("poruka je poslata")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        val databaseListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val books = arrayListOf<Book>()
                    val users = arrayListOf<String>()
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "simpleChat") {
                            val user = postSnapshot.child("users")
                            for (child in user.children) {
                                users.add(child.key.toString())
                            }
                            val adapter = UsersAdapter(users)
                            usersRecycler.adapter = adapter
                        } else {
                            val person1: Book = postSnapshot.getValue(Book::class.java)!!
                            books.add(person1)
                        }
                        val adapter = BooksAdapter(books)
                      //  booksRecycler.adapter = adapter
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }

        database.addValueEventListener(databaseListener)*/
    }

    private fun initHelpers() {
        fragmentHelper = FragmentHelper(this)
        fragmentHelper.replaceFragment(BookListFragment::class.java)
        bookVM = ViewModelProvider(this).get(BookViewModel::class.java)
        userVM = ViewModelProvider(this).get(UserViewModel::class.java)
        preferencesHelper = PreferencesHelper(this)
        auth = Firebase.auth
    }

    private fun initDrawer() {
        toggle = ActionBarDrawerToggle(
            this, drawer_layout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.itemIconTintList = null
        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun setToolbarListeners() {
        toolbarItemAdd.setOnClickListener {
            fragmentHelper.replaceFragment(BookFragment::class.java)
        }
        toolbarItemProfile.setOnClickListener {
            fragmentHelper.replaceFragment(UserProfileFragment::class.java)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImageUtil.dispatchTakePictureIntent(this)
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT ).show()
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
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT ).show()
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
                        bookVM.imageUrl = bitmap?.let { encodeBitmap(it) }
                    } else if(fragmentHelper.isFragmentVisible(UserProfileFragment::class.java)){
                        userVM.imageUrl.value = bitmap?.let { encodeBitmap(it) }
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    ImageUtil.addingPictureCanceled(this)
                }
            }
            else -> {
                if (resultCode == RESULT_OK && data != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
                    if (fragmentHelper.isFragmentVisible(BookFragment::class.java)) {
                        bookImage.setImageBitmap(bitmap)
                    }
                    bookVM.imageUrl = bitmap?.let { encodeBitmap(it) }
                }
            }
        }
    }

    private fun encodeBitmap(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toggle.isDrawerIndicatorEnabled =
            fragmentHelper.isFragmentVisible(BookListFragment::class.java)
        supportActionBar!!.subtitle = null

        when {
            fragmentHelper.isFragmentVisible(BookListFragment::class.java) -> {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                toggle.isDrawerIndicatorEnabled = true
                supportActionBar!!.title = null
            }
            fragmentHelper.isFragmentVisible(BookFragment::class.java) -> {
                toggle.isDrawerIndicatorEnabled = false
            }
            fragmentHelper.isFragmentVisible(ChatListFragment::class.java) -> supportActionBar!!.title =
                "Messages"
        }

        bottomAppBar.isVisible = fragmentHelper.isFragmentVisible(BookListFragment::class.java)
        menuInflater.inflate(R.menu.menu_main, menu)

        val actionOpenChats = menu?.findItem(R.id.action_chat)
        actionOpenChats?.isVisible = fragmentHelper.isFragmentVisible(BookListFragment::class.java)

        actionOpenChats?.setOnMenuItemClickListener {
            fragmentHelper.replaceFragment(ChatListFragment::class.java)
           true
        }

        if (fragmentHelper.isFragmentVisible(BookListFragment::class.java)) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onStart() {
        super.onStart()
        if(preferencesHelper.getUserId().isEmpty()){
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawer_item_chats -> {
                fragmentHelper.replaceFragment(ChatListFragment::class.java)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = !item.isChecked
        return when (item.itemId) {
            android.R.id.home -> {
                if (fragmentHelper.isFragmentVisible(BookListFragment::class.java)) {
                    drawer_layout.openDrawer(GravityCompat.START)
                } else {
                    onBackPressed()
                }
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}