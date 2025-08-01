Equilibrium App

Equilibrium is an Android application designed for real-time, voice-based conversations with an AI advisor to support emotional and physical well-being. It leverages Ultravox AI for natural language interaction.

Features 
Real-time Voice Chat: Talk to an AI advisor.

Text & Voice Input: Use either method for conversation.

Custom Male AI Voice: Features the Amrut-English-Indian voice.

Intuitive UI: Clean design with animating cards and subtle gradients.

Technologies 
Android App (Kotlin): Ultravox AI SDK, AndroidX, Material Design, OkHttp, Coroutines.

Node.js Backend: Express.js, Axios, Dotenv.

Setup & Installation 
Follow these steps to get the app running on your machine.

1. Backend Server Setup
This server securely connects to the Ultravox AI API.

Navigate to your server directory:

Bash

cd /path/to/your/ultravox-server
Install Node.js dependencies:

Bash

npm install
Create .env file:

In the same directory as server.js, create a file named .env (exactly like that).

Add your Ultravox AI Key inside it:

ULTRAVOX_API_KEY="YOUR_ULTRAVOX_API_KEY_HERE"
Start the server:

Bash

node server.js
Keep this terminal open while using the app.

2. Android App Setup
Open Project: Open your Android project in Android Studio.

Configure BackendApi.kt:

Open app/src/main/java/com/example/botactivity/BackendApi.kt.

Set SERVER_URL based on your testing setup:

Emulator: http://10.0.2.2:3000/create-call

Physical Device: http://YOUR_COMPUTER_IP:3000/create-call (e.g., http://192.168.1.100:3000/create-call). Ensure your phone is on the same Wi-Fi as your computer.

Sync Gradle: Click the "Sync Project with Gradle Files" button in Android Studio.

Clean & Rebuild: Go to Build > Clean Project, then Build > Rebuild Project.

Running the App 
Ensure your Node.js server is running.

Run the Android App from Android Studio on an emulator or physical device.

Grant Microphone Permissions when prompted.

Start Conversation: Tap the "Tap here to start a dialog" bar or the mic icon.

The AI should greet you with a male voice, and you can begin your conversation!
