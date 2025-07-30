package com.example.socialconnect1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var logoutButton: TextView
    private lateinit var editProfile: TextView
    private lateinit var notifications: TextView
    private lateinit var privacyPolicy: TextView
    private lateinit var aboutApp: TextView
    private lateinit var helpSupport: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        logoutButton = view.findViewById(R.id.logoutButton)
        editProfile = view.findViewById(R.id.editProfile)
        notifications = view.findViewById(R.id.notifications)
        privacyPolicy = view.findViewById(R.id.privacyPolicy)
        aboutApp = view.findViewById(R.id.aboutApp)
        helpSupport = view.findViewById(R.id.helpSupport)

        logoutButton.setOnClickListener {
            // Clear session: token, preferences, etc. (depends on your auth handling)
            // e.g. clear SharedPreferences or token cache here if you're not using jan.supabase

            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), SecondActivity::class.java))
            requireActivity().finish()
        }

        editProfile.setOnClickListener {
            // Open EditProfileActivity
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        notifications.setOnClickListener {
            Toast.makeText(requireContext(), "Notification settings coming soon", Toast.LENGTH_SHORT).show()
        }

        privacyPolicy.setOnClickListener {
            val url = "https://example.com/privacy"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        aboutApp.setOnClickListener {
            Toast.makeText(requireContext(), "Social Connect v1.0\nBy Ushbah", Toast.LENGTH_LONG).show()
        }

        helpSupport.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@socialconnect.com")
                putExtra(Intent.EXTRA_SUBJECT, "Need Help - Social Connect App")
            }
            startActivity(supportIntent)
        }

        return view
    }
}
