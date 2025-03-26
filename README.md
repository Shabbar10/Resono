# Overview
Resono is designed to be a pltform for creators of video content that would like real feedback on their videos. Resono captures viewer's emotion using their webcam while watching the video and generates analytics to better inform the creators on whether the video meets certain thresholds and goals or not. Additionally, the video being watched has its speakers diarized (labeled) and their speech transcribed and summarized. The transcript is used to generate subtitles as well.

Resono is meant to be a companion to another project, [Shortify](https://github.com/SurabSebait/Shortify/tree/secondary).

# Tech Stack
The project combines Python, Deep Learning, and Java.
- We're using JavaFX for the frontend.
- The Facial Emotion Analysis model is our own custom-built CNN model.
- The diarization model is PyAnnote.
- The transcription model is OpenAI's Whisper model.

# Steps to Run
- After cloning the repo, inside `NLP/`, run `model.py`. That is the diarization and transciption Flask server. As of now, it will return an 'srt' file (subtitles) for the video in question.
- Inside `Java/python_backend/`, run `server2.py`. This is the Flask server that houses the CNN model for emotion recognition.
- Finally, in `Java/webcam-viewer/`, run using Maven or in IntelliJ.


[Demo Video](https://drive.google.com/file/d/1J5WVy1Oq0eeA2azIjoudItKtMFlaT2k3/view?usp=sharing)
