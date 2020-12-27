package com.example.demoappforfirebase


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappforfirebase.Activity.SignUpActivity
import com.example.demoappforfirebase.Fragment.*
import com.example.demoappforfirebase.Model.BookViewModel
import com.example.demoappforfirebase.Utils.FragmentHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_book.*
import kotlinx.android.synthetic.main.view_content_main.*
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var database: DatabaseReference
    private lateinit var fragmentHelper: FragmentHelper
    private lateinit var bookVM: BookViewModel
    private lateinit var usersRecycler: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initHelpers()
        initDrawer()
        setListeners()
        //cao djole
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

    private fun setListeners() {
        addBook.setOnClickListener {
            fragmentHelper.replaceFragment(BookFragment::class.java)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (!fragmentHelper.isFragmentVisible(BookListFragment::class.java)) {
            when {
                fragmentHelper.isFragmentVisible(BookFragment::class.java) -> {
                    fragmentHelper.replaceFragment(BookListFragment::class.java)
                }
                fragmentHelper.isFragmentVisible(BookMoreDetailsFragment::class.java) -> {
                    fragmentHelper.replaceFragment(BookListFragment::class.java)
                }
                fragmentHelper.isFragmentVisible(ChatFragment::class.java) -> {
                    fragmentHelper.replaceFragment(BookMoreDetailsFragment::class.java)
                }

                fragmentHelper.isFragmentVisible(ChatsFragment::class.java) -> {
                    fragmentHelper.replaceFragment(BookListFragment::class.java)
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras: Bundle = data?.extras!!
            val imageBitmap = extras["data"] as Bitmap?
            if (fragmentHelper.isFragmentVisible(BookFragment::class.java)) {
                bookImage.setImageBitmap(imageBitmap)
            }
            bookVM.imageUrl.value = imageBitmap?.let { encodeBitmap(it) }
        }
    }

    fun encodeBitmap(bitmap: Bitmap): String {
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
                toggle.isDrawerIndicatorEnabled = true
            }
            fragmentHelper.isFragmentVisible(BookFragment::class.java) -> {
                toggle.isDrawerIndicatorEnabled = false
            }
        }
        addBook.isVisible = fragmentHelper.isFragmentVisible(BookListFragment::class.java)
        if (fragmentHelper.isFragmentVisible(BookListFragment::class.java)) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawer_item_chats -> {
                fragmentHelper.replaceFragment(ChatsFragment::class.java)
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