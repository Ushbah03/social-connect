package com.example.socialconnect1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialconnect1.realtime.RealtimeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.postsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 🔧 FAB click setup
        val fab = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.createPostFab)
        fab.setOnClickListener {
            // ✅ Get user_id from SharedPreferences
            val prefs = requireContext().getSharedPreferences("MyAppPrefs", 0)
            val userId = prefs.getString("user_id", "") ?: ""

            if (userId.isEmpty()) {
                Toast.makeText(requireContext(), "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        fetchPosts()
        return view
    }

    private fun fetchPosts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = requireContext().getSharedPreferences("MyAppPrefs", 0)
                val accessToken = prefs.getString("access_token", "") ?: ""

                val postList = fetchPostsFromSupabase(accessToken)

                withContext(Dispatchers.Main) {
                    Log.d("PostDebug", "Fetched ${postList.size} posts: $postList")

                    adapter = PostAdapter(postList)
                    recyclerView.adapter = adapter

                    // ✅ Add animation to RecyclerView
                    val animation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.item_anim_fade_slide)
                    val controller = android.view.animation.LayoutAnimationController(animation).apply {
                        order = android.view.animation.LayoutAnimationController.ORDER_NORMAL
                        delay = 0.15f
                    }
                    recyclerView.layoutAnimation = controller
                    recyclerView.scheduleLayoutAnimation()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error loading posts: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val realtimeClient = RealtimeClient()

    override fun onStart() {
        super.onStart()

        realtimeClient.connect { type, postId ->
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "New $type on post $postId", Toast.LENGTH_SHORT).show()
                // Optionally: refreshPost(postId) or refetch all posts
            }
        }
    }

    override fun onStop() {
        super.onStop()
        realtimeClient.disconnect()
    }

}
