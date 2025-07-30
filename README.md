# 📱 Social Connect - Android App

Social Connect is a minimal social media application developed as part of a mobile app development internship. The app allows users to sign up, create posts, interact with other users via likes and comments, and receive real-time notifications—all powered by [Supabase](https://supabase.io).

---

## 🌟 Features

### ✅ Authentication
- Sign up, log in, and logout using Supabase REST API
- Password reset (OTP + new password method)

### 🧑‍💼 Profile Management
- Create/update profile with name, bio, and profile picture
- Profile image upload to Supabase Storage
- View other users' profiles

### 📝 Post Management

- Create text/image posts
- Upload images to Supabase Storage
- View posts in scrollable RecyclerView
- Show timestamps
- Grid-style post gallery in profile (like Instagram)

### ❤️ Likes & 💬 Comments
- Like/unlike posts with animations
- Add/view comments in bottom sheet
- Show like/comment counts
- Notifications on like/comment

### 🔔 Notifications
- Real-time notifications using Supabase Realtime
- Notification panel with sender details (username, avatar)

### 📡 Realtime Updates
- Realtime listener via WebSocket for new likes/comments

### ✨ UI Enhancements
- Modern Material UI
- Gradient post borders
- Smooth animations (like/share)
- Post feed animation on load

---

---

## 📦 Tech Stack

| Tech          | Purpose                          |
|---------------|----------------------------------|
| Kotlin        | Main development language        |
| XML           | UI Layouts                       |
| Supabase      | Auth, Realtime, PostgREST, Storage |
| OkHttp        | HTTP networking                  |
| Glide         | Image loading                    |
| RecyclerView  | Post feed & profile post grid    |
| BottomSheet   | Comments UI                      |
| SharedPreferences | Auth token & session mgmt   |

---

## 🛠️ How to Run

1. Clone the repo:
   ```bash
   git clone https://github.com/Ushbah03/social-connect-android.git
