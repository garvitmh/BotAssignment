// A simple Node.js server using the Express framework
const express = require('express');
const axios = require('axios');
const cors = require('cors');
require('dotenv').config(); // Loads environment variables from a .env file

const app = express();
const port = 3000; // The port your server will run on

// Define your voice ID (get this from your Ultravox console)
// Updated to the full ID for Amrut-English-Indian voice
const DEFAULT_MALE_VOICE_ID = "a0df06e1-d90a-444a-906a-b9c873796f4e";
const FEMALE_VOICE_ID = "YOUR_FEMALE_VOICE_ID"; // Replace with your actual Female Voice ID (if you have one, or a default)

app.use(cors()); // Allows your app to talk to this server
app.use(express.json()); // Enable Express to parse JSON request bodies

// This is the main endpoint your app will call
app.post('/create-call', async (req, res) => {
    console.log("Received request to create a call...");

    const ultravoxApiKey = process.env.ULTRAVOX_API_KEY;

    if (!ultravoxApiKey) {
        console.error("API Key is not set in the .env file.");
        return res.status(500).json({ error: 'Server configuration error: API key missing.' });
    }

    try {
        // This is the secure, server-to-server call to the Ultravox API
        const response = await axios.post('https://api.ultravox.ai/api/calls',
            {
                systemPrompt: `You are Equilibrium, a highly empathetic, knowledgeable, and insightful emotional well-being advisor and therapist.
                Your purpose is to provide guidance, understanding, and practical strategies for managing emotions and improving mental and emotional health.
                You possess deep expertise in human psychology, emotional regulation techniques, cognitive-behavioral strategies, and mindfulness practices.
                When a user describes a situation or an emotion, analyze it with compassion and offer actionable, empathetic advice.
                Focus on providing concise, impactful, and supportive responses. Avoid lengthy monologues.
                Your responses should be encouraging, non-judgmental, and always prioritize the user's well-being.
                Encourage self-reflection and provide tools the user can apply in their daily life.
                NEVER offer medical diagnoses or replace professional medical advice. Always default to empathy and practical coping mechanisms.
                Keep your responses focused and direct, aiming for clarity and actionable steps.`,
                initialOutputMedium: "MESSAGE_MEDIUM_TEXT", // Bot speaks its initial response
                voice: DEFAULT_MALE_VOICE_ID // Directly use the male voice ID
            },
            {
                headers: {
                    'X-API-Key': ultravoxApiKey,
                    'Content-Type': 'application/json'
                }
            }
        );

        // Extract the joinUrl from the response
        const joinUrl = response.data.joinUrl;
        console.log("Successfully created call. Join URL:", joinUrl);

        // Send the joinUrl back to your Android app
        res.json({ joinUrl: joinUrl });

    } catch (error) {
        console.error('Error calling Ultravox API:', error.response ? error.response.data : error.message);
        res.status(500).json({ error: 'Failed to create call with Ultravox API.' });
    }
});

app.listen(port, () => {
    console.log(`Ultravox server listening on port ${port}`);
    console.log("Waiting for your app to request a 'joinUrl'...");
});